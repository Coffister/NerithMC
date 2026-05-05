package gg.nerith.core.api.events;

import gg.nerith.core.island.Island;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class NerithIslandResetEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    private boolean cancelled;

    private final Island island;
    private final Player owner;
    private double penaltyPercent;
    private double penaltyAmount;

    public NerithIslandResetEvent(Island island, Player owner, double penaltyPercent) {
        this.island = island;
        this.owner = owner;
        this.penaltyPercent = penaltyPercent;
    }

    public Island getIsland() { return island; }
    public Player getOwner() { return owner; }
    public double getPenaltyPercent() { return penaltyPercent; }
    public double getPenaltyAmount() { return penaltyAmount; }
    public void setPenaltyAmount(double amount) { penaltyAmount = amount; }

    @Override public boolean isCancelled() { return cancelled; }
    @Override public void setCancelled(boolean c) { cancelled = c; }
    @Override public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
