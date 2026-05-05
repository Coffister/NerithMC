package gg.nerith.core.gui;

import gg.nerith.core.NerithCore;
import gg.nerith.core.island.Island;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

/**
 * Reset confirmation dialog — 27 slots.
 *
 *   Row 0: border
 *   Row 1: border  [CONFIRM@11]  ...  [CANCEL@15]  border
 *   Row 2: border (all filler)
 */
public class ResetConfirmGUI implements IslandGui {

    private static final Component TITLE = Component.text("⚠ Reset ostrova?", NamedTextColor.RED, TextDecoration.BOLD);

    private final NerithCore plugin;
    private final Player player;
    private final Island island;

    public ResetConfirmGUI(NerithCore plugin, Player player, Island island) {
        this.plugin  = plugin;
        this.player  = player;
        this.island  = island;
    }

    public void open() {
        Inventory inv = Bukkit.createInventory(null, 27, TITLE);
        GuiUtil.border(inv);

        int penalty = plugin.getConfigManager().getResetPenalty(island.getPhase());

        // Slot 11 — Confirm (red, scary)
        inv.setItem(11, GuiUtil.item(Material.RED_WOOL,
                Component.text("✖ Potvrdiť Reset", NamedTextColor.RED, TextDecoration.BOLD),
                Component.text("Fáza: " + island.getPhase(), NamedTextColor.GRAY),
                Component.text("Bloky: " + island.getBlocksBroken(), NamedTextColor.GRAY),
                Component.empty(),
                Component.text("⚠ Penalizácia: " + penalty + "% fragmentov", NamedTextColor.DARK_RED),
                Component.empty(),
                Component.text("Klikni pre potvrdenie!", NamedTextColor.RED)
        ));

        // Slot 15 — Cancel (green, safe)
        inv.setItem(15, GuiUtil.item(Material.GREEN_WOOL,
                Component.text("✔ Zrušiť", NamedTextColor.GREEN, TextDecoration.BOLD),
                Component.text("Vráť sa späť bez zmeny", NamedTextColor.GRAY)
        ));

        player.openInventory(inv);
        plugin.getGuiListener().register(player, this);
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        switch (event.getRawSlot()) {
            case 11 -> {
                player.closeInventory();
                plugin.getIslandResetter().resetIsland(player, island);
            }
            case 15 -> new MainIslandGUI(plugin, player).open();
        }
    }
}
