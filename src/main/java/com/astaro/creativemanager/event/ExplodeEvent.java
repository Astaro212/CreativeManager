package com.astaro.creativemanager.event;

import com.astaro.creativemanager.CreativeManager;
import com.astaro.creativemanager.data.BlockLog;
import com.astaro.creativemanager.data.BlockLogService;
import com.astaro.creativemanager.settings.Protections;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.persistence.PersistentDataType;

import javax.annotation.Nullable;
import java.util.UUID;

public class ExplodeEvent implements Listener {

    private final CreativeManager plugin;
    private final BlockLogService logService;

    private final NamespacedKey uuidKey;
    private final NamespacedKey dateKey;

    public ExplodeEvent(CreativeManager cm) {
        this.plugin = cm;
        this.logService = cm.getBlockLogService();
        this.uuidKey = new NamespacedKey(cm, "uuid");
        this.dateKey = new NamespacedKey(cm, "date");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent e) {
        if (!plugin.getSettings().getProtection(Protections.LOOT)) return;

        for (Block block : e.blockList()) {
            if (logService.isCreativeBlock(block.getLocation())) {
                block.setType(Material.AIR);
                logService.removeLog(block.getLocation());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFallingBlockStart(EntityChangeBlockEvent event) {
        if (event.getTo() == Material.AIR && event.getEntity() instanceof FallingBlock fallingBlock) {
            BlockLog log = logService.getLog(event.getBlock().getLocation());
            if (log != null) {
                registerMetadata(fallingBlock, log.playerUUID());
                fallingBlock.setDropItem(false);
                logService.removeLog(event.getBlock().getLocation());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFallingBlockLand(EntityChangeBlockEvent event) {
        if (event.getEntity() instanceof FallingBlock fallingBlock && event.getTo() != Material.AIR) {
            UUID ownerUuid = getOwnerFromMetadata(fallingBlock);
            if (ownerUuid != null) {
                logService.logBlock(event.getBlock().getLocation(), ownerUuid);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onOtherBlockChange(EntityChangeBlockEvent e) {
        if (e.getTo() == Material.AIR && !(e.getEntity() instanceof FallingBlock)) {
            if (logService.isCreativeBlock(e.getBlock().getLocation())) {
                e.setCancelled(true);
            }
        }
    }

    private void registerMetadata(Entity entity, UUID uuid) {
        entity.getPersistentDataContainer().set(uuidKey, PersistentDataType.STRING, uuid.toString());
        entity.getPersistentDataContainer().set(dateKey, PersistentDataType.LONG, System.currentTimeMillis());
    }

    @Nullable
    private UUID getOwnerFromMetadata(Entity entity) {
        String uuidStr = entity.getPersistentDataContainer().get(uuidKey, PersistentDataType.STRING);
        return uuidStr != null ? UUID.fromString(uuidStr) : null;
    }
}
