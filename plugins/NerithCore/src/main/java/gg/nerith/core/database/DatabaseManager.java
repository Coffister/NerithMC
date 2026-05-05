package gg.nerith.core.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import gg.nerith.core.NerithCore;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private final NerithCore plugin;
    private HikariDataSource dataSource;

    public DatabaseManager(NerithCore plugin) {
        this.plugin = plugin;
    }

    public void connect() throws SQLException {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" +
                plugin.getConfigManager().getDbHost() + ":" +
                plugin.getConfigManager().getDbPort() + "/" +
                plugin.getConfigManager().getDbName() +
                "?autoReconnect=true&useSSL=false&allowPublicKeyRetrieval=true");
        config.setUsername(plugin.getConfigManager().getDbUsername());
        config.setPassword(plugin.getConfigManager().getDbPassword());
        config.setMaximumPoolSize(plugin.getConfigManager().getDbPoolSize());
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.setPoolName("NerithCore-Pool");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        dataSource = new HikariDataSource(config);
        createTables();
    }

    private void createTables() throws SQLException {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS islands (
                    id              VARCHAR(36) PRIMARY KEY,
                    owner_uuid      VARCHAR(36) NOT NULL,
                    island_type     ENUM('solo','coop') NOT NULL DEFAULT 'solo',
                    phase           INT NOT NULL DEFAULT 1,
                    blocks_broken   BIGINT NOT NULL DEFAULT 0,
                    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    last_active     TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    spawn_x         DOUBLE,
                    spawn_y         DOUBLE,
                    spawn_z         DOUBLE,
                    world           VARCHAR(64) NOT NULL DEFAULT 'oneblock_world',
                    current_block   VARCHAR(64) NOT NULL DEFAULT 'STONE'
                )
            """);
            // Add column if upgrading from older schema
            try {
                stmt.executeUpdate("ALTER TABLE islands ADD COLUMN current_block VARCHAR(64) NOT NULL DEFAULT 'STONE'");
            } catch (Exception ignored) {}

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS island_members (
                    island_id       VARCHAR(36) NOT NULL,
                    player_uuid     VARCHAR(36) NOT NULL,
                    role            ENUM('owner','member') NOT NULL DEFAULT 'member',
                    joined_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    PRIMARY KEY (island_id, player_uuid),
                    FOREIGN KEY (island_id) REFERENCES islands(id) ON DELETE CASCADE
                )
            """);

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS island_stats (
                    island_id       VARCHAR(36) PRIMARY KEY,
                    mobs_killed     BIGINT DEFAULT 0,
                    bosses_killed   INT DEFAULT 0,
                    events_triggered INT DEFAULT 0,
                    treasure_found  INT DEFAULT 0,
                    FOREIGN KEY (island_id) REFERENCES islands(id) ON DELETE CASCADE
                )
            """);

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS island_resets (
                    id              INT AUTO_INCREMENT PRIMARY KEY,
                    island_id       VARCHAR(36) NOT NULL,
                    player_uuid     VARCHAR(36) NOT NULL,
                    phase_at_reset  INT NOT NULL,
                    blocks_at_reset BIGINT NOT NULL,
                    penalty_amount  DOUBLE NOT NULL,
                    reset_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS island_permissions (
                    island_id       VARCHAR(36) PRIMARY KEY,
                    allow_break     BOOLEAN DEFAULT FALSE,
                    allow_place     BOOLEAN DEFAULT FALSE,
                    allow_interact  BOOLEAN DEFAULT FALSE,
                    allow_chest     BOOLEAN DEFAULT FALSE,
                    allow_kill_mobs BOOLEAN DEFAULT FALSE,
                    FOREIGN KEY (island_id) REFERENCES islands(id) ON DELETE CASCADE
                )
            """);

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS island_powerups (
                    island_id       VARCHAR(36) NOT NULL,
                    powerup_type    ENUM('void_shield','fragment_saver') NOT NULL,
                    active          BOOLEAN DEFAULT TRUE,
                    obtained_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    PRIMARY KEY (island_id, powerup_type),
                    FOREIGN KEY (island_id) REFERENCES islands(id) ON DELETE CASCADE
                )
            """);
        }
    }

    public Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            throw new SQLException("Database not connected");
        }
        return dataSource.getConnection();
    }

    public void disconnect() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    public boolean isConnected() {
        return dataSource != null && !dataSource.isClosed();
    }
}
