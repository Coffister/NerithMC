package gg.nerith.core.island;

import java.util.UUID;

public class IslandStats {

    private final UUID islandId;
    private long mobsKilled;
    private int bossesKilled;
    private int eventsTriggered;
    private int treasureFound;

    public IslandStats(UUID islandId, long mobsKilled, int bossesKilled, int eventsTriggered, int treasureFound) {
        this.islandId = islandId;
        this.mobsKilled = mobsKilled;
        this.bossesKilled = bossesKilled;
        this.eventsTriggered = eventsTriggered;
        this.treasureFound = treasureFound;
    }

    public UUID getIslandId() { return islandId; }
    public long getMobsKilled() { return mobsKilled; }
    public int getBossesKilled() { return bossesKilled; }
    public int getEventsTriggered() { return eventsTriggered; }
    public int getTreasureFound() { return treasureFound; }
}
