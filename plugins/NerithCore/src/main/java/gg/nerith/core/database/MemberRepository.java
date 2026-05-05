package gg.nerith.core.database;

import gg.nerith.core.NerithCore;
import gg.nerith.core.island.IslandMember;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MemberRepository {

    private final NerithCore plugin;

    public MemberRepository(NerithCore plugin) {
        this.plugin = plugin;
    }

    public void save(UUID islandId, UUID playerUuid, IslandMember.Role role) throws SQLException {
        String sql = """
            INSERT INTO island_members (island_id, player_uuid, role)
            VALUES (?, ?, ?)
            ON DUPLICATE KEY UPDATE role=VALUES(role)
        """;
        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, islandId.toString());
            ps.setString(2, playerUuid.toString());
            ps.setString(3, role.name().toLowerCase());
            ps.executeUpdate();
        }
    }

    public List<IslandMember> findByIsland(UUID islandId) throws SQLException {
        String sql = "SELECT * FROM island_members WHERE island_id=?";
        List<IslandMember> members = new ArrayList<>();
        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, islandId.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                members.add(new IslandMember(
                        UUID.fromString(rs.getString("island_id")),
                        UUID.fromString(rs.getString("player_uuid")),
                        IslandMember.Role.valueOf(rs.getString("role").toUpperCase())
                ));
            }
        }
        return members;
    }

    public void remove(UUID islandId, UUID playerUuid) throws SQLException {
        String sql = "DELETE FROM island_members WHERE island_id=? AND player_uuid=?";
        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, islandId.toString());
            ps.setString(2, playerUuid.toString());
            ps.executeUpdate();
        }
    }

    public void updateRole(UUID islandId, UUID playerUuid, IslandMember.Role role) throws SQLException {
        String sql = "UPDATE island_members SET role=? WHERE island_id=? AND player_uuid=?";
        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, role.name().toLowerCase());
            ps.setString(2, islandId.toString());
            ps.setString(3, playerUuid.toString());
            ps.executeUpdate();
        }
    }
}
