package gg.nerith.core.config;

import gg.nerith.core.NerithCore;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ConfigManager {

    private final NerithCore plugin;
    private final Map<Integer, PhaseConfig> phases = new LinkedHashMap<>();
    private final Map<Integer, Integer> resetPenalties = new HashMap<>();

    public ConfigManager(NerithCore plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.reloadConfig();
        phases.clear();
        resetPenalties.clear();
        loadPhases();
        loadPenalties();
    }

    private void loadPhases() {
        ConfigurationSection sec = plugin.getConfig().getConfigurationSection("phases");
        if (sec == null) return;
        for (String key : sec.getKeys(false)) {
            int phase = Integer.parseInt(key);
            ConfigurationSection ps = sec.getConfigurationSection(key);
            if (ps == null) continue;
            String name = ps.getString("name", "Phase " + phase);
            long blocksRequired = ps.getLong("blocks-required", 0);
            String loreLine = ps.getString("lore-line", "");
            String particle = ps.getString("phase-up-particle", "SMOKE_NORMAL");
            List<String> rewardKitRaw = ps.getStringList("reward-kit");
            phases.put(phase, new PhaseConfig(phase, name, blocksRequired, loreLine, particle, rewardKitRaw));
        }
    }

    private void loadPenalties() {
        ConfigurationSection sec = plugin.getConfig().getConfigurationSection("reset-penalties");
        if (sec == null) return;
        for (String key : sec.getKeys(false)) {
            resetPenalties.put(Integer.parseInt(key), sec.getInt(key, 0));
        }
    }

    public PhaseConfig getPhaseConfig(int phase) {
        return phases.getOrDefault(phase, new PhaseConfig(phase, "Phase " + phase, Long.MAX_VALUE, "", "SMOKE_NORMAL", List.of()));
    }

    public int getPhaseCount() {
        return phases.size();
    }

    public Map<Integer, PhaseConfig> getPhases() {
        return Collections.unmodifiableMap(phases);
    }

    public int getResetPenalty(int phase) {
        return resetPenalties.getOrDefault(phase, 0);
    }

    public String getDbHost() { return plugin.getConfig().getString("database.host", "localhost"); }
    public int getDbPort() { return plugin.getConfig().getInt("database.port", 3306); }
    public String getDbName() { return plugin.getConfig().getString("database.name", "nerith"); }
    public String getDbUsername() { return plugin.getConfig().getString("database.username", "nerith_user"); }
    public String getDbPassword() { return plugin.getConfig().getString("database.password", "CHANGE_ME"); }
    public int getDbPoolSize() { return plugin.getConfig().getInt("database.pool-size", 10); }

    public int getIslandMaxMembers() { return plugin.getConfig().getInt("island.max-members", 4); }
    public int getIslandSpacing() { return plugin.getConfig().getInt("island.spacing", 1000); }
    public int getVoidYLevel() { return plugin.getConfig().getInt("island.void-y-level", 0); }

    public int getFragmentIslandsMin() { return plugin.getConfig().getInt("island.fragment-islands.min-count", 3); }
    public int getFragmentIslandsMax() { return plugin.getConfig().getInt("island.fragment-islands.max-count", 6); }
    public int getFragmentIslandsMinRadius() { return plugin.getConfig().getInt("island.fragment-islands.min-radius", 300); }
    public int getFragmentIslandsMaxRadius() { return plugin.getConfig().getInt("island.fragment-islands.max-radius", 400); }

    public boolean isStarterKitEnabled() { return plugin.getConfig().getBoolean("starter-kit.enabled", true); }

    public List<ItemStack> getStarterKitItems() {
        List<String> raw = plugin.getConfig().getStringList("starter-kit.items");
        List<ItemStack> items = new ArrayList<>();
        for (String entry : raw) {
            String[] parts = entry.split(":");
            try {
                Material mat = Material.valueOf(parts[0]);
                int amount = parts.length > 1 ? Integer.parseInt(parts[1]) : 1;
                items.add(new ItemStack(mat, amount));
            } catch (Exception ignored) {}
        }
        return items;
    }

    public boolean isWebApiEnabled() { return plugin.getConfig().getBoolean("web-api.enabled", true); }
    public int getWebApiPort() { return plugin.getConfig().getInt("web-api.port", 8080); }

    public String getLeaderboardWeeklyDay() { return plugin.getConfig().getString("leaderboard.weekly-settle-day", "SUNDAY"); }
    public String getLeaderboardWeeklyTime() { return plugin.getConfig().getString("leaderboard.weekly-settle-time", "20:00"); }
    public int getLeaderboardMonthlyDay() { return plugin.getConfig().getInt("leaderboard.monthly-settle-day", 1); }
}
