package net.dungeonrealms.handlers;

import com.minebone.anvilapi.core.AnvilApi;
import com.minebone.anvilapi.nms.anvil.AnvilGUIInterface;
import com.minebone.anvilapi.nms.anvil.AnvilSlot;
import net.dungeonrealms.donate.DonationEffects;
import net.dungeonrealms.entities.utils.EntityAPI;
import net.dungeonrealms.entities.utils.MountUtils;
import net.dungeonrealms.entities.utils.PetUtils;
import net.dungeonrealms.guild.Guild;
import net.dungeonrealms.inventory.Menu;
import net.dungeonrealms.mechanics.ParticleAPI;
import net.dungeonrealms.mongo.DatabaseAPI;
import net.dungeonrealms.mongo.EnumData;
import net.dungeonrealms.network.NetworkAPI;
import net.minecraft.server.v1_8_R3.Entity;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

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
        Mail Below
         */
        if (name.equals("Mailbox")) {
            event.setCancelled(true);
        }

        /*
        Pets Below
         */
        if (name.equalsIgnoreCase("Pet Selection")) {
            event.setCancelled(true);
            if (event.getCurrentItem().getType() == Material.BARRIER) {
                player.closeInventory();
                return;
            }
            if (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR && event.getCurrentItem().getType() != Material.BARRIER) {
                if (EntityAPI.hasPetOut(player.getUniqueId())) {
                    Entity entity = EntityAPI.getPlayerPet(player.getUniqueId());
                    if (entity.isAlive()) {
                        entity.getBukkitEntity().remove();
                    }
                    if (DonationEffects.ENTITY_PARTICLE_EFFECTS.containsKey(entity)) {
                        DonationEffects.ENTITY_PARTICLE_EFFECTS.remove(entity);
                    }
                    EntityAPI.removePlayerPetList(player.getUniqueId());
                }
                net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(event.getCurrentItem());
                if (nmsStack.getTag() == null || nmsStack.getTag().getString("petType") == null) {
                    player.sendMessage("Uh oh... Something went wrong with your pet! Please inform a staff member! [NBTTag]");
                    player.closeInventory();
                    return;
                }
                PetUtils.spawnPet(player.getUniqueId(), nmsStack.getTag().getString("petType"));
            }
        }

        /*
        Mounts Below
         */
        if (name.equalsIgnoreCase("Mount Selection")) {
            event.setCancelled(true);
            if (event.getCurrentItem().getType() == Material.BARRIER) {
                player.closeInventory();
                return;
            }
            if (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR && event.getCurrentItem().getType() != Material.BARRIER) {
                if (EntityAPI.hasMountOut(player.getUniqueId())) {
                    Entity entity = EntityAPI.getPlayerMount(player.getUniqueId());
                    if (entity.isAlive()) {
                        entity.getBukkitEntity().remove();
                    }
                    if (DonationEffects.ENTITY_PARTICLE_EFFECTS.containsKey(entity)) {
                        DonationEffects.ENTITY_PARTICLE_EFFECTS.remove(entity);
                    }
                    EntityAPI.removePlayerMountList(player.getUniqueId());
                }
                if (EntityAPI.hasPetOut(player.getUniqueId())) {
                    Entity entity = EntityAPI.getPlayerPet(player.getUniqueId());
                    if (entity.isAlive()) {
                        entity.getBukkitEntity().remove();
                    }
                    if (DonationEffects.ENTITY_PARTICLE_EFFECTS.containsKey(entity)) {
                        DonationEffects.ENTITY_PARTICLE_EFFECTS.remove(entity);
                    }
                    EntityAPI.removePlayerPetList(player.getUniqueId());
                    player.sendMessage("Your pet has returned home as you have summoned your mount");
                }
                net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(event.getCurrentItem());
                if (nmsStack.getTag() == null || nmsStack.getTag().getString("mountType") == null) {
                    player.sendMessage("Uh oh... Something went wrong with your mount! Please inform a staff member! [NBTTag]");
                    player.closeInventory();
                    return;
                }
                MountUtils.spawnMount(player.getUniqueId(), nmsStack.getTag().getString("mountType"));
            }
        }

        /*
        Particle Trails Below
         */
        if (name.equalsIgnoreCase("Particle Trail Selection")) {
            event.setCancelled(true);
            if (event.getCurrentItem().getType() == Material.BARRIER) {
                player.closeInventory();
                return;
            }
            net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(event.getCurrentItem());
            if (nmsStack.getTag() == null || nmsStack.getTag().getString("trailType") == null) {
                player.sendMessage("Uh oh... Something went wrong with your trail! Please inform a staff member! [NBTTag]");
                player.closeInventory();
                return;
            }
            DonationEffects.PLAYER_PARTICLE_EFFECTS.put(player, ParticleAPI.ParticleEffect.getByName(nmsStack.getTag().getString("trailType")));
            player.sendMessage("You have enabled the " + nmsStack.getTag().getString("trailType") + " particle trail!");
        }

        /*
        Guilds Below
         */
        if (name.equals("Guild Management")) {
            String guildName = (String) DatabaseAPI.getInstance().getData(EnumData.GUILD, player.getUniqueId());
            event.setCancelled(true);
            switch (slot) {
                case 0:
                    Menu.openPlayerGuildInventory(player);
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
                        invitePlayerGUI.setSlot(AnvilSlot.INPUT_LEFT, Menu.editItem(new ItemStack(Material.SKULL_ITEM, 1, (short) 3), "Type name here..", new String[]{}));
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
                        removePlayerGUI.setSlot(AnvilSlot.INPUT_LEFT, Menu.editItem(new ItemStack(Material.SKULL_ITEM, 1, (short) 3), "Type name here..", new String[]{}));
                        removePlayerGUI.open();
                    } else {
                        player.sendMessage(ChatColor.RED + "You do not have the required permissions to remove a player!");
                    }
                    break;
                case 13:
                    AnvilGUIInterface promotePlayerGUI = AnvilApi.createNewGUI(player, anvilClick -> {
                        switch (anvilClick.getSlot()) {
                            case OUTPUT:
                                anvilClick.setWillClose(true);
                                anvilClick.setWillDestroy(true);
                                Guild.getInstance().promotePlayer(player, anvilClick.getName());
                        }
                    });
                    promotePlayerGUI.setSlot(AnvilSlot.INPUT_LEFT, Menu.editItem(new ItemStack(Material.SKULL_ITEM, 1, (short) 3), "Type name here..", new String[]{}));
                    promotePlayerGUI.open();
                    break;
                case 14:
                    AnvilGUIInterface demotePlayerGUI = AnvilApi.createNewGUI(player, anvilClick -> {
                        switch (anvilClick.getSlot()) {
                            case OUTPUT:
                                anvilClick.setWillClose(true);
                                anvilClick.setWillDestroy(true);
                                Guild.getInstance().demotePlayer(player, anvilClick.getName());
                        }
                    });
                    demotePlayerGUI.setSlot(AnvilSlot.INPUT_LEFT, Menu.editItem(new ItemStack(Material.SKULL_ITEM, 1, (short) 3), "Type name here..", new String[]{}));
                    demotePlayerGUI.open();
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
                        pickIconGUI.setSlot(AnvilSlot.INPUT_LEFT, Menu.editItem(new ItemStack(Material.DIAMOND), "Type material here..", new String[]{
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
        }
        if (name.endsWith("- Officers")) {
            event.setCancelled(true);
            if (slot == 0) {
                Menu.openPlayerGuildInventory(player);
            }
        } else if (name.endsWith("- Members")) {
            event.setCancelled(true);
            if (slot == 0) {
                Menu.openPlayerGuildInventory(player);
            }
        } else if (name.endsWith(" - (Bank Logs)")) {
            event.setCancelled(true);
            if (slot == 0) {
                Menu.openPlayerGuildLog(player);
            }
        } else if (name.endsWith("- (Invite Logs)")) {
            event.setCancelled(true);
            if (slot == 0) {
                Menu.openPlayerGuildLog(player);
            }
        } else if (name.endsWith(" - (Login Logs)")) {
            event.setCancelled(true);
            if (slot == 0) {
                Menu.openPlayerGuildLog(player);
            }
        } else if (name.endsWith("- (Logs)")) {
            event.setCancelled(true);
            if (slot > 18) return;
            switch (slot) {
                case 0:
                    Menu.openPlayerGuildInventory(player);
                    break;
                case 12:
                    Menu.openPlayerGuildLogLogins(player);
                    break;
                case 13:
                    Menu.openPlayerGuildLogInvitations(player);
                    break;
                case 14:
                    Menu.openPlayerGuildLogBankClicks(player);
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
                    Menu.openPlayerGuildLog(player);
                    break;
                case 1:
                    Menu.openGuildManagement(player);
                    break;
                case 17:
                    Menu.openGuildRankingBoard(player);
                    break;
                case 18:
                    Menu.openGuildOfficers(player);
                    break;
                case 27:
                    Menu.openGuildMembers(player);
                    break;
            }
        }
    }
}
