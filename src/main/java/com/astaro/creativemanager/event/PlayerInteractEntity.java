package com.astaro.creativemanager.event;

import com.astaro.creativemanager.CreativeManager;
import com.astaro.creativemanager.settings.Protections;
import com.astaro.creativemanager.utils.CMUtils;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import java.util.Map;

/**
 * Player interact entity event listener.
 * Updated for 1.21.4 (2026) - Optimized logic and Map handling.
 */
public class PlayerInteractEntity implements Listener {

    private final CreativeManager plugin;

    public PlayerInteractEntity(CreativeManager plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onUse(PlayerInteractEntityEvent e) {
        Player p = e.getPlayer();

        if (p.getGameMode() != GameMode.CREATIVE) return;

        if (e.getRightClicked().hasMetadata("NPC")) {
            if (plugin.getSettings().getProtection(Protections.PL_CITIZENS) &&
                    !p.hasPermission("creativemanager.bypass.entity")) {

                CMUtils.sendMessage(p, "permission.plugins", Map.of("{PLUGIN}", "Citizens"));
                e.setCancelled(true);
            }
            return;
        }

        if (plugin.getSettings().getProtection(Protections.ENTITY) &&
                !p.hasPermission("creativemanager.bypass.entity")) {

            String entityName = e.getRightClicked().getType().name().toLowerCase();

            if (!p.hasPermission("creativemanager.bypass.entity." + entityName)) {
                if (plugin.getSettings().getConfig().getBoolean("send-player-messages")) {
                    CMUtils.sendMessage(p, "permission.entity");
                }
                e.setCancelled(true);
            }
        }
    }
}
