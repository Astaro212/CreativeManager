package com.astaro.creativemanager.event;

import com.astaro.creativemanager.CreativeManager;
import com.astaro.creativemanager.data.BlockLog;
import com.astaro.creativemanager.data.BlockLogService;
import com.astaro.creativemanager.utils.BlockUtils;
import org.bukkit.Location;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.world.StructureGrowEvent;

import java.util.UUID;

public class BlockEvent implements Listener {

    private final CreativeManager plugin;
    private final BlockLogService logService;

    public BlockEvent(CreativeManager cm) {
        this.plugin = cm;
        this.logService = cm.getBlockLogService();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    void onBlockGrow(BlockGrowEvent event) {
        Block block = event.getBlock();
        Location loc = block.getLocation();

        switch (block.getType()) {
            case CACTUS, SUGAR_CANE -> {
                BlockLog parentLog = logService.getLog(block.getRelative(BlockFace.DOWN).getLocation());
                if (parentLog != null) {
                    logService.logBlock(loc, parentLog.playerUUID());
                }
            }
            case PUMPKIN, MELON -> {
                for (Block b : BlockUtils.getAdjacentBlocks(block)) {
                    if (Tag.CROPS.isTagged(b.getType())) {
                        BlockLog bLog = logService.getLog(b.getLocation());
                        if (bLog != null) {
                            logService.logBlock(loc, bLog.playerUUID());
                            break;
                        }
                    }
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    void onStructureGrow(StructureGrowEvent event) {
        BlockLog sourceLog = logService.getLog(event.getLocation());
        if (sourceLog != null) {
            UUID owner = sourceLog.playerUUID();
            for (BlockState state : event.getBlocks()) {
                logService.logBlock(state.getLocation(), owner);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    void onBlockSpread(BlockSpreadEvent event) {
        BlockLog sourceLog = logService.getLog(event.getSource().getLocation());
        if (sourceLog != null) {
            logService.logBlock(event.getBlock().getLocation(), sourceLog.playerUUID());
        }
    }
}
