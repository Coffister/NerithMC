package gg.nerith.core.gui;

import gg.nerith.core.NerithCore;
import gg.nerith.core.island.Island;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Leaderboard — top 10 islands, 54 slots.
 *
 * Podium layout:
 *   Slot 13  → #1 (gold block)
 *   Slot 20  → #2 (iron block)
 *   Slot 24  → #3 (copper block)
 *   Slots 28-34 (inner row 3) → #4-#7
 *   Slots 37-43 (inner row 4) → #8-#10
 *   Slot 49  → Back
 */
public class LeaderboardGUI implements IslandGui {

    private static final Component TITLE = Component.text("✦ Ostrov  ", NamedTextColor.GOLD, TextDecoration.BOLD)
            .append(Component.text("| ", NamedTextColor.DARK_GRAY).decoration(TextDecoration.BOLD, false))
            .append(Component.text("Rebríček", NamedTextColor.WHITE).decoration(TextDecoration.BOLD, false));

    private static final int[] SLOTS = {
            13,                     // #1
            20, 24,                 // #2, #3
            28, 29, 30, 31, 32, 33, 34, // #4-#7 (only 4 used)
            37, 38, 39              // #8-#10
    };

    private static final Material[] RANK_MATERIALS = {
            Material.GOLD_BLOCK,    // #1
            Material.IRON_BLOCK,    // #2
            Material.COPPER_BLOCK,  // #3
            Material.GOLD_ORE,      // #4
            Material.GOLD_ORE,      // #5
            Material.IRON_ORE,      // #6
            Material.IRON_ORE,      // #7
            Material.COAL_ORE,      // #8
            Material.COAL_ORE,      // #9
            Material.COAL_ORE       // #10
    };

    private static final String[] RANK_PREFIXES = {
            "§6§l🥇 #1", "§7§l🥈 #2", "§c§l🥉 #3",
            "§e#4", "§e#5", "§e#6", "§e#7",
            "§f#8", "§f#9", "§f#10"
    };

    private final NerithCore plugin;
    private final Player player;

    public LeaderboardGUI(NerithCore plugin, Player player) {
        this.plugin  = plugin;
        this.player  = player;
    }

    public void open() {
        Inventory inv = Bukkit.createInventory(null, 54, TITLE);
        GuiUtil.border(inv);

        List<Island> top = plugin.getLeaderboardManager().getTopIslands(10);

        for (int i = 0; i < Math.min(top.size(), SLOTS.length) && i < 10; i++) {
            Island island  = top.get(i);
            String phaseName = plugin.getConfigManager().getPhaseConfig(island.getPhase()).getName();
            OfflinePlayer owner = Bukkit.getOfflinePlayer(island.getOwnerUuid());
            String ownerName = owner.getName() != null ? owner.getName()
                    : island.getOwnerUuid().toString().substring(0, 8) + "…";

            inv.setItem(SLOTS[i], buildEntry(i, island, ownerName, phaseName));
        }

        // Back button
        inv.setItem(49, GuiUtil.item(Material.ARROW,
                Component.text("« Späť", NamedTextColor.GRAY, TextDecoration.BOLD)
        ));

        player.openInventory(inv);
        plugin.getGuiListener().register(player, this);
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        if (event.getRawSlot() == 49) {
            new MainIslandGUI(plugin, player).open();
        }
    }

    // ── Entry builder ─────────────────────────────────────────────────────────

    private ItemStack buildEntry(int rank, Island island, String ownerName, String phaseName) {
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Vlastník: ", NamedTextColor.GRAY)
                .append(Component.text(ownerName, NamedTextColor.WHITE)));
        lore.add(Component.text("Fáza: ", NamedTextColor.GRAY)
                .append(Component.text(island.getPhase() + "  " + phaseName, NamedTextColor.YELLOW)));
        lore.add(Component.text("Bloky: ", NamedTextColor.GRAY)
                .append(Component.text(String.valueOf(island.getBlocksBroken()), NamedTextColor.WHITE)));
        lore.add(Component.text("Typ: ", NamedTextColor.GRAY)
                .append(Component.text(island.getType().name(), NamedTextColor.AQUA)));

        Component name = Component.text(RANK_PREFIXES[rank] + "  " + ownerName)
                .decoration(TextDecoration.ITALIC, false);

        return GuiUtil.item(RANK_MATERIALS[rank], name, lore);
    }
}
