package com.astaro.creativemanager.we;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.eventbus.Subscribe;
import com.astaro.creativemanager.CreativeManager;
import com.astaro.creativemanager.log.BlockLog;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.Iterator;


public class WorldEditListener {

    private CreativeManager plugin;

    public WorldEditListener(CreativeManager plugin) {
        this.plugin = plugin;
    }

    @Subscribe
    public void onEditSessionEnd(EditSessionEvent e) {
        if (e.getStage() != EditSession.Stage.BEFORE_CHANGE) return;

        if (e.getActor() == null || !e.getActor().isPlayer()) return;

        Player p = Bukkit.getServer().getPlayer(e.getActor().getUniqueId());
        World w = Bukkit.getWorld(e.getWorld().getName());
        if (w == null || p == null) return;

        try {
            Region selection = WorldEdit.getInstance().getSessionManager().get(e.getActor()).getSelection();
            if (selection == null) {
                Bukkit.getLogger().warning("[WERC] Could not get selection for " + p.getName());
                return;
            }

            Bukkit.getLogger().info("[WERC] Starting batch logging for " + p.getName() + " in world " + w.getName());

            int totalBlocks = 0;
            int savedBlocks = 0;

            Iterator<BlockVector3> iterator = selection.iterator();
            while (iterator.hasNext()) {
                BlockVector3 v = iterator.next();
                totalBlocks++;

                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    Block b = w.getBlockAt(v.getBlockX(), v.getBlockY(), v.getBlockZ());

                    if (b.getType() != org.bukkit.Material.AIR) {
                        BlockLog log = new BlockLog(b, p.getPlayer());
                        log.setLocation(b.getLocation());
                        plugin.getDataManager().save();
                    }
                });

                // Avoid overloading the main thread TaskQueue
                if (totalBlocks % 1000 == 0) {
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException ignored) {
                    }
                }
            }
            Bukkit.getLogger().info("[WERC] Logging finished. Processed: " + totalBlocks + " blocks.");


        } catch (Exception ex) {
            Bukkit.getLogger().severe("[WERC] Error during batch logging: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}

