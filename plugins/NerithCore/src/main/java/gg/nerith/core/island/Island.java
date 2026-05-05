package gg.nerith.core.island;

import java.util.UUID;

public class Island {

    public enum Type { SOLO, COOP }

    private final UUID id;
    private final UUID ownerUuid;
    private Type type;
    private int phase;
    private long blocksBroken;
    private double spawnX;
    private double spawnY;
    private double spawnZ;
    private String world;
    private String currentBlock;

    public Island(UUID id, UUID ownerUuid, Type type, int phase, long blocksBroken,
                  double spawnX, double spawnY, double spawnZ, String world, String currentBlock) {
        this.id = id;
        this.ownerUuid = ownerUuid;
        this.type = type;
        this.phase = phase;
        this.blocksBroken = blocksBroken;
        this.spawnX = spawnX;
        this.spawnY = spawnY;
        this.spawnZ = spawnZ;
        this.world = world;
        this.currentBlock = currentBlock != null ? currentBlock : "STONE";
    }

    public UUID getId() { return id; }
    public UUID getOwnerUuid() { return ownerUuid; }
    public Type getType() { return type; }
    public int getPhase() { return phase; }
    public long getBlocksBroken() { return blocksBroken; }
    public double getSpawnX() { return spawnX; }
    public double getSpawnY() { return spawnY; }
    public double getSpawnZ() { return spawnZ; }
    public String getWorld() { return world; }
    public String getCurrentBlock() { return currentBlock; }

    public void setType(Type type) { this.type = type; }
    public void setPhase(int phase) { this.phase = phase; }
    public void setBlocksBroken(long blocksBroken) { this.blocksBroken = blocksBroken; }
    public void setSpawn(double x, double y, double z) { spawnX = x; spawnY = y; spawnZ = z; }
    public void setWorld(String world) { this.world = world; }
    public void setCurrentBlock(String currentBlock) { this.currentBlock = currentBlock; }

    public void incrementBlocksBroken() { blocksBroken++; }

    public int getCenterX() {
        return (int) (id.getMostSignificantBits() % 1000000) * 1000;
    }

    public int getCenterZ() {
        return (int) (id.getLeastSignificantBits() % 1000000) * 1000;
    }
}
