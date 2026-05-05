package gg.nerith.core.database;

import gg.nerith.core.NerithCore;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class ResetRepository {

    private final NerithCore plugin;

    public ResetRepository(NerithCore plugin) {
        this.plugin = plugin;
    }

    public void record(UUID islandId, UUID playerUuid, int phaseAtReset, long blocksAtReset, double penaltyAmount) throws SQLException {
        String sql = """
            INSERT INTO island_resets (island_id, player_uuid, phase_at_reset, blocks_at_reset, penalty_amount)
            VALUES (?, ?, ?, ?, ?)
        """;
        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, islandId.toString());
            ps.setString(2, playerUuid.toString());
            ps.setInt(3, phaseAtReset);
            ps.setLong(4, blocksAtReset);
            ps.setDouble(5, penaltyAmount);
            ps.executeUpdate();
        }
    }

    public int countResets(UUID islandId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM island_resets WHERE island_id=?";
        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, islandId.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }
}
