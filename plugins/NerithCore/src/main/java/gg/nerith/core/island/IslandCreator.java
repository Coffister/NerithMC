package gg.nerith.core.island;

import gg.nerith.core.NerithCore;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Random;
import java.util.logging.Level;

public class IslandCreator {

    private final NerithCore plugin;
    private final Random random = new Random();

    public IslandCreator(NerithCore plugin) {
        this.plugin = plugin;
    }

    public void createIslandWorld(Island island, Player owner) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            World world = Bukkit.getWorld(island.getWorld());
            if (world == null) {
                plugin.getLogger().severe("[NerithCore] OneBlock world '" + island.getWorld() + "' not found during island creation!");
                return;
            }

            // OneBlock sits at Y=64; player spawns at Y=65
            int cx = (int) island.getSpawnX();
            int blockY = 64;
            int cz = (int) island.getSpawnZ();

            Block oneBlock = world.getBlockAt(cx, blockY, cz);
            oneBlock.setType(Material.STONE, false);

            island.setSpawn(cx, blockY + 1, cz);
            try {
                plugin.getIslandRepository().updateSpawn(island.getId(), cx, blockY + 1, cz);
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Failed to update spawn", e);
            }

            plugin.getOneBlockListener().registerOneBlock(island.getId(), oneBlock.getLocation());

            spawnFragmentIslands(island, world);

            Location spawnLoc = new Location(world, cx + 0.5, blockY + 1, cz + 0.5);
            owner.teleport(spawnLoc);
        });
    }

    private void spawnFragmentIslands(Island island, World world) {
        File structuresDir = new File(plugin.getDataFolder(), "structures");
        if (!structuresDir.exists() || structuresDir.listFiles() == null) {
            return;
        }

        int minCount = plugin.getConfigManager().getFragmentIslandsMin();
        int maxCount = plugin.getConfigManager().getFragmentIslandsMax();
        int count = minCount + random.nextInt(maxCount - minCount + 1);

        int minRadius = plugin.getConfigManager().getFragmentIslandsMinRadius();
        int maxRadius = plugin.getConfigManager().getFragmentIslandsMaxRadius();

        int cx = (int) island.getSpawnX();
        int cz = (int) island.getSpawnZ();

        for (int i = 0; i < count; i++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            int radius = minRadius + random.nextInt(maxRadius - minRadius + 1);
            int fx = cx + (int) (radius * Math.cos(angle));
            int fz = cz + (int) (radius * Math.sin(angle));
            placeFragmentIsland(world, fx, 64, fz);
        }
    }

    private void placeFragmentIsland(World world, int x, int y, int z) {
        Block base = world.getBlockAt(x, y, z);
        base.setType(Material.MOSSY_COBBLESTONE);
        Block top = world.getBlockAt(x, y + 1, z);
        top.setType(Material.CHEST);
    }
}
