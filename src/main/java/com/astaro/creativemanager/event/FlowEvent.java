package com.astaro.creativemanager.event;

import com.astaro.creativemanager.CreativeManager;
import com.astaro.creativemanager.log.BlockLog;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;

public class FlowEvent implements Listener {

    private final CreativeManager plugin;

    public FlowEvent(CreativeManager plugin)
    {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    void onBlockFromTo(BlockFromToEvent event) {
        BlockLog blockLog = plugin.getDataManager().getBlockFrom(event.getToBlock().getLocation());
        if(blockLog != null && blockLog.isCreative())
        {
            event.setCancelled(true);
            event.getToBlock().setType(Material.AIR);
            plugin.getDataManager().removeBlock(blockLog.getLocation());
        }
    }
}
