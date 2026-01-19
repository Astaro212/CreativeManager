package com.astaro.creativemanager.event;

import com.astaro.creativemanager.CreativeManager;
import com.astaro.creativemanager.settings.Protections;
import com.astaro.creativemanager.utils.CMUtils;
import org.bukkit.GameMode;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.projectiles.ProjectileSource;

/**
 * Player hit event listener.
 * Updated for 1.21.4 (2026) - No static access.
 */
public class PlayerHitEvent implements Listener {
	private final boolean enableProjectile;
	private final CreativeManager plugin;

	public PlayerHitEvent(boolean enableProjectile, CreativeManager plugin) {
		this.enableProjectile = enableProjectile;
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onEntityHit(EntityDamageByEntityEvent e) {
		if (!(e.getDamager() instanceof Player attacker)) return;
		if (attacker.getGameMode() != GameMode.CREATIVE) return;

		if (e.getEntity() instanceof ArmorStand) return;

		if (e.getEntity() instanceof Player) {
			if (plugin.getSettings().getProtection(Protections.PVP) &&
					!attacker.hasPermission("creativemanager.bypass.pvp")) {

				sendProtectionMessage(attacker, "permission.hit.player");
				e.setCancelled(true);
			}
		} else {
			if (plugin.getSettings().getProtection(Protections.PVE) &&
					!attacker.hasPermission("creativemanager.bypass.pve")) {

				sendProtectionMessage(attacker, "permission.hit.monster");
				e.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onProjectileHit(ProjectileHitEvent e) {
		if (!enableProjectile || e.getHitEntity() == null) return;
		if (e.getHitEntity() instanceof ArmorStand) return;

		ProjectileSource source = e.getEntity().getShooter();
		if (!(source instanceof Player attacker)) return;
		if (attacker.getGameMode() != GameMode.CREATIVE) return;

		if (e.getHitEntity() instanceof Player) {
			if (plugin.getSettings().getProtection(Protections.PVP) &&
					!attacker.hasPermission("creativemanager.bypass.pvp")) {

				sendProtectionMessage(attacker, "permission.hit.player");
				e.setCancelled(true);
				e.getEntity().remove();
			}
		} else {
			if (plugin.getSettings().getProtection(Protections.PVE) &&
					!attacker.hasPermission("creativemanager.bypass.pve")) {

				sendProtectionMessage(attacker, "permission.hit.monster");
				e.setCancelled(true);
				e.getEntity().remove();
			}
		}
	}


	private void sendProtectionMessage(Player player, String messageKey) {
		if (plugin.getSettings().getConfig().getBoolean("send-player-messages")) {
			CMUtils.sendMessage(player, messageKey);
		}
	}
}
