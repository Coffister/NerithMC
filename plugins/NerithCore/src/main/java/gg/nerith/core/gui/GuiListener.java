package gg.nerith.core.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Central Bukkit listener that routes inventory clicks to whichever
 * IslandGui instance is currently open for the clicking player.
 *
 * Registration timing: each GUI calls register(player, this) AFTER
 * player.openInventory(), so the InventoryCloseEvent for the previous
 * screen has already fired and cleaned up the old entry.
 */
public class GuiListener implements Listener {

    private final Map<UUID, IslandGui> open = new HashMap<>();

    public void register(Player player, IslandGui gui) {
        open.put(player.getUniqueId(), gui);
    }

    public void unregister(Player player) {
        open.remove(player.getUniqueId());
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        IslandGui gui = open.get(player.getUniqueId());
        if (gui == null) return;

        // Always cancel clicks inside our GUIs to prevent item movement
        event.setCancelled(true);

        // Only handle clicks in the top (GUI) inventory, not the player's own hotbar
        if (event.getClickedInventory() == null) return;
        if (!event.getClickedInventory().equals(event.getView().getTopInventory())) return;

        gui.handleClick(event);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player player) {
            open.remove(player.getUniqueId());
        }
    }
}
