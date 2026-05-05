package gg.nerith.core.database;

import gg.nerith.core.NerithCore;
import gg.nerith.core.island.IslandStats;

import java.sql.*;
import java.util.Optional;
import java.util.UUID;

public class StatsRepository {

    private final NerithCore plugin;

    public StatsRepository(NerithCore plugin) {
        this.plugin = plugin;
    }

    public void init(UUID islandId) throws SQLException {
        String sql = "INSERT IGNORE INTO island_stats (island_id) VALUES (?)";
        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, islandId.toString());
            ps.executeUpdate();
        }
    }

    public Optional<IslandStats> find(UUID islandId) throws SQLException {
        String sql = "SELECT * FROM island_stats WHERE island_id=?";
        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, islandId.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(new IslandStats(
                        UUID.fromString(rs.getString("island_id")),
                        rs.getLong("mobs_killed"),
                        rs.getInt("bosses_killed"),
                        rs.getInt("events_triggered"),
                        rs.getInt("treasure_found")
                ));
            }
        }
        return Optional.empty();
    }

    public void incrementMobsKilled(UUID islandId) throws SQLException {
        update(islandId, "mobs_killed", 1);
    }

    public void incrementBossesKilled(UUID islandId) throws SQLException {
        update(islandId, "bosses_killed", 1);
    }

    public void incrementEventsTriggered(UUID islandId) throws SQLException {
        update(islandId, "events_triggered", 1);
    }

    public void incrementTreasureFound(UUID islandId) throws SQLException {
        update(islandId, "treasure_found", 1);
    }

    private void update(UUID islandId, String column, long amount) throws SQLException {
        String sql = "UPDATE island_stats SET " + column + "=" + column + "+? WHERE island_id=?";
        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, amount);
            ps.setString(2, islandId.toString());
            ps.executeUpdate();
        }
    }

    public long getTotalMobsKilled() throws SQLException {
        return sumColumn("mobs_killed");
    }

    public long getTotalBlocksBroken() throws SQLException {
        String sql = "SELECT SUM(blocks_broken) FROM islands";
        try (Connection conn = plugin.getDatabaseManager().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getLong(1);
        }
        return 0;
    }

    private long sumColumn(String column) throws SQLException {
        String sql = "SELECT SUM(" + column + ") FROM island_stats";
        try (Connection conn = plugin.getDatabaseManager().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getLong(1);
        }
        return 0;
    }
}
