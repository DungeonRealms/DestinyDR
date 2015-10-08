package net.dungeonrealms.listeners;

import com.connorlinfoot.bountifulapi.BountifulAPI;
import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.chat.Chat;
import net.dungeonrealms.donate.DonationEffects;
import net.dungeonrealms.duel.DuelMechanics;
import net.dungeonrealms.duel.DuelWager;
import net.dungeonrealms.entities.utils.EntityAPI;
import net.dungeonrealms.mechanics.WebAPI;
import net.dungeonrealms.mongo.DatabaseAPI;
import net.dungeonrealms.rank.Subscription;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Map;

/**
 * Created by Nick on 9/17/2015.
 */
public class MainListener implements Listener {

    /**
     * Monitors and checks the players language.
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onChat(AsyncPlayerChatEvent event) {
        Chat.getInstance().doChat(event);
    }

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
        Subscription.getInstance().handleJoin(event);
        if (WebAPI.ANNOUNCEMENTS != null && WebAPI.ANNOUNCEMENTS.size() > 0) {
            Bukkit.getScheduler().scheduleAsyncDelayedTask(DungeonRealms.getInstance(), () -> {
                for (Map.Entry<String, Integer> e : WebAPI.ANNOUNCEMENTS.entrySet()) {
                    BountifulAPI.sendTitle(player, 1, e.getValue(), 1, ChatColor.translateAlternateColorCodes('&', e.getKey().split("@")[0]),
                            ChatColor.translateAlternateColorCodes('&', e.getKey().split("@")[1].split(",")[0]));
                }
            }, 5l);
        }
        for (String s : WebAPI.JOIN_INFORMATION) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', s));
        }
        player.getInventory().clear();
        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> API.handleLogin(player.getUniqueId()), 20L);
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
        Player player = event.getPlayer();
        if (EntityAPI.hasPetOut(player.getUniqueId())) {
            net.minecraft.server.v1_8_R3.Entity playerPet = EntityAPI.getPlayerPet(player.getUniqueId());
            if (playerPet.isAlive()) { // Safety check
                playerPet.dead = true;
            }
            EntityAPI.removePlayerPetList(player.getUniqueId());
        }

        if (EntityAPI.hasMountOut(player.getUniqueId())) {
            net.minecraft.server.v1_8_R3.Entity playerMount = EntityAPI.getPlayerMount(player.getUniqueId());
            if (playerMount.isAlive()) { // Safety check
                if (playerMount.passenger != null) {
                    playerMount.passenger = null;
                }
                playerMount.dead = true;
            }
            EntityAPI.removePlayerMountList(player.getUniqueId());
        }

        // Player leaves while in duel
        if (DuelMechanics.isDueling(player.getUniqueId())) {
            DuelMechanics.getWager(player.getUniqueId()).handleLogOut(player.getUniqueId());
        }
        API.handleLogout(player.getUniqueId());
    }

    /**
     * Handling Duels. When a player punches another player.
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void playerPunchPlayer(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player && event.getDamager() instanceof Player)) return;
        Player p1 = (Player) event.getDamager();
        Player p2 = (Player) event.getEntity();
        if (API.isInSafeRegion(p1.getUniqueId()) && API.isInSafeRegion(p2.getUniqueId())) {
            if (DuelMechanics.isDueling(p2.getUniqueId())) {
                // If player they're punching is their duel partner
                if (DuelMechanics.isDuelPartner(p1.getUniqueId(), p2.getUniqueId())) {
                    if (p2.getHealth() - event.getDamage() <= 0) {
                        // if they're gonna die this hit end duel
                        DuelWager wager = DuelMechanics.getWager(p1.getUniqueId());
                        if (wager != null) {
                            event.setCancelled(true);
                            p2.setHealth(0.5);
                            wager.endDuel(p1, p2);
                        }
                    }
                } else
                    p1.sendMessage("That's not you're dueling partner!");
            } else {
                event.setCancelled(true);
                if (DuelMechanics.isOnCooldown(p1.getUniqueId())) {
                    p1.sendMessage(ChatColor.RED + "You must wait to send another Duel Request");
                    return;
                }
                if (DuelMechanics.isPendingDuel(p1.getUniqueId())) {
                    if (DuelMechanics.isPendingDuelPartner(p1.getUniqueId(), p2.getUniqueId())) {
                        DuelMechanics.launchWager(p1, p2);
                        // Remove from pending
                        DuelMechanics.cancelRequestedDuel(p1.getUniqueId());
                    } else {
                        if (!DuelMechanics.isOnCooldown(p1.getUniqueId())) {
                            DuelMechanics.cancelRequestedDuel(p1.getUniqueId());
                            DuelMechanics.sendDuelRequest(p1.getUniqueId(), p2.getUniqueId());
                        } else {
                            p1.sendMessage(ChatColor.RED + "You must wait to send another Duel Request");
                        }

                    }
                } else {
                    if (DuelMechanics.isPendingDuel(p2.getUniqueId()))
                        DuelMechanics.cancelRequestedDuel(p2.getUniqueId());
                    DuelMechanics.sendDuelRequest(p1.getUniqueId(), p2.getUniqueId());
                }
            }
        }
    }

    /**
     * Checks player movement, adds a trail of gold blocks if they have
     * the perk and the situation is correct.
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!(DonationEffects.playerGoldBlockTrail.contains(event.getPlayer()))) return;
        if (!(player.getWorld().getName().equalsIgnoreCase(Bukkit.getWorlds().get(0).getName()))) return;
        if (player.getLocation().getBlock().getType() != Material.AIR) return;
        Material material = player.getLocation().subtract(0, 1, 0).getBlock().getType();
        if (material == Material.DIRT || material == Material.GRASS || material == Material.STONE || material == Material.COBBLESTONE || material == Material.GRAVEL
                || material == Material.LOG || material == Material.LEAVES || material == Material.SMOOTH_BRICK || material == Material.BEDROCK || material == Material.GLASS
                || material == Material.SANDSTONE || material == Material.SAND || material == Material.BOOKSHELF || material == Material.MOSSY_COBBLESTONE || material == Material.OBSIDIAN
                || material == Material.SNOW_BLOCK || material == Material.ICE || material == Material.CLAY || material == Material.STAINED_CLAY || material == Material.WOOL) {
            DonationEffects.playerGoldBlockTrailLocation.put(player.getLocation().subtract(0, 1, 0).getBlock().getLocation(), material);
            player.getLocation().subtract(0, 1, 0).getBlock().setType(Material.GOLD_BLOCK);
            player.getLocation().subtract(0, 1, 0).getBlock().setMetadata("time", new FixedMetadataValue(DungeonRealms.getInstance(), 10));
        }
    }
}
