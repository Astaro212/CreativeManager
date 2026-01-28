package com.astaro.creativemanager;

import com.astaro.creativemanager.data.BlockLogRepository;
import com.astaro.creativemanager.data.BlockLogService;
import com.astaro.creativemanager.manager.DatabaseManager;
import com.astaro.creativemanager.event.*;
import com.astaro.creativemanager.manager.InventoryManager;
import com.sk89q.worldedit.WorldEdit;
import com.astaro.creativemanager.commands.Commands;
import com.astaro.creativemanager.commands.cm.CreativeManagerCommands;
import com.astaro.creativemanager.commands.cm.CreativeManagerCommandTab;
import com.astaro.creativemanager.event.plugin.ChestShop;
import com.astaro.creativemanager.event.plugin.ItemsAdderListener;
import com.astaro.creativemanager.event.plugin.SlimeFun;
import com.astaro.creativemanager.settings.Settings;
import com.astaro.creativemanager.task.SaveTask;
import com.astaro.creativemanager.we.BlockBreakListener;
import com.astaro.creativemanager.we.WorldEditListener;
import fr.k0bus.k0buscore.config.Lang;
import fr.k0bus.k0buscore.updater.UpdateChecker;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class CreativeManager extends JavaPlugin {

    public static String TAG = "&r[&cCreativeManager&r] ";
    public static final String TAG_INV = "&l&4CM &r> ";
    private Settings settings;
    private static Lang lang;
    private DatabaseManager manager;
    private BlockLogService service;
    private BlockLogRepository repo;
    private int saveTask;
    private InventoryManager invmanager;
    private static final HashMap<String, Set<Material>> tagMap = new HashMap<>();
    private static UpdateChecker updateChecker;
    private static int antiSpamTick;
    LegacyComponentSerializer serializer = LegacyComponentSerializer.legacyAmpersand();

    @Override
    public void onEnable() {
        super.onEnable();
        getComponentLogger().info(serializer.deserialize("&9============================================================="));
        updateChecker = new UpdateChecker(this, 75097);
        if (updateChecker.isUpToDate()) {
            getComponentLogger().info(serializer.deserialize("&2" + this.getDescription().getName() + " &av" + this.getDescription().getVersion()));
        } else {
            getComponentLogger().info(serializer.deserialize("&2" + this.getDescription().getName() + " &cv" + this.getDescription().getVersion() +
                    " (Update " + updateChecker.getVersion() + " available on SpigotMC)"));
        }
        new Metrics(this, 11481);
        getComponentLogger().info(serializer.deserialize("&9============================================================="));
        getComponentLogger().info(serializer.deserialize("&2Created by K0bus for AkuraGaming, refactored by Astaro212"));
        getComponentLogger().info(serializer.deserialize("&9============================================================="));
        getComponentLogger().info(serializer.deserialize("&2Check config file for update"));
        this.updateConfig();
        getComponentLogger().info(serializer.deserialize("&2Loading config file"));
        this.loadConfigManager();
        manager = new DatabaseManager(this);
        repo = new BlockLogRepository(this.manager, this);
        service = new BlockLogService(this, this.repo);
        invmanager = new InventoryManager(this, manager);
        this.registerEvent(this.getServer().getPluginManager());
        getComponentLogger().info(serializer.deserialize("&2Listener registered"));
        this.registerCommand();
        getComponentLogger().info(serializer.deserialize("&2Commands registered"));
        this.registerPermissions();
        this.loadLog();
        this.loadTags();
        this.saveTask = SaveTask.run(this);
        if (getSettings().getConfig().getBoolean("stop-inventory-save"))
            getComponentLogger().warn(serializer.deserialize("&cWarning : &4'stop-inventory-save' set on 'true' then all features about inventory as been disabled !"));
        getComponentLogger().info(serializer.deserialize("&9============================================================="));
    }

    public void loadConfigManager() {
        settings = new Settings(this);
        getComponentLogger().info(serializer.deserialize("&2Configuration loaded"));
        lang = new Lang(settings.getLang(), this);
        getComponentLogger().info(serializer.deserialize("&2Language loaded &7[" + settings.getLang() + "]"));
        TAG = settings.getTag();
        antiSpamTick = settings.getConfig().getInt("antispam-tick");
    }

    public void updateConfig() {
        String[] langs = {"en_EN", "es_ES", "fr_FR", "it_IT", "ru_RU", "zh_CN"};
        for (String lang : langs) {
            saveResource("lang/" + lang + ".yml", false);
        }

        File configFile = new File(getDataFolder(), "config.yml");
        if (configFile.exists()) {
            FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

            if (config.contains("blacklist")) {
                ConfigurationSection cs = config.getConfigurationSection("blacklist");
                if (cs != null) {
                    config.set("list", cs);
                    config.set("blacklist", null);
                    try {
                        config.save(configFile);
                        getComponentLogger().info("Config migration: 'blacklist' node moved to 'list'");
                    } catch (IOException e) {
                        getComponentLogger().error("Could not save migrated config!");
                    }
                }
            }
        }

        saveDefaultConfig();
    }

    private void registerEvent(PluginManager pm) {
        pm.registerEvents(new PlayerBuild(this), this);
        pm.registerEvents(new PlayerBreak(this), this);
        pm.registerEvents(new PlayerInteract(this), this);
        pm.registerEvents(new PlayerInteractEntity(this), this);
        pm.registerEvents(new PlayerInteractAtEntity(this), this);
        pm.registerEvents(new PlayerDrop(this), this);
        pm.registerEvents(new PlayerGamemodeChange(this, invmanager), this);
        pm.registerEvents(new PlayerQuit(this, invmanager), this);
        pm.registerEvents(new PlayerLogin(this, invmanager), this);
        pm.registerEvents(new PistonEvent(this), this);
        pm.registerEvents(new MonsterSpawnEvent(this), this);
        pm.registerEvents(new ProjectileThrow(this), this);
        pm.registerEvents(new InventoryOpen(this), this);
        pm.registerEvents(new PlayerPreCommand(this), this);
        pm.registerEvents(new ExplodeEvent(this), this);
        pm.registerEvents(new PlayerDeath(), this);
        pm.registerEvents(new FlowEvent(this), this);
        pm.registerEvents(new BlockEvent(this, service), this);
        pm.registerEvents(new WorldEvent(this), this);
        pm.registerEvents(new BlockBreakListener(this), this);
        /*  Add event checked for old version */
        try {
            ItemMeta.class.getMethod("getPersistentDataContainer", (Class<?>[]) null);
            pm.registerEvents(new InventoryMove(this), this);
        } catch (NoSuchMethodException | SecurityException e) {
            getComponentLogger().info(serializer.deserialize("NBT Protection disabled on your Minecraft version"));
            pm.registerEvents(new InventoryMove(this), this);
        }
        try {
            ProjectileHitEvent.class.getMethod("getHitEntity", (Class<?>[]) null);
            pm.registerEvents(new PlayerHitEvent(true, this), this);
        } catch (NoSuchMethodException | SecurityException e) {
            getComponentLogger().info(serializer.deserialize("PvP / PvE Protection can't protect from projectile on this Spigot version !"));
            pm.registerEvents(new PlayerHitEvent(false, this), this);
        }
        try {
            Class.forName("org.bukkit.event.entity.EntityPickupItemEvent");
            pm.registerEvents(new PlayerPickup(this), this);
        } catch (ClassNotFoundException e) {
            getComponentLogger().info(serializer.deserialize("Player pickup protection not enabled on this Spigot version !"));
        }
        /* Add plugin event */
        if (getServer().getPluginManager().isPluginEnabled("Slimefun"))
            pm.registerEvents(new SlimeFun(this), this);
        if (getServer().getPluginManager().isPluginEnabled("ChestShop"))
            pm.registerEvents(new ChestShop(this), this);
        if (getServer().getPluginManager().isPluginEnabled("ItemsAdder"))
            pm.registerEvents(new ItemsAdderListener(this), this);
        if (getServer().getPluginManager().isPluginEnabled("WorldEdit") && getServer().getPluginManager().isPluginEnabled("WorldGuard")) {
            WorldEdit.getInstance().getEventBus().register(new WorldEditListener(this));
        }
    }

    private void registerCommand() {
        PluginCommand mainCommand = this.getCommand("cm");
        if (mainCommand != null) {
            mainCommand.setExecutor(new CreativeManagerCommands(this));
            mainCommand.setTabCompleter(new CreativeManagerCommandTab((Commands) mainCommand.getExecutor()));
        }
    }

    private void registerPermissions() {
        PluginManager pm = getServer().getPluginManager();
        int n = 0;
        for (EntityType entityType : EntityType.values()) {
            registerPerm("creativemanager.bypass.entity." + entityType.name(), pm);
            n++;
        }
        registerPerm("creativemanager.bypass.deathdrop", pm);
        getComponentLogger().info(serializer.deserialize("&2Entities permissions registered ! &7[" + n + "]"));

        /* Add plugin permissions */
        if (getServer().getPluginManager().isPluginEnabled("ChestShop")) {
            registerPerm("creativemanager.bypass.chestshop", pm);
            getComponentLogger().info(serializer.deserialize("&2ChestShop permissions registered !"));
        }
        if (getServer().getPluginManager().isPluginEnabled("ItemsAdder")) {
            registerPerm("creativemanager.bypass.itemsadder.furnituresplace", pm);
            registerPerm("creativemanager.bypass.itemsadder.blockplace", pm);
            registerPerm("creativemanager.bypass.itemsadder.blockbreak", pm);
            registerPerm("creativemanager.bypass.itemsadder.blockinteract", pm);
            registerPerm("creativemanager.bypass.itemsadder.furnituresinteract", pm);
            registerPerm("creativemanager.bypass.itemsadder.killentity", pm);
            getComponentLogger().info(serializer.deserialize("&2ItemsAdder permissions registered !"));
        }
        if (getServer().getPluginManager().isPluginEnabled("Slimefun")) {
            registerPerm("creativemanager.bypass.slimefun", pm);
            getComponentLogger().info(serializer.deserialize("&2Slimefun permissions registered !"));
        }
    }

    private void registerPerm(String permission, PluginManager pm) {
        if (!pm.getPermissions().contains(new Permission(permission))) {
            try {
                pm.addPermission(new Permission(permission));
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    private void loadTags() {
        try {
            Field[] fieldlist = Tag.class.getDeclaredFields();
            for (Field fld : fieldlist) {
                try {
                    Set<Material> set = ((Tag<Material>) fld.get(null)).getValues();
                    tagMap.put(fld.getName(), set);
                } catch (Exception ignored) {
                }
            }
            getComponentLogger().info(serializer.deserialize("&2Tag loaded from Spigot ! &7[" + tagMap.size() + "]"));
        } catch (NoClassDefFoundError e) {
            getComponentLogger().info(serializer.deserialize("&cThis minecraft version could not use the TAG system."));
        }
    }

    private void loadLog() {
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            int total = 0;
            try {
                total = repo.getTotalEntries().get();
            } catch (InterruptedException | ExecutionException e) {
               e.printStackTrace();
            }
            getComponentLogger().info(serializer.deserialize("Database contains " + total + " creative block logs."));
        });
    }

    public Settings getSettings() {
        return settings;
    }

    public static Lang getLang() {
        return lang;
    }

    public static HashMap<String, Set<Material>> getTagMap() {
        return tagMap;
    }


    public static UpdateChecker getUpdateChecker() {
        return updateChecker;
    }

    public DatabaseManager getManager() {
        return this.manager;
    }

    public BlockLogService getBlockLogService() {
        return service;
    }

    public BlockLogRepository getRepo() {
        return repo;
    }

    public InventoryManager getInvManager() {
        return invmanager;
    }

    @Override
    public void onDisable() {
        if (Bukkit.getScheduler().isCurrentlyRunning(saveTask) || Bukkit.getScheduler().isQueued(saveTask))
            Bukkit.getScheduler().cancelTask(saveTask);
        if (service != null)
            service.shutdown();

    }
}
