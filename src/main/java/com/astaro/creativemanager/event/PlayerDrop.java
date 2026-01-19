package com.astaro.creativemanager.event;

import com.astaro.creativemanager.CreativeManager;
import com.astaro.creativemanager.settings.Protections;
import com.astaro.creativemanager.utils.CMUtils;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

/**
 * Player drop item event listener.
 */
public class PlayerDrop implements Listener {

	/**
	 * Instantiates a new Player drop.
	 *
	 */
	CreativeManager plugin;
	public PlayerDrop(CreativeManager plugin) {
		this.plugin = plugin;
	}

	/**
	 * On drop.
	 *
	 * @param e the event.
	 */
	@EventHandler(ignoreCancelled = true)
	public void onDrop(PlayerDropItemEvent e) {
		Player p = e.getPlayer();
		if (plugin.getSettings().getProtection(Protections.DROP) && p.getGameMode().equals(GameMode.CREATIVE)) {
			if (!p.hasPermission("creativemanager.bypass.drop")) {
				if (plugin.getSettings().isSendMessageEnabled())
					CMUtils.sendMessage(p, "permission.drop");
				e.setCancelled(true);
			}
		}
	}
}
