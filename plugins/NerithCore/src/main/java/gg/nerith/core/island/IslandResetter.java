package gg.nerith.core.island;

import gg.nerith.core.NerithCore;
import gg.nerith.core.api.events.NerithIslandResetEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class IslandResetter {

    private final NerithCore plugin;

    public IslandResetter(NerithCore plugin) {
        this.plugin = plugin;
    }

    public void resetIsland(Player owner, Island island) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                int phase = island.getPhase();
                long blocks = island.getBlocksBroken();
                double penaltyPercent = plugin.getConfigManager().getResetPenalty(phase) / 100.0;

                NerithIslandResetEvent event = new NerithIslandResetEvent(island, owner, penaltyPercent);
                Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getPluginManager().callEvent(event));

                if (event.isCancelled()) return;

                plugin.getResetRepository().record(
                        island.getId(), owner.getUniqueId(), phase, blocks, event.getPenaltyAmount()
                );

                List<IslandMember> members = plugin.getIslandManager().getMembers(island.getId());

                plugin.getIslandManager().deleteIsland(island);

                Bukkit.getScheduler().runTask(plugin, () -> {
                    teleportToLobby(owner);
                    for (IslandMember member : members) {
                        if (member.getPlayerUuid().equals(owner.getUniqueId())) continue;
                        Player p = Bukkit.getPlayer(member.getPlayerUuid());
                        if (p != null) {
                            teleportToLobby(p);
                            plugin.getMessageManager().send(p, "island.reset.member-notified");
                        }
                    }

                    try {
                        plugin.getIslandManager().createIsland(owner, Island.Type.SOLO);
                    } catch (SQLException ex) {
                        plugin.getLogger().log(Level.SEVERE, "Failed to create new island after reset", ex);
                    }
                });

            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to reset island", e);
                Bukkit.getScheduler().runTask(plugin,
                        () -> plugin.getMessageManager().send(owner, "island.reset.error"));
            }
        });
    }

    private void teleportToLobby(Player player) {
        World lobby = Bukkit.getWorld("hlavni_uzel");
        if (lobby != null) {
            player.teleport(lobby.getSpawnLocation());
        } else {
            player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
        }
    }

    public double calculatePenaltyAmount(Island island, double balance) {
        double pct = plugin.getConfigManager().getResetPenalty(island.getPhase()) / 100.0;
        return balance * pct;
    }
}
