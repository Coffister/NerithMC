package gg.nerith.core.config;

import gg.nerith.core.NerithCore;
import gg.nerith.core.island.Island;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Map;

public class MessageManager {

    private final NerithCore plugin;
    private YamlConfiguration messages;
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();

    public MessageManager(NerithCore plugin) {
        this.plugin = plugin;
    }

    public void load() {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) plugin.saveResource("messages.yml", false);
        messages = YamlConfiguration.loadConfiguration(file);
    }

    public void send(CommandSender sender, String key) {
        send(sender, key, Map.of());
    }

    public void send(CommandSender sender, String key, Map<String, String> placeholders) {
        String raw = getRaw(key);
        for (var entry : placeholders.entrySet()) {
            raw = raw.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        sender.sendMessage(LEGACY.deserialize(raw));
    }

    public void sendToIsland(Island island, String key) {
        plugin.getIslandManager().getMembers(island.getId()).stream()
                .map(m -> Bukkit.getPlayer(m.getPlayerUuid()))
                .filter(p -> p != null && p.isOnline())
                .forEach(p -> send(p, key));
    }

    public String getRaw(String key) {
        String val = messages.getString(key);
        if (val == null) {
            plugin.getLogger().warning("Missing message key: " + key);
            return "&c[Missing: " + key + "]";
        }
        return val;
    }

    public Component get(String key) {
        return LEGACY.deserialize(getRaw(key));
    }
}
