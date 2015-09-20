package net.dungeonrealms.listeners;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;

import com.connorlinfoot.bountifulapi.BountifulAPI;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.duel.DuelMechanics;
import net.dungeonrealms.entities.utils.EntityAPI;
import net.dungeonrealms.mechanics.PlayerManager;
import net.dungeonrealms.mechanics.WebAPI;
import net.dungeonrealms.mongo.DatabaseAPI;
import net.dungeonrealms.teleportation.TeleportAPI;
import net.md_5.bungee.api.ChatColor;

/**
 * Created by Nick on 9/17/2015.
 */
public class MainListener implements Listener {

	/**
	 * This event is used for the Database.
	 *
	 * @param event
	 * @since 1.0
	 */
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
	public void onAsyncJoin(AsyncPlayerPreLoginEvent event) {
		DatabaseAPI.getInstance().requestPlayer(event.getUniqueId());
	}

	/**
	 * This event is the main event once the player has actually entered the
	 * world! It is now safe to do things to the player e.g BountifulAPI or
	 * adding PotionEffects.. etc..
	 *
	 * @param event
	 * @since 1.0
	 */
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
	public void onJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if (WebAPI.ANNOUNCEMENTS != null && WebAPI.ANNOUNCEMENTS.size() > 0) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
			for (Map.Entry<String, Integer> e : WebAPI.ANNOUNCEMENTS.entrySet()) {
				BountifulAPI.sendTitle(player, 1, e.getValue(), 1, e.getKey().replace(":", ""),
						e.getKey().split(":")[0]);
				try {
					Thread.sleep(e.getValue());
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
			} , 5l);
		}
		TeleportAPI.addPlayerHearthstoneCD(event.getPlayer().getUniqueId(), 120);

		// Makes sure the player has hearthstone.
		PlayerManager.checkInventory(player);

	}

	/**
	 * Cancel spawning unless it's CUSTOM. So we don't have RANDOM SHEEP. We
	 * have.. CUSTOM SHEEP. RAWR SHEEP EAT ME>.. AH RUN!
	 *
	 * @param event
	 * @WARNING: THIS EVENT IS VERY INTENSIVE!
	 * @since 1.0
	 */
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
	public void onSpawn(CreatureSpawnEvent event) {
		/*
		 * if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.CUSTOM)
		 * { event.setCancelled(true); }
		 */
	}

	/**
	 * Makes sure to despawn mounts on dismount and remove from hashmap
	 *
	 * @param event
	 * @since 1.0
	 */
	@EventHandler(priority = EventPriority.MONITOR)
	public void onMountDismount(VehicleExitEvent event) {
		if (!(event.getExited() instanceof Player))
			return;
		if (EntityAPI.hasMountOut(event.getExited().getUniqueId())) {
			if (event.getVehicle().hasMetadata("type")) {
			String metaValue = event.getVehicle().getMetadata("type").get(0).asString();
			if (metaValue.equalsIgnoreCase("mount")) {
				event.getVehicle().remove();
				EntityAPI.removePlayerMountList(event.getExited().getUniqueId());
				event.getExited().sendMessage("For it's own safety, your mount has returned to the stable.");
			}
			}
		}
	}

	/**
	 * Handles player leaving the server
	 *
	 * @param event
	 * @since 1.0
	 */
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerQuit(PlayerQuitEvent event) {
		if (EntityAPI.hasPetOut(event.getPlayer().getUniqueId())) {
			net.minecraft.server.v1_8_R3.Entity playerPet = EntityAPI.getPlayerPet(event.getPlayer().getUniqueId());
			if (playerPet.isAlive()) { // Safety check
			playerPet.dead = true;
			}
			EntityAPI.removePlayerPetList(event.getPlayer().getUniqueId());
		}

		if (EntityAPI.hasMountOut(event.getPlayer().getUniqueId())) {
			net.minecraft.server.v1_8_R3.Entity playerMount = EntityAPI.getPlayerMount(event.getPlayer().getUniqueId());
			if (playerMount.isAlive()) { // Safety check
			if (playerMount.passenger != null) {
				playerMount.passenger = null;
			}
			playerMount.dead = true;
			}
			EntityAPI.removePlayerMountList(event.getPlayer().getUniqueId());
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void playerPunchPlayer(EntityDamageByEntityEvent e) {
		if (e.getEntity() instanceof Player && e.getDamager() instanceof Player) {
			Player p1 = (Player) e.getDamager();
			Player p2 = (Player) e.getEntity();
			if (DuelMechanics.isDueling(p2)) {
			// If player they're punching is their duel partner
			if (DuelMechanics.isDuelPartner(p1, p2)) {
				if (p2.getHealth() - e.getDamage() <= 0) {
					// if they're gonna die this hit end duel
					e.setCancelled(true);
					p2.setHealth(0.5);
					DuelMechanics.endDuel(p1, p2);
				}
			} else
				p1.sendMessage("That's not you're dueling partner!");
			} else {
			e.setCancelled(true);
			if (DuelMechanics.isOnCooldown(p1)) {
				p1.sendMessage(ChatColor.RED + "You must wait to send another Duel Request");
				return;
			}
			if (DuelMechanics.isPendingDuel(p1)) {
				if (DuelMechanics.isDuelPartner(p1, p2)) {
					DuelMechanics.setupDuel(p1, p2);
				} else {
					if (!DuelMechanics.isOnCooldown(p1)) {
						DuelMechanics.cancelRequestedDuel(p1);
						DuelMechanics.sendDuelRequest(p1, p2);
					} else {
						p1.sendMessage(ChatColor.RED + "You must wait to send another Duel Request");
						return;
					}

				}
			} else {
				if (DuelMechanics.isPendingDuel(p2))
					DuelMechanics.cancelRequestedDuel(p2);
				DuelMechanics.sendDuelRequest(p1, p2);
			}
			}
		}
	}
}
