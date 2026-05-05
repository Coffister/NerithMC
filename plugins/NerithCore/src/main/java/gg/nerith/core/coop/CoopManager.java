package gg.nerith.core.coop;

import gg.nerith.core.NerithCore;
import gg.nerith.core.api.events.NerithMemberJoinEvent;
import gg.nerith.core.api.events.NerithMemberLeaveEvent;
import gg.nerith.core.island.Island;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class CoopManager {

    private final NerithCore plugin;
    private final Map<UUID, UUID> pendingInvites = new HashMap<>();
    private final Map<UUID, UUID> pendingRequests = new HashMap<>();

    public CoopManager(NerithCore plugin) {
        this.plugin = plugin;
    }

    public void sendInvite(Player owner, Player target) {
        plugin.getIslandManager().getIslandByPlayer(owner.getUniqueId()).ifPresentOrElse(island -> {
            if (island.getType() != Island.Type.COOP) {
                plugin.getMessageManager().send(owner, "coop.not-coop-island");
                return;
            }
            int max = plugin.getConfigManager().getIslandMaxMembers();
            if (plugin.getIslandManager().getMemberCount(island) >= max) {
                plugin.getMessageManager().send(owner, "coop.island-full");
                return;
            }
            if (plugin.getIslandManager().getIslandByPlayer(target.getUniqueId()).isPresent()) {
                plugin.getMessageManager().send(owner, "coop.target-has-island");
                return;
            }
            pendingInvites.put(target.getUniqueId(), owner.getUniqueId());
            plugin.getMessageManager().send(owner, "coop.invite-sent",
                    Map.of("player", target.getName()));
            plugin.getMessageManager().send(target, "coop.invite-received",
                    Map.of("owner", owner.getName()));

            Bukkit.getScheduler().runTaskLater(plugin, () ->
                    pendingInvites.remove(target.getUniqueId()), 20L * 60);
        }, () -> plugin.getMessageManager().send(owner, "island.no-island"));
    }

    public void sendRequest(Player requester, Player target) {
        plugin.getIslandManager().getIslandByPlayer(target.getUniqueId()).ifPresentOrElse(island -> {
            if (island.getType() != Island.Type.COOP) {
                plugin.getMessageManager().send(requester, "coop.not-coop-island");
                return;
            }
            if (plugin.getIslandManager().getIslandByPlayer(requester.getUniqueId()).isPresent()) {
                plugin.getMessageManager().send(requester, "island.already-has-island");
                return;
            }
            pendingRequests.put(requester.getUniqueId(), target.getUniqueId());
            plugin.getMessageManager().send(requester, "coop.request-sent",
                    Map.of("player", target.getName()));
            Player owner = Bukkit.getPlayer(island.getOwnerUuid());
            if (owner != null) {
                plugin.getMessageManager().send(owner, "coop.request-received",
                        Map.of("player", requester.getName()));
            }
        }, () -> plugin.getMessageManager().send(requester, "coop.target-no-island"));
    }

    public void acceptInvite(Player player) {
        UUID ownerUuid = pendingInvites.remove(player.getUniqueId());
        if (ownerUuid == null) {
            plugin.getIslandManager().getIslandByPlayer(player.getUniqueId()).ifPresent(island -> {
                UUID requesterUuid = pendingRequests.entrySet().stream()
                        .filter(e -> e.getValue().equals(player.getUniqueId()))
                        .map(Map.Entry::getKey).findFirst().orElse(null);
                if (requesterUuid != null) {
                    pendingRequests.remove(requesterUuid);
                    Player requester = Bukkit.getPlayer(requesterUuid);
                    if (requester != null) acceptMember(requester, island, player);
                    return;
                }
            });
            plugin.getMessageManager().send(player, "coop.no-pending");
            return;
        }
        plugin.getIslandManager().getIslandByPlayer(ownerUuid).ifPresentOrElse(island -> {
            acceptMember(player, island, Bukkit.getPlayer(ownerUuid));
        }, () -> plugin.getMessageManager().send(player, "island.no-island"));
    }

    private void acceptMember(Player newMember, Island island, Player notifyOwner) {
        NerithMemberJoinEvent event = new NerithMemberJoinEvent(island, newMember);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;
        try {
            plugin.getIslandManager().addMember(island, newMember.getUniqueId());
            plugin.getMessageManager().send(newMember, "coop.joined",
                    Map.of("island", island.getOwnerUuid().toString()));
            if (notifyOwner != null) {
                plugin.getMessageManager().send(notifyOwner, "coop.member-joined",
                        Map.of("player", newMember.getName()));
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to add member", e);
        }
    }

    public void denyInviteOrRequest(Player player) {
        if (pendingInvites.remove(player.getUniqueId()) != null) {
            plugin.getMessageManager().send(player, "coop.invite-denied");
            return;
        }
        pendingRequests.remove(player.getUniqueId());
        plugin.getMessageManager().send(player, "coop.request-denied");
    }

    public void kickMember(Player owner, Player target) {
        plugin.getIslandManager().getIslandByPlayer(owner.getUniqueId()).ifPresentOrElse(island -> {
            if (!island.getOwnerUuid().equals(owner.getUniqueId())) {
                plugin.getMessageManager().send(owner, "island.not-owner");
                return;
            }
            if (!plugin.getIslandManager().isMember(island, target.getUniqueId())) {
                plugin.getMessageManager().send(owner, "coop.not-member");
                return;
            }
            NerithMemberLeaveEvent event = new NerithMemberLeaveEvent(island, target, true);
            Bukkit.getPluginManager().callEvent(event);
            try {
                plugin.getIslandManager().removeMember(island, target.getUniqueId());
                plugin.getMessageManager().send(target, "coop.kicked");
                plugin.getMessageManager().send(owner, "coop.kicked-success",
                        Map.of("player", target.getName()));
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to kick member", e);
            }
        }, () -> plugin.getMessageManager().send(owner, "island.no-island"));
    }

    public void leaveIsland(Player player) {
        plugin.getIslandManager().getIslandByPlayer(player.getUniqueId()).ifPresentOrElse(island -> {
            if (island.getOwnerUuid().equals(player.getUniqueId())) {
                plugin.getMessageManager().send(player, "island.owner-cannot-leave");
                return;
            }
            NerithMemberLeaveEvent event = new NerithMemberLeaveEvent(island, player, false);
            Bukkit.getPluginManager().callEvent(event);
            try {
                plugin.getIslandManager().removeMember(island, player.getUniqueId());
                plugin.getMessageManager().send(player, "coop.left");
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to leave island", e);
            }
        }, () -> plugin.getMessageManager().send(player, "island.no-island"));
    }

    public void transferOwnership(Player owner, Player target) {
        plugin.getIslandManager().getIslandByPlayer(owner.getUniqueId()).ifPresentOrElse(island -> {
            if (!island.getOwnerUuid().equals(owner.getUniqueId())) {
                plugin.getMessageManager().send(owner, "island.not-owner");
                return;
            }
            if (!plugin.getIslandManager().isMember(island, target.getUniqueId())) {
                plugin.getMessageManager().send(owner, "coop.not-member");
                return;
            }
            try {
                plugin.getIslandManager().transferOwnership(island, target.getUniqueId());
                plugin.getMessageManager().send(owner, "coop.transfer-done",
                        Map.of("player", target.getName()));
                plugin.getMessageManager().send(target, "coop.transfer-received");
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to transfer ownership", e);
            }
        }, () -> plugin.getMessageManager().send(owner, "island.no-island"));
    }
}
