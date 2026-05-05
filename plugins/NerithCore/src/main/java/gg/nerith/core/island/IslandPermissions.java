package gg.nerith.core.island;

import java.util.UUID;

public class IslandPermissions {

    private final UUID islandId;
    private boolean allowBreak;
    private boolean allowPlace;
    private boolean allowInteract;
    private boolean allowChest;
    private boolean allowKillMobs;

    public IslandPermissions(UUID islandId) {
        this.islandId = islandId;
    }

    public IslandPermissions(UUID islandId, boolean allowBreak, boolean allowPlace,
                             boolean allowInteract, boolean allowChest, boolean allowKillMobs) {
        this.islandId = islandId;
        this.allowBreak = allowBreak;
        this.allowPlace = allowPlace;
        this.allowInteract = allowInteract;
        this.allowChest = allowChest;
        this.allowKillMobs = allowKillMobs;
    }

    public UUID getIslandId() { return islandId; }
    public boolean isAllowBreak() { return allowBreak; }
    public boolean isAllowPlace() { return allowPlace; }
    public boolean isAllowInteract() { return allowInteract; }
    public boolean isAllowChest() { return allowChest; }
    public boolean isAllowKillMobs() { return allowKillMobs; }

    public void setAllowBreak(boolean v) { allowBreak = v; }
    public void setAllowPlace(boolean v) { allowPlace = v; }
    public void setAllowInteract(boolean v) { allowInteract = v; }
    public void setAllowChest(boolean v) { allowChest = v; }
    public void setAllowKillMobs(boolean v) { allowKillMobs = v; }
}
