package gg.nerith.core.gui;

import gg.nerith.core.NerithCore;
import gg.nerith.core.island.Island;
import gg.nerith.core.void_death.PowerupManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.Set;

/**
 * Active powerup display — 27 slots (read-only).
 *
 *   Row 0: border
 *   Row 1: border  [VOID_SHIELD@11]  ...  [FRAGMENT_SAVER@15]  border
 *   Row 2: border  ...  [BACK@22]  ...  border
 */
public class IslandPerksGUI implements IslandGui {

    private static final Component TITLE = Component.text("✦ Ostrov  ", NamedTextColor.GOLD, TextDecoration.BOLD)
            .append(Component.text("| ", NamedTextColor.DARK_GRAY).decoration(TextDecoration.BOLD, false))
            .append(Component.text("Vylepšenia", NamedTextColor.WHITE).decoration(TextDecoration.BOLD, false));

    private final NerithCore plugin;
    private final Player player;
    private final Island island;

    public IslandPerksGUI(NerithCore plugin, Player player, Island island) {
        this.plugin  = plugin;
        this.player  = player;
        this.island  = island;
    }

    public void open() {
        Inventory inv = Bukkit.createInventory(null, 27, TITLE);
        GuiUtil.border(inv);

        Set<PowerupManager.PowerupType> powerups = plugin.getPowerupManager().getActivePowerups(island);
        boolean hasShield = powerups.contains(PowerupManager.PowerupType.VOID_SHIELD);
        boolean hasSaver  = powerups.contains(PowerupManager.PowerupType.FRAGMENT_SAVER);

        // 11 — Void Shield
        inv.setItem(11, GuiUtil.item(
                hasShield ? Material.SHIELD : Material.GRAY_DYE,
                Component.text("Void Shield", hasShield ? NamedTextColor.GREEN : NamedTextColor.DARK_GRAY, TextDecoration.BOLD),
                Component.text(hasShield ? "✔ Aktívny" : "✖ Nevlastníš", hasShield ? NamedTextColor.GREEN : NamedTextColor.RED),
                Component.empty(),
                Component.text("Katapultuje ťa späť na ostrov", NamedTextColor.GRAY),
                Component.text("keď spadneš do voidu.", NamedTextColor.GRAY),
                Component.empty(),
                Component.text("Získaš od NPC questov.", NamedTextColor.DARK_GRAY)
        ));

        // 15 — Fragment Saver
        inv.setItem(15, GuiUtil.item(
                hasSaver ? Material.ENDER_CHEST : Material.GRAY_DYE,
                Component.text("Fragment Saver", hasSaver ? NamedTextColor.GREEN : NamedTextColor.DARK_GRAY, TextDecoration.BOLD),
                Component.text(hasSaver ? "✔ Aktívny" : "✖ Nevlastníš", hasSaver ? NamedTextColor.GREEN : NamedTextColor.RED),
                Component.empty(),
                Component.text("Zachráni 70% predmetov pri", NamedTextColor.GRAY),
                Component.text("páde do voidu. Jednorazové.", NamedTextColor.GRAY),
                Component.empty(),
                Component.text("Získaš od NPC questov.", NamedTextColor.DARK_GRAY)
        ));

        // 22 — Back
        inv.setItem(22, GuiUtil.item(Material.ARROW,
                Component.text("« Späť", NamedTextColor.GRAY, TextDecoration.BOLD)
        ));

        player.openInventory(inv);
        plugin.getGuiListener().register(player, this);
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        if (event.getRawSlot() == 22) {
            new MainIslandGUI(plugin, player).open();
        }
    }
}
