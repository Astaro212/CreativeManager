package com.astaro.creativemanager.event;

import com.astaro.creativemanager.CreativeManager;
import com.astaro.creativemanager.data.BlockLogService;
import com.astaro.creativemanager.settings.Protections;
import com.astaro.creativemanager.utils.SearchUtils;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.List;

public class PlayerBreak implements Listener {
    private final CreativeManager plugin;
    private final BlockLogService logService;

    public PlayerBreak(CreativeManager instance) {
        this.plugin = instance;
        this.logService = instance.getBlockLogService();
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        Block block = e.getBlock();
        Location loc = block.getLocation();
        GameMode gm = p.getGameMode();

        if (gm == GameMode.CREATIVE) {
            if (plugin.getSettings().getProtection(Protections.BUILD) && !p.hasPermission("creativemanager.bypass.build")) {
                e.setCancelled(true);
                return;
            }

            if (isBlacklisted(p, block)) {
                e.setCancelled(true);
                return;
            }

            if (logService.isCreativeBlock(loc)) {
                logService.removeLog(loc);
            }
            return;
        }

        if (logService.isCreativeBlock(loc)) {

            if (!p.hasPermission("creativemanager.bypass.break-creative")) {

                if (plugin.getSettings().getProtection(Protections.LOOT)) {
                    e.setDropItems(false);
                    e.setExpToDrop(0);

                    block.setType(Material.AIR);
                    logService.removeLog(loc);

                    e.setCancelled(true);
                }
            } else {
                logService.removeLog(loc);
            }
        }
    }

    private boolean isBlacklisted(Player p, Block b) {
        if (p.hasPermission("creativemanager.bypass.blacklist.break")) return false;

        String blockName = b.getType().name().toLowerCase();
        if (p.hasPermission("creativemanager.bypass.blacklist.break." + blockName)) return false;

        List<String> blacklist = plugin.getSettings().getBreakBL();
        String mode = plugin.getSettings().getConfig().getString("list.mode.break", "blacklist");

        boolean inList = SearchUtils.inList(blacklist, b);
        return mode.equalsIgnoreCase("whitelist") ? !inList : inList;
    }
}
