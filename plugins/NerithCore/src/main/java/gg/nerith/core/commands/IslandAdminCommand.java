package gg.nerith.core.commands;

import gg.nerith.core.NerithCore;
import gg.nerith.core.island.Island;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

public class IslandAdminCommand implements CommandExecutor, TabCompleter {

    private final NerithCore plugin;

    public IslandAdminCommand(NerithCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("nerith.admin")) {
            sender.sendMessage("No permission.");
            return true;
        }
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "goto" -> handleGoto(sender, args);
            case "reset" -> handleReset(sender, args);
            case "setphase" -> handleSetPhase(sender, args);
            case "info" -> handleInfo(sender, args);
            case "reload" -> handleReload(sender);
            case "leaderboard" -> handleLeaderboard(sender, args);
            default -> sendHelp(sender);
        }
        return true;
    }

    private void handleGoto(CommandSender sender, String[] args) {
        if (args.length < 2) { sender.sendMessage("Usage: /isa goto <player>"); return; }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) { sender.sendMessage("Player not found."); return; }
        plugin.getIslandManager().getIslandByPlayer(target.getUniqueId()).ifPresentOrElse(island -> {
            if (sender instanceof Player admin) {
                plugin.getIslandManager().teleportToIsland(admin, island);
                admin.sendMessage("Teleported to " + target.getName() + "'s island.");
            }
        }, () -> sender.sendMessage(target.getName() + " has no island."));
    }

    private void handleReset(CommandSender sender, String[] args) {
        if (args.length < 2) { sender.sendMessage("Usage: /isa reset <player>"); return; }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) { sender.sendMessage("Player not found."); return; }
        plugin.getIslandManager().getIslandByPlayer(target.getUniqueId()).ifPresentOrElse(island -> {
            plugin.getIslandResetter().resetIsland(target, island);
            sender.sendMessage("Force reset island of " + target.getName() + ".");
        }, () -> sender.sendMessage(target.getName() + " has no island."));
    }

    private void handleSetPhase(CommandSender sender, String[] args) {
        if (args.length < 3) { sender.sendMessage("Usage: /isa setphase <player> <phase>"); return; }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) { sender.sendMessage("Player not found."); return; }
        int phase;
        try { phase = Integer.parseInt(args[2]); } catch (NumberFormatException e) {
            sender.sendMessage("Invalid phase number."); return;
        }
        plugin.getIslandManager().getIslandByPlayer(target.getUniqueId()).ifPresentOrElse(island -> {
            try {
                plugin.getIslandManager().setPhase(island, phase);
                sender.sendMessage("Set " + target.getName() + "'s island to phase " + phase + ".");
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "Failed to set phase", e);
                sender.sendMessage("Database error.");
            }
        }, () -> sender.sendMessage(target.getName() + " has no island."));
    }

    private void handleInfo(CommandSender sender, String[] args) {
        if (args.length < 2) { sender.sendMessage("Usage: /isa info <player>"); return; }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) { sender.sendMessage("Player not found."); return; }
        plugin.getIslandManager().getIslandByPlayer(target.getUniqueId()).ifPresentOrElse(island -> {
            sender.sendMessage("Island ID: " + island.getId());
            sender.sendMessage("Owner: " + island.getOwnerUuid());
            sender.sendMessage("Phase: " + island.getPhase());
            sender.sendMessage("Blocks: " + island.getBlocksBroken());
            sender.sendMessage("Type: " + island.getType().name());
            sender.sendMessage("World: " + island.getWorld());
            sender.sendMessage("Spawn: " + island.getSpawnX() + ", " + island.getSpawnY() + ", " + island.getSpawnZ());
        }, () -> sender.sendMessage(target.getName() + " has no island."));
    }

    private void handleReload(CommandSender sender) {
        plugin.getConfigManager().load();
        plugin.getBlockPoolManager().load();
        plugin.getMessageManager().load();
        sender.sendMessage("[NerithCore] Config reloaded.");
    }

    private void handleLeaderboard(CommandSender sender, String[] args) {
        if (args.length < 3 || !args[1].equalsIgnoreCase("settle")) {
            sender.sendMessage("Usage: /isa leaderboard settle <weekly|monthly>");
            return;
        }
        switch (args[2].toLowerCase()) {
            case "weekly" -> {
                plugin.getLeaderboardManager().settleWeekly();
                sender.sendMessage("Weekly leaderboard settled.");
            }
            case "monthly" -> {
                plugin.getLeaderboardManager().settleMonthly();
                sender.sendMessage("Monthly leaderboard settled.");
            }
            default -> sender.sendMessage("Unknown period. Use weekly or monthly.");
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("/isa goto <player> | reset <player> | setphase <player> <n> | info <player> | reload | leaderboard settle <weekly|monthly>");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (!sender.hasPermission("nerith.admin")) return List.of();
        if (args.length == 1) return List.of("goto", "reset", "setphase", "info", "reload", "leaderboard");
        if (args.length == 2 && args[0].equalsIgnoreCase("leaderboard")) return List.of("settle");
        if (args.length == 3 && args[0].equalsIgnoreCase("leaderboard")) return List.of("weekly", "monthly");
        return List.of();
    }
}
