package com.astaro.creativemanager.event;

import com.astaro.creativemanager.CreativeManager;
import com.astaro.creativemanager.settings.Protections;
import com.astaro.creativemanager.utils.CMUtils;
import com.astaro.creativemanager.utils.SearchUtils;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SpawnEggMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Player interact event listener.
 * Updated for 1.21.4 - No static, optimized checks.
 */
public class PlayerInteract implements Listener {

    private final CreativeManager plugin;

    public PlayerInteract(CreativeManager plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void checkBlacklistUse(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        ItemStack itemStack = e.getItem();

        if (p.getGameMode() != GameMode.CREATIVE || itemStack == null) return;
        if (p.hasPermission("creativemanager.bypass.blacklist.use")) return;

        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && itemStack.getType().isBlock()) return;

        String itemName = itemStack.getType().name().toLowerCase();
        if (p.hasPermission("creativemanager.bypass.blacklist.use." + itemName)) return;

        List<String> blacklist = plugin.getSettings().getUseBL();
        String mode = plugin.getSettings().getConfig().getString("list.mode.use", "blacklist");

        boolean inList = SearchUtils.inList(blacklist, itemStack);
        if ((mode.equals("whitelist") && !inList) || (mode.equals("blacklist") && inList)) {
            HashMap<String, String> replaceMap = new HashMap<>();
            replaceMap.put("{ITEM}", itemName.replace("_", " "));
            CMUtils.sendMessage(p, "blacklist.use", replaceMap);
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void checkContainer(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        Block block = e.getClickedBlock();

        if (p.getGameMode() != GameMode.CREATIVE) return;
        if (!plugin.getSettings().getProtection(Protections.CONTAINER)) return;
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK || block == null) return;

        if (p.isSneaking() && p.getInventory().getItemInMainHand().getType().isBlock()) return;

        if (block.getType() == Material.CAMPFIRE || block.getType() == Material.SOUL_CAMPFIRE) {
            if (!Tag.ITEMS_SHOVELS.isTagged(p.getInventory().getItemInMainHand().getType())) {
                if (!p.hasPermission("creativemanager.bypass.container")) {
                    sendProtectionMessage(p, "permission.container");
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void checkBlacklistUseBlock(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        Block block = e.getClickedBlock();

        if (p.getGameMode() != GameMode.CREATIVE || block == null) return;
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (p.isSneaking() && e.getItem() != null) return;

        List<String> blacklist = plugin.getSettings().getUseBlockBL();
        if (blacklist.isEmpty()) return;
        if (p.hasPermission("creativemanager.bypass.blacklist.useblock")) return;

        String mode = plugin.getSettings().getConfig().getString("list.mode.useblock", "blacklist");
        boolean inList = SearchUtils.inList(blacklist, block);

        if ((mode.equals("whitelist") && !inList) || (mode.equals("blacklist") && inList)) {
            sendProtectionMessage(p, "blacklist.useblock");
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void checkSpawnEgg(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        ItemStack itemStack = e.getItem();

        if (p.getGameMode() != GameMode.CREATIVE || itemStack == null) return;
        if (p.hasPermission("creativemanager.bypass.spawn_egg")) return;
        if (!plugin.getSettings().getProtection(Protections.SPAWN)) return;

        // В 1.21.4 SpawnEggMeta всегда доступна, Class.forName не нужен
        if (itemStack.getItemMeta() instanceof SpawnEggMeta) {
            sendProtectionMessage(p, "permission.spawn");
            e.setCancelled(true);
        }
    }

    private void sendProtectionMessage(Player p, String key) {
        if (plugin.getSettings().getConfig().getBoolean("send-player-messages")) {
            CMUtils.sendMessage(p, key);
        }
    }
}
