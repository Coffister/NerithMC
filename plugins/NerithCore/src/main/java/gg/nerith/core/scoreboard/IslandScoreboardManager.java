package gg.nerith.core.scoreboard;

import gg.nerith.core.NerithCore;
import gg.nerith.core.config.PhaseConfig;
import gg.nerith.core.island.Island;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.*;

public class IslandScoreboardManager implements Listener {

    private static final int BAR_WIDTH = 10;

    private final NerithCore plugin;

    public IslandScoreboardManager(NerithCore plugin) {
        this.plugin = plugin;
    }

    // ── Events ────────────────────────────────────────────────────────────────

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // Delay so island cache and world are ready
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            plugin.getIslandManager().getIslandByPlayer(player.getUniqueId())
                    .ifPresent(island -> show(player, island));
        }, 40L);
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /** Renders/refreshes the sidebar for a single player. Always call on main thread. */
    public void show(Player player, Island island) {
        ScoreboardManager sbm = Bukkit.getScoreboardManager();
        Scoreboard board = sbm.getNewScoreboard();

        Component title = Component.text("✦ ONEBLOCK ✦", NamedTextColor.GOLD, TextDecoration.BOLD);
        Objective obj = board.registerNewObjective("nerith_ob", Criteria.DUMMY, title);
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        renderLines(obj, island);
        player.setScoreboard(board);
    }

    /** Re-renders the scoreboard for every online member of the given island. */
    public void updateAll(Island island) {
        plugin.getIslandManager().getMembers(island.getId()).forEach(member -> {
            Player p = Bukkit.getPlayer(member.getPlayerUuid());
            if (p != null && p.isOnline()) show(p, island);
        });
    }

    // ── Rendering ─────────────────────────────────────────────────────────────

    private void renderLines(Objective obj, Island island) {
        int phase       = island.getPhase();
        long broken     = island.getBlocksBroken();
        int totalPhases = plugin.getConfigManager().getPhaseCount();
        boolean maxPhase = phase >= totalPhases;

        PhaseConfig curConfig  = plugin.getConfigManager().getPhaseConfig(phase);
        String phaseName       = curConfig.getName();
        long phaseStart        = curConfig.getBlocksRequired();

        long phaseEnd = 0;
        double pct    = 1.0;
        if (!maxPhase) {
            PhaseConfig nextConfig = plugin.getConfigManager().getPhaseConfig(phase + 1);
            phaseEnd = nextConfig.getBlocksRequired();
            long range = phaseEnd - phaseStart;
            pct = range > 0 ? Math.min(1.0, Math.max(0.0, (double)(broken - phaseStart) / range)) : 0;
        }

        int filled = (int) Math.round(pct * BAR_WIDTH);
        String bar = ChatColor.GREEN + "█".repeat(filled) + ChatColor.DARK_GRAY + "█".repeat(BAR_WIDTH - filled);

        int members = plugin.getIslandManager().getMemberCount(island);

        // Scores are displayed highest-first on the sidebar
        int s = 10;
        line(obj, gap(0), s--);
        line(obj, ChatColor.YELLOW + "Fáza  " + ChatColor.WHITE + phase + ChatColor.GRAY + "/" + totalPhases, s--);
        line(obj, ChatColor.AQUA + "" + ChatColor.BOLD + phaseName, s--);
        line(obj, gap(1), s--);
        line(obj, ChatColor.YELLOW + "Bloky  " + ChatColor.WHITE + broken, s--);
        if (!maxPhase) {
            line(obj, ChatColor.YELLOW + "Ďalšia  " + ChatColor.WHITE + phaseEnd, s--);
            line(obj, bar + " " + ChatColor.YELLOW + String.format("%d%%", Math.round(pct * 100)), s--);
        } else {
            line(obj, ChatColor.GOLD + "" + ChatColor.BOLD + "✔ Finálna fáza!", s--);
        }
        line(obj, gap(2), s--);
        line(obj, ChatColor.GRAY + "Členovia  " + ChatColor.WHITE + members, s--);
        line(obj, gap(3), s--);
    }

    /** Unique invisible padding strings to avoid duplicate-entry collisions. */
    private static String gap(int id) {
        return ChatColor.RESET.toString() + " ".repeat(id + 1);
    }

    private static void line(Objective obj, String text, int score) {
        obj.getScore(text).setScore(score);
    }
}
