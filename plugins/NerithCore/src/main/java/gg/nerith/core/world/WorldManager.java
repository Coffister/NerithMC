package gg.nerith.core.world;

import gg.nerith.core.NerithCore;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;

public class WorldManager {

    private final NerithCore plugin;

    public WorldManager(NerithCore plugin) {
        this.plugin = plugin;
    }

    public void ensureWorlds() {
        ensureOneBlockWorld();
        ensureLobbyWorld();
    }

    private void ensureOneBlockWorld() {
        if (plugin.getServer().getWorld("oneblock_world") != null) return;

        plugin.getLogger().info("[NerithCore] Creating oneblock_world (void)...");
        World world = new WorldCreator("oneblock_world")
                .environment(World.Environment.NORMAL)
                .type(WorldType.FLAT)
                .generator(new VoidGenerator())
                .generateStructures(false)
                .createWorld();

        if (world != null) {
            world.setSpawnFlags(false, false);
            world.setAutoSave(true);
            world.setTime(6000);
            world.setGameRuleValue("doWeatherCycle", "false");
            world.setGameRuleValue("doMobSpawning", "false");
            world.setGameRuleValue("keepInventory", "false");
            world.setGameRuleValue("announceAdvancements", "false");
            plugin.getLogger().info("[NerithCore] oneblock_world created successfully.");
        } else {
            plugin.getLogger().severe("[NerithCore] Failed to create oneblock_world!");
        }
    }

    private void ensureLobbyWorld() {
        if (plugin.getServer().getWorld("hlavni_uzel") != null) return;

        plugin.getLogger().info("[NerithCore] Creating hlavni_uzel (lobby)...");
        World world = new WorldCreator("hlavni_uzel")
                .environment(World.Environment.NORMAL)
                .type(WorldType.NORMAL)
                .generateStructures(false)
                .createWorld();

        if (world != null) {
            plugin.getLogger().info("[NerithCore] hlavni_uzel created successfully.");
        } else {
            plugin.getLogger().warning("[NerithCore] Could not create hlavni_uzel, using default world as lobby.");
        }
    }
}
