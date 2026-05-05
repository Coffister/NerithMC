package gg.nerith.core.gui;

import gg.nerith.core.NerithCore;
import gg.nerith.core.island.Island;
import gg.nerith.core.island.IslandStats;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.sql.SQLException;
import java.util.logging.Level;

/**
 * Island statistics screen — 27 slots.
 *
 * Layout:
 *   Row 0: border
 *   Row 1: border [MOBS@10] [BOSSES@12] [EVENTS@14] [TREASURES@16] border
 *   Row 2: border  ....  [BACK@22]  ....  border
 */
public class StatsGUI implements IslandGui {

    private static final Component TITLE = Component.text("✦ Ostrov  ", NamedTextColor.GOLD, TextDecoration.BOLD)
            .append(Component.text("| ", NamedTextColor.DARK_GRAY).decoration(TextDecoration.BOLD, false))
            .append(Component.text("Štatistiky", NamedTextColor.WHITE).decoration(TextDecoration.BOLD, false));

    private final NerithCore plugin;
    private final Player player;
    private final Island island;

    public StatsGUI(NerithCore plugin, Player player, Island island) {
        this.plugin  = plugin;
        this.player  = player;
        this.island  = island;
    }

    public void open() {
        Inventory inv = Bukkit.createInventory(null, 27, TITLE);
        GuiUtil.border(inv);

        IslandStats stats = loadStats();
        long broken = island.getBlocksBroken();

        // 10 — Mobs killed
        inv.setItem(10, GuiUtil.item(Material.DIAMOND_SWORD,
                Component.text("Zabití Mobovia", NamedTextColor.RED, TextDecoration.BOLD),
                Component.text("Celkovo: ", NamedTextColor.GRAY)
                        .append(Component.text(stats != null ? String.valueOf(stats.getMobsKilled()) : "?", NamedTextColor.WHITE))
        ));

        // 12 — Bosses killed
        inv.setItem(12, GuiUtil.item(Material.WITHER_SKELETON_SKULL,
                Component.text("Zabití Bossovia", NamedTextColor.DARK_RED, TextDecoration.BOLD),
                Component.text("Celkovo: ", NamedTextColor.GRAY)
                        .append(Component.text(stats != null ? String.valueOf(stats.getBossesKilled()) : "?", NamedTextColor.WHITE))
        ));

        // 14 — Special events triggered
        inv.setItem(14, GuiUtil.item(Material.BLAZE_POWDER,
                Component.text("Špeciálne Udalosti", NamedTextColor.GOLD, TextDecoration.BOLD),
                Component.text("Spustené: ", NamedTextColor.GRAY)
                        .append(Component.text(stats != null ? String.valueOf(stats.getEventsTriggered()) : "?", NamedTextColor.WHITE))
        ));

        // 16 — Treasure found
        inv.setItem(16, GuiUtil.item(Material.GOLD_NUGGET,
                Component.text("Nájdené Poklady", NamedTextColor.YELLOW, TextDecoration.BOLD),
                Component.text("Celkovo: ", NamedTextColor.GRAY)
                        .append(Component.text(stats != null ? String.valueOf(stats.getTreasureFound()) : "?", NamedTextColor.WHITE))
        ));

        // 22 — Blocks broken (from Island object, always accurate)
        inv.setItem(22, GuiUtil.item(Material.DIAMOND_PICKAXE,
                Component.text("Rozbitých Blokov", NamedTextColor.AQUA, TextDecoration.BOLD),
                Component.text("Celkovo: ", NamedTextColor.GRAY)
                        .append(Component.text(String.valueOf(broken), NamedTextColor.WHITE))
        ));

        // Back
        inv.setItem(26, GuiUtil.item(Material.ARROW,
                Component.text("« Späť", NamedTextColor.GRAY, TextDecoration.BOLD)
        ));

        player.openInventory(inv);
        plugin.getGuiListener().register(player, this);
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        if (event.getRawSlot() == 26) {
            new MainIslandGUI(plugin, player).open();
        }
    }

    private IslandStats loadStats() {
        try {
            return plugin.getStatsRepository().find(island.getId()).orElse(null);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load island stats", e);
            return null;
        }
    }
}
