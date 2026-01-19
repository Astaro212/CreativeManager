package com.astaro.creativemanager.data;

import com.astaro.creativemanager.CreativeManager;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

public class BlockLogService {

    private final BlockLogRepository repository;
    private final CreativeManager instance;
    private final ConcurrentLinkedQueue<BlockLog> saveQueue = new ConcurrentLinkedQueue<>();

    private final Cache<Long, UUID> cache = Caffeine.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .maximumSize(200_000) // Увеличили для 2026 года, так как Long->UUID очень легкие
            .build();

    public BlockLogService(CreativeManager plugin, BlockLogRepository repository) {
        this.repository = repository;
        this.instance = plugin;
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::flushQueue, 100L, 100L);
    }

    private long getBlockKey(int x, int y, int z) {
        return ((long) x & 0x7FFFFFFL) | (((long) z & 0x7FFFFFFL) << 27) | (((long) y & 0x3FFL) << 54);
    }


    public boolean isCreativeBlock(Location loc) {
        return cache.getIfPresent(getBlockKey(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())) != null;
    }

    public void logBlock(String world, int x, int y, int z, UUID playerUuid) {
        long key = getBlockKey(x, y, z);
        cache.put(key, playerUuid);
        saveQueue.add(new BlockLog(world, x, y, z, playerUuid));
    }

    public void logBlock(Location loc, UUID playerUuid) {
        logBlock(loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), playerUuid);
    }

    public void loadChunk(String world, int cx, int cz) {
        repository.loadChunk(world, cx, cz).thenAccept(logs -> {
            if (logs == null || logs.isEmpty()) return;
            for (BlockLog log : logs) {
                cache.put(getBlockKey(log.x(), log.y(), log.z()), log.playerUUID());
            }
        });
    }

    public CompletableFuture<Boolean> isCreativeAsync(Location loc) {
        UUID cached = cache.getIfPresent(getBlockKey(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
        if (cached != null) return CompletableFuture.completedFuture(true);

        return CompletableFuture.supplyAsync(() ->
                repository.exists(loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())
        );
    }

    public void removeLog(Location loc) {
        cache.invalidate(getBlockKey(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
        Bukkit.getScheduler().runTaskAsynchronously(instance, () ->
                repository.delete(loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())
        );
    }

    public BlockLog getLog(Location loc) {
        if (loc == null || loc.getWorld() == null) return null;
        UUID playerUuid = cache.getIfPresent(getBlockKey(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
        if (playerUuid == null) return null;

        return new BlockLog(loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), playerUuid);
    }

    private void flushQueue() {
        if (saveQueue.isEmpty()) return;
        List<BlockLog> batch = new ArrayList<>();
        BlockLog record;
        while (batch.size() < 2000 && (record = saveQueue.poll()) != null) {
            batch.add(record);
        }
        if (!batch.isEmpty()) repository.saveBatch(batch);
    }

    public void shutdown() {
        instance.getLogger().info("Saving pending block logs...");
        flushQueue();
    }
}
