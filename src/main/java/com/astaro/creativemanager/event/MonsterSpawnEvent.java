package com.astaro.creativemanager.event;

import com.astaro.creativemanager.CreativeManager;
import com.astaro.creativemanager.data.BlockLog;
import com.astaro.creativemanager.data.BlockLogService;
import com.astaro.creativemanager.settings.Protections;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.HashSet;
import java.util.Set;

public class MonsterSpawnEvent implements Listener {

    private final CreativeManager plugin;
    private final BlockLogService logService;

    public MonsterSpawnEvent(CreativeManager plugin) {
        this.plugin = plugin;
        this.logService = plugin.getBlockLogService();
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onSpawn(CreatureSpawnEvent e) {

        if (!plugin.getSettings().getProtection(Protections.SPAWN_BUILD)) return;
        CreatureSpawnEvent.SpawnReason reason = e.getSpawnReason();

        if (reason != CreatureSpawnEvent.SpawnReason.BUILD_SNOWMAN &&
                reason != CreatureSpawnEvent.SpawnReason.BUILD_IRONGOLEM &&
                reason != CreatureSpawnEvent.SpawnReason.BUILD_WITHER) {
            return;
        }

        Block baseBlock = e.getLocation().getBlock();
        Set<Block> blocksToCheck = new HashSet<>();

        switch (reason) {
            case BUILD_SNOWMAN -> {
                addRelativeBlocks(blocksToCheck, baseBlock, new int[][]{{0,0,0}, {0,1,0}, {0,2,0}});
            }
            case BUILD_IRONGOLEM -> {
                addRelativeBlocks(blocksToCheck, baseBlock, new int[][]{
                        {0,0,0}, {0,1,0}, {0,2,0}, // Body
                        {0,1,1}, {0,1,-1},         // Hands Z
                        {1,1,0}, {-1,1,0}          // Hands X
                });
            }
            case BUILD_WITHER -> {
                addRelativeBlocks(blocksToCheck, baseBlock, new int[][]{
                        {0,0,0}, {0,1,0}, {0,2,0}, // Body
                        {0,1,1}, {0,2,1}, {0,1,-1}, {0,2,-1}, // Head Z
                        {1,1,0}, {1,2,0}, {-1,1,0}, {-1,2,0}  // Head X
                });
            }
        }

        for (Block block : blocksToCheck) {
            BlockLog log = logService.getLog(block.getLocation());
            if (log != null) {

                var player = plugin.getServer().getPlayer(log.playerUUID());
                if (player != null && player.hasPermission("creativemanager.bypass.spawn_build")) {
                    continue;
                }

                e.setCancelled(true);
                return;
            }
        }
    }

    private void addRelativeBlocks(Set<Block> set, Block base, int[][] offsets) {
        for (int[] o : offsets) {
            set.add(base.getRelative(o[0], o[1], o[2]));
        }
    }
}
