package gg.nerith.core.api.events;

import gg.nerith.core.island.Island;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class NerithIslandCreateEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Island island;
    private final Player owner;

    public NerithIslandCreateEvent(Island island, Player owner) {
        this.island = island;
        this.owner = owner;
    }

    public Island getIsland() { return island; }
    public Player getOwner() { return owner; }

    @Override public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
