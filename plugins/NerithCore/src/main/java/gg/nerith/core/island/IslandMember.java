package gg.nerith.core.island;

import java.util.UUID;

public class IslandMember {

    public enum Role { OWNER, MEMBER }

    private final UUID islandId;
    private final UUID playerUuid;
    private Role role;

    public IslandMember(UUID islandId, UUID playerUuid, Role role) {
        this.islandId = islandId;
        this.playerUuid = playerUuid;
        this.role = role;
    }

    public UUID getIslandId() { return islandId; }
    public UUID getPlayerUuid() { return playerUuid; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public boolean isOwner() { return role == Role.OWNER; }
}
