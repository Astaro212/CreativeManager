package com.astaro.creativemanager.services;

import com.astaro.creativemanager.CreativeManager;
import com.astaro.creativemanager.utils.CMUtils;
import com.astaro.creativemanager.utils.SearchUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ItemBlacklist {

    /**
     * Асинхронная проверка инвентаря на наличие запрещенных предметов.
     */
    public static void asyncCheck(CreativeManager plugin, Player player) {
        if (player.hasPermission("creativemanager.bypass.blacklist.get")) return;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            ItemStack[] contents = player.getInventory().getContents();
            List<Integer> toRemove = new ArrayList<>();

            // Кэшируем настройки для этого прохода
            List<String> blacklist = plugin.getSettings().getGetBL();
            String listMode = plugin.getSettings().getConfig().getString("list.mode.get", "blacklist");

            for (int i = 0; i < contents.length; i++) {
                ItemStack item = contents[i];
                if (item == null || item.getType().isAir()) continue;

                if (isBlackListed(player, item, blacklist, listMode)) {
                    toRemove.add(i);
                }
            }
            if (!toRemove.isEmpty()) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    for (int slot : toRemove) {
                        ItemStack item = player.getInventory().getItem(slot);
                        if (item != null) {
                            item.setAmount(0);
                        }
                    }
                });
            }
        });
    }

    private static boolean isBlackListed(Player player, ItemStack item, List<String> blacklist, String mode) {
        String itemName = item.getType().name().toLowerCase();

        if (player.hasPermission("creativemanager.bypass.blacklist.get." + itemName)) return false;

        boolean inList = SearchUtils.inList(blacklist, item);
        boolean shouldRemove = (mode.equals("whitelist") && !inList) || (mode.equals("blacklist") && inList);

        if (shouldRemove) {
            CMUtils.sendMessage(player, "blacklist.get", Map.of("{ITEM}", itemName.replace("_", " ")));
            return true;
        }
        return false;
    }
}
