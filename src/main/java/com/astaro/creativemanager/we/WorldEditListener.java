package com.astaro.creativemanager.we;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.eventbus.Subscribe;
import com.astaro.creativemanager.CreativeManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class WorldEditListener {

    private final CreativeManager plugin;

    public WorldEditListener(CreativeManager plugin) {
        this.plugin = plugin;
    }

    @Subscribe
    public void onEditSession(EditSessionEvent e) {
        // Логируем ДО внесения изменений
        if (e.getStage() != EditSession.Stage.BEFORE_CHANGE) return;
        if (e.getActor() == null || !e.getActor().isPlayer()) return;

        UUID playerUuid = e.getActor().getUniqueId();
        Player p = Bukkit.getPlayer(playerUuid);

        if (p == null || !p.getGameMode().equals(org.bukkit.GameMode.CREATIVE)) return;

        String worldName = e.getWorld().getName();


        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                Region selection = WorldEdit.getInstance().getSessionManager()
                        .get(e.getActor()).getSelection(e.getWorld());

                if (selection == null) return;

                if (plugin.getSettings().isDebug())
                    plugin.getLogger().info("[WE-Log] Batch logging for " + p.getName() + " started...");

                int totalBlocks = 0;
                for (BlockVector3 v : selection) {
                    plugin.getBlockLogService().logBlock(worldName, v.x(), v.y(), v.z(), playerUuid);
                    totalBlocks++;
                }

                if (plugin.getSettings().isDebug())
                    plugin.getLogger().info("[WE-Log] Finished. Queued: " + totalBlocks + " blocks.");

            } catch (Exception ex) {
                // Если сессия сброшена или регион не найден - просто выходим
            }
        });
    }
}
