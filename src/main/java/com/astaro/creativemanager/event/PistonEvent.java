package com.astaro.creativemanager.event;

import com.astaro.creativemanager.CreativeManager;
import com.astaro.creativemanager.data.BlockLog;
import com.astaro.creativemanager.data.BlockLogService;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.PistonMoveReaction;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;

import java.util.List;

public class PistonEvent implements Listener {
    private final CreativeManager plugin;
    private final BlockLogService logService;

    public PistonEvent(CreativeManager instance) {
        this.plugin = instance;
        this.logService = instance.getBlockLogService();
    }

    @EventHandler(ignoreCancelled = true)
    public void onExtend(BlockPistonExtendEvent event) {
        BlockFace direction = event.getDirection();

        processPistonMove(direction, event.getBlocks());

        Block piston = event.getBlock();
        BlockLog pistonLog = logService.getLog(piston.getLocation());
        if (pistonLog != null) {
            Block head = piston.getRelative(direction);

            logService.logBlock(head.getLocation(), pistonLog.playerUUID());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onRetract(BlockPistonRetractEvent event) {
        BlockFace direction = event.getDirection();

        processPistonMove(direction, event.getBlocks());

        Block piston = event.getBlock();
        BlockLog pistonLog = logService.getLog(piston.getLocation());
        if (pistonLog != null) {
            Block headLocation = piston.getRelative(direction);
            logService.removeLog(headLocation.getLocation());
        }
    }

    private void processPistonMove(BlockFace direction, List<Block> blocks) {
        if (blocks.isEmpty()) return;

        for (int i = blocks.size() - 1; i >= 0; i--) {
            Block block = blocks.get(i);
            Location oldLoc = block.getLocation();
            BlockLog log = logService.getLog(oldLoc);

            if (log != null) {
                if (block.getPistonMoveReaction() == PistonMoveReaction.BREAK) {
                    logService.removeLog(oldLoc);
                } else {
                    Location newLoc = block.getRelative(direction).getLocation();

                    logService.removeLog(oldLoc);
                    logService.logBlock(newLoc, log.playerUUID());
                }
            }
        }
    }
}
