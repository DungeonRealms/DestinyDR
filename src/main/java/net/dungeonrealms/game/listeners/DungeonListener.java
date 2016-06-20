package net.dungeonrealms.game.listeners;

import net.dungeonrealms.API;
import net.dungeonrealms.game.events.PlayerEnterRegionEvent;
import net.dungeonrealms.game.mechanics.DungeonManager;
import net.dungeonrealms.game.mechanics.ItemManager;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.world.entities.Entities;
import net.dungeonrealms.game.world.entities.utils.EntityAPI;
import net.dungeonrealms.game.world.party.Affair;
import net.dungeonrealms.game.world.teleportation.Teleportation;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * Created by Kieran Quigley (Proxying) on 15-Jun-16.
 */
public class DungeonListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void T1Death(EntityDeathEvent event) {
        if (!event.getEntity().getWorld().getName().contains("DUNGEON")) return;
        if (event.getEntity() instanceof Player) return;
        if (DungeonManager.getInstance().getDungeon(event.getEntity().getWorld()) == null) return;
        if (DungeonManager.getInstance().getDungeon(event.getEntity().getWorld()).getType() != DungeonManager.DungeonType.BANDIT_TROVE)
            return;
        if (event.getEntity().hasMetadata("elite")) {
            if (event.getEntity().hasMetadata("customname")) {
                String name = ChatColor.stripColor(event.getEntity().getMetadata("customname").get(0).asString());
                if (name.equalsIgnoreCase("Mad Bandit Pyromancer")) {
                    ItemStack key = ItemManager.createItem(Material.GLOWSTONE_DUST, ChatColor.GREEN + "Magical Dust", new String[]{
                            ChatColor.GRAY.toString() + ChatColor.ITALIC.toString() + "A strange substance that animates objects.", ChatColor.RED + "Dungeon Item"});
                    if (event.getEntity().getKiller() != null) {
                        event.getEntity().getKiller().getInventory().addItem(key);
                    } else {
                        event.getEntity().getWorld().dropItemNaturally(event.getEntity().getLocation().add(0, 1, 0), key);
                    }
                    for (Player player : API.getNearbyPlayers(event.getEntity().getLocation(), 30)) {
                        player.sendMessage(ChatColor.RED + event.getEntity().getMetadata("customname").get(0).asString() + ChatColor.WHITE + ": " + ChatColor.WHITE + "Talk about going out with a...blast.");
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void T3Death(EntityDeathEvent event) {
        if (!event.getEntity().getWorld().getName().contains("DUNGEON")) return;
        if (event.getEntity() instanceof Player) return;
        if (DungeonManager.getInstance().getDungeon(event.getEntity().getWorld()) == null) return;
        if (DungeonManager.getInstance().getDungeon(event.getEntity().getWorld()).getType() != DungeonManager.DungeonType.VARENGLADE)
            return;
        DungeonManager.DungeonObject dungeonObject = DungeonManager.getInstance().getDungeon(event.getEntity().getWorld());
        if (dungeonObject.keysDropped <= 10) {
            if (new Random().nextInt(20) <= 14) {
                ItemStack key = ItemManager.createItem(Material.TRIPWIRE_HOOK, ChatColor.LIGHT_PURPLE + "A mystical key", new String[]{
                        ChatColor.GRAY.toString() + ChatColor.ITALIC.toString() + "One of four mysterious keys.", ChatColor.RED + "Dungeon Item"});
                if (event.getEntity().getKiller() != null) {
                    event.getEntity().getKiller().getInventory().addItem(key);
                } else {
                    event.getEntity().getWorld().dropItemNaturally(new Location(event.getEntity().getWorld(), 36, 54, -4), key);
                }
                dungeonObject.keysDropped = dungeonObject.keysDropped + 1;
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void playerAttemptDungeonExit(PlayerEnterRegionEvent event) {
        if (!event.getPlayer().getWorld().getName().contains("DUNGEON")) return;
        if (event.getRegion().toLowerCase().startsWith("exit_instance")) {
            Player player = event.getPlayer();
            if (player.isOnline() && API.getGamePlayer(player) != null) {
                if (API.getGamePlayer(player).isInDungeon()) {
                    if (!DatabaseAPI.getInstance().getData(EnumData.CURRENT_LOCATION, player.getUniqueId()).equals("")) {
                        String[] locationString = String.valueOf(DatabaseAPI.getInstance().getData(EnumData.CURRENT_LOCATION, player.getUniqueId())).split(",");
                        player.teleport(new Location(Bukkit.getWorlds().get(0), Double.parseDouble(locationString[0]), Double.parseDouble(locationString[1]), Double.parseDouble(locationString[2]), Float.parseFloat(locationString[3]), Float.parseFloat(locationString[4])));
                    } else {
                        player.teleport(Teleportation.Cyrennica);
                    }
                }
            }
            if (Affair.getInstance().isInParty(player)) {
                List<Player> toSend = new ArrayList<>();
                Affair.AffairO party = Affair.getInstance().getParty(player).get();
                toSend.addAll(party.getMembers());
                toSend.add(party.getOwner());
                for (Player player1 : toSend) {
                    if (player.getName().equals(player1.getName())) {
                        continue;
                    }
                    player1.sendMessage(ChatColor.LIGHT_PURPLE.toString() + "<" + ChatColor.BOLD + "P" + ChatColor.LIGHT_PURPLE + ">" + ChatColor.GRAY + " "
                            + player.getName() + " has " + ChatColor.RED + ChatColor.UNDERLINE + "left" + ChatColor.GRAY + " the dungeon.");
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void playerAttemptDungeonEnter(PlayerEnterRegionEvent event) {
        if (!event.getPlayer().getWorld().equals(Bukkit.getWorlds().get(0))) return;
        if (event.getRegion().toLowerCase().startsWith("instance_")) {
            Player player = event.getPlayer();
            if (DungeonManager.getInstance().getPlayers_Entering_Dungeon().containsKey(player.getName())) {
                player.sendMessage(ChatColor.GRAY + "You currently have a Dungeon cooldown timer, please wait " + ChatColor.RED + ChatColor.UNDERLINE
                        + DungeonManager.getInstance().getPlayers_Entering_Dungeon().get(player.getName()) + "s" + ChatColor.RESET + ChatColor.RED + " before entering.");
                return;
            }
            if (EntityAPI.hasPetOut(event.getPlayer().getUniqueId())) {
                net.minecraft.server.v1_9_R2.Entity pet = Entities.PLAYER_PETS.get(event.getPlayer().getUniqueId());
                pet.dead = true;
                EntityAPI.removePlayerPetList(event.getPlayer().getUniqueId());
            }
            if (EntityAPI.hasMountOut(event.getPlayer().getUniqueId())) {
                net.minecraft.server.v1_9_R2.Entity mount = Entities.PLAYER_MOUNTS.get(event.getPlayer().getUniqueId());
                mount.dead = true;
                EntityAPI.removePlayerMountList(event.getPlayer().getUniqueId());
            }
            if (API.getGamePlayer(player) == null || API.getGamePlayer(player).getLevel() < 5) {
                player.sendMessage(ChatColor.RED + "You need to be " + ChatColor.UNDERLINE + "at least" + ChatColor.RED + " level 5 to enter a dungeon.");
                return;
            }
            String dungeonName = event.getRegion().substring(event.getRegion().indexOf("_") + 1, event.getRegion().length());
            if (dungeonName.equalsIgnoreCase("dodungeon")) {
                dungeonName = "DODungeon";
            }
            if (dungeonName.equalsIgnoreCase("t1dungeon")) {
                dungeonName = "T1Dungeon";
            }

            boolean isPartyInInstance = false;

            if (Affair.getInstance().isInParty(player)) {
                Affair.AffairO party = Affair.getInstance().getParty(player).get();
                List<Player> partyMembers = new ArrayList<>();
                partyMembers.add(party.getOwner());
                partyMembers.addAll(party.getMembers());
                DungeonManager.DungeonObject partyDungeon = null;
                for (Player player1 : partyMembers) {
                    if (player1.getWorld().getName().contains("DUNGEON")) {
                        partyDungeon = DungeonManager.getInstance().getDungeon(player1.getWorld());
                        isPartyInInstance = true;
                        break;
                    }
                }
                if (isPartyInInstance) {
                    if (partyDungeon != null) {
                        switch (partyDungeon.getType()) {
                            case BANDIT_TROVE:
                                if (!dungeonName.equalsIgnoreCase("T1Dungeon")) {
                                    player.sendMessage(ChatColor.RED + "Your party is already inside a " + ChatColor.UNDERLINE + "different" + ChatColor.RED + " instanced dungeon.");
                                    player.sendMessage(ChatColor.GRAY + "You'll need to either leave your current party or wait for them to finish their run.");
                                    return;
                                }
                                break;
                            case VARENGLADE:
                                if (!dungeonName.equalsIgnoreCase("DODungeon")) {
                                    player.sendMessage(ChatColor.RED + "Your party is already inside a " + ChatColor.UNDERLINE + "different" + ChatColor.RED + " instanced dungeon.");
                                    player.sendMessage(ChatColor.GRAY + "You'll need to either leave your current party or wait for them to finish their run.");
                                    return;
                                }
                                break;
                            case THE_INFERNAL_ABYSS:
                                if (!dungeonName.equalsIgnoreCase("fireydungeon")) {
                                    player.sendMessage(ChatColor.RED + "Your party is already inside a " + ChatColor.UNDERLINE + "different" + ChatColor.RED + " instanced dungeon.");
                                    player.sendMessage(ChatColor.GRAY + "You'll need to either leave your current party or wait for them to finish their run.");
                                    return;
                                }
                                break;
                            default:
                                return;
                        }
                        if (!partyDungeon.getPlayerList().containsKey(player)) {
                            player.sendMessage(ChatColor.RED + "This Dungeon was created before you joined the party, you cannot join this session.");
                            return;
                        }
                        boolean hasTeleported = false;
                        DungeonManager.getInstance().getPlayers_Entering_Dungeon().put(player.getName(), 600);
                        for (Player player1 : partyMembers) {
                            if (player.getName().equals(player1.getName())) {
                                continue;
                            }
                            if (player1.getWorld().getName().contains("DUNGEON")) {
                                player1.sendMessage(ChatColor.LIGHT_PURPLE + "<" + ChatColor.BOLD + "P" + ChatColor.LIGHT_PURPLE + ">" + ChatColor.GRAY
                                        + " " + player.getName() + " has " + ChatColor.GREEN + ChatColor.UNDERLINE + "joined" + ChatColor.GRAY
                                        + " the dungeon.");
                                if (!hasTeleported) {
                                    player.teleport(player1.getLocation());
                                    player.setFallDistance(0F);
                                    hasTeleported = true;
                                }
                            }
                        }
                        return;
                    }
                    return;
                }
                if (!(Affair.getInstance().getParty(player).get()).getOwner().getName().equals(player.getName())) {
                    player.sendMessage(ChatColor.RED + "You are " + ChatColor.UNDERLINE + "NOT" + ChatColor.RED + " the party leader.");
                    player.sendMessage(ChatColor.GRAY + "Only the party leader can start a new dungeon instance.");
                    return;
                }
            } else {
                Affair.getInstance().createParty(player);
            }

            if (!DungeonManager.getInstance().canCreateInstance()) {
                player.sendMessage(ChatColor.RED + "All available dungeon instances are " + ChatColor.UNDERLINE + "full" + ChatColor.RED
                        + " on this shard.");
                player.sendMessage(ChatColor.GRAY + "Use /shard to try a different one.");
                return;
            }

            Map<Player, Boolean> partyList = new HashMap<>();
            for (Player player1 : Affair.getInstance().getParty(player).get().getMembers()) {
                if (player1.getLocation().distanceSquared(player.getLocation()) <= 200) {
                    partyList.put(player1, true);
                } else {
                    partyList.put(player1, false);
                }
            }
            partyList.put(player, true);
            DungeonManager.DungeonType dungeonType;
            if (dungeonName.equalsIgnoreCase("T1Dungeon")) {
                dungeonType = DungeonManager.DungeonType.BANDIT_TROVE;
            } else if (dungeonName.equalsIgnoreCase("DODungeon")) {
                dungeonType = DungeonManager.DungeonType.VARENGLADE;
            } else if (dungeonName.equalsIgnoreCase("fireydungeon")) {
                dungeonType = DungeonManager.DungeonType.THE_INFERNAL_ABYSS;
            } else {
                dungeonType = null;
            }
            if (dungeonType == null) return;
            DungeonManager.getInstance().getPlayers_Entering_Dungeon().put(player.getName(), 600);
            DungeonManager.getInstance().createNewInstance(dungeonType, partyList, dungeonName);
            player.sendMessage(ChatColor.GRAY + "Loading Instance: '" + ChatColor.UNDERLINE + dungeonType.name().replaceAll("_", " ") + ChatColor.GRAY
                    + "' -- Please wait...");
        }
    }
}
