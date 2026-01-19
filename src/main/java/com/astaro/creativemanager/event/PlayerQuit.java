package com.astaro.creativemanager.event;

import com.Acrobot.ChestShop.Libs.ORMlite.stmt.query.In;
import com.astaro.creativemanager.CreativeManager;
import com.astaro.creativemanager.manager.InventoryManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Player quit/kick event listeners.
 */
public class PlayerQuit implements Listener {
	private final CreativeManager plugin;
	private final InventoryManager im;

	/**
	 * Instantiates a new Player quit.
	 *
	 * @param instance the instance.
	 */
	public PlayerQuit(CreativeManager instance, InventoryManager manager) {
		plugin = instance;
		im = manager;
	}

	/**
	 * On player quit.
	 *
	 * @param e the event.
	 */
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onQuit(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		im.saveInventoryAsync(p, p.getGameMode());
	}

	/**
	 * On player kicked.
	 *
	 * @param e the event.
	 */
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onKicked(PlayerKickEvent e) {
		Player p = e.getPlayer();
		im.saveInventoryAsync(p, p.getGameMode());
	}
}
