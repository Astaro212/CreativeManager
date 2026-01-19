package com.astaro.creativemanager.utils;

import com.astaro.creativemanager.CreativeManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class SpigotUtils {

    public static String getPlayerDisplayname(Player player) {
        return LegacyComponentSerializer.legacySection().serialize(player.displayName());
    }

    public static String getPluginName(CreativeManager plugin) {
        return plugin.getDescription().getName();
    }


    public static void setItemMetaLore(ItemMeta itemMeta, List<String> lore) {
        itemMeta.setLore(lore);
    }

    public static void setItemMetaDisplayname(ItemMeta itemMeta, String displayName) {
        itemMeta.setDisplayName(displayName);
    }

    public static boolean isPaper() {
        return Bukkit.getName().equalsIgnoreCase("Paper") ||
                Bukkit.getName().equalsIgnoreCase("Purpur");
    }

    public static Inventory createInventory(int row, String title) {
        return Bukkit.createInventory(null, 9 * row, Component.text(title));
    }

    public static boolean isLifecycleSupported() {
        try {
            Class.forName("io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents");
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }
}
