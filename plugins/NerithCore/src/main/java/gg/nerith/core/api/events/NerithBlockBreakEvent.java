package gg.nerith.core.api.events;

import gg.nerith.core.island.Island;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class NerithBlockBreakEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    private boolean cancelled;

    private final Island island;
    private final Player player;
    private final Block block;

    public NerithBlockBreakEvent(Island island, Player player, Block block) {
        this.island = island;
        this.player = player;
        this.block = block;
    }

    public Island getIsland() { return island; }
    public Player getPlayer() { return player; }
    public Block getBlock() { return block; }

    @Override public boolean isCancelled() { return cancelled; }
    @Override public void setCancelled(boolean c) { cancelled = c; }
    @Override public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
