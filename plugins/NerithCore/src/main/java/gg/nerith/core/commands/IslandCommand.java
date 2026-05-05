package gg.nerith.core.commands;

import gg.nerith.core.NerithCore;
import gg.nerith.core.gui.*;
import gg.nerith.core.island.Island;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

public class IslandCommand implements CommandExecutor, TabCompleter {

    private final NerithCore plugin;

    public IslandCommand(NerithCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command is player-only.");
            return true;
        }

        if (args.length == 0) {
            new MainIslandGUI(plugin, player).open();
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create" -> handleCreate(player, args);
            case "home" -> handleHome(player);
            case "invite" -> handleInvite(player, args);
            case "join" -> handleJoin(player, args);
            case "accept" -> plugin.getCoopManager().acceptInvite(player);
            case "deny" -> plugin.getCoopManager().denyInviteOrRequest(player);
            case "kick" -> handleKick(player, args);
            case "leave" -> plugin.getCoopManager().leaveIsland(player);
            case "transfer" -> handleTransfer(player, args);
            case "reset" -> handleReset(player);
            case "info" -> handleInfo(player, args);
            case "top" -> new LeaderboardGUI(plugin, player).open();
            case "setspawn" -> handleSetSpawn(player);
            default -> plugin.getMessageManager().send(player, "command.unknown");
        }
        return true;
    }

    private void handleCreate(Player player, String[] args) {
        if (plugin.getIslandManager().getIslandByPlayer(player.getUniqueId()).isPresent()) {
            plugin.getMessageManager().send(player, "island.already-has-island");
            return;
        }
        if (!player.hasPermission("nerith.island.create")) {
            plugin.getMessageManager().send(player, "no-permission");
            return;
        }
        Island.Type type = (args.length > 1 && args[1].equalsIgnoreCase("coop"))
                ? Island.Type.COOP : Island.Type.SOLO;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                plugin.getIslandManager().createIsland(player, type);
                Bukkit.getScheduler().runTask(plugin, () ->
                        plugin.getMessageManager().send(player, "island.created"));
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to create island", e);
                Bukkit.getScheduler().runTask(plugin, () ->
                        plugin.getMessageManager().send(player, "island.create-error"));
            }
        });
    }

    private void handleHome(Player player) {
        plugin.getIslandManager().getIslandByPlayer(player.getUniqueId()).ifPresentOrElse(
                island -> plugin.getIslandManager().teleportToIsland(player, island),
                () -> plugin.getMessageManager().send(player, "island.no-island")
        );
    }

    private void handleInvite(Player player, String[] args) {
        if (args.length < 2) { plugin.getMessageManager().send(player, "command.usage.invite"); return; }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) { plugin.getMessageManager().send(player, "player.not-found"); return; }
        plugin.getCoopManager().sendInvite(player, target);
    }

    private void handleJoin(Player player, String[] args) {
        if (args.length < 2) { plugin.getMessageManager().send(player, "command.usage.join"); return; }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) { plugin.getMessageManager().send(player, "player.not-found"); return; }
        plugin.getCoopManager().sendRequest(player, target);
    }

    private void handleKick(Player player, String[] args) {
        if (args.length < 2) { plugin.getMessageManager().send(player, "command.usage.kick"); return; }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) { plugin.getMessageManager().send(player, "player.not-found"); return; }
        plugin.getCoopManager().kickMember(player, target);
    }

    private void handleTransfer(Player player, String[] args) {
        if (args.length < 2) { plugin.getMessageManager().send(player, "command.usage.transfer"); return; }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) { plugin.getMessageManager().send(player, "player.not-found"); return; }
        plugin.getCoopManager().transferOwnership(player, target);
    }

    private void handleReset(Player player) {
        if (!player.hasPermission("nerith.island.reset")) {
            plugin.getMessageManager().send(player, "no-permission");
            return;
        }
        plugin.getIslandManager().getIslandByPlayer(player.getUniqueId()).ifPresentOrElse(
                island -> new ResetConfirmGUI(plugin, player, island).open(),
                () -> plugin.getMessageManager().send(player, "island.no-island")
        );
    }

    private void handleInfo(Player player, String[] args) {
        Optional<Island> islandOpt = plugin.getIslandManager().getIslandByPlayer(player.getUniqueId());
        islandOpt.ifPresentOrElse(island -> {
            plugin.getConfigManager().getPhaseConfig(island.getPhase());
            player.sendMessage(Component.text("=== Island Info ===", NamedTextColor.GOLD));
            player.sendMessage(Component.text("Phase: " + island.getPhase() + " - " +
                    plugin.getConfigManager().getPhaseConfig(island.getPhase()).getName(), NamedTextColor.YELLOW));
            player.sendMessage(Component.text("Blocks broken: " + island.getBlocksBroken(), NamedTextColor.YELLOW));
            player.sendMessage(Component.text("Type: " + island.getType().name(), NamedTextColor.YELLOW));
            player.sendMessage(Component.text("Members: " + plugin.getIslandManager().getMemberCount(island), NamedTextColor.YELLOW));
        }, () -> plugin.getMessageManager().send(player, "island.no-island"));
    }

    private void handleSetSpawn(Player player) {
        plugin.getIslandManager().getIslandByPlayer(player.getUniqueId()).ifPresentOrElse(island -> {
            if (!island.getOwnerUuid().equals(player.getUniqueId())) {
                plugin.getMessageManager().send(player, "island.not-owner");
                return;
            }
            try {
                plugin.getIslandManager().setSpawn(island, player.getLocation());
                plugin.getMessageManager().send(player, "island.spawn-set");
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "Failed to set spawn", e);
            }
        }, () -> plugin.getMessageManager().send(player, "island.no-island"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("create", "home", "invite", "join", "accept", "deny", "kick",
                    "leave", "transfer", "reset", "info", "top", "setspawn");
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("create")) {
            return List.of("coop");
        }
        return List.of();
    }
}
