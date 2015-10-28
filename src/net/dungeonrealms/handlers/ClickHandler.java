package net.dungeonrealms.handlers;

import com.minebone.anvilapi.core.AnvilApi;
import com.minebone.anvilapi.nms.anvil.AnvilGUIInterface;
import com.minebone.anvilapi.nms.anvil.AnvilSlot;
import net.dungeonrealms.banks.BankMechanics;
import net.dungeonrealms.combat.CombatLog;
import net.dungeonrealms.donate.DonationEffects;
import net.dungeonrealms.entities.utils.EntityAPI;
import net.dungeonrealms.entities.utils.MountUtils;
import net.dungeonrealms.entities.utils.PetUtils;
import net.dungeonrealms.guild.Guild;
import net.dungeonrealms.inventory.PlayerMenus;
import net.dungeonrealms.mechanics.ParticleAPI;
import net.dungeonrealms.mongo.DatabaseAPI;
import net.dungeonrealms.mongo.EnumData;
import net.dungeonrealms.mongo.EnumOperators;
import net.dungeonrealms.network.NetworkAPI;
import net.dungeonrealms.teleportation.TeleportAPI;
import net.dungeonrealms.teleportation.Teleportation;
import net.minecraft.server.v1_8_R3.Entity;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nick on 10/2/2015.
 */
public class ClickHandler {

    static ClickHandler instance = null;

    public static ClickHandler getInstance() {
        if (instance == null) {
            instance = new ClickHandler();
        }
        return instance;
    }

    public void doClick(InventoryClickEvent event) {
        String name = event.getInventory().getName();
        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();
        if (slot == -999) return;

        /*
        Animal Tamer NPC
         */
        if (name.equals("Mount Vendor")) {
            event.setCancelled(true);
            if (event.getCurrentItem().getType() != Material.AIR) {
                net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(event.getCurrentItem());
                if (nmsStack == null) return;
                if (nmsStack.getTag() == null) return;
                if (slot > 27) return;
                List<String> playerMounts = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.MOUNTS, player.getUniqueId());
                if (playerMounts.contains(nmsStack.getTag().getString("mountType"))) {
                    player.sendMessage(ChatColor.RED + "You already own this mount!");
                    return;
                } else {
                    if (BankMechanics.getInstance().takeGemsFromInventory(nmsStack.getTag().getInt("mountCost"), player)) {
                        DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$PUSH, "collectibles.mounts", nmsStack.getTag().getString("mountType").toUpperCase(), true);
                        player.sendMessage(ChatColor.GREEN + "You have purchased the " + nmsStack.getTag().getString("mountType") + " mount!");
                        return;
                    } else {
                        player.sendMessage(ChatColor.RED + "You cannot afford this mount!");
                        return;
                    }
                }
            }
            return;
        }

        /*
        Friend Management
         */
        if (name.equals("Friend Management")) {
            event.setCancelled(true);
            if (slot >= 44) return;
            if (slot == 0) {
                AnvilGUIInterface addFriendGUI = AnvilApi.createNewGUI(player, anvilClick -> {
                    switch (anvilClick.getSlot()) {
                        case OUTPUT:
                            anvilClick.setWillClose(true);
                            anvilClick.setWillDestroy(true);
                            if (Bukkit.getPlayer(anvilClick.getName()) != null) {
                                FriendHandler.getInstance().sendRequest(player, Bukkit.getPlayer(anvilClick.getName()));
                            } else {
                                player.sendMessage(ChatColor.RED + "Error, that player doesn't exist!");
                            }
                            break;
                    }
                });
                addFriendGUI.setSlot(AnvilSlot.INPUT_LEFT, PlayerMenus.editItem(new ItemStack(Material.SKULL_ITEM, 1, (short) 3), "Type name here..", new String[]{}));
                addFriendGUI.open();
                return;
            }
            FriendHandler.getInstance().addOrRemove(player, event.getClick(), event.getCurrentItem());
            //other things.
        }

        /*
        Mail Below
         */
        if (name.equals("Mailbox")) {
            event.setCancelled(true);
            if (event.getCurrentItem() != null) {
                ItemStack clickedItem = event.getCurrentItem();
                MailHandler.getInstance().giveItemToPlayer(clickedItem, player);
            }
            return;
        }

        /*
        Pets Below
         */
        if (name.equalsIgnoreCase("Pet Selection")) {
            event.setCancelled(true);
            if (event.getCurrentItem().getType() == Material.BARRIER) {
                PlayerMenus.openPlayerProfileMenu(player);
                return;
            }
            if (event.getCurrentItem().getType() == Material.LEASH) {
                if (EntityAPI.hasPetOut(player.getUniqueId())) {
                    Entity entity = EntityAPI.getPlayerPet(player.getUniqueId());
                    if (entity.isAlive()) {
                        entity.getBukkitEntity().remove();
                    }
                    if (DonationEffects.getInstance().ENTITY_PARTICLE_EFFECTS.containsKey(entity)) {
                        DonationEffects.getInstance().ENTITY_PARTICLE_EFFECTS.remove(entity);
                    }
                    EntityAPI.removePlayerPetList(player.getUniqueId());
                    player.sendMessage(ChatColor.WHITE + "[" + ChatColor.GOLD.toString() + ChatColor.BOLD + "DONATE" + ChatColor.WHITE + "]" + ChatColor.AQUA + " Your Pet has returned home!");
                } else {
                    player.sendMessage(ChatColor.WHITE + "[" + ChatColor.GOLD.toString() + ChatColor.BOLD + "DONATE" + ChatColor.WHITE + "]" + ChatColor.AQUA + " You currently do not have a pet in the world!");
                }
                return;
            }
            if (event.getCurrentItem().getType() != Material.AIR && event.getCurrentItem().getType() != Material.BARRIER && event.getCurrentItem().getType() != Material.LEASH) {
                if (EntityAPI.hasPetOut(player.getUniqueId())) {
                    Entity entity = EntityAPI.getPlayerPet(player.getUniqueId());
                    if (entity.isAlive()) {
                        entity.getBukkitEntity().remove();
                    }
                    if (DonationEffects.getInstance().ENTITY_PARTICLE_EFFECTS.containsKey(entity)) {
                        DonationEffects.getInstance().ENTITY_PARTICLE_EFFECTS.remove(entity);
                    }
                    EntityAPI.removePlayerPetList(player.getUniqueId());
                }
                net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(event.getCurrentItem());
                if (nmsStack.getTag() == null || nmsStack.getTag().getString("petType") == null) {
                    player.sendMessage("Uh oh... Something went wrong with your pet! Please inform a staff member! [NBTTag]");
                    player.closeInventory();
                    return;
                }
                String particleType = "";
                if (nmsStack.getTag().getString("particleType") != null) {
                    particleType = nmsStack.getTag().getString("particleType");
                }
                PetUtils.spawnPet(player.getUniqueId(), nmsStack.getTag().getString("petType"), particleType);
            }
        }

        /*
        Mounts Below
         */
        if (name.equalsIgnoreCase("Mount Selection")) {
            event.setCancelled(true);
            if (event.getCurrentItem().getType() == Material.BARRIER) {
                PlayerMenus.openPlayerProfileMenu(player);
                return;
            }
            if (event.getCurrentItem().getType() == Material.LEASH) {
                if (EntityAPI.hasMountOut(player.getUniqueId())) {
                    Entity entity = EntityAPI.getPlayerMount(player.getUniqueId());
                    if (entity.isAlive()) {
                        entity.getBukkitEntity().remove();
                    }
                    if (DonationEffects.getInstance().ENTITY_PARTICLE_EFFECTS.containsKey(entity)) {
                        DonationEffects.getInstance().ENTITY_PARTICLE_EFFECTS.remove(entity);
                    }
                    EntityAPI.removePlayerMountList(player.getUniqueId());
                    player.sendMessage(ChatColor.WHITE + "[" + ChatColor.GOLD.toString() + ChatColor.BOLD + "DONATE" + ChatColor.WHITE + "]" + ChatColor.AQUA + " Your Mount has returned home!");
                } else {
                    player.sendMessage(ChatColor.WHITE + "[" + ChatColor.GOLD.toString() + ChatColor.BOLD + "DONATE" + ChatColor.WHITE + "]" + ChatColor.AQUA + " You currently do not have a mount in the world!");
                }
                return;
            }
            if (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR && event.getCurrentItem().getType() != Material.BARRIER && event.getCurrentItem().getType() != Material.LEASH) {
                if (EntityAPI.hasMountOut(player.getUniqueId())) {
                    Entity entity = EntityAPI.getPlayerMount(player.getUniqueId());
                    if (entity.isAlive()) {
                        entity.getBukkitEntity().remove();
                    }
                    if (DonationEffects.getInstance().ENTITY_PARTICLE_EFFECTS.containsKey(entity)) {
                        DonationEffects.getInstance().ENTITY_PARTICLE_EFFECTS.remove(entity);
                    }
                    EntityAPI.removePlayerMountList(player.getUniqueId());
                    player.sendMessage(ChatColor.WHITE + "[" + ChatColor.GOLD.toString() + ChatColor.BOLD + "DONATE" + ChatColor.WHITE + "]" + ChatColor.AQUA + " Your Mount has returned home as you've summoned another companion!");
                }
                if (EntityAPI.hasPetOut(player.getUniqueId())) {
                    Entity entity = EntityAPI.getPlayerPet(player.getUniqueId());
                    if (entity.isAlive()) {
                        entity.getBukkitEntity().remove();
                    }
                    if (DonationEffects.getInstance().ENTITY_PARTICLE_EFFECTS.containsKey(entity)) {
                        DonationEffects.getInstance().ENTITY_PARTICLE_EFFECTS.remove(entity);
                    }
                    EntityAPI.removePlayerPetList(player.getUniqueId());
                    player.sendMessage(ChatColor.WHITE + "[" + ChatColor.GOLD.toString() + ChatColor.BOLD + "DONATE" + ChatColor.WHITE + "]" + ChatColor.AQUA + " Your Pet has returned home as you've summoned another companion!");
                }
                net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(event.getCurrentItem());
                if (nmsStack.getTag() == null || nmsStack.getTag().getString("mountType") == null) {
                    player.sendMessage("Uh oh... Something went wrong with your mount! Please inform a staff member! [NBTTag]");
                    player.closeInventory();
                    return;
                }
                MountUtils.spawnMount(player.getUniqueId(), nmsStack.getTag().getString("mountType"));
            }
            return;
        }

        /*
        Particle Trails Below
         */
        if (name.equalsIgnoreCase("Player Trail Selection")) {
            event.setCancelled(true);
            if (event.getCurrentItem().getType() == Material.BARRIER) {
                PlayerMenus.openPlayerProfileMenu(player);
                return;
            }
            if (event.getCurrentItem().getType() == Material.ARMOR_STAND) {
                if (DonationEffects.getInstance().PLAYER_PARTICLE_EFFECTS.containsKey(player)) {
                    DonationEffects.getInstance().PLAYER_PARTICLE_EFFECTS.remove(player);
                    player.sendMessage(ChatColor.WHITE + "[" + ChatColor.GOLD.toString() + ChatColor.BOLD + "DONATE" + ChatColor.WHITE + "]" + ChatColor.AQUA + " You have disabled your Player trail!");
                } else {
                    player.sendMessage(ChatColor.WHITE + "[" + ChatColor.GOLD.toString() + ChatColor.BOLD + "DONATE" + ChatColor.WHITE + "]" + ChatColor.AQUA + " You don't have a Player trail enabled!");
                }
                return;
            }
            if (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR && event.getCurrentItem().getType() != Material.BARRIER && event.getCurrentItem().getType() != Material.ARMOR_STAND) {
                net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(event.getCurrentItem());
                if (nmsStack.getTag() == null || nmsStack.getTag().getString("playerTrailType") == null) {
                    player.sendMessage("Uh oh... Something went wrong with your Player trail! Please inform a staff member! [NBTTag]");
                    player.closeInventory();
                    return;
                }
                DonationEffects.getInstance().PLAYER_PARTICLE_EFFECTS.put(player, ParticleAPI.ParticleEffect.getByName(nmsStack.getTag().getString("playerTrailType")));
                player.sendMessage(ChatColor.WHITE + "[" + ChatColor.GOLD.toString() + ChatColor.BOLD + "DONATE" + ChatColor.WHITE + "]" + ChatColor.AQUA + " You have enabled the " + ChatColor.RED + nmsStack.getTag().getString("playerTrailType") + ChatColor.AQUA + " Player trail!");
            }
        }


        /*
        Profile PlayerMenus Below
         */
        if (name.equals("Profile")) {
            event.setCancelled(true);
            switch (slot) {
                case 0: //todo: attributes
                    break;
                case 1:
                    PlayerMenus.openFriendInventory(player);
                    break;
                case 6:
                    PlayerMenus.openPlayerParticleMenu(player);
                    break;
                case 7:
                    PlayerMenus.openPlayerMountMenu(player);
                    break;
                case 8:
                    PlayerMenus.openPlayerPetMenu(player);
                    break;
                case 22:
                    if (!(CombatLog.isInCombat(player))) {
                        if (TeleportAPI.isPlayerCurrentlyTeleporting(player.getUniqueId())) {
                            player.sendMessage("You cannot restart a teleport during a cast!");
                            return;
                        }
                        if (TeleportAPI.canUseHearthstone(player.getUniqueId())) {
                            net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(event.getCurrentItem());
                            Teleportation.getInstance().teleportPlayer(player.getUniqueId(), Teleportation.EnumTeleportType.HEARTHSTONE, nmsItem.getTag());
                            break;
                        } else {
                            player.sendMessage(ChatColor.RED + "You currently cannot use your Hearthstone because of Alignment, World or Cooldown issues!");
                            break;
                        }
                    } else {
                        player.sendMessage(ChatColor.GREEN.toString() + ChatColor.BOLD + "TELEPORT " + ChatColor.RED + "You are in combat! " + ChatColor.RED.toString() + "(" + ChatColor.UNDERLINE + CombatLog.COMBAT.get(player) + "s" + ChatColor.RED + ")");
                        break;
                    }
                default:
                    break;
            }
            return;
        }


        /*
        Guilds Below
         */
        if (name.equals("Guild Management")) {
            String guildName = (String) DatabaseAPI.getInstance().getData(EnumData.GUILD, player.getUniqueId());
            event.setCancelled(true);
            switch (slot) {
                case 0:
                    PlayerMenus.openPlayerGuildInventory(player);
                    break;
                case 10:
                    if (Guild.getInstance().isOfficer(guildName, player.getUniqueId()) || Guild.getInstance().isOwner(player.getUniqueId(), guildName) || Guild.getInstance().isCoOwner(player.getUniqueId(), guildName)) {
                        AnvilGUIInterface invitePlayerGUI = AnvilApi.createNewGUI(player, anvilClick -> {
                            switch (anvilClick.getSlot()) {
                                case OUTPUT:
                                    anvilClick.setWillClose(true);
                                    anvilClick.setWillDestroy(true);
                                    Guild.getInstance().invitePlayer(player, anvilClick.getName());
                                    break;
                            }
                        });
                        invitePlayerGUI.setSlot(AnvilSlot.INPUT_LEFT, PlayerMenus.editItem(new ItemStack(Material.SKULL_ITEM, 1, (short) 3), "Type name here..", new String[]{}));
                        invitePlayerGUI.open();
                    } else {
                        player.sendMessage(ChatColor.RED + "You do not have the required permissions to invite a player!");
                    }
                    break;
                case 11:
                    if (Guild.getInstance().isOfficer(guildName, player.getUniqueId()) || Guild.getInstance().isOwner(player.getUniqueId(), guildName) || Guild.getInstance().isCoOwner(player.getUniqueId(), guildName)) {
                        AnvilGUIInterface removePlayerGUI = AnvilApi.createNewGUI(player, anvilClick -> {
                            switch (anvilClick.getSlot()) {
                                case OUTPUT:
                                    anvilClick.setWillClose(true);
                                    anvilClick.setWillDestroy(true);
                                    Guild.getInstance().removePlayer(player, anvilClick.getName());
                                    break;
                            }
                        });
                        removePlayerGUI.setSlot(AnvilSlot.INPUT_LEFT, PlayerMenus.editItem(new ItemStack(Material.SKULL_ITEM, 1, (short) 3), "Type name here..", new String[]{}));
                        removePlayerGUI.open();
                    } else {
                        player.sendMessage(ChatColor.RED + "You do not have the required permissions to remove a player!");
                    }
                    break;
                case 13:
                    if (Guild.getInstance().isOwner(player.getUniqueId(), guildName) || Guild.getInstance().isCoOwner(player.getUniqueId(), guildName)) {
                        AnvilGUIInterface promotePlayerGUI = AnvilApi.createNewGUI(player, anvilClick -> {
                            switch (anvilClick.getSlot()) {
                                case OUTPUT:
                                    anvilClick.setWillClose(true);
                                    anvilClick.setWillDestroy(true);
                                    Guild.getInstance().promotePlayer(player, anvilClick.getName());
                            }
                        });
                        promotePlayerGUI.setSlot(AnvilSlot.INPUT_LEFT, PlayerMenus.editItem(new ItemStack(Material.SKULL_ITEM, 1, (short) 3), "Type name here..", new String[]{}));
                        promotePlayerGUI.open();
                    } else {
                        player.sendMessage(ChatColor.RED + "You do not have the required permissions to promote a player!");
                    }
                    break;
                case 14:
                    if (Guild.getInstance().isOwner(player.getUniqueId(), guildName) || Guild.getInstance().isCoOwner(player.getUniqueId(), guildName)) {

                        AnvilGUIInterface demotePlayerGUI = AnvilApi.createNewGUI(player, anvilClick -> {
                            switch (anvilClick.getSlot()) {
                                case OUTPUT:
                                    anvilClick.setWillClose(true);
                                    anvilClick.setWillDestroy(true);
                                    Guild.getInstance().demotePlayer(player, anvilClick.getName());
                            }
                        });
                        demotePlayerGUI.setSlot(AnvilSlot.INPUT_LEFT, PlayerMenus.editItem(new ItemStack(Material.SKULL_ITEM, 1, (short) 3), "Type name here..", new String[]{}));
                        demotePlayerGUI.open();
                    } else {
                        player.sendMessage(ChatColor.RED + "You do not have the required permissions to demote a player!");
                    }
                    break;
                case 36:
                    if (Guild.getInstance().isOwner(player.getUniqueId(), guildName) || Guild.getInstance().isCoOwner(player.getUniqueId(), guildName)) {
                        AnvilGUIInterface pickIconGUI = AnvilApi.createNewGUI(player, anvilClick -> {
                            switch (anvilClick.getSlot()) {
                                case OUTPUT:
                                    anvilClick.setWillClose(true);
                                    anvilClick.setWillDestroy(true);
                                    if (Material.getMaterial(anvilClick.getName().toUpperCase()) == null) {
                                        player.sendMessage(ChatColor.RED + "The material you specified is invalid! Examples: DIRT, DIAMOND_SWORD, DIAMOND_PICKAXE, DIRT");
                                    } else {
                                        Guild.getInstance().setGuildIcon(guildName, Material.getMaterial(anvilClick.getName().toUpperCase()));
                                        NetworkAPI.getInstance().sendAllGuildMessage(guildName, ChatColor.GREEN + "Icon has been set to " + anvilClick.getName());
                                    }
                            }
                        });
                        pickIconGUI.setSlot(AnvilSlot.INPUT_LEFT, PlayerMenus.editItem(new ItemStack(Material.DIAMOND), "Type material here..", new String[]{
                                ChatColor.GRAY + "",
                                ChatColor.GRAY + "How to use:",
                                ChatColor.GRAY + "dirt -> dirt block",
                                ChatColor.GRAY + "diamond_axe -> diamond axe",
                        }));
                        pickIconGUI.open();
                    } else {
                        player.sendMessage(ChatColor.RED + "You do not have the required permissions to set the Guild Icon!");
                    }
                    break;
            }
            return;
        }
        if (name.endsWith("- Officers")) {
            event.setCancelled(true);
            if (slot == 0) {
                PlayerMenus.openPlayerGuildInventory(player);
            }
        } else if (name.endsWith("- Members")) {
            event.setCancelled(true);
            if (slot == 0) {
                PlayerMenus.openPlayerGuildInventory(player);
            }
        } else if (name.endsWith(" - (Bank Logs)")) {
            event.setCancelled(true);
            if (slot == 0) {
                PlayerMenus.openPlayerGuildLog(player);
            }
        } else if (name.endsWith("- (Invite Logs)")) {
            event.setCancelled(true);
            if (slot == 0) {
                PlayerMenus.openPlayerGuildLog(player);
            }
        } else if (name.endsWith(" - (Login Logs)")) {
            event.setCancelled(true);
            if (slot == 0) {
                PlayerMenus.openPlayerGuildLog(player);
            }
        } else if (name.endsWith("- (Logs)")) {
            event.setCancelled(true);
            if (slot > 18) return;
            switch (slot) {
                case 0:
                    PlayerMenus.openPlayerGuildInventory(player);
                    break;
                case 12:
                    PlayerMenus.openPlayerGuildLogLogins(player);
                    break;
                case 13:
                    PlayerMenus.openPlayerGuildLogInvitations(player);
                    break;
                case 14:
                    PlayerMenus.openPlayerGuildLogBankClicks(player);
                    break;

            }
        } else if (name.equals("Top Guilds")) {
            event.setCancelled(true);
        } else if (name.equals("Guild Management")) {
            event.setCancelled(true);
        } else if (name.startsWith("Guild - ")) {
            event.setCancelled(true);
            if (slot > 54) return;
            switch (slot) {
                case 0:
                    PlayerMenus.openPlayerGuildLog(player);
                    break;
                case 1:
                    PlayerMenus.openGuildManagement(player);
                    break;
                case 17:
                    PlayerMenus.openGuildRankingBoard(player);
                    break;
                case 18:
                    PlayerMenus.openGuildOfficers(player);
                    break;
                case 27:
                    PlayerMenus.openGuildMembers(player);
                    break;
            }
        }
    }
}
