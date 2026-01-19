package com.astaro.creativemanager.task;

import com.astaro.creativemanager.CreativeManager;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

/**
 * Save task class.
 * Updated for 1.21.4 (2026) - Handles SQL Batch saving.
 */
public class SaveTask {

    /**
     * Запускает задачу периодического сохранения.
     *
     * @param plugin экземпляр плагина.
     * @return ID запущенной задачи.
     */
    public static int run(CreativeManager plugin) {
        int interval = plugin.getConfig().getInt("save-interval", 300);

        if (interval > 0) {
            BukkitTask task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
                plugin.getLogger().info("Starting periodic data save...");
                if (plugin.getBlockLogService() != null) {
                    plugin.getBlockLogService().shutdown();
                }
                plugin.getLogger().info("Periodic save completed.");
            }, interval * 20L, interval * 20L);

            return task.getTaskId();
        }
        return 0;
    }
}
