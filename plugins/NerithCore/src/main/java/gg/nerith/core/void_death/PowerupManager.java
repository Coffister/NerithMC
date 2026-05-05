package gg.nerith.core.void_death;

import gg.nerith.core.NerithCore;
import gg.nerith.core.island.Island;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class PowerupManager {

    public enum PowerupType { VOID_SHIELD, FRAGMENT_SAVER }

    private final NerithCore plugin;
    private final Map<UUID, Set<PowerupType>> cache = new ConcurrentHashMap<>();

    public PowerupManager(NerithCore plugin) {
        this.plugin = plugin;
    }

    public boolean hasPowerup(Island island, PowerupType type) {
        Set<PowerupType> set = cache.get(island.getId());
        if (set != null) return set.contains(type);
        try {
            return loadHasPowerup(island.getId(), type);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to check powerup", e);
            return false;
        }
    }

    public void grantPowerup(Island island, PowerupType type) throws SQLException {
        String sql = """
            INSERT INTO island_powerups (island_id, powerup_type, active)
            VALUES (?, ?, TRUE)
            ON DUPLICATE KEY UPDATE active=TRUE
        """;
        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, island.getId().toString());
            ps.setString(2, type.name().toLowerCase());
            ps.executeUpdate();
        }
        cache.computeIfAbsent(island.getId(), k -> ConcurrentHashMap.newKeySet()).add(type);
    }

    public void consumePowerup(Island island, PowerupType type) throws SQLException {
        String sql = "UPDATE island_powerups SET active=FALSE WHERE island_id=? AND powerup_type=?";
        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, island.getId().toString());
            ps.setString(2, type.name().toLowerCase());
            ps.executeUpdate();
        }
        Set<PowerupType> set = cache.get(island.getId());
        if (set != null) set.remove(type);
    }

    public Set<PowerupType> getActivePowerups(Island island) {
        try {
            Set<PowerupType> result = new HashSet<>();
            String sql = "SELECT powerup_type FROM island_powerups WHERE island_id=? AND active=TRUE";
            try (Connection conn = plugin.getDatabaseManager().getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, island.getId().toString());
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    try {
                        result.add(PowerupType.valueOf(rs.getString("powerup_type").toUpperCase()));
                    } catch (Exception ignored) {}
                }
            }
            cache.put(island.getId(), ConcurrentHashMap.newKeySet());
            cache.get(island.getId()).addAll(result);
            return result;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load powerups", e);
            return Set.of();
        }
    }

    private boolean loadHasPowerup(UUID islandId, PowerupType type) throws SQLException {
        String sql = "SELECT active FROM island_powerups WHERE island_id=? AND powerup_type=? AND active=TRUE";
        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, islandId.toString());
            ps.setString(2, type.name().toLowerCase());
            return ps.executeQuery().next();
        }
    }
}
