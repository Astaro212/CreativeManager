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

    private final Cache<Location, BlockLog> cache = Caffeine.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .maximumSize(100_000)
            .build();

    public BlockLogService(CreativeManager plugin, BlockLogRepository repository) {
        this.repository = repository;
        this.instance = plugin;
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::flushQueue, 100L, 100L);
    }

    public void logBlock(Location loc, UUID playerUuid) {
        BlockLog record = new BlockLog(loc, playerUuid);
        cache.put(loc, record);
        saveQueue.add(record);
    }

    private void flushQueue() {
        if (saveQueue.isEmpty()) return;

        List<BlockLog> batch = new ArrayList<>();
        BlockLog record;
        while (batch.size() < 2000 && (record = saveQueue.poll()) != null) {
            batch.add(record);
        }

        if (!batch.isEmpty()) {
            repository.saveBatch(batch);
        }
    }

    public boolean isCreativeBlock(Location loc) {
        return cache.getIfPresent(loc) != null;
    }

    public CompletableFuture<Boolean> isCreativeAsync(Location loc) {
        BlockLog cached = cache.getIfPresent(loc);
        if (cached != null) return CompletableFuture.completedFuture(true);

        return CompletableFuture.supplyAsync(() -> {
            return repository.exists(loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        });
    }

    public void loadChunk(String world, int cx, int cz) {
        repository.loadChunk(world, cx, cz).thenAccept(logs -> {
            if (logs == null || logs.isEmpty()) return;
            for (BlockLog log : logs) {
                Location loc = log.toLocation();
                if (loc != null) cache.put(loc, log);
            }
        });
    }

    public void removeLog(Location loc) {
        cache.invalidate(loc);
        Bukkit.getScheduler().runTaskAsynchronously(instance, () -> {
            repository.delete(loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        });
    }

    public void shutdown() {
        instance.getLogger().info("Saving pending block logs before shutdown...");
        flushQueue();
    }
}
