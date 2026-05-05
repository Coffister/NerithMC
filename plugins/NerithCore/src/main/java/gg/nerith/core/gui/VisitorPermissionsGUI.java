package gg.nerith.core.gui;

import gg.nerith.core.NerithCore;
import gg.nerith.core.island.Island;
import gg.nerith.core.island.IslandPermissions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;
import java.util.logging.Level;

/**
 * Visitor permission toggles — 54 slots (owner only).
 *
 * Layout:
 *   Row 0: border
 *   Row 1: border  [BREAK@11]  [PLACE@13]  [INTERACT@15]  border
 *   Row 2: border  (filler)                               border
 *   Row 3: border  [CHEST@29]  [MOBS@31]                  border
 *   Row 4: border  (filler)                               border
 *   Row 5: border  [BACK@49]   border
 */
public class VisitorPermissionsGUI implements IslandGui {

    private static final Component TITLE = Component.text("✦ Ostrov  ", NamedTextColor.GOLD, TextDecoration.BOLD)
            .append(Component.text("| ", NamedTextColor.DARK_GRAY).decoration(TextDecoration.BOLD, false))
            .append(Component.text("Nastavenia", NamedTextColor.WHITE).decoration(TextDecoration.BOLD, false));

    private final NerithCore plugin;
    private final Player player;
    private final Island island;

    // Mutable inventory reference — updated on every toggle
    private Inventory inv;

    public VisitorPermissionsGUI(NerithCore plugin, Player player, Island island) {
        this.plugin  = plugin;
        this.player  = player;
        this.island  = island;
    }

    public void open() {
        inv = Bukkit.createInventory(null, 54, TITLE);
        render(inv);
        player.openInventory(inv);
        plugin.getGuiListener().register(player, this);
    }

    private void render(Inventory target) {
        GuiUtil.border(target);
        IslandPermissions perms = plugin.getIslandManager().getPermissions(island.getId());

        target.setItem(11, toggle("Lámanie blokov",  "Návštevníci môžu lámať bloky",    perms.isAllowBreak()));
        target.setItem(13, toggle("Kladenie blokov", "Návštevníci môžu klásť bloky",    perms.isAllowPlace()));
        target.setItem(15, toggle("Interakcia",      "Návštevníci môžu interagovať",    perms.isAllowInteract()));
        target.setItem(29, toggle("Otvorenie truhiel","Návštevníci môžu otvárať truhly", perms.isAllowChest()));
        target.setItem(31, toggle("Zabíjanie mobov", "Návštevníci môžu zabíjať mobloch", perms.isAllowKillMobs()));

        target.setItem(49, GuiUtil.item(Material.ARROW,
                Component.text("« Späť", NamedTextColor.GRAY, TextDecoration.BOLD)
        ));
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        if (!island.getOwnerUuid().equals(player.getUniqueId())) {
            plugin.getMessageManager().send(player, "island.not-owner");
            return;
        }

        IslandPermissions perms = plugin.getIslandManager().getPermissions(island.getId());
        boolean changed = true;

        switch (event.getRawSlot()) {
            case 11 -> perms.setAllowBreak(!perms.isAllowBreak());
            case 13 -> perms.setAllowPlace(!perms.isAllowPlace());
            case 15 -> perms.setAllowInteract(!perms.isAllowInteract());
            case 29 -> perms.setAllowChest(!perms.isAllowChest());
            case 31 -> perms.setAllowKillMobs(!perms.isAllowKillMobs());
            case 49 -> { new MainIslandGUI(plugin, player).open(); return; }
            default -> changed = false;
        }

        if (changed) {
            try {
                plugin.getIslandManager().savePermissions(perms);
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "Failed to save permissions", e);
            }
            // Re-render in-place (same open inventory, no close/re-open)
            render(player.getOpenInventory().getTopInventory());
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static ItemStack toggle(String name, String description, boolean enabled) {
        return GuiUtil.item(
                enabled ? Material.LIME_DYE : Material.RED_DYE,
                Component.text(name, enabled ? NamedTextColor.GREEN : NamedTextColor.RED, TextDecoration.BOLD),
                Component.text(description, NamedTextColor.GRAY),
                Component.empty(),
                Component.text(enabled ? "✔ Povolené  –  Klikni na zakázanie"
                                       : "✖ Zakázané  –  Klikni na povolenie",
                        enabled ? NamedTextColor.GREEN : NamedTextColor.RED)
        );
    }
}
