package com.astaro.creativemanager.data;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class BlockLogRepository {

    private final DatabaseManager db;

    public BlockLogRepository(DatabaseManager db) {
        this.db = db;
        initTable();
    }

    public void initTable() {
        try (Connection conn = db.getConnection(); Statement s = conn.createStatement()) {
            s.executeUpdate("CREATE TABLE IF NOT EXISTS block_log (" +
                    "world VARCHAR(64), x INT, y INT, z INT, player VARCHAR(36), " +
                    "PRIMARY KEY (world, x, y, z))");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveBatch(Collection<BlockLog> logs) {
        String sql = "REPLACE INTO block_log (world, x, y, z, player) VALUES (?, ?, ?, ?, ?)";
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
            e.printStackTrace();
        }
    }

    public void delete(String world, int x, int y, int z) {
        String sql = "DELETE FROM block_log WHERE world=? AND x=? AND y=? AND z=?";
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, world);
            ps.setInt(2, x);
            ps.setInt(3, y);
            ps.setInt(4, z);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean exists(String world, int x, int y, int z) {
        String sql = "SELECT 1 FROM block_log WHERE world=? AND x=? AND y=? AND z=? LIMIT 1";
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, world);
            ps.setInt(2, x);
            ps.setInt(3, y);
            ps.setInt(4, z);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public CompletableFuture<List<BlockLog>> loadChunk(String world, int cx, int cz) {
        return CompletableFuture.supplyAsync(() -> {
            List<BlockLog> logs = new ArrayList<>();
            String sql = "SELECT * FROM block_log WHERE world = ? AND x >= ? AND x <= ? AND z >= ? AND z <= ?";

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

    public int getTotalEntries() {
        String sql = "SELECT COUNT(*) FROM block_log";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
