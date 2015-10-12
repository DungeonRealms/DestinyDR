package net.dungeonrealms.handlers;

import com.minebone.anvilapi.core.AnvilApi;
import com.minebone.anvilapi.nms.anvil.AnvilGUIInterface;
import com.minebone.anvilapi.nms.anvil.AnvilSlot;
import net.dungeonrealms.API;
import net.dungeonrealms.guild.Guild;
import net.dungeonrealms.inventory.Menu;
import net.dungeonrealms.mongo.DatabaseAPI;
import net.dungeonrealms.mongo.EnumData;
import net.dungeonrealms.mongo.EnumOperators;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

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

    public void doGuildClick(InventoryClickEvent event) {
        String name = event.getInventory().getName();
        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();
        if (slot == -999) return;
        if (name.equals("Guild Management")) {
            event.setCancelled(true);
            switch (slot) {
                case 0:
                    Menu.openPlayerGuildInventory(player);
                    break;
                case 10:
                    AnvilGUIInterface invitePlayerGUI = AnvilApi.createNewGUI(player, anvilClick -> {
                        switch (anvilClick.getSlot()) {
                            case OUTPUT:
                                anvilClick.setWillClose(true);
                                anvilClick.setWillDestroy(true);
                                UUID uuid = API.getUUIDFromName(anvilClick.getName());
                                if (API.isOnline(uuid)) {
                                    if (Guild.getInstance().isGuildNull(API.getUUIDFromName(anvilClick.getName()))) {
                                        //Player is Online and not not inside of a guild.
                                        String guildName = (String) DatabaseAPI.getInstance().getData(EnumData.GUILD, player.getUniqueId());
                                        if (Guild.getInstance().isAlreadyInvited(guildName, player.getUniqueId())) {
                                            player.sendMessage(ChatColor.RED + "That player is already invited to your guild!");
                                        } else {
                                            DatabaseAPI.getInstance().update(uuid, EnumOperators.$PUSH, "notices.guildInvites", guildName + "," + (System.currentTimeMillis() / 1000l), true);
                                            DatabaseAPI.getInstance().updateGuild(guildName, EnumOperators.$PUSH, "invitations", player.getUniqueId().toString(), true);
                                            player.sendMessage(ChatColor.GREEN + "Player has been invited to a guild!");
                                        }
                                    } else {
                                        player.sendMessage(ChatColor.RED + "That player is inside of a Guild!");
                                        player.playSound(player.getLocation(), Sound.ANVIL_BREAK, 1f, 63f);
                                    }
                                } else {
                                    player.sendMessage(ChatColor.RED + "That player isn't on your local server!");
                                    player.playSound(player.getLocation(), Sound.ANVIL_BREAK, 1f, 63f);
                                }
                                break;
                        }
                    });
                    invitePlayerGUI.setSlot(AnvilSlot.INPUT_LEFT, Menu.editItem(new ItemStack(Material.SKULL_ITEM, 1, (short) 3), "Type name here..", new String[]{}));
                    invitePlayerGUI.open();
                    break;
                case 11:
                    AnvilGUIInterface removePlayerGUI = AnvilApi.createNewGUI(player, anvilClick -> {
                        switch (anvilClick.getSlot()) {
                            case OUTPUT:
                                UUID uuid = API.getUUIDFromName(anvilClick.getName());
                                String guildName = (String) DatabaseAPI.getInstance().getData(EnumData.GUILD, player.getUniqueId());
                                if (API.isOnline(uuid)) {
                                    if (Guild.getInstance().isAlreadyInvited(guildName, uuid)) {
                                        DatabaseAPI.getInstance().update(uuid, EnumOperators.$PULL, "notices.guildInvites", guildName, true);
                                        DatabaseAPI.getInstance().updateGuild(guildName, EnumOperators.$PULL, "invitations", player.getUniqueId().toString(), true);
                                    } else {
                                        DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, "info.guild", "", true);
                                        if (Guild.getInstance().isMember(guildName, uuid)) {
                                            DatabaseAPI.getInstance().updateGuild(guildName, EnumOperators.$PULL, "info.members", player.getUniqueId().toString(), true);
                                        } else if (Guild.getInstance().isOfficer(guildName, uuid)) {
                                            DatabaseAPI.getInstance().updateGuild(guildName, EnumOperators.$PULL, "info.officers", player.getUniqueId().toString(), true);
                                        }
                                    }
                                } else {
                                    //If the player they're wanting to remove isn't online.. Ohh shit..
                                }
                                break;
                        }
                    });
                    removePlayerGUI.setSlot(AnvilSlot.INPUT_LEFT, Menu.editItem(new ItemStack(Material.SKULL_ITEM, 1, (short) 3), "Type name here..", new String[]{}));
                    removePlayerGUI.open();
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
