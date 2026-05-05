package gg.nerith.core.gui;

import gg.nerith.core.NerithCore;
import gg.nerith.core.config.PhaseConfig;
import gg.nerith.core.island.Island;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.sql.SQLException;

/**
 * Main island hub — 54 slots.
 *
 * Layout (with island):
 *   Row 0: border
 *   Row 1: border  [INFO @ 13]  border
 *   Row 2: border  [HOME@20]  [MEMBERS@22]  [SETTINGS@24]  border
 *   Row 3: border  [STATS@29]  [PHASE@31]   [PERKS@33]     border
 *   Row 4: border  [LEADER@40]              border
 *   Row 5: border  [RESET@49]  border
 */
public class MainIslandGUI implements IslandGui {

    private static final Component TITLE = Component.text("✦ Ostrov  ", NamedTextColor.GOLD, TextDecoration.BOLD)
            .append(Component.text("| ", NamedTextColor.DARK_GRAY).decoration(TextDecoration.BOLD, false))
            .append(Component.text("Menu", NamedTextColor.WHITE).decoration(TextDecoration.BOLD, false));

    private final NerithCore plugin;
    private final Player player;
    private final Island island; // null → player has no island

    public MainIslandGUI(NerithCore plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.island = plugin.getIslandManager().getIslandByPlayer(player.getUniqueId()).orElse(null);
    }

    public void open() {
        Inventory inv = Bukkit.createInventory(null, 54, TITLE);
        GuiUtil.border(inv);
        if (island == null) buildNoIsland(inv); else buildWithIsland(inv);
        player.openInventory(inv);
        plugin.getGuiListener().register(player, this);
    }

    // ── No-island screen ──────────────────────────────────────────────────────

    private void buildNoIsland(Inventory inv) {
        inv.setItem(22, GuiUtil.item(Material.GRASS_BLOCK,
                Component.text("Vytvoriť ostrov", NamedTextColor.GREEN, TextDecoration.BOLD),
                Component.text("Klikni  →  Solo ostrov", NamedTextColor.GRAY),
                Component.text("Shift+Klik  →  Coop ostrov", NamedTextColor.GRAY)
        ));
    }

    // ── Island screen ─────────────────────────────────────────────────────────

    private void buildWithIsland(Inventory inv) {
        int phase = island.getPhase();
        long broken = island.getBlocksBroken();
        int totalPhases = plugin.getConfigManager().getPhaseCount();
        boolean maxPhase = phase >= totalPhases;

        PhaseConfig cur = plugin.getConfigManager().getPhaseConfig(phase);
        long phaseStart = cur.getBlocksRequired();
        long phaseEnd   = maxPhase ? broken
                : plugin.getConfigManager().getPhaseConfig(phase + 1).getBlocksRequired();
        double pct = (maxPhase || phaseEnd == phaseStart) ? 1.0
                : (double)(broken - phaseStart) / (phaseEnd - phaseStart);

        String bar = GuiUtil.progressBar(pct);
        int members = plugin.getIslandManager().getMemberCount(island);

        // 13 — Island info card
        inv.setItem(13, GuiUtil.item(Material.NETHER_STAR,
                Component.text(cur.getName(), NamedTextColor.AQUA, TextDecoration.BOLD),
                Component.text("Fáza ", NamedTextColor.GRAY)
                        .append(Component.text(phase + " / " + totalPhases, NamedTextColor.YELLOW)),
                Component.text("Bloky ", NamedTextColor.GRAY)
                        .append(Component.text(String.valueOf(broken), NamedTextColor.WHITE)),
                Component.text(bar + " §e" + Math.round(pct * 100) + "%"),
                maxPhase
                        ? Component.text("✔ Finálna fáza!", NamedTextColor.GOLD)
                        : Component.text("Ďalšia fáza za " + Math.max(0, phaseEnd - broken) + " blokov", NamedTextColor.GRAY)
        ));

        // 20 — Home
        inv.setItem(20, GuiUtil.item(Material.RED_BED,
                Component.text("Domov", NamedTextColor.GREEN, TextDecoration.BOLD),
                Component.text("Teleportuj sa na ostrov", NamedTextColor.GRAY)
        ));

        // 22 — Members
        inv.setItem(22, GuiUtil.item(Material.PLAYER_HEAD,
                Component.text("Členovia", NamedTextColor.AQUA, TextDecoration.BOLD),
                Component.text("Spravuj členov ostrova", NamedTextColor.GRAY),
                Component.text("Aktuálne: " + members, NamedTextColor.GRAY)
        ));

        // 24 — Settings (owner only visual hint)
        boolean isOwner = island.getOwnerUuid().equals(player.getUniqueId());
        inv.setItem(24, GuiUtil.item(Material.COMPARATOR,
                Component.text("Nastavenia", isOwner ? NamedTextColor.YELLOW : NamedTextColor.DARK_GRAY, TextDecoration.BOLD),
                Component.text("Povolenia pre návštevníkov", NamedTextColor.GRAY),
                isOwner ? Component.empty()
                        : Component.text("Len pre vlastníka ostrova", NamedTextColor.RED)
        ));

        // 29 — Stats
        inv.setItem(29, GuiUtil.item(Material.BOOK,
                Component.text("Štatistiky", NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD),
                Component.text("Mobovia, udalosti, poklady…", NamedTextColor.GRAY)
        ));

        // 31 — Phase progress / phase list
        inv.setItem(31, GuiUtil.item(Material.EXPERIENCE_BOTTLE,
                Component.text("Fázy ostrova", NamedTextColor.YELLOW, TextDecoration.BOLD),
                Component.text("Prehľad fáz a odmien", NamedTextColor.GRAY)
        ));

        // 33 — Perks
        inv.setItem(33, GuiUtil.item(Material.BLAZE_POWDER,
                Component.text("Vylepšenia", NamedTextColor.GOLD, TextDecoration.BOLD),
                Component.text("Aktívne powerupy a štíty", NamedTextColor.GRAY)
        ));

        // 40 — Leaderboard
        inv.setItem(40, GuiUtil.item(Material.GOLD_INGOT,
                Component.text("Rebríček", NamedTextColor.YELLOW, TextDecoration.BOLD),
                Component.text("Top 10 ostrovov", NamedTextColor.GRAY)
        ));

        // 49 — Reset (danger)
        int penalty = plugin.getConfigManager().getResetPenalty(phase);
        inv.setItem(49, GuiUtil.item(Material.TNT,
                Component.text("⚠ Reset ostrova", NamedTextColor.RED, TextDecoration.BOLD),
                Component.text("Zmaže celý progress!", NamedTextColor.DARK_RED),
                Component.text("Penalizácia: " + penalty + "% blokov", NamedTextColor.GRAY)
        ));
    }

    // ── Click handling ────────────────────────────────────────────────────────

    @Override
    public void handleClick(InventoryClickEvent event) {
        int slot = event.getRawSlot();

        if (island == null) {
            if (slot == 22) createIsland(event.isShiftClick());
            return;
        }

        switch (slot) {
            case 20 -> { // Home — close then teleport (close triggers unregister via onClose)
                player.closeInventory();
                plugin.getIslandManager().teleportToIsland(player, island);
            }
            case 22 -> new MembersGUI(plugin, player, island).open();
            case 24 -> {
                if (!island.getOwnerUuid().equals(player.getUniqueId())) {
                    plugin.getMessageManager().send(player, "island.not-owner");
                    return;
                }
                new VisitorPermissionsGUI(plugin, player, island).open();
            }
            case 29 -> new StatsGUI(plugin, player, island).open();
            case 31 -> new IslandPerksGUI(plugin, player, island).open();
            case 33 -> new IslandPerksGUI(plugin, player, island).open();
            case 40 -> new LeaderboardGUI(plugin, player).open();
            case 49 -> new ResetConfirmGUI(plugin, player, island).open();
        }
    }

    private void createIsland(boolean coop) {
        player.closeInventory();
        try {
            plugin.getIslandManager().createIsland(player, coop ? Island.Type.COOP : Island.Type.SOLO);
        } catch (SQLException e) {
            plugin.getMessageManager().send(player, "island.create-failed");
            plugin.getLogger().warning("Island creation failed: " + e.getMessage());
        }
    }
}
