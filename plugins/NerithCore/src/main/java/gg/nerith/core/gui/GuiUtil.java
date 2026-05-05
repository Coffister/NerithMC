package gg.nerith.core.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class GuiUtil {

    private GuiUtil() {}

    /** Creates an ItemStack with a non-italic display name and optional lore lines. */
    public static ItemStack item(Material mat, Component name, Component... lore) {
        ItemStack stack = new ItemStack(mat);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(name.decoration(TextDecoration.ITALIC, false));
        if (lore.length > 0) {
            meta.lore(Arrays.stream(lore)
                    .map(l -> l.decoration(TextDecoration.ITALIC, false))
                    .collect(Collectors.toList()));
        }
        stack.setItemMeta(meta);
        return stack;
    }

    /** Creates an ItemStack with a list of lore lines. */
    public static ItemStack item(Material mat, Component name, List<Component> lore) {
        return item(mat, name, lore.toArray(new Component[0]));
    }

    /** Invisible placeholder pane. */
    public static ItemStack filler() {
        ItemStack stack = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(Component.empty());
        stack.setItemMeta(meta);
        return stack;
    }

    /**
     * Fills the outermost ring of an inventory (top, bottom, left, right)
     * with gray glass panes.
     */
    public static void border(Inventory inv) {
        int size = inv.getSize();
        int rows = size / 9;
        ItemStack pane = filler();
        for (int i = 0; i < 9; i++)           inv.setItem(i, pane);
        for (int i = size - 9; i < size; i++) inv.setItem(i, pane);
        for (int r = 1; r < rows - 1; r++) {
            inv.setItem(r * 9, pane);
            inv.setItem(r * 9 + 8, pane);
        }
    }

    /** Creates a player-skull ItemStack tied to the given offline player. */
    public static ItemStack skull(OfflinePlayer player) {
        ItemStack stack = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) stack.getItemMeta();
        meta.setOwningPlayer(player);
        stack.setItemMeta(meta);
        return stack;
    }

    /** Builds a 10-char progress bar string, e.g. "§a████░░░░░░". */
    public static String progressBar(double fraction) {
        int filled = (int) Math.round(Math.min(1.0, Math.max(0.0, fraction)) * 10);
        return "§a" + "█".repeat(filled) + "§8" + "█".repeat(10 - filled);
    }
}
