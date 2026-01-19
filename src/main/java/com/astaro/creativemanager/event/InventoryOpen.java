package com.astaro.creativemanager.event;

import com.astaro.creativemanager.CreativeManager;
import com.astaro.creativemanager.settings.Protections;
import com.astaro.creativemanager.settings.Settings;
import com.astaro.creativemanager.utils.CMUtils;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import java.util.EnumSet;
import java.util.Set;

public class InventoryOpen implements Listener {

    private final CreativeManager plugin;
    private final Settings settings;

    private static final Set<InventoryType> PROTECTED_TYPES = EnumSet.of(
            InventoryType.CHEST,
            InventoryType.FURNACE,
            InventoryType.BLAST_FURNACE,
            InventoryType.SMOKER,
            InventoryType.BARREL,
            InventoryType.BEACON,
            InventoryType.BREWING,
            InventoryType.DISPENSER,
            InventoryType.DROPPER,
            InventoryType.HOPPER,
            InventoryType.SHULKER_BOX,
            InventoryType.LECTERN,
            InventoryType.ENDER_CHEST
    );

    public InventoryOpen(CreativeManager plugin) {
        this.plugin = plugin;
        this.settings = plugin.getSettings();
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent e) {
        if (!(e.getPlayer() instanceof Player p)) return;
        if (p.getGameMode() != GameMode.CREATIVE) return;

        if (settings.getProtection(Protections.CONTAINER) && isProtectedChest(e.getInventory())) {
            if (!p.hasPermission("creativemanager.bypass.container")) {
                cancelEvent(e, p, "permission.container");
                return;
            }
        }

        if (settings.getProtection(Protections.GUI)) {
            if (!PROTECTED_TYPES.contains(e.getInventory().getType())) {
                if (!p.hasPermission("creativemanager.bypass.gui")) {
                    cancelEvent(e, p, "permission.gui");
                }
            }
        }
    }

    private void cancelEvent(InventoryOpenEvent e, Player p, String messageKey) {
        if (settings.isSendMessageEnabled()) {
            CMUtils.sendMessage(p, messageKey);
        }
        e.setCancelled(true);
    }

    private boolean isProtectedChest(Inventory inventory) {
        InventoryType type = inventory.getType();

        if (!PROTECTED_TYPES.contains(type)) return false;
        if (inventory.getHolder() != null) {
            String holderClass = inventory.getHolder().getClass().getName();
            return holderClass.startsWith("org.bukkit") || holderClass.startsWith("net.minecraft");
        }

        return type == InventoryType.ENDER_CHEST;
    }
}
