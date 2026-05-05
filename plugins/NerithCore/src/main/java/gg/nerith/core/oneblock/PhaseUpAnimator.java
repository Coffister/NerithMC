package gg.nerith.core.oneblock;

import gg.nerith.core.NerithCore;
import gg.nerith.core.config.PhaseConfig;
import gg.nerith.core.island.Island;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class PhaseUpAnimator {

    private final NerithCore plugin;

    public PhaseUpAnimator(NerithCore plugin) {
        this.plugin = plugin;
    }

    public void playAnimation(Island island, PhaseConfig config, Player trigger) {
        UUID islandId = island.getId();
        plugin.getOneBlockListener().freezeOneBlock(islandId);

        Location center = getOneBlockLocation(island, trigger);

        spawnParticles(center, config.getParticle());

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            spawnParticles(center, config.getParticle());
        }, 20L);

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            spawnParticles(center, config.getParticle());
            plugin.getOneBlockListener().unfreezeOneBlock(islandId);
        }, 60L);
    }

    private void spawnParticles(Location center, Particle particle) {
        if (center == null || center.getWorld() == null) return;
        World world = center.getWorld();
        for (int i = 0; i < 20; i++) {
            double angle = Math.random() * 2 * Math.PI;
            double radius = 1.5;
            double x = center.getX() + radius * Math.cos(angle);
            double z = center.getZ() + radius * Math.sin(angle);
            try {
                world.spawnParticle(particle, x, center.getY() + 0.5, z, 3);
            } catch (Exception ignored) {}
        }
    }

    private Location getOneBlockLocation(Island island, Player fallback) {
        return new Location(fallback.getWorld(), island.getSpawnX(), island.getSpawnY() - 1, island.getSpawnZ());
    }
}
