package com.astaro.creativemanager.settings;

import com.astaro.creativemanager.CreativeManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Settings class.
 */
public class Settings {

    private final CreativeManager cm;
    private FileConfiguration config;
    private File configFile;

    /**
     * Instantiates a new Settings.
     *
     * @param instance the instance
     */
    public Settings(CreativeManager instance) {
        this.cm = instance;
        this.saveDefaultConfig();
        this.reload();
    }

    /**
     * Reloads configuration
     */
    public void reload() {
        if (configFile == null) {
            configFile = new File(cm.getDataFolder(), "config.yml");
        }
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    /**
     * Save current config
     */
    public void save() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            cm.getLogger().severe("Could not save config.yml");
        }
    }

    /**
     * Save default config file
     */
    public void saveDefaultConfig() {
        if (!cm.getDataFolder().exists()) cm.getDataFolder().mkdirs();
        File file = new File(cm.getDataFolder(), "config.yml");
        if (!file.exists()) {
            cm.saveResource("config.yml", false);
        }
    }

    /**
     * Gets send message part
     *
     * @return boolean
     */
    public boolean isSendMessageEnabled() {
        return config.getBoolean("send-player-messages", true);
    }

    /**
     * Debug?
     *
     * @return boolean
     */
    public boolean isDebug() {
        return config.getBoolean("debug", false);
    }

    /**
     * Gets protection.
     *
     * @param protections the protections.
     * @return the protection.
     */
    public boolean getProtection(Protections protections) {
        return config.getBoolean("protections." + protections.getName());
    }

    /**
     * Gets protection.
     *
     * @param protections the protections.
     * @param value       the protections.
     */
    public void setProtection(Protections protections, boolean value) {
        config.set("protections." + protections.getName(), value);
    }

    /**
     * Creative inv enable boolean.
     *
     * @return True if yes, otherwise false.
     */
    public boolean creativeInvEnable() {
        return config.getBoolean("inventory.creative");
    }

    /**
     * Adventure inv enable boolean.
     *
     * @return True if yes, otherwise false.
     */
    public boolean adventureInvEnable() {
        return config.getBoolean("inventory.adventure");
    }

    /**
     * Spectator inv enable boolean.
     *
     * @return True if yes, otherwise false.
     */
    public boolean spectatorInvEnable() {
        return config.getBoolean("inventory.spectator");
    }

    /**
     * Gets place bl.
     *
     * @return the placed blocks.
     */
    public List<String> getPlaceBL() {
        return config.getStringList("list.place");
    }

    /**
     * Gets use bl.
     *
     * @return the use blacklist.
     */
    public List<String> getUseBL() {
        return config.getStringList("list.use");
    }

    /**
     * Gets use bl.
     *
     * @return the use blacklist.
     */
    public List<String> getUseBlockBL() {
        return config.getStringList("list.useblock");
    }

    /**
     * Gets get bl.
     *
     * @return the get blacklist.
     */
    public List<String> getGetBL() {
        return config.getStringList("list.get");
    }

    /**
     * Gets break bl.
     *
     * @return the break blacklist.
     */
    public List<String> getBreakBL() {
        return config.getStringList("list.break");
    }

    /**
     * Gets command bl.
     *
     * @return the command blacklist.
     */
    public List<String> getCommandBL() {
        return config.getStringList("list.commands");
    }

    /**
     * Gets command bl.
     *
     * @return the command blacklist.
     */
    public List<String> getNBTWhitelist() {
        return config.getStringList("list.nbt-whitelist");
    }

    /**
     * Gets lore.
     *
     * @return the lore.
     */
    public List<String> getLore() {
        return config.getStringList("creative-lore");
    }

    public String getLang() {
        return config.getString("lang");
    }

    public String getTag() {
        return config.getString("tag", "<gray>[<green>CreativeManager</green>]</gray> ");
    }

    public Configuration getConfig() {
        return this.config;
    }

    public record DatabaseCreds(
            boolean enabled,
            String type,
            String host,
            int port,
            String database,
            String username,
            String password
    ) {
    }

    public DatabaseCreds getDatabaseCreds() {
        ConfigurationSection msec = config.getConfigurationSection("mysql");
        if (msec == null) {
            return new DatabaseCreds(false, "mysql", "localhost", 3306, "cm_log", "minecraft", "minecraft1");
        }
        return new DatabaseCreds(
                msec.getBoolean("enabled", false),
                msec.getString("type"),
                msec.getString("host"),
                msec.getInt("port"),
                msec.getString("database"),
                msec.getString("username"),
                msec.getString("password")
        );
    }


}
