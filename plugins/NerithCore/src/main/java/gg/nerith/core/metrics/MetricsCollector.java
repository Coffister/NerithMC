package gg.nerith.core.metrics;

import gg.nerith.core.NerithCore;
import gg.nerith.core.island.Island;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class MetricsCollector {

    private final NerithCore plugin;

    public MetricsCollector(NerithCore plugin) {
        this.plugin = plugin;
    }

    public Map<String, Object> getGlobalStats() {
        Map<String, Object> stats = new HashMap<>();
        try {
            stats.put("total_islands", plugin.getIslandRepository().countTotal());
            stats.put("total_blocks_broken", plugin.getStatsRepository().getTotalBlocksBroken());
            stats.put("total_mobs_killed", plugin.getStatsRepository().getTotalMobsKilled());
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to collect global stats", e);
        }
        return stats;
    }

    public List<Map<String, Object>> getLeaderboard(int limit) {
        List<Island> top = plugin.getLeaderboardManager().getTopIslands(limit);
        return top.stream().map(island -> {
            Map<String, Object> entry = new HashMap<>();
            entry.put("island_id", island.getId().toString());
            entry.put("owner_uuid", island.getOwnerUuid().toString());
            entry.put("phase", island.getPhase());
            entry.put("phase_name", plugin.getConfigManager().getPhaseConfig(island.getPhase()).getName());
            entry.put("blocks_broken", island.getBlocksBroken());
            return entry;
        }).toList();
    }

    public Map<String, Object> getIslandData(Island island) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", island.getId().toString());
        data.put("owner_uuid", island.getOwnerUuid().toString());
        data.put("type", island.getType().name().toLowerCase());
        data.put("phase", island.getPhase());
        data.put("phase_name", plugin.getConfigManager().getPhaseConfig(island.getPhase()).getName());
        data.put("blocks_broken", island.getBlocksBroken());
        data.put("world", island.getWorld());
        data.put("member_count", plugin.getIslandManager().getMemberCount(island));
        return data;
    }
}
