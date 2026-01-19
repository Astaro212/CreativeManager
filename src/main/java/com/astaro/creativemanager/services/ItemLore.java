package com.astaro.creativemanager.services;

import com.astaro.creativemanager.CreativeManager;
import com.astaro.creativemanager.utils.CMUtils;
import com.astaro.creativemanager.utils.TextUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ItemLore {

    public static void asyncCheck(CreativeManager plugin, Player player) {
        // Уходим в асинхрон, чтобы не тормозить сервер расчетами строк
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            List<String> configLore = plugin.getSettings().getLore();
            if (configLore.isEmpty()) return;

            ItemStack[] contents = player.getInventory().getContents();

            Bukkit.getScheduler().runTask(plugin, () -> {
                for (ItemStack item : contents) {
                    if (item == null || item.getType().isAir()) continue;
                    applyLore(item, player, configLore);
                }
            });
        });
    }

    private static void applyLore(ItemStack item, Player player, List<String> configLore) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        List<String> newLore = new ArrayList<>();
        for (String line : configLore) {
            newLore.add(getFinalString(line, player, item));
        }

        meta.setLore(newLore);
        item.setItemMeta(meta);
    }

    private static String getFinalString(String string, Player player, ItemStack itemStack) {
        return CMUtils.parse(TextUtils.replacePlaceholders(
                string,
                Map.of(
                        "PLAYER", player.getName(),
                        "UUID", player.getUniqueId().toString(),
                        "ITEM", itemStack.getType().name().replace("_", " ").toLowerCase()
                )));
    }
}
