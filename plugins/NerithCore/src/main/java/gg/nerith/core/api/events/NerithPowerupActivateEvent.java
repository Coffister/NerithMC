package gg.nerith.core.api.events;

import gg.nerith.core.island.Island;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class NerithPowerupActivateEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    public enum PowerupType { VOID_SHIELD, FRAGMENT_SAVER }

    private final Player player;
    private final Island island;
    private final PowerupType powerupType;

    public NerithPowerupActivateEvent(Player player, Island island, PowerupType powerupType) {
        this.player = player;
        this.island = island;
        this.powerupType = powerupType;
    }

    public Player getPlayer() { return player; }
    public Island getIsland() { return island; }
    public PowerupType getPowerupType() { return powerupType; }

    @Override public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
