package gg.nerith.core.gui;

import org.bukkit.event.inventory.InventoryClickEvent;

/** Marker interface implemented by every island GUI screen. */
public interface IslandGui {
    void handleClick(InventoryClickEvent event);
}
