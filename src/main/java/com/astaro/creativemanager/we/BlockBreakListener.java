package com.astaro.creativemanager.we;

import com.astaro.creativemanager.CreativeManager;
import com.astaro.creativemanager.log.BlockLog;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class BlockBreakListener implements Listener {

    private final CreativeManager plugin;

    public BlockBreakListener(CreativeManager plug) {
        this.plugin = plug;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onBlockBreak(BlockBreakEvent event) {

    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onEntityExplode(EntityExplodeEvent e) {
        e.blockList().forEach(block -> {
            BlockLog log = plugin.getDataManager().getBlockFrom(block.getLocation());
            if (log != null) {
                Bukkit.getLogger().info("[WERC DEBUG] Block found and deleted.");
                block.setType(Material.AIR);
                plugin.getDataManager().delete(log);
            }
        });
    }
}

