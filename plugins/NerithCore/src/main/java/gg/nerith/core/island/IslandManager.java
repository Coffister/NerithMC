package gg.nerith.core.island;

import gg.nerith.core.NerithCore;
import gg.nerith.core.api.events.NerithIslandCreateEvent;
import gg.nerith.core.config.PhaseConfig;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class IslandManager {

    private final NerithCore plugin;
    private final Map<UUID, Island> islandByOwner = new ConcurrentHashMap<>();
    private final Map<UUID, Island> islandByMember = new ConcurrentHashMap<>();
    private final Map<UUID, Island> islandById = new ConcurrentHashMap<>();
    private final Map<UUID, List<IslandMember>> memberCache = new ConcurrentHashMap<>();
    private final Map<UUID, IslandPermissions> permCache = new ConcurrentHashMap<>();
    private final Set<UUID> starterKitGiven = ConcurrentHashMap.newKeySet();

    private long nextIslandIndex = 1;

    public void loadNextIslandIndex() throws SQLException {
        nextIslandIndex = plugin.getIslandRepository().countTotal() + 1;
        plugin.getLogger().info("[NerithCore] Next island index: " + nextIslandIndex);
    }

    public void repairAndRegisterOneBlocks() {
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            World obWorld = plugin.getServer().getWorld("oneblock_world");
            if (obWorld == null) {
                plugin.getLogger().severe("[NerithCore] oneblock_world not available for OneBlock repair!");
                return;
            }
            try {
                List<Island> all = plugin.getIslandRepository().findAll();
                int repaired = 0;
                for (Island island : all) {
                    if (!island.getWorld().equals("oneblock_world")) continue;

                    cacheIsland(island);

                    // Spawn is where the player stands; OneBlock is one below
                    int bx = (int) island.getSpawnX();
                    int by = (int) island.getSpawnY() - 1;
                    int bz = (int) island.getSpawnZ();

                    // Ensure spawn Y is at least 65 (block at 64, player at 65)
                    if (by < 63) {
                        by = 64;
                        island.setSpawn(bx, 65, bz);
                        plugin.getIslandRepository().updateSpawn(island.getId(), bx, 65, bz);
                    }

                    org.bukkit.Material savedMaterial;
                    try {
                        savedMaterial = org.bukkit.Material.valueOf(island.getCurrentBlock());
                    } catch (Exception e) {
                        savedMaterial = org.bukkit.Material.STONE;
                    }

                    org.bukkit.block.Block block = obWorld.getBlockAt(bx, by, bz);
                    if (block.getType() != savedMaterial) {
                        block.setType(savedMaterial, false);
                        repaired++;
                    }

                    plugin.getOneBlockListener().registerOneBlock(
                            island.getId(),
                            new org.bukkit.Location(obWorld, bx, by, bz)
                    );

                    // Load members into cache
                    List<IslandMember> members = plugin.getMemberRepository().findByIsland(island.getId());
                    memberCache.put(island.getId(), new java.util.ArrayList<>(members));
                    for (IslandMember m : members) {
                        if (m.isOwner()) {
                            islandByOwner.put(m.getPlayerUuid(), island);
                        } else {
                            islandByMember.put(m.getPlayerUuid(), island);
                        }
                    }
                }
                plugin.getLogger().info("[NerithCore] Registered " + all.size() + " island(s), repaired " + repaired + " missing OneBlock(s).");
            } catch (SQLException e) {
                plugin.getLogger().log(java.util.logging.Level.SEVERE, "[NerithCore] Failed to repair OneBlocks", e);
            }
        });
    }

    public IslandManager(NerithCore plugin) {
        this.plugin = plugin;
    }

    public Island createIsland(Player owner, Island.Type type) throws SQLException {
        UUID id = UUID.randomUUID();
        int[] coords = nextIslandCoords();
        double cx = coords[0];
        double cy = 64;
        double cz = coords[1];

        Island island = new Island(id, owner.getUniqueId(), type, 1, 0, cx, cy, cz, "oneblock_world", "STONE");
        plugin.getIslandRepository().save(island);
        plugin.getMemberRepository().save(id, owner.getUniqueId(), IslandMember.Role.OWNER);
        plugin.getStatsRepository().init(id);
        initPermissions(id);

        cacheIsland(island);
        memberCache.put(id, new ArrayList<>(List.of(
                new IslandMember(id, owner.getUniqueId(), IslandMember.Role.OWNER)
        )));

        NerithIslandCreateEvent event = new NerithIslandCreateEvent(island, owner);
        Bukkit.getPluginManager().callEvent(event);

        plugin.getIslandCreator().createIslandWorld(island, owner);

        if (!starterKitGiven.contains(owner.getUniqueId()) && plugin.getConfigManager().isStarterKitEnabled()) {
            giveStarterKit(owner);
            starterKitGiven.add(owner.getUniqueId());
        }

        return island;
    }

    private void initPermissions(UUID islandId) throws SQLException {
        String sql = "INSERT IGNORE INTO island_permissions (island_id) VALUES (?)";
        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, islandId.toString());
            ps.executeUpdate();
        }
        permCache.put(islandId, new IslandPermissions(islandId));
    }

    private void giveStarterKit(Player player) {
        for (ItemStack item : plugin.getConfigManager().getStarterKitItems()) {
            player.getInventory().addItem(item);
        }
        plugin.getMessageManager().send(player, "kit.given");
    }

    public Optional<Island> getIslandByPlayer(UUID playerUuid) {
        Island island = islandByOwner.get(playerUuid);
        if (island != null) return Optional.of(island);
        return Optional.ofNullable(islandByMember.get(playerUuid));
    }

    public Optional<Island> getIslandById(UUID islandId) {
        return Optional.ofNullable(islandById.get(islandId));
    }

    public Optional<Island> loadIslandByPlayer(UUID playerUuid) throws SQLException {
        Optional<Island> opt = plugin.getIslandRepository().findByOwner(playerUuid);
        if (opt.isEmpty()) {
            opt = plugin.getIslandRepository().findByMember(playerUuid);
        }
        opt.ifPresent(this::cacheIsland);
        return opt;
    }

    public List<IslandMember> getMembers(UUID islandId) {
        return memberCache.getOrDefault(islandId, List.of());
    }

    public void addMember(Island island, UUID playerUuid) throws SQLException {
        plugin.getMemberRepository().save(island.getId(), playerUuid, IslandMember.Role.MEMBER);
        IslandMember member = new IslandMember(island.getId(), playerUuid, IslandMember.Role.MEMBER);
        memberCache.computeIfAbsent(island.getId(), k -> new ArrayList<>()).add(member);
        islandByMember.put(playerUuid, island);
    }

    public void removeMember(Island island, UUID playerUuid) throws SQLException {
        plugin.getMemberRepository().remove(island.getId(), playerUuid);
        memberCache.getOrDefault(island.getId(), List.of()).removeIf(m -> m.getPlayerUuid().equals(playerUuid));
        islandByMember.remove(playerUuid);
    }

    public void transferOwnership(Island island, UUID newOwner) throws SQLException {
        UUID oldOwner = island.getOwnerUuid();
        plugin.getIslandRepository().updateOwner(island.getId(), newOwner);
        plugin.getMemberRepository().updateRole(island.getId(), oldOwner, IslandMember.Role.MEMBER);
        plugin.getMemberRepository().updateRole(island.getId(), newOwner, IslandMember.Role.OWNER);

        islandByOwner.remove(oldOwner);
        islandByOwner.put(newOwner, island);
        islandByMember.remove(newOwner);
        islandByMember.put(oldOwner, island);

        memberCache.getOrDefault(island.getId(), List.of()).forEach(m -> {
            if (m.getPlayerUuid().equals(oldOwner)) m.setRole(IslandMember.Role.MEMBER);
            if (m.getPlayerUuid().equals(newOwner)) m.setRole(IslandMember.Role.OWNER);
        });
    }

    public void setPhase(Island island, int phase) throws SQLException {
        island.setPhase(phase);
        plugin.getIslandRepository().updatePhaseAndBlocks(island.getId(), phase, island.getBlocksBroken());
    }

    public void setSpawn(Island island, Location loc) throws SQLException {
        island.setSpawn(loc.getX(), loc.getY(), loc.getZ());
        plugin.getIslandRepository().updateSpawn(island.getId(), loc.getX(), loc.getY(), loc.getZ());
    }

    public IslandPermissions getPermissions(UUID islandId) {
        return permCache.computeIfAbsent(islandId, id -> {
            try {
                return loadPermissions(id);
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load permissions for " + id, e);
                return new IslandPermissions(id);
            }
        });
    }

    public void savePermissions(IslandPermissions perms) throws SQLException {
        String sql = """
            INSERT INTO island_permissions (island_id, allow_break, allow_place, allow_interact, allow_chest, allow_kill_mobs)
            VALUES (?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                allow_break=VALUES(allow_break), allow_place=VALUES(allow_place),
                allow_interact=VALUES(allow_interact), allow_chest=VALUES(allow_chest),
                allow_kill_mobs=VALUES(allow_kill_mobs)
        """;
        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, perms.getIslandId().toString());
            ps.setBoolean(2, perms.isAllowBreak());
            ps.setBoolean(3, perms.isAllowPlace());
            ps.setBoolean(4, perms.isAllowInteract());
            ps.setBoolean(5, perms.isAllowChest());
            ps.setBoolean(6, perms.isAllowKillMobs());
            ps.executeUpdate();
        }
        permCache.put(perms.getIslandId(), perms);
    }

    private IslandPermissions loadPermissions(UUID islandId) throws SQLException {
        String sql = "SELECT * FROM island_permissions WHERE island_id=?";
        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, islandId.toString());
            var rs = ps.executeQuery();
            if (rs.next()) {
                return new IslandPermissions(islandId,
                        rs.getBoolean("allow_break"),
                        rs.getBoolean("allow_place"),
                        rs.getBoolean("allow_interact"),
                        rs.getBoolean("allow_chest"),
                        rs.getBoolean("allow_kill_mobs"));
            }
        }
        return new IslandPermissions(islandId);
    }

    public void deleteIsland(Island island) throws SQLException {
        plugin.getIslandRepository().delete(island.getId());
        evictIsland(island);
    }

    public boolean isOnIsland(Player player, Island island) {
        World world = Bukkit.getWorld(island.getWorld());
        if (world == null) return false;
        Location loc = player.getLocation();
        if (!loc.getWorld().equals(world)) return false;
        int spacing = plugin.getConfigManager().getIslandSpacing();
        double dx = Math.abs(loc.getX() - island.getSpawnX());
        double dz = Math.abs(loc.getZ() - island.getSpawnZ());
        return dx < spacing / 2.0 && dz < spacing / 2.0;
    }

    public boolean isMember(Island island, UUID playerUuid) {
        return getMembers(island.getId()).stream()
                .anyMatch(m -> m.getPlayerUuid().equals(playerUuid));
    }

    public int getMemberCount(Island island) {
        return getMembers(island.getId()).size();
    }

    public void teleportToIsland(Player player, Island island) {
        World world = Bukkit.getWorld(island.getWorld());
        if (world == null) {
            plugin.getMessageManager().send(player, "island.world-not-found");
            return;
        }
        Location loc = new Location(world, island.getSpawnX() + 0.5, island.getSpawnY(), island.getSpawnZ() + 0.5);
        player.teleport(loc);
        plugin.getIslandScoreboardManager().show(player, island);
    }

    private void cacheIsland(Island island) {
        islandById.put(island.getId(), island);
        islandByOwner.put(island.getOwnerUuid(), island);
    }

    private void evictIsland(Island island) {
        islandById.remove(island.getId());
        islandByOwner.remove(island.getOwnerUuid());
        memberCache.getOrDefault(island.getId(), List.of())
                .forEach(m -> islandByMember.remove(m.getPlayerUuid()));
        memberCache.remove(island.getId());
        permCache.remove(island.getId());
    }

    private int[] nextIslandCoords() {
        int spacing = plugin.getConfigManager().getIslandSpacing();
        long idx = nextIslandIndex - 1;
        // Spiral grid layout: row by row
        long side = (long) Math.ceil(Math.sqrt(nextIslandIndex));
        int x = (int) (idx % side) * spacing;
        int z = (int) (idx / side) * spacing;
        nextIslandIndex++;
        return new int[]{x, z};
    }
}
