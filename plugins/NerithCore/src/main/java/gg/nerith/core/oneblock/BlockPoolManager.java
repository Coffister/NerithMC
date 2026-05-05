package gg.nerith.core.oneblock;

import gg.nerith.core.NerithCore;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

public class BlockPoolManager {

    public record PoolEntry(Material material, int weight, String specialEvent) {}

    private final NerithCore plugin;
    private final Map<Integer, List<PoolEntry>> phasePools = new HashMap<>();

    public BlockPoolManager(NerithCore plugin) {
        this.plugin = plugin;
    }

    public void load() {
        phasePools.clear();
        ConfigurationSection phases = plugin.getConfig().getConfigurationSection("phases");
        if (phases == null) return;
        for (String key : phases.getKeys(false)) {
            int phase = Integer.parseInt(key);
            ConfigurationSection pool = phases.getConfigurationSection(key + ".block-pool");
            List<PoolEntry> entries = new ArrayList<>();
            if (pool != null) {
                for (String mat : pool.getKeys(false)) {
                    ConfigurationSection entry = pool.getConfigurationSection(mat);
                    if (entry == null) continue;
                    try {
                        Material material = Material.valueOf(mat.toUpperCase());
                        int weight = entry.getInt("weight", 1);
                        String special = entry.getString("special", null);
                        entries.add(new PoolEntry(material, weight, special));
                    } catch (Exception ignored) {}
                }
            }
            if (entries.isEmpty()) {
                entries.add(defaultForPhase(phase));
            }
            phasePools.put(phase, entries);
        }
    }

    public PoolEntry pickRandom(int phase) {
        List<PoolEntry> pool = phasePools.getOrDefault(phase, List.of(new PoolEntry(Material.STONE, 1, null)));
        int totalWeight = pool.stream().mapToInt(PoolEntry::weight).sum();
        int roll = new Random().nextInt(Math.max(totalWeight, 1));
        int cumulative = 0;
        for (PoolEntry entry : pool) {
            cumulative += entry.weight();
            if (roll < cumulative) return entry;
        }
        return pool.get(pool.size() - 1);
    }

    private PoolEntry defaultForPhase(int phase) {
        return switch (phase) {
            case 1 -> new PoolEntry(Material.STONE, 1, null);
            case 2 -> new PoolEntry(Material.GRASS_BLOCK, 1, null);
            case 3 -> new PoolEntry(Material.MOSSY_COBBLESTONE, 1, null);
            case 4 -> new PoolEntry(Material.SAND, 1, null);
            case 5 -> new PoolEntry(Material.NETHERRACK, 1, null);
            case 6 -> new PoolEntry(Material.PURPUR_BLOCK, 1, null);
            case 7 -> new PoolEntry(Material.END_STONE, 1, null);
            default -> new PoolEntry(Material.STONE, 1, null);
        };
    }
}
