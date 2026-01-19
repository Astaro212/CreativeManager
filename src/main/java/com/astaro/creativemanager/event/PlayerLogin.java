package com.astaro.creativemanager.event;

import com.astaro.creativemanager.CreativeManager;
import com.astaro.creativemanager.manager.InventoryManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class PlayerLogin implements Listener {
    private final CreativeManager plugin;
    private final InventoryManager inventoryManager;

    public PlayerLogin(CreativeManager plugin, InventoryManager inventoryManager) {
        this.plugin = plugin;
        this.inventoryManager = inventoryManager;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onLogin(PlayerJoinEvent e) {
        Player player = e.getPlayer();

        // 1. Проверка обновлений (Bungee Chat API)
        if (player.hasPermission("creativemanager.admin.update")) {
            if (plugin.getSettings().getConfig().getBoolean("send-admin-update-message")) {
                if (!CreativeManager.getUpdateChecker().isUpToDate()) {
                    sendUpdateNotice(player);
                }
            }
        }


        boolean forceGamemode = false;
        try (BufferedReader is = new BufferedReader(new FileReader("server.properties"))) {
            Properties props = new Properties();
            props.load(is);
            forceGamemode = Boolean.parseBoolean(props.getProperty("force-gamemode"));
        } catch (IOException exception) {

        }

        if (forceGamemode && !player.hasPermission("creativemanager.bypass.inventory")) {
            inventoryManager.hasContentAsync(player.getUniqueId()).thenAccept(hasContent -> {
                if (hasContent) {
                    inventoryManager.loadInventoryAsync(player, plugin.getServer().getDefaultGameMode());
                } else {
                    inventoryManager.saveInventoryAsync(player, player.getGameMode());
                }
            });
        }
    }

    private void sendUpdateNotice(Player player) {
        BaseComponent[] tag = TextComponent.fromLegacyText(CreativeManager.TAG + " ");
        TextComponent message = new TextComponent("CreativeManager updated on Spigot ");
        message.setColor(ChatColor.GOLD);

        TextComponent version = new TextComponent("v" + CreativeManager.getUpdateChecker().getVersion() + " ");
        version.setColor(ChatColor.GREEN);

        BaseComponent[] button = TextComponent.fromLegacyText("§7§l>> §r§5Click here to go on Spigot Page");
        ClickEvent click = new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.spigotmc.org/resources/75097");
        for (BaseComponent b : button) b.setClickEvent(click);

        player.spigot().sendMessage(new ComponentBuilder().append(tag).append(message).append(version).create());
        player.spigot().sendMessage(button);
    }
}
