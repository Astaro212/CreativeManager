package com.astaro.creativemanager.data;

import com.astaro.creativemanager.CreativeManager;
import com.astaro.creativemanager.manager.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class BlockLogRepository {

    private final DatabaseManager db;
    private final String logPrefix;
    private final String invPrefix;


    public BlockLogRepository(DatabaseManager db, CreativeManager plugin) {
        this.db = db;
        this.logPrefix = plugin.getSettings().getConfig().getConfigurationSection("mysql").getString("log_table_prefix") + "block_log";
        invPrefix = plugin.getSettings().getConfig().getConfigurationSection("mysql").getString("inventory_table_prefix") + "player_inventories";
        initTable();
    }

    public void initTable() {
        try (Connection conn = db.getConnection(); Statement s = conn.createStatement()) {
            s.executeUpdate("CREATE TABLE IF NOT EXISTS " + logPrefix + " (" +
                    "world VARCHAR(64), x INT, y INT, z INT, player VARCHAR(36), " +
                    "PRIMARY KEY (world, x, y, z))");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS " + invPrefix + " (" +
                    "uuid VARCHAR(36)NOT NULL," +
                    "gamemode VARCHAR(20)NOT NULL," +
                    "content LONGTEXT NOT NULL," +
                    "armor LONGTEXT NOT NULL," +
                    "PRIMARY KEY(uuid, gamemode))"
            );
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public CompletableFuture<Void> saveBatch(Collection<BlockLog> logs) {
        return CompletableFuture.runAsync(() -> {
            String sql = "REPLACE INTO " + logPrefix + " (world, x, y, z, player) VALUES (?, ?, ?, ?, ?)";
            try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                conn.setAutoCommit(false);
                for (BlockLog log : logs) {
                    ps.setString(1, log.worldName());
                    ps.setInt(2, log.x());
                    ps.setInt(3, log.y());
                    ps.setInt(4, log.z());
                    ps.setString(5, log.playerUUID().toString());
                    ps.addBatch();
                }
                ps.executeBatch();
                conn.commit();
            } catch (SQLException e) {
                throw new CompletionException(e);
            }
        }).exceptionally(exc -> {
            exc.printStackTrace();
            return null;
        });
    }

    public CompletableFuture<Void> delete(String world, int x, int y, int z) {
        return CompletableFuture.runAsync(() -> {
            String sql = "DELETE FROM " + logPrefix + " WHERE world=? AND x=? AND y=? AND z=?";
            try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, world);
                ps.setInt(2, x);
                ps.setInt(3, y);
                ps.setInt(4, z);
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new CompletionException(e);
            }
        }).exceptionally(exc -> {
            exc.printStackTrace();
            return null;
        });
    }

    public CompletableFuture<Boolean> exists(String world, int x, int y, int z) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT 1 FROM " + logPrefix + " WHERE world=? AND x=? AND y=? AND z=? LIMIT 1";
            try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, world);
                ps.setInt(2, x);
                ps.setInt(3, y);
                ps.setInt(4, z);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            } catch (SQLException e) {
                throw new CompletionException(e);
            }
        }).exceptionally(exc -> {
            exc.printStackTrace();
            return false;
        });
    }

    public CompletableFuture<List<BlockLog>> loadChunk(String world, int cx, int cz) {
        return CompletableFuture.supplyAsync(() -> {
            List<BlockLog> logs = new ArrayList<>();
            String sql = "SELECT * FROM " + logPrefix + " WHERE world = ? AND x >= ? AND x <= ? AND z >= ? AND z <= ?";

            int minX = cx * 16;
            int maxX = minX + 15;
            int minZ = cz * 16;
            int maxZ = minZ + 15;

            try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, world);
                ps.setInt(2, minX);
                ps.setInt(3, maxX);
                ps.setInt(4, minZ);
                ps.setInt(5, maxZ);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        logs.add(new BlockLog(
                                rs.getString("world"),
                                rs.getInt("x"),
                                rs.getInt("y"),
                                rs.getInt("z"),
                                UUID.fromString(rs.getString("player"))
                        ));
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return logs;
        });
    }

    public CompletableFuture<Integer> getTotalEntries() {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT COUNT(*) FROM " + logPrefix;
            try (Connection conn = db.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            } catch (SQLException e) {
                throw new CompletionException(e);
            }
            return 0;
        }).exceptionally(exc -> {
            exc.printStackTrace();
            return 0;
        });
    }

    public String getInvPrefix() {
        return invPrefix;
    }


}
