package com.astaro.creativemanager.manager;

import com.astaro.creativemanager.CreativeManager;
import com.astaro.creativemanager.data.BlockLogRepository;
import com.astaro.creativemanager.utils.InventoryUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.sql.*;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class InventoryManager {

    private final CreativeManager plugin;
    private final DatabaseManager db;
    private final NamespacedKey armorKey;

    public InventoryManager(CreativeManager plugin, DatabaseManager db) {
        this.plugin = plugin;
        this.db = db;
        this.armorKey = new NamespacedKey(plugin, "creative_armor");
    }


    public CompletableFuture<Void> saveInventoryAsync(Player player, GameMode gm) {
        if (plugin.getConfig().getBoolean("stop-inventory-save")) return CompletableFuture.completedFuture(null);
        if (player.hasPermission("creativemanager.bypass.inventory-save")) return CompletableFuture.completedFuture(null);

        String uuid = player.getUniqueId().toString();
        String gmName = gm.name();
        String content = InventoryUtils.itemStackArrayToBase64(player.getInventory().getContents());
        String armor = InventoryUtils.itemStackArrayToBase64(player.getInventory().getArmorContents());

        return CompletableFuture.runAsync(() -> {
            String sql = "REPLACE INTO " + plugin.getRepo().getInvPrefix() + " (uuid, gamemode, content, armor) VALUES (?, ?, ?, ?)";
            try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, uuid);
                ps.setString(2, gmName);
                ps.setString(3, content);
                ps.setString(4, armor);
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("Error saving SQL inventory: " + e.getMessage());
            }
        });
    }

    public CompletableFuture<Void> loadInventoryAsync(Player player, GameMode gm) {
        String uuid = player.getUniqueId().toString();
        String gmName = gm.name();

        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT content, armor FROM " + plugin.getRepo().getInvPrefix() + " WHERE uuid = ? AND gamemode = ?";
            try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, uuid);
                ps.setString(2, gmName);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return new String[]{rs.getString("content"), rs.getString("armor")};
                }
            } catch (SQLException e) { e.printStackTrace(); }
            return null;
        }).thenAccept(data -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (data != null) {
                    try {
                        player.getInventory().setContents(InventoryUtils.itemStackArrayFromBase64(data[0]));
                        player.getInventory().setArmorContents(InventoryUtils.itemStackArrayFromBase64(data[1]));
                    } catch (Exception e) {
                        plugin.getLogger().severe("Error decoding inventory: " + e.getMessage());
                    }
                } else {
                    player.getInventory().clear();
                }
            });
        });
    }

    public CompletableFuture<Boolean> hasContentAsync(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT 1 FROM " + plugin.getRepo().getInvPrefix() + " WHERE uuid = ? LIMIT 1";
            try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, uuid.toString());
                try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
            } catch (SQLException e) { return false; }
        });
    }


    public void applyCreativeArmor(Player player) {
        ItemStack[] armor = new ItemStack[4];
        armor[3] = createArmorItem(plugin.getConfig().getString("creative-armor.helmet", "CHAINMAIL_HELMET"));
        armor[2] = createArmorItem(plugin.getConfig().getString("creative-armor.chestplate", "CHAINMAIL_CHESTPLATE"));
        armor[1] = createArmorItem(plugin.getConfig().getString("creative-armor.leggings", "CHAINMAIL_LEGGINGS"));
        armor[0] = createArmorItem(plugin.getConfig().getString("creative-armor.boots", "CHAINMAIL_BOOTS"));

        player.getInventory().setArmorContents(armor);
    }

    private ItemStack createArmorItem(String materialName) {
        Material mat = Material.matchMaterial(materialName);
        if (mat == null) mat = Material.AIR;
        ItemStack item = new ItemStack(mat);
        if (mat != Material.AIR) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.getPersistentDataContainer().set(armorKey, PersistentDataType.BYTE, (byte) 1);
                item.setItemMeta(meta);
            }
        }
        return item;
    }

    public NamespacedKey getArmorKey() {
        return armorKey;
    }
}
