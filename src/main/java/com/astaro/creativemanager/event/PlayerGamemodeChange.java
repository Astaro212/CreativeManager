package com.astaro.creativemanager.event;

import com.astaro.creativemanager.CreativeManager;
import com.astaro.creativemanager.manager.InventoryManager;
import com.astaro.creativemanager.settings.Protections;
import com.astaro.creativemanager.settings.Settings;
import com.astaro.creativemanager.utils.CMUtils;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerGameModeChangeEvent;

import java.util.HashMap;
import java.util.Map;

public class PlayerGamemodeChange implements Listener {
	private final CreativeManager plugin;
	private final Settings settings;
	private final InventoryManager inventoryManager;

	public PlayerGamemodeChange(CreativeManager instance, InventoryManager manager) {
		this.plugin = instance;
		this.settings = instance.getSettings();
		this.inventoryManager = manager;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onGMChange(PlayerGameModeChangeEvent e) {
		Player p = e.getPlayer();

		// Закрываем инвентарь при смене GM (защита от дюпа через открытые окна)
		if (!p.getOpenInventory().getType().equals(InventoryType.CRAFTING)) {
			p.closeInventory();
		}

		if (e.getNewGameMode() == p.getGameMode()) return;
		if (settings.getConfig().getBoolean("stop-inventory-save")) return;

		if (!p.hasPermission("creativemanager.bypass.inventory")) {
			GameMode gmFrom = getTargetGamemode(p.getGameMode());
			GameMode gmTo = getTargetGamemode(e.getNewGameMode());

			if (!gmFrom.equals(gmTo)) {
				// Асинхронное сохранение и загрузка
				inventoryManager.saveInventoryAsync(p, gmFrom)
						.thenRun(() -> inventoryManager.loadInventoryAsync(p, gmTo));
			}

			// Установка "Креативной брони"
			handleCreativeArmor(p, e.getNewGameMode());

			// Сообщение о смене
			if (settings.isSendMessageEnabled()) {
				HashMap<String, String> placeholders = new HashMap<>();
				placeholders.put("{GAMEMODE}", e.getNewGameMode().name().toLowerCase());
				CMUtils.sendMessage(p, "inventory.change", placeholders);
			}
		}

		// Очистка эффектов
		handleEffectRemoval(p);
	}

		private void handleCreativeArmor(Player p, GameMode newGM) {
			if (newGM == GameMode.CREATIVE && settings.getProtection(Protections.ARMOR)) {
				if (!p.hasPermission("creativemanager.bypass.armor")) {
					inventoryManager.applyCreativeArmor(p);
				}
			}
		}

	private void handleEffectRemoval(Player p) {
		if (settings.getProtection(Protections.REMOVE_EFFECTS) &&
				!p.hasPermission("creativemanager.bypass.effects-cleaner")) {
			p.getActivePotionEffects().forEach(effect -> p.removePotionEffect(effect.getType()));
		}
	}

	private GameMode getTargetGamemode(GameMode current) {
		return switch (current) {
			case CREATIVE -> settings.creativeInvEnable() ? GameMode.CREATIVE : GameMode.SURVIVAL;
			case ADVENTURE -> settings.adventureInvEnable() ? GameMode.ADVENTURE : GameMode.SURVIVAL;
			case SPECTATOR -> settings.spectatorInvEnable() ? GameMode.SPECTATOR : GameMode.SURVIVAL;
			default -> GameMode.SURVIVAL;
		};
	}
}
