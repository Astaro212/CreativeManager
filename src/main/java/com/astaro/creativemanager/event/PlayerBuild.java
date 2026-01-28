package com.astaro.creativemanager.event;

import com.astaro.creativemanager.CreativeManager;
import com.astaro.creativemanager.data.BlockLogService;
import com.astaro.creativemanager.settings.Protections;
import com.astaro.creativemanager.settings.Settings;
import com.astaro.creativemanager.utils.BlockUtils;
import com.astaro.creativemanager.utils.CMUtils;
import com.astaro.creativemanager.utils.SearchUtils;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.List;

public class PlayerBuild implements Listener {
	private final CreativeManager plugin;
	private final Settings settings;
	private final BlockLogService logService;

	public PlayerBuild(CreativeManager instance) {
		this.plugin = instance;
		this.settings = instance.getSettings();
		this.logService = instance.getBlockLogService();
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent e) {
		Player p = e.getPlayer();
		Block block = e.getBlock();
		GameMode gm = p.getGameMode();
		if (gm == GameMode.CREATIVE) {
			if (settings.getProtection(Protections.BUILD) && !p.hasPermission("creativemanager.bypass.build")) {
				sendCreativeMessage(p, "permission.build");
				e.setCancelled(true);
				return;
			}
			if (isBlacklisted(p, block)) {
				sendCreativeMessage(p, "blacklist.place");
				e.setCancelled(true);
				return;
			}
			if (settings.getProtection(Protections.BUILD_CONTAINER) && !p.hasPermission("creativemanager.bypass.build-container")) {
				if (block.getState() instanceof Container container) {
					container.getInventory().clear();
					container.update();
				}
			}
			if (!p.hasPermission("creativemanager.bypass.logged")) {
				List<Block> structure = BlockUtils.getBlockStructure(block);
				for (Block b : structure) {
					logService.logBlock(b.getLocation(), p.getUniqueId());
				}
			}
		}
		else {
			if (logService.isCreativeBlock(block.getLocation())) {
				logService.removeLog(block.getLocation());
			}
		}
	}

	private boolean isBlacklisted(Player p, Block b) {
		if (p.hasPermission("creativemanager.bypass.blacklist.place")) return false;

		String blockName = b.getType().name().toLowerCase();
		if (p.hasPermission("creativemanager.bypass.blacklist.place." + blockName)) return false;

		List<String> blacklist = settings.getPlaceBL();
		if (blacklist.isEmpty()) return false;

		boolean isWhitelist = "whitelist".equalsIgnoreCase(settings.getConfig().getString("list.mode.place"));
		boolean inList = SearchUtils.inList(blacklist, b);

		return isWhitelist != inList;
	}

	private void sendCreativeMessage(Player p, String key) {
		if (settings.getConfig().getBoolean("send-player-messages")) {
			CMUtils.sendMessage(p, key);
		}
	}
}
