package gg.nerith.core.oneblock;

import gg.nerith.core.NerithCore;
import gg.nerith.core.api.events.NerithBlockBreakEvent;
import gg.nerith.core.island.Island;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class OneBlockListener implements Listener {

    private final NerithCore plugin;
    // island ID → OneBlock location
    private final Map<UUID, Location> oneBlockLocations = new ConcurrentHashMap<>();
    // location string → island ID (reverse lookup for physics events)
    private final Map<String, UUID> locationIndex = new ConcurrentHashMap<>();
    private final Set<UUID> frozen = ConcurrentHashMap.newKeySet();

    public OneBlockListener(NerithCore plugin) {
        this.plugin = plugin;
    }

    public void registerOneBlock(UUID islandId, Location loc) {
        Location blockLoc = loc.getBlock().getLocation();
        // Remove old index entry if re-registering
        Location old = oneBlockLocations.put(islandId, blockLoc);
        if (old != null) locationIndex.remove(locKey(old));
        locationIndex.put(locKey(blockLoc), islandId);
    }

    public void unregisterOneBlock(UUID islandId) {
        Location loc = oneBlockLocations.remove(islandId);
        if (loc != null) locationIndex.remove(locKey(loc));
    }

    public void freezeOneBlock(UUID islandId) { frozen.add(islandId); }
    public void unfreezeOneBlock(UUID islandId) { frozen.remove(islandId); }

    public boolean isOneBlock(Location loc) {
        return locationIndex.containsKey(locKey(loc));
    }

    // ── Physics protection ────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPhysics(BlockPhysicsEvent event) {
        if (locationIndex.containsKey(locKey(event.getBlock().getLocation()))) {
            event.setCancelled(true);
        }
    }

    // Catches a FallingBlock entity trying to replace the OneBlock (e.g. sand entity landing)
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (locationIndex.containsKey(locKey(event.getBlock().getLocation()))) {
            event.setCancelled(true);
        }
    }

    // ── OneBlock break ────────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();

        Optional<Island> islandOpt = plugin.getIslandManager().getIslandByPlayer(player.getUniqueId());
        if (islandOpt.isEmpty()) return;

        Island island = islandOpt.get();
        Location oneBlockLoc = oneBlockLocations.get(island.getId());
        if (oneBlockLoc == null) return;
        if (!block.getLocation().equals(oneBlockLoc)) return;

        if (frozen.contains(island.getId())) {
            event.setCancelled(true);
            plugin.getMessageManager().send(player, "oneblock.frozen");
            return;
        }

        NerithBlockBreakEvent customEvent = new NerithBlockBreakEvent(island, player, block);
        plugin.getServer().getPluginManager().callEvent(customEvent);
        if (customEvent.isCancelled()) {
            event.setCancelled(true);
            return;
        }

        event.setDropItems(false);
        island.incrementBlocksBroken();

        BlockPoolManager.PoolEntry entry = plugin.getBlockPoolManager().pickRandom(island.getPhase());

        if (entry.specialEvent() != null) {
            plugin.getSpecialEventManager().trigger(island, player, entry.specialEvent());
        } else {
            giveBlockDrop(player, entry);
        }

        plugin.getPhaseManager().checkPhaseUp(island, player);
        plugin.getIslandScoreboardManager().updateAll(island);

        Material nextMaterial = entry.specialEvent() != null ? Material.STONE : entry.material();
        island.setCurrentBlock(nextMaterial.name());
        saveBlockCountAndType(island, nextMaterial);

        plugin.getServer().getScheduler().runTask(plugin, () ->
                block.setType(nextMaterial, false)
        );
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void giveBlockDrop(Player player, BlockPoolManager.PoolEntry entry) {
        Location oneBlockLoc = oneBlockLocations.get(
                plugin.getIslandManager().getIslandByPlayer(player.getUniqueId())
                        .map(i -> i.getId()).orElse(null));

        // Spawn 2 blocks above the OneBlock (Y+2) — clear of the block itself,
        // the new regen block, and any blocks the player placed at foot level.
        Location dropLoc = oneBlockLoc != null
                ? oneBlockLoc.clone().add(0.5, 2.0, 0.5)
                : player.getEyeLocation();

        Item item = player.getWorld().dropItem(dropLoc, new org.bukkit.inventory.ItemStack(entry.material()));
        item.setVelocity(new Vector(0, 0.2, 0));
    }

    private void saveBlockCountAndType(Island island, Material material) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                plugin.getIslandRepository().updatePhaseAndBlocks(
                        island.getId(), island.getPhase(), island.getBlocksBroken());
                plugin.getIslandRepository().updateCurrentBlock(island.getId(), material.name());
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "Failed to persist block state", e);
            }
        });
    }

    private static String locKey(Location loc) {
        return loc.getWorld().getName() + ":" + loc.getBlockX() + ":" + loc.getBlockY() + ":" + loc.getBlockZ();
    }
}
