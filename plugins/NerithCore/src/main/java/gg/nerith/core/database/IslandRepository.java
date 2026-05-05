package gg.nerith.core.database;

import gg.nerith.core.NerithCore;
import gg.nerith.core.island.Island;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class IslandRepository {

    private final NerithCore plugin;

    public IslandRepository(NerithCore plugin) {
        this.plugin = plugin;
    }

    public void save(Island island) throws SQLException {
        String sql = """
            INSERT INTO islands (id, owner_uuid, island_type, phase, blocks_broken, spawn_x, spawn_y, spawn_z, world, current_block)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                island_type=VALUES(island_type), phase=VALUES(phase), blocks_broken=VALUES(blocks_broken),
                spawn_x=VALUES(spawn_x), spawn_y=VALUES(spawn_y), spawn_z=VALUES(spawn_z),
                world=VALUES(world), current_block=VALUES(current_block)
        """;
        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, island.getId().toString());
            ps.setString(2, island.getOwnerUuid().toString());
            ps.setString(3, island.getType().name().toLowerCase());
            ps.setInt(4, island.getPhase());
            ps.setLong(5, island.getBlocksBroken());
            ps.setDouble(6, island.getSpawnX());
            ps.setDouble(7, island.getSpawnY());
            ps.setDouble(8, island.getSpawnZ());
            ps.setString(9, island.getWorld());
            ps.setString(10, island.getCurrentBlock());
            ps.executeUpdate();
        }
    }

    public Optional<Island> findByOwner(UUID ownerUuid) throws SQLException {
        String sql = "SELECT * FROM islands WHERE owner_uuid = ?";
        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ownerUuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    public Optional<Island> findById(UUID islandId) throws SQLException {
        String sql = "SELECT * FROM islands WHERE id = ?";
        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, islandId.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    public Optional<Island> findByMember(UUID playerUuid) throws SQLException {
        String sql = """
            SELECT i.* FROM islands i
            JOIN island_members m ON i.id = m.island_id
            WHERE m.player_uuid = ?
        """;
        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, playerUuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    public List<Island> findTopByPhaseAndBlocks(int limit) throws SQLException {
        String sql = "SELECT * FROM islands ORDER BY phase DESC, blocks_broken DESC LIMIT ?";
        List<Island> list = new ArrayList<>();
        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public void updatePhaseAndBlocks(UUID islandId, int phase, long blocksBroken) throws SQLException {
        String sql = "UPDATE islands SET phase=?, blocks_broken=? WHERE id=?";
        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, phase);
            ps.setLong(2, blocksBroken);
            ps.setString(3, islandId.toString());
            ps.executeUpdate();
        }
    }

    public void updateSpawn(UUID islandId, double x, double y, double z) throws SQLException {
        String sql = "UPDATE islands SET spawn_x=?, spawn_y=?, spawn_z=? WHERE id=?";
        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, x);
            ps.setDouble(2, y);
            ps.setDouble(3, z);
            ps.setString(4, islandId.toString());
            ps.executeUpdate();
        }
    }

    public void updateOwner(UUID islandId, UUID newOwner) throws SQLException {
        String sql = "UPDATE islands SET owner_uuid=? WHERE id=?";
        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newOwner.toString());
            ps.setString(2, islandId.toString());
            ps.executeUpdate();
        }
    }

    public void delete(UUID islandId) throws SQLException {
        String sql = "DELETE FROM islands WHERE id=?";
        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, islandId.toString());
            ps.executeUpdate();
        }
    }

    public List<Island> findAll() throws SQLException {
        String sql = "SELECT * FROM islands";
        List<Island> list = new ArrayList<>();
        try (Connection conn = plugin.getDatabaseManager().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public long countTotal() throws SQLException {
        String sql = "SELECT COUNT(*) FROM islands";
        try (Connection conn = plugin.getDatabaseManager().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getLong(1);
        }
        return 0;
    }

    public void updateCurrentBlock(UUID islandId, String material) throws SQLException {
        String sql = "UPDATE islands SET current_block=? WHERE id=?";
        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, material);
            ps.setString(2, islandId.toString());
            ps.executeUpdate();
        }
    }

    private Island mapRow(ResultSet rs) throws SQLException {
        return new Island(
                UUID.fromString(rs.getString("id")),
                UUID.fromString(rs.getString("owner_uuid")),
                Island.Type.valueOf(rs.getString("island_type").toUpperCase()),
                rs.getInt("phase"),
                rs.getLong("blocks_broken"),
                rs.getDouble("spawn_x"),
                rs.getDouble("spawn_y"),
                rs.getDouble("spawn_z"),
                rs.getString("world"),
                rs.getString("current_block")
        );
    }
}
