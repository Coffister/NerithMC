package gg.nerith.core.api.events;

import gg.nerith.core.island.Island;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class NerithMemberLeaveEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Island island;
    private final Player player;
    private final boolean kicked;

    public NerithMemberLeaveEvent(Island island, Player player, boolean kicked) {
        this.island = island;
        this.player = player;
        this.kicked = kicked;
    }

    public Island getIsland() { return island; }
    public Player getPlayer() { return player; }
    public boolean isKicked() { return kicked; }

    @Override public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
