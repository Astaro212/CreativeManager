package com.astaro.creativemanager.data;

import com.astaro.creativemanager.CreativeManager;
import com.astaro.creativemanager.settings.Settings;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;

public class DatabaseManager {

    private final CreativeManager plugin;
    private HikariDataSource source;

    public DatabaseManager(CreativeManager instance) {
        this.plugin = instance;
        try {
            setDatabase(plugin.getSettings());
        } catch (Exception e){
            plugin.getLogger().log(Level.SEVERE, "Couldn't init database");
        }
    }

    private void setDatabase(Settings settings) {
        Settings.DatabaseCreds creds = settings.getDatabaseCreds();
        HikariConfig config = new HikariConfig();
        if (creds.enabled()) {
            String type = creds.type().toLowerCase();
            switch(type){
                case "mysql" ->{
                    config.setDriverClassName("com.mysql.cj.jdbc.Driver");
                    config.setJdbcUrl("jdbc:mysql://" + creds.host() + ":" + creds.port() + "/" + creds.database());
                }
                case "mariadb" ->{
                    config.setDriverClassName("org.mariadb.jdbc.Driver");
                    config.setJdbcUrl("jdbc:mariadb://" + creds.host() + ":" + creds.port() + "/" + creds.database());
                }
            }
            config.setUsername(creds.username());
            config.setPassword(creds.password());

            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");

        } else {
            File dbFile = new File(plugin.getDataFolder(), "database.db");
            config.setDriverClassName("org.sqlite.JDBC");
            config.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
        }

        config.setMaximumPoolSize(15);
        config.setMinimumIdle(5);
        config.setMaxLifetime(1800000);
        config.setConnectionTimeout(5000);
        config.setPoolName("CreativeManagerPool");

        this.source = new HikariDataSource(config);

        try (Connection ignored = this.source.getConnection()) {
            plugin.getLogger().info("Database connection pool established successfully.");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to verify database connection!", e);
        }
    }

    /**
     * Return connection
     * @return Connection
     */
    public Connection getConnection() throws SQLException {
        if (this.source == null) throw new SQLException("DataSource is not initialized!");
        return this.source.getConnection();
    }

    /**
     * Close connection
     */
    public void close(){
        if(this.source != null && !this.source.isClosed()){
            this.source.close();
            plugin.getLogger().info("Database connection pool closed.");
        }
    }

}
