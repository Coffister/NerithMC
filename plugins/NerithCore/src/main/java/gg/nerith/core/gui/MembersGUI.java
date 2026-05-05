package gg.nerith.core.gui;

import gg.nerith.core.NerithCore;
import gg.nerith.core.island.Island;
import gg.nerith.core.island.IslandMember;
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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Members management screen — 54 slots.
 *
 * Inner area (rows 1-4, cols 1-7) = up to 28 member skulls.
 * Row 5: border + Back at slot 49.
 *
 * Owner actions:
 *   Left-click  → nothing (info only)
 *   Shift-click → kick member (owner cannot kick themselves)
 */
public class MembersGUI implements IslandGui {

    private static final Component TITLE = Component.text("✦ Ostrov  ", NamedTextColor.GOLD, TextDecoration.BOLD)
            .append(Component.text("| ", NamedTextColor.DARK_GRAY).decoration(TextDecoration.BOLD, false))
            .append(Component.text("Členovia", NamedTextColor.WHITE).decoration(TextDecoration.BOLD, false));

    // Inner slots: rows 1-4, cols 1-7 (skip border cols 0 and 8)
    private static final int[] MEMBER_SLOTS = {
            10,11,12,13,14,15,16,
            19,20,21,22,23,24,25,
            28,29,30,31,32,33,34,
            37,38,39,40,41,42,43
    };

    private final NerithCore plugin;
    private final Player player;
    private final Island island;
    private final boolean isOwner;

    /** slot → UUID of the member displayed there */
    private final Map<Integer, UUID> slotToMember = new HashMap<>();

    public MembersGUI(NerithCore plugin, Player player, Island island) {
        this.plugin   = plugin;
        this.player   = player;
        this.island   = island;
        this.isOwner  = island.getOwnerUuid().equals(player.getUniqueId());
    }

    public void open() {
        Inventory inv = Bukkit.createInventory(null, 54, TITLE);
        GuiUtil.border(inv);

        List<IslandMember> members = plugin.getIslandManager().getMembers(island.getId());
        slotToMember.clear();

        for (int i = 0; i < members.size() && i < MEMBER_SLOTS.length; i++) {
            IslandMember m  = members.get(i);
            int slot        = MEMBER_SLOTS[i];
            slotToMember.put(slot, m.getPlayerUuid());
            inv.setItem(slot, buildSkull(m));
        }

        // Back button
        inv.setItem(49, GuiUtil.item(Material.ARROW,
                Component.text("« Späť", NamedTextColor.GRAY, TextDecoration.BOLD)
        ));

        player.openInventory(inv);
        plugin.getGuiListener().register(player, this);
    }

    // ── Click handling ────────────────────────────────────────────────────────

    @Override
    public void handleClick(InventoryClickEvent event) {
        int slot = event.getRawSlot();

        if (slot == 49) {
            new MainIslandGUI(plugin, player).open();
            return;
        }

        UUID targetUuid = slotToMember.get(slot);
        if (targetUuid == null) return;

        if (event.isShiftClick() && isOwner) {
            if (targetUuid.equals(player.getUniqueId())) return; // can't kick self
            if (targetUuid.equals(island.getOwnerUuid())) return; // can't kick owner
            kickMember(targetUuid);
        }
    }

    private void kickMember(UUID targetUuid) {
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetUuid);
        try {
            plugin.getIslandManager().removeMember(island, targetUuid);
            Player online = Bukkit.getPlayer(targetUuid);
            if (online != null) plugin.getMessageManager().send(online, "island.kicked");
            // Refresh the screen
            new MembersGUI(plugin, player, island).open();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to kick member", e);
        }
    }

    // ── Skull builder ─────────────────────────────────────────────────────────

    private ItemStack buildSkull(IslandMember m) {
        OfflinePlayer op  = Bukkit.getOfflinePlayer(m.getPlayerUuid());
        String name       = op.getName() != null ? op.getName() : m.getPlayerUuid().toString().substring(0, 8);
        boolean online    = op.isOnline();
        boolean memberIsOwner = m.isOwner();

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text(memberIsOwner ? "👑 Vlastník" : "Člen",
                memberIsOwner ? NamedTextColor.GOLD : NamedTextColor.GRAY));
        lore.add(Component.text(online ? "● Online" : "○ Offline",
                online ? NamedTextColor.GREEN : NamedTextColor.DARK_GRAY));
        if (isOwner && !m.getPlayerUuid().equals(player.getUniqueId()) && !memberIsOwner) {
            lore.add(Component.empty());
            lore.add(Component.text("Shift+Klik → Vyhodiť", NamedTextColor.RED));
        }

        ItemStack skull = GuiUtil.skull(op);
        SkullMeta meta  = (SkullMeta) skull.getItemMeta();
        meta.displayName(Component.text(name,
                memberIsOwner ? NamedTextColor.GOLD : NamedTextColor.WHITE,
                TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
        meta.lore(lore.stream()
                .map(l -> l.decoration(TextDecoration.ITALIC, false))
                .toList());
        skull.setItemMeta(meta);
        return skull;
    }
}
