package gg.nerith.core.api.events;

import gg.nerith.core.island.Island;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class NerithSpecialBlockEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    private boolean cancelled;

    public enum SpecialType { TREASURE_BLOCK, BOSS_SPAWN, LORE_BLOCK }

    private final Island island;
    private final Player trigger;
    private final SpecialType specialType;

    public NerithSpecialBlockEvent(Island island, Player trigger, SpecialType specialType) {
        this.island = island;
        this.trigger = trigger;
        this.specialType = specialType;
    }

    public Island getIsland() { return island; }
    public Player getTrigger() { return trigger; }
    public SpecialType getSpecialType() { return specialType; }

    @Override public boolean isCancelled() { return cancelled; }
    @Override public void setCancelled(boolean c) { cancelled = c; }
    @Override public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
