package gg.nerith.core.coop;

import gg.nerith.core.NerithCore;
import gg.nerith.core.island.Island;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Optional;

public class CoopListener implements Listener {

    private final NerithCore plugin;

    public CoopListener(NerithCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Optional<Island> islandOpt = getIslandAt(player);
        if (islandOpt.isEmpty()) return;
        Island island = islandOpt.get();
        if (plugin.getIslandManager().isMember(island, player.getUniqueId())) return;
        if (!plugin.getIslandPermissionManager().canBreak(player, island)) {
            event.setCancelled(true);
            plugin.getMessageManager().send(player, "island.perm.no-break");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Optional<Island> islandOpt = getIslandAt(player);
        if (islandOpt.isEmpty()) return;
        Island island = islandOpt.get();
        if (plugin.getIslandManager().isMember(island, player.getUniqueId())) return;
        if (!plugin.getIslandPermissionManager().canPlace(player, island)) {
            event.setCancelled(true);
            plugin.getMessageManager().send(player, "island.perm.no-place");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        if (block == null) return;
        Optional<Island> islandOpt = getIslandAt(player);
        if (islandOpt.isEmpty()) return;
        Island island = islandOpt.get();
        if (plugin.getIslandManager().isMember(island, player.getUniqueId())) return;

        boolean isChest = block.getType().name().contains("CHEST") || block.getType().name().contains("BARREL") || block.getType().name().contains("SHULKER");
        if (isChest) {
            if (!plugin.getIslandPermissionManager().canOpenChest(player, island)) {
                event.setCancelled(true);
                plugin.getMessageManager().send(player, "island.perm.no-chest");
            }
        } else if (!plugin.getIslandPermissionManager().canInteract(player, island)) {
            event.setCancelled(true);
            plugin.getMessageManager().send(player, "island.perm.no-interact");
        }
    }

    private Optional<Island> getIslandAt(Player player) {
        if (!"oneblock_world".equals(player.getWorld().getName())) return Optional.empty();
        return plugin.getIslandManager().getIslandByPlayer(player.getUniqueId());
    }
}
