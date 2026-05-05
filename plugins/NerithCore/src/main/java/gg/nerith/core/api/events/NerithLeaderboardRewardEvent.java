package gg.nerith.core.api.events;

import gg.nerith.core.island.Island;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class NerithLeaderboardRewardEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    public enum Period { WEEKLY, MONTHLY }

    private final UUID winnerUuid;
    private final Island island;
    private final Period period;
    private final int rank;

    public NerithLeaderboardRewardEvent(UUID winnerUuid, Island island, Period period, int rank) {
        this.winnerUuid = winnerUuid;
        this.island = island;
        this.period = period;
        this.rank = rank;
    }

    public UUID getWinnerUuid() { return winnerUuid; }
    public Island getIsland() { return island; }
    public Period getPeriod() { return period; }
    public int getRank() { return rank; }

    @Override public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
