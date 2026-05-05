package gg.nerith.core.island;

import gg.nerith.core.NerithCore;
import org.bukkit.entity.Player;

public class IslandPermissionManager {

    private final NerithCore plugin;

    public IslandPermissionManager(NerithCore plugin) {
        this.plugin = plugin;
    }

    public boolean canBreak(Player visitor, Island island) {
        if (plugin.getIslandManager().isMember(island, visitor.getUniqueId())) return true;
        return plugin.getIslandManager().getPermissions(island.getId()).isAllowBreak();
    }

    public boolean canPlace(Player visitor, Island island) {
        if (plugin.getIslandManager().isMember(island, visitor.getUniqueId())) return true;
        return plugin.getIslandManager().getPermissions(island.getId()).isAllowPlace();
    }

    public boolean canInteract(Player visitor, Island island) {
        if (plugin.getIslandManager().isMember(island, visitor.getUniqueId())) return true;
        return plugin.getIslandManager().getPermissions(island.getId()).isAllowInteract();
    }

    public boolean canOpenChest(Player visitor, Island island) {
        if (plugin.getIslandManager().isMember(island, visitor.getUniqueId())) return true;
        return plugin.getIslandManager().getPermissions(island.getId()).isAllowChest();
    }

    public boolean canKillMobs(Player visitor, Island island) {
        if (plugin.getIslandManager().isMember(island, visitor.getUniqueId())) return true;
        return plugin.getIslandManager().getPermissions(island.getId()).isAllowKillMobs();
    }
}
