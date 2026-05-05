package gg.nerith.core.api.events;

import gg.nerith.core.island.Island;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class NerithVoidDeathEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    private boolean cancelled;

    private final Player player;
    private final Island island;
    private boolean itemsDestroyed;

    public NerithVoidDeathEvent(Player player, Island island, boolean itemsDestroyed) {
        this.player = player;
        this.island = island;
        this.itemsDestroyed = itemsDestroyed;
    }

    public Player getPlayer() { return player; }
    public Island getIsland() { return island; }
    public boolean isItemsDestroyed() { return itemsDestroyed; }
    public void setItemsDestroyed(boolean v) { itemsDestroyed = v; }

    @Override public boolean isCancelled() { return cancelled; }
    @Override public void setCancelled(boolean c) { cancelled = c; }
    @Override public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
