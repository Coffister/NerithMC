package gg.nerith.core.oneblock;

import gg.nerith.core.NerithCore;
import gg.nerith.core.api.events.NerithSpecialBlockEvent;
import gg.nerith.core.island.Island;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;

public class SpecialEventManager {

    private final NerithCore plugin;

    public SpecialEventManager(NerithCore plugin) {
        this.plugin = plugin;
    }

    public void trigger(Island island, Player player, String specialType) {
        NerithSpecialBlockEvent.SpecialType type;
        try {
            type = NerithSpecialBlockEvent.SpecialType.valueOf(specialType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return;
        }

        NerithSpecialBlockEvent event = new NerithSpecialBlockEvent(island, player, type);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        switch (type) {
            case TREASURE_BLOCK -> handleTreasure(island, player);
            case BOSS_SPAWN -> handleBossSpawn(island, player);
            case LORE_BLOCK -> handleLore(island, player);
        }

        trackEvent(island);
    }

    private void handleTreasure(Island island, Player player) {
        player.getWorld().dropItemNaturally(player.getLocation(), new ItemStack(Material.DIAMOND, 1));
        player.getWorld().dropItemNaturally(player.getLocation(), new ItemStack(Material.GOLD_INGOT, 3));
        plugin.getMessageManager().sendToIsland(island, "special.treasure");
        try {
            plugin.getStatsRepository().incrementTreasureFound(island.getId());
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to increment treasure stat", e);
        }
    }

    private void handleBossSpawn(Island island, Player player) {
        Location loc = player.getLocation().add(2, 0, 2);
        player.getWorld().spawnEntity(loc, EntityType.ZOMBIE);
        plugin.getMessageManager().sendToIsland(island, "special.boss-spawn");
        try {
            plugin.getStatsRepository().incrementEventsTriggered(island.getId());
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to increment event stat", e);
        }
    }

    private void handleLore(Island island, Player player) {
        String lore = plugin.getMessageManager().getRaw("special.lore-block");
        plugin.getIslandManager().getMembers(island.getId()).stream()
                .map(m -> Bukkit.getPlayer(m.getPlayerUuid()))
                .filter(p -> p != null && p.isOnline())
                .forEach(p -> p.sendMessage(Component.text(lore, NamedTextColor.LIGHT_PURPLE)));
        player.getWorld().playSound(player.getLocation(), org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 0.8f);
    }

    private void trackEvent(Island island) {
        try {
            plugin.getStatsRepository().incrementEventsTriggered(island.getId());
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to track special event", e);
        }
    }
}
