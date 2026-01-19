package com.astaro.creativemanager.event;

import com.astaro.creativemanager.CreativeManager;
import com.astaro.creativemanager.data.BlockLogService;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;

public class FlowEvent implements Listener {

    private final CreativeManager plugin;
    private final BlockLogService logService;

    public FlowEvent(CreativeManager plugin) {
        this.plugin = plugin;
        this.logService = plugin.getBlockLogService();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    void onBlockFromTo(BlockFromToEvent event) {
        Block toBlock = event.getToBlock();

        if (logService.isCreativeBlock(toBlock.getLocation())) {
            event.setCancelled(true);
            toBlock.setType(Material.AIR);
            logService.removeLog(toBlock.getLocation());
        }
    }
}
