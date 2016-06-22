package net.dungeonrealms.game.listeners;

import net.dungeonrealms.API;
import net.dungeonrealms.game.handlers.FriendHandler;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.mongo.EnumOperators;
import net.dungeonrealms.game.player.combat.CombatLog;
import net.dungeonrealms.game.world.entities.Entities;
import net.dungeonrealms.game.world.entities.utils.EntityAPI;
import net.dungeonrealms.game.world.realms.instance.RealmInstance;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.*;

/**
 * Class written by APOLLOSOFTWARE.IO on 6/21/2016
 */
public class RealmListener {

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
//    @EventHandler(priority = EventPriority.LOWEST)
//    public void onPlayerEnterPortal(PlayerPortalEvent event) {
//        if (event.getPlayer().getWorld().equals(Bukkit.getWorlds().get(0))) {
//            if (EntityAPI.hasPetOut(event.getPlayer().getUniqueId())) {
//                net.minecraft.server.v1_9_R2.Entity pet = Entities.PLAYER_PETS.get(event.getPlayer().getUniqueId());
//                pet.dead = true;
//                EntityAPI.removePlayerPetList(event.getPlayer().getUniqueId());
//            }
//            if (EntityAPI.hasMountOut(event.getPlayer().getUniqueId())) {
//                net.minecraft.server.v1_9_R2.Entity mount = Entities.PLAYER_MOUNTS.get(event.getPlayer().getUniqueId());
//                mount.dead = true;
//                EntityAPI.removePlayerMountList(event.getPlayer().getUniqueId());
//            }
//            if (!CombatLog.isInCombat(event.getPlayer())) {
//                if (RealmInstance.getInstance().getRealmLocation(event.getFrom(), event.getPlayer()) != null) {
//                    String locationAsString = event.getFrom().getX() + "," + (event.getFrom().getY() + 1) + "," + event.getFrom().getZ() + "," + event.getFrom().getYaw() + "," + event.getFrom().getPitch();
//                    DatabaseAPI.getInstance().update(event.getPlayer().getUniqueId(), EnumOperators.$SET, EnumData.CURRENT_LOCATION, locationAsString, true);
//                    event.setTo(RealmInstance.getInstance().getRealmLocation(event.getFrom(), event.getPlayer()));
//                    RealmInstance.getInstance().addPlayerToRealmList(event.getPlayer(), RealmInstance.getInstance().getRealmViaLocation(event.getFrom()));
//                } else {
//                    event.setCancelled(true);
//                }
//            } else {
//                event.setCancelled(true);
//                event.getPlayer().sendMessage(ChatColor.RED + "You cannot enter a realm while in combat!");
//            }
//        } else {
//            if (!DatabaseAPI.getInstance().getData(EnumData.CURRENT_LOCATION, event.getPlayer().getUniqueId()).equals("")) {
//                String[] locationString = String.valueOf(DatabaseAPI.getInstance().getData(EnumData.CURRENT_LOCATION, event.getPlayer().getUniqueId())).split(",");
//                event.setTo(new Location(Bukkit.getWorlds().get(0), Double.parseDouble(locationString[0]), Double.parseDouble(locationString[1]), Double.parseDouble(locationString[2]), Float.parseFloat(locationString[3]), Float.parseFloat(locationString[4])));
//                RealmInstance.getInstance().removePlayerFromRealmList(event.getPlayer(), RealmInstance.getInstance().getPlayersCurrentRealm(event.getPlayer()));
//            } else {
//                Location realmPortalLocation = RealmInstance.getInstance().getPortalLocationFromRealmWorld(event.getPlayer());
//                event.setTo(realmPortalLocation.clone().add(0, 2, 0));
//            }
//            event.getPlayer().setFlying(false);
//        }
//    }

}
