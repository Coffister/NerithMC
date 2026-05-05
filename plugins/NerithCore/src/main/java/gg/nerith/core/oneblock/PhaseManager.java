package gg.nerith.core.oneblock;

import gg.nerith.core.NerithCore;
import gg.nerith.core.api.events.NerithPhaseUpEvent;
import gg.nerith.core.config.PhaseConfig;
import gg.nerith.core.island.Island;
import gg.nerith.core.island.IslandMember;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class PhaseManager {

    private final NerithCore plugin;

    public PhaseManager(NerithCore plugin) {
        this.plugin = plugin;
    }

    public void checkPhaseUp(Island island, Player breaker) {
        int currentPhase = island.getPhase();
        int nextPhase = currentPhase + 1;

        if (nextPhase > plugin.getConfigManager().getPhaseCount()) return;

        PhaseConfig nextConfig = plugin.getConfigManager().getPhaseConfig(nextPhase);
        if (island.getBlocksBroken() < nextConfig.getBlocksRequired()) return;

        NerithPhaseUpEvent event = new NerithPhaseUpEvent(island, currentPhase, nextPhase, breaker);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        island.setPhase(nextPhase);
        try {
            plugin.getIslandRepository().updatePhaseAndBlocks(island.getId(), nextPhase, island.getBlocksBroken());
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to update phase in database", e);
        }

        plugin.getPhaseUpAnimator().playAnimation(island, nextConfig, breaker);
        broadcastPhaseUp(island, nextConfig);
        giveRewardKit(island, nextConfig);
        plugin.getIslandScoreboardManager().updateAll(island);
    }

    private void broadcastPhaseUp(Island island, PhaseConfig config) {
        List<Player> members = getOnlineMembers(island.getId());
        Title title = Title.title(
                Component.text(config.getName(), NamedTextColor.GOLD),
                Component.text(config.getLoreLine(), NamedTextColor.GRAY),
                Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(4), Duration.ofMillis(500))
        );
        String chatMsg = plugin.getMessageManager().getRaw("phase.up.chat")
                .replace("{phase}", String.valueOf(config.getPhase()))
                .replace("{name}", config.getName());
        for (Player p : members) {
            p.showTitle(title);
            p.sendMessage(Component.text(chatMsg, NamedTextColor.GOLD));
        }
    }

    private void giveRewardKit(Island island, PhaseConfig config) {
        List<ItemStack> kit = config.getRewardKit();
        if (kit.isEmpty()) return;
        for (Player p : getOnlineMembers(island.getId())) {
            kit.forEach(item -> p.getInventory().addItem(item.clone()));
        }
    }

    private List<Player> getOnlineMembers(UUID islandId) {
        return plugin.getIslandManager().getMembers(islandId).stream()
                .map(m -> Bukkit.getPlayer(m.getPlayerUuid()))
                .filter(p -> p != null && p.isOnline())
                .toList();
    }
}
