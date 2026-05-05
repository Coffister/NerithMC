package gg.nerith.core;

import gg.nerith.core.commands.IslandAdminCommand;
import gg.nerith.core.commands.IslandCommand;
import gg.nerith.core.config.ConfigManager;
import gg.nerith.core.config.MessageManager;
import gg.nerith.core.coop.CoopListener;
import gg.nerith.core.coop.CoopManager;
import gg.nerith.core.database.*;
import gg.nerith.core.island.*;
import gg.nerith.core.leaderboard.LeaderboardManager;
import gg.nerith.core.metrics.MetricsCollector;
import gg.nerith.core.metrics.WebApiServer;
import gg.nerith.core.oneblock.*;
import gg.nerith.core.void_death.PowerupManager;
import gg.nerith.core.void_death.VoidDeathListener;
import gg.nerith.core.gui.GuiListener;
import gg.nerith.core.scoreboard.IslandScoreboardManager;
import gg.nerith.core.world.WorldManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;

public final class NerithCore extends JavaPlugin {

    private ConfigManager configManager;
    private MessageManager messageManager;
    private DatabaseManager databaseManager;
    private IslandRepository islandRepository;
    private MemberRepository memberRepository;
    private StatsRepository statsRepository;
    private ResetRepository resetRepository;
    private IslandManager islandManager;
    private IslandCreator islandCreator;
    private IslandResetter islandResetter;
    private IslandPermissionManager islandPermissionManager;
    private BlockPoolManager blockPoolManager;
    private PhaseManager phaseManager;
    private OneBlockListener oneBlockListener;
    private PhaseUpAnimator phaseUpAnimator;
    private SpecialEventManager specialEventManager;
    private CoopManager coopManager;
    private PowerupManager powerupManager;
    private LeaderboardManager leaderboardManager;
    private MetricsCollector metricsCollector;
    private WebApiServer webApiServer;
    private WorldManager worldManager;
    private IslandScoreboardManager islandScoreboardManager;
    private GuiListener guiListener;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        configManager = new ConfigManager(this);
        configManager.load();

        messageManager = new MessageManager(this);
        messageManager.load();

        databaseManager = new DatabaseManager(this);
        try {
            databaseManager.connect();
            getLogger().info("[NerithCore] Database connected successfully.");
        } catch (SQLException e) {
            getLogger().severe("[NerithCore] Failed to connect to database: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        islandRepository = new IslandRepository(this);
        memberRepository = new MemberRepository(this);
        statsRepository = new StatsRepository(this);
        resetRepository = new ResetRepository(this);

        islandCreator = new IslandCreator(this);
        islandResetter = new IslandResetter(this);
        islandPermissionManager = new IslandPermissionManager(this);
        islandManager = new IslandManager(this);
        try {
            islandManager.loadNextIslandIndex();
        } catch (SQLException e) {
            getLogger().warning("[NerithCore] Could not load island index from DB: " + e.getMessage());
        }

        worldManager = new WorldManager(this);
        worldManager.ensureWorlds();
        islandManager.repairAndRegisterOneBlocks();

        blockPoolManager = new BlockPoolManager(this);
        blockPoolManager.load();

        phaseUpAnimator = new PhaseUpAnimator(this);
        phaseManager = new PhaseManager(this);
        specialEventManager = new SpecialEventManager(this);
        oneBlockListener = new OneBlockListener(this);

        islandScoreboardManager = new IslandScoreboardManager(this);
        guiListener = new GuiListener();

        coopManager = new CoopManager(this);
        powerupManager = new PowerupManager(this);
        leaderboardManager = new LeaderboardManager(this);
        metricsCollector = new MetricsCollector(this);

        registerListeners();
        registerCommands();

        if (configManager.isWebApiEnabled()) {
            webApiServer = new WebApiServer(this, configManager.getWebApiPort());
            webApiServer.startServer();
        }

        leaderboardManager.scheduleAutoSettle();

        getLogger().info("[NerithCore] v" + getDescription().getVersion() + " enabled. " +
                configManager.getPhaseCount() + " phases loaded.");
    }

    @Override
    public void onDisable() {
        if (webApiServer != null) webApiServer.stop();
        if (databaseManager != null) databaseManager.disconnect();
        getLogger().info("[NerithCore] Disabled.");
    }

    private void registerListeners() {
        var pm = getServer().getPluginManager();
        pm.registerEvents(oneBlockListener, this);
        pm.registerEvents(guiListener, this);
        pm.registerEvents(islandScoreboardManager, this);
        pm.registerEvents(new CoopListener(this), this);
        pm.registerEvents(new VoidDeathListener(this), this);
    }

    private void registerCommands() {
        IslandCommand islandCmd = new IslandCommand(this);
        getCommand("island").setExecutor(islandCmd);
        getCommand("island").setTabCompleter(islandCmd);

        IslandAdminCommand adminCmd = new IslandAdminCommand(this);
        getCommand("islandadmin").setExecutor(adminCmd);
        getCommand("islandadmin").setTabCompleter(adminCmd);
    }

    public ConfigManager getConfigManager() { return configManager; }
    public MessageManager getMessageManager() { return messageManager; }
    public DatabaseManager getDatabaseManager() { return databaseManager; }
    public IslandRepository getIslandRepository() { return islandRepository; }
    public MemberRepository getMemberRepository() { return memberRepository; }
    public StatsRepository getStatsRepository() { return statsRepository; }
    public ResetRepository getResetRepository() { return resetRepository; }
    public IslandManager getIslandManager() { return islandManager; }
    public IslandCreator getIslandCreator() { return islandCreator; }
    public IslandResetter getIslandResetter() { return islandResetter; }
    public IslandPermissionManager getIslandPermissionManager() { return islandPermissionManager; }
    public BlockPoolManager getBlockPoolManager() { return blockPoolManager; }
    public PhaseManager getPhaseManager() { return phaseManager; }
    public OneBlockListener getOneBlockListener() { return oneBlockListener; }
    public PhaseUpAnimator getPhaseUpAnimator() { return phaseUpAnimator; }
    public SpecialEventManager getSpecialEventManager() { return specialEventManager; }
    public CoopManager getCoopManager() { return coopManager; }
    public PowerupManager getPowerupManager() { return powerupManager; }
    public LeaderboardManager getLeaderboardManager() { return leaderboardManager; }
    public MetricsCollector getMetricsCollector() { return metricsCollector; }
    public WorldManager getWorldManager() { return worldManager; }
    public IslandScoreboardManager getIslandScoreboardManager() { return islandScoreboardManager; }
    public GuiListener getGuiListener() { return guiListener; }
}
