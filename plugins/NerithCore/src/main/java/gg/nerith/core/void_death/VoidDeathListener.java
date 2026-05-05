package gg.nerith.core.void_death;

import gg.nerith.core.NerithCore;
import gg.nerith.core.api.events.NerithPowerupActivateEvent;
import gg.nerith.core.api.events.NerithVoidDeathEvent;
import gg.nerith.core.island.Island;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import java.sql.SQLException;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;

public class VoidDeathListener implements Listener {

    private final NerithCore plugin;

    public VoidDeathListener(NerithCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        int voidY = plugin.getConfigManager().getVoidYLevel();
        if (player.getLocation().getY() > voidY) return;
        if (!"oneblock_world".equals(player.getWorld().getName())) return;

        Optional<Island> islandOpt = plugin.getIslandManager().getIslandByPlayer(player.getUniqueId());
        if (islandOpt.isEmpty()) return;

        Island island = islandOpt.get();
        Set<PowerupManager.PowerupType> powerups = plugin.getPowerupManager().getActivePowerups(island);

        if (powerups.contains(PowerupManager.PowerupType.VOID_SHIELD)) {
            activateVoidShield(player, island);
            return;
        }

        if (powerups.contains(PowerupManager.PowerupType.FRAGMENT_SAVER)) {
            activateFragmentSaver(player, island);
            return;
        }

        handleVoidDeath(player, island, true);
    }

    private void activateVoidShield(Player player, Island island) {
        NerithPowerupActivateEvent event = new NerithPowerupActivateEvent(player, island, NerithPowerupActivateEvent.PowerupType.VOID_SHIELD);
        Bukkit.getPluginManager().callEvent(event);

        try {
            plugin.getPowerupManager().consumePowerup(island, PowerupManager.PowerupType.VOID_SHIELD);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to consume void shield", e);
        }

        Location islandSpawn = new Location(player.getWorld(), island.getSpawnX(), island.getSpawnY(), island.getSpawnZ());
        player.teleport(islandSpawn);
        player.setVelocity(new Vector(0, 1.5, 0));
        plugin.getMessageManager().send(player, "powerup.void-shield.activated");
    }

    private void activateFragmentSaver(Player player, Island island) {
        NerithPowerupActivateEvent event = new NerithPowerupActivateEvent(player, island, NerithPowerupActivateEvent.PowerupType.FRAGMENT_SAVER);
        Bukkit.getPluginManager().callEvent(event);

        try {
            plugin.getPowerupManager().consumePowerup(island, PowerupManager.PowerupType.FRAGMENT_SAVER);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to consume fragment saver", e);
        }

        plugin.getMessageManager().send(player, "powerup.fragment-saver.activated");
        handleVoidDeath(player, island, false);
    }

    private void handleVoidDeath(Player player, Island island, boolean destroyAll) {
        NerithVoidDeathEvent event = new NerithVoidDeathEvent(player, island, destroyAll);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        if (event.isItemsDestroyed()) {
            player.getInventory().clear();
        } else {
            destroyPercent(player, 0.30);
        }

        player.setExp(0);
        player.setLevel(0);

        Location spawn = new Location(player.getWorld(), island.getSpawnX(), island.getSpawnY(), island.getSpawnZ());
        Bukkit.getScheduler().runTask(plugin, () -> {
            player.teleport(spawn);
            player.setHealth(Math.max(1.0, player.getHealth() - 10));
        });

        plugin.getMessageManager().send(player, "void.death");
    }

    private void destroyPercent(Player player, double pct) {
        var inv = player.getInventory();
        for (int i = 0; i < inv.getSize(); i++) {
            if (Math.random() < pct) {
                inv.setItem(i, null);
            }
        }
    }
}
