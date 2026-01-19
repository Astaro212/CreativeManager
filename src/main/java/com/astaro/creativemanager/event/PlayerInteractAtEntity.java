package com.astaro.creativemanager.event;

import com.astaro.creativemanager.CreativeManager;
import com.astaro.creativemanager.settings.Protections;
import com.astaro.creativemanager.utils.CMUtils;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Player interact at entity event listener.
 * Updated for 1.21.4 (2026) - No static access, optimized logic.
 */
public class PlayerInteractAtEntity implements Listener {

    private final CreativeManager plugin;

    public PlayerInteractAtEntity(CreativeManager plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onUse(PlayerInteractAtEntityEvent e) {
        Player p = e.getPlayer();
        if (p.getGameMode() != GameMode.CREATIVE) return;

        Entity entity = e.getRightClicked();

        if (entity.hasMetadata("NPC")) {
            if (plugin.getSettings().getProtection(Protections.PL_CITIZENS) &&
                    !p.hasPermission("creativemanager.bypass.entity")) {

                Map<String, String> replaceMap = Collections.singletonMap("{PLUGIN}", "Citizens");
                CMUtils.sendMessage(p, "permission.plugins", replaceMap);
                e.setCancelled(true);
            }
            return;
        }

        if (plugin.getSettings().getProtection(Protections.ENTITY)) {
            String entityName = entity.getType().name().toLowerCase();

            if (!p.hasPermission("creativemanager.bypass.entity") &&
                    !p.hasPermission("creativemanager.bypass.entity." + entityName)) {

                if (plugin.getSettings().getConfig().getBoolean("send-player-messages")) {
                    CMUtils.sendMessage(p, "permission.entity");
                }
                e.setCancelled(true);
            }
        }
    }
}
