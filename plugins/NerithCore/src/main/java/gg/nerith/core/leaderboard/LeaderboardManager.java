package gg.nerith.core.leaderboard;

import gg.nerith.core.NerithCore;
import gg.nerith.core.api.events.NerithLeaderboardRewardEvent;
import gg.nerith.core.island.Island;
import org.bukkit.Bukkit;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;

public class LeaderboardManager {

    private final NerithCore plugin;

    public LeaderboardManager(NerithCore plugin) {
        this.plugin = plugin;
    }

    public List<Island> getTopIslands(int limit) {
        try {
            return plugin.getIslandRepository().findTopByPhaseAndBlocks(limit);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to fetch leaderboard", e);
            return List.of();
        }
    }

    public void settleWeekly() {
        settle(NerithLeaderboardRewardEvent.Period.WEEKLY);
    }

    public void settleMonthly() {
        settle(NerithLeaderboardRewardEvent.Period.MONTHLY);
    }

    private void settle(NerithLeaderboardRewardEvent.Period period) {
        List<Island> top = getTopIslands(10);
        if (top.isEmpty()) return;

        for (int i = 0; i < top.size(); i++) {
            Island island = top.get(i);
            NerithLeaderboardRewardEvent event = new NerithLeaderboardRewardEvent(
                    island.getOwnerUuid(), island, period, i + 1
            );
            Bukkit.getPluginManager().callEvent(event);
        }

        plugin.getLogger().info("[NerithCore] " + period.name() + " leaderboard settled. Winner: " + top.get(0).getOwnerUuid());
    }

    public void scheduleAutoSettle() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            String day = plugin.getConfigManager().getLeaderboardWeeklyDay();
            if (now.getDayOfWeek().name().equalsIgnoreCase(day)) {
                String[] timeParts = plugin.getConfigManager().getLeaderboardWeeklyTime().split(":");
                if (now.getHour() == Integer.parseInt(timeParts[0]) && now.getMinute() < 1) {
                    Bukkit.getScheduler().runTask(plugin, this::settleWeekly);
                }
            }
            int monthDay = plugin.getConfigManager().getLeaderboardMonthlyDay();
            if (now.getDayOfMonth() == monthDay && now.getHour() == 20 && now.getMinute() < 1) {
                Bukkit.getScheduler().runTask(plugin, this::settleMonthly);
            }
        }, 20L * 60, 20L * 60);
    }
}
