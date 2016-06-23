package net.dungeonrealms.game.listeners;

import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.mongo.EnumOperators;
import net.dungeonrealms.game.player.combat.CombatLog;
import net.dungeonrealms.game.world.entities.Entities;
import net.dungeonrealms.game.world.entities.utils.EntityAPI;
import net.dungeonrealms.game.world.realms.Realms;
import net.dungeonrealms.game.world.realms.instance.obj.RealmStatus;
import net.dungeonrealms.game.world.realms.instance.obj.RealmToken;
import net.minecraft.server.v1_9_R2.Entity;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPortalEvent;

/**
 * Class written by APOLLOSOFTWARE.IO on 6/21/2016
 */
public class RealmListener implements Listener {

    //    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
//    public void changeWorld(PlayerChangedWorldEvent event) {
//    }
//
//    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
//    public void playerQuit(PlayerQuitEvent event) {
//        removePlayerRealm(event.getPlayer(), true);
//        if (PENDING_REALMS.contains(event.getPlayer())) {
//            PENDING_REALMS.remove(event.getPlayer());
//        }
//    }
//
//    @EventHandler(priority = EventPriority.LOWEST)
//    public void playerDropItemInRealm(PlayerDropItemEvent event) {
//        if (API.getGamePlayer(event.getPlayer()) == null) return;
//        if (!API.getGamePlayer(event.getPlayer()).isInRealm()) return;
//        event.setCancelled(true);
//        event.getPlayer().sendMessage(ChatColor.RED + "You cannot drop items in Realms. If you wish give them to another player, please trade them.");
//    }
//
//    @EventHandler(priority = EventPriority.LOWEST)
//    public void cancelPlayersBlockOpenInRealm(PlayerInteractEvent event) {
//        if (API.getGamePlayer(event.getPlayer()) == null) return;
//        if (!API.getGamePlayer(event.getPlayer()).isInRealm()) return;
//        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
//        Block block = event.getClickedBlock();
//        if (block == null) return;
//        if (event.getPlayer().isOp() || event.getPlayer().getGameMode() == GameMode.CREATIVE) return;
//        Material mat = block.getType();
//        if (mat != Material.CHEST) return;
//        event.setCancelled(true);
//        event.getPlayer().sendMessage(ChatColor.RED + "This block shouldn't be in a Realm... How'd it get here?");
//    }
//
//    /**
//     * Handles a player breaking a block
//     * within a realm.
//     *
//     * @param event
//     * @since 1.0
//     */
//    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
//    public void onPlayerBreakBlockInRealm(BlockBreakEvent event) {
//        if (event.getPlayer().getWorld().equals(Bukkit.getWorlds().get(0))) return;
//        if (event.getPlayer().getWorld().getName().contains("DUNGEON")) return;
//        if (event.getPlayer().getWorld().getName().contains("DUEL")) return;
//        if (event.getBlock().getType() == Material.PORTAL) {
//            event.setCancelled(true);
//            event.getPlayer().sendMessage(ChatColor.RED + "You cannot break Portal blocks!");
//        }
//        if (event.getPlayer().isOp() || event.getPlayer().getGameMode() == GameMode.CREATIVE) return;
//        if (!FriendHandler.getInstance().areFriends(event.getPlayer(), RealmInstance.getInstance().getPlayersCurrentRealm(event.getPlayer()).getRealmOwner().getUniqueId())) {
//            event.setCancelled(true);
//            event.setExpToDrop(0);
//            event.getPlayer().sendMessage(ChatColor.RED + "You cannot place blocks in this realm, please ask the owner to add you to their friends list!");
//        }
//        /*if (!Instance.getInstance().getPlayersCurrentRealm(event.getPlayer()).getRealmBuilders().contains(event.getPlayer())) {
//            event.setCancelled(true);
//            event.setExpToDrop(0);
//            event.getPlayer().sendMessage(net.md_5.bungee.api.ChatColor.RED + "You cannot break blocks in this realm, please ask the owner to add you to the builders list!");
//        }*/
//    }
//
//    /**
//     * Handles a player placing a block
//     * within a realm.
//     *
//     * @param event
//     * @since 1.0
//     */
//    @EventHandler(priority = EventPriority.NORMAL)
//    public void onPlayerPlaceBlockInRealm(BlockPlaceEvent event) {
//        if (event.getPlayer().getWorld().equals(Bukkit.getWorlds().get(0))) return;
//        if (event.getPlayer().getWorld().getName().contains("DUNGEON")) return;
//        if (event.getPlayer().isOp() || event.getPlayer().getGameMode() == GameMode.CREATIVE) return;
//        if (event.getBlockPlaced().getType() == Material.PORTAL) {
//            event.setCancelled(true);
//            event.getPlayer().sendMessage(ChatColor.RED + "You cannot place Portal blocks!");
//            return;
//        }
//        if (event.getBlockAgainst().getType() == Material.PORTAL) {
//            event.setCancelled(true);
//            event.getPlayer().sendMessage(ChatColor.RED + "You cannot place blocks on-top of Portal blocks!");
//            return;
//        }
//        if (!FriendHandler.getInstance().areFriends(event.getPlayer(), RealmInstance.getInstance().getPlayersCurrentRealm(event.getPlayer()).getRealmOwner().getUniqueId())) {
//            event.setCancelled(true);
//            event.getPlayer().sendMessage(ChatColor.RED + "You cannot place blocks in this realm, please ask the owner to add you to their friends list!");
//        }
//    }
//
//
//    /**
//     * Handles a player entering a portal,
//     * teleports them to wherever they should
//     * be, or cancels it if they're in combat
//     * etc.
//     *
//     * @param event
//     * @since 1.0
//     */


    @EventHandler
    public void onPortalDestory(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_BLOCK) return;
        if (!event.getPlayer().getWorld().equals(Bukkit.getWorlds().get(0))) return;

        RealmToken realm = Realms.getInstance().getRealm(event.getClickedBlock().getLocation());

        if (realm != null && realm.getOwner().equals(event.getPlayer().getUniqueId()))
            Realms.getInstance().closeRealmPortal(realm.getOwner());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerEnterPortal(PlayerPortalEvent event) {
        if (event.getPlayer().getWorld().equals(Bukkit.getWorlds().get(0))) {
            if (EntityAPI.hasPetOut(event.getPlayer().getUniqueId())) {
                Entity pet = Entities.PLAYER_PETS.get(event.getPlayer().getUniqueId());
                pet.dead = true;
                EntityAPI.removePlayerPetList(event.getPlayer().getUniqueId());
            }
            if (EntityAPI.hasMountOut(event.getPlayer().getUniqueId())) {
                Entity mount = Entities.PLAYER_MOUNTS.get(event.getPlayer().getUniqueId());
                mount.dead = true;
                EntityAPI.removePlayerMountList(event.getPlayer().getUniqueId());
            }

            if (!CombatLog.isInCombat(event.getPlayer())) {
                RealmToken realm = Realms.getInstance().getRealm(event.getFrom());

                if (realm == null) return;

                if (!Realms.getInstance().isRealmLoaded(realm.getOwner()))
                    return;

                if (realm.getStatus() != RealmStatus.OPENED) return;


                // SAVES THEIR LOCATION
                String locationAsString = event.getFrom().getX() + "," + (event.getFrom().getY() + 1) + "," + event.getFrom().getZ() + "," + event.getFrom().getYaw() + "," + event.getFrom().getPitch();
                DatabaseAPI.getInstance().update(event.getPlayer().getUniqueId(), EnumOperators.$SET, EnumData.CURRENT_LOCATION, locationAsString, true);

                event.setTo(Realms.getInstance().getRealmWorld(realm.getOwner()).getSpawnLocation());
                realm.getPlayersInRealm().add(event.getPlayer().getUniqueId());
            } else {
                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.RED + "You cannot enter a realm while in combat!");
            }
        } else if (Realms.getInstance().getRealm(event.getPlayer().getLocation().getWorld()) != null) {
            RealmToken realm = Realms.getInstance().getRealm(event.getPlayer().getLocation().getWorld());
            event.setTo(realm.getPortalLocation().clone().add(0, 1, 0));
            realm.getPlayersInRealm().remove(event.getPlayer().getUniqueId());
        }
    }
}
