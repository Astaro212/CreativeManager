package com.astaro.creativemanager.event;

import com.astaro.creativemanager.CreativeManager;
import com.astaro.creativemanager.services.ItemBlacklist;
import com.astaro.creativemanager.services.ItemLore;
import com.astaro.creativemanager.settings.Protections;
import com.astaro.creativemanager.settings.Settings;
import com.astaro.creativemanager.utils.CMUtils;
import com.astaro.creativemanager.utils.SearchUtils;
import de.tr7zw.changeme.nbtapi.NBT;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;

import java.util.ArrayList;
import java.util.List;

public class InventoryMove implements Listener {

    private final CreativeManager plugin;
    private final Settings settings;
    private final boolean nbtEnabled;

    public InventoryMove(CreativeManager plugin) {
        this.plugin = plugin;
        this.settings = plugin.getSettings();
        this.nbtEnabled = settings.getProtection(Protections.CUSTOM_NBT);
    }

    @EventHandler(ignoreCancelled = true)
    void onInventoryInteract(InventoryInteractEvent event) {
        if (event.getInventory().getHolder() instanceof Player p) {
            if (p.getGameMode() != GameMode.CREATIVE) return;
            ItemBlacklist.asyncCheck(plugin, p);
            ItemLore.asyncCheck(plugin, p);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClick(InventoryCreativeEvent e) {
        Player player = (Player) e.getWhoClicked();

        if (isDropClick(e.getClick())) {
            if (settings.getProtection(Protections.DROP) && !player.hasPermission("creativemanager.bypass.drop")) {
                if (settings.isSendMessageEnabled())
                    CMUtils.sendMessage(player, "permission.drop");
                e.setCancelled(true);
            }
        }

        if (e.getSlotType() == InventoryType.SlotType.ARMOR) {
            if (settings.getProtection(Protections.ARMOR) && !player.hasPermission("creativemanager.bypass.armor")) {
                e.setResult(Event.Result.DENY);
                e.setCancelled(true);
            }
        }

    }


    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void checkIllegalItems(final InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        if (p.getGameMode() != GameMode.CREATIVE) return;

        List<ItemStack> items = getAffectedItems(e);

        for (ItemStack item : items) {
            if (item == null || item.getType() == Material.AIR) continue;

            if (settings.getProtection(Protections.ENCHANT_AND_POTION) && !p.hasPermission("creativemanager.bypass.enchants-and-potions")) {
                stripEnchantsAndPotions(item);
            }

            if (nbtEnabled && !p.hasPermission("creativemanager.bypass.custom_nbt")) {
                stripCustomNBT(item);
            }
        }
    }

    private void stripEnchantsAndPotions(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        meta.getEnchants().keySet().forEach(meta::removeEnchant);

        if (meta instanceof PotionMeta potionMeta) {
            potionMeta.clearCustomEffects();
        }
        item.setItemMeta(meta);
    }

    private void stripCustomNBT(ItemStack item) {
        NBT.modify(item, nbt -> {
            List<String> whitelist = settings.getNBTWhitelist();
            for (String key : nbt.getKeys()) {
                if (!SearchUtils.inList(whitelist, key)) {
                    nbt.removeKey(key);
                }
            }
        });

        ItemMeta meta = item.getItemMeta();
        if (meta != null && !meta.getPersistentDataContainer().isEmpty()) {
            meta.getPersistentDataContainer().getKeys().forEach(key -> meta.getPersistentDataContainer().remove(key));
            item.setItemMeta(meta);
        }
    }

    private List<ItemStack> getAffectedItems(InventoryClickEvent e) {
        List<ItemStack> list = new ArrayList<>();
        e.getCursor();
        list.add(e.getCursor());
        if (e.getCurrentItem() != null) list.add(e.getCurrentItem());
        return list;
    }

    private boolean isDropClick(ClickType type) {
        return type == ClickType.DROP || type == ClickType.CONTROL_DROP ||
                type == ClickType.WINDOW_BORDER_LEFT || type == ClickType.WINDOW_BORDER_RIGHT;
    }
}
