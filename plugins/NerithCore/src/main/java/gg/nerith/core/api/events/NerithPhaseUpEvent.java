package gg.nerith.core.api.events;

import gg.nerith.core.island.Island;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class NerithPhaseUpEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    private boolean cancelled;

    private final Island island;
    private final int oldPhase;
    private final int newPhase;
    private final Player trigger;

    public NerithPhaseUpEvent(Island island, int oldPhase, int newPhase, Player trigger) {
        this.island = island;
        this.oldPhase = oldPhase;
        this.newPhase = newPhase;
        this.trigger = trigger;
    }

    public Island getIsland() { return island; }
    public int getOldPhase() { return oldPhase; }
    public int getNewPhase() { return newPhase; }
    public Player getTrigger() { return trigger; }

    @Override public boolean isCancelled() { return cancelled; }
    @Override public void setCancelled(boolean c) { cancelled = c; }
    @Override public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
