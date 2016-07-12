package net.dungeonrealms.game.commands;

import net.dungeonrealms.game.commands.generic.BasicCommand;
import net.dungeonrealms.game.mastery.ItemSerialization;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.player.banks.Storage;
import net.dungeonrealms.game.player.rank.Rank;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Chase on Nov 11, 2015
 */
public class CommandModeration extends BasicCommand {
    public CommandModeration(String command, String usage, String description) {
        super(command, usage, description);
    }

    public static Map<UUID, UUID> offline_inv_watchers = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String string, String[] args) {
        if (s instanceof ConsoleCommandSender) return false;
        Player sender = (Player) s;

        if (!Rank.isGM(sender)) return false;

        if (args.length == 0) {
            s.sendMessage(usage);
            return true;
        }

        switch (args[0]) {
            case "tp":
                String playerName = args[1];
                Player player = Bukkit.getPlayer(playerName);
                if (player != null) {
                    sender.teleport(player.getLocation());
                    sender.sendMessage("Teleported to " + player.getName());
                } else
                    sender.sendMessage(ChatColor.RED + playerName + " not online");
                break;
            case "invsee":
                playerName = args[1];

                if (Bukkit.getPlayer(playerName) != null) {
                    sender.openInventory(Bukkit.getPlayer(playerName).getInventory());
                } else {

                    if (DatabaseAPI.getInstance().getUUIDFromName(playerName).equals("")) {
                        sender.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + playerName + ChatColor.RED + " does not exist in our database.");
                        return true;
                    }

                    UUID p_uuid = UUID.fromString(DatabaseAPI.getInstance().getUUIDFromName(playerName));
                    Inventory inventoryView = Bukkit.createInventory(null, 36, playerName + "'s Offline Inventory View");

                    String playerInv = (String) DatabaseAPI.getInstance().getData(EnumData.INVENTORY, p_uuid);
                    if (playerInv != null && playerInv.length() > 0 && !playerInv.equalsIgnoreCase("null")) {
                        ItemStack[] items = ItemSerialization.fromString(playerInv, 36).getContents();
                        inventoryView.setContents(items);
                    }

                    offline_inv_watchers.put(sender.getUniqueId(), p_uuid);
                    sender.openInventory(inventoryView);
                }
                break;
            case "armorsee":
                playerName = args[1];
                player = Bukkit.getPlayer(playerName);
                if (player != null) {
                    Inventory inv = Bukkit.createInventory(null, 9, player.getName() + " Armor");
                    for (int i = 0; i < 4; i++) {
                        ItemStack stack = player.getInventory().getArmorContents()[i];
                        inv.addItem(stack);
                    }
                    inv.setItem(8, player.getEquipment().getItemInMainHand());
                    sender.openInventory(inv);
                }
                break;
            case "hide":
                sender.sendMessage(ChatColor.YELLOW + "Please use " + ChatColor.BOLD + ChatColor.UNDERLINE + "/gm" + ChatColor.YELLOW + ".");
                // @todo: remove this later on.
                break;
            case "banksee":
                playerName = args[1];
                if (Bukkit.getPlayer(playerName) != null) {
                    Storage storage = BankMechanics.getInstance().getStorage(Bukkit.getPlayer(playerName).getUniqueId());
                    sender.openInventory(storage.inv);
                }
                break;
            case "gems":
                playerName = args[1];
                if (Bukkit.getPlayer(playerName) != null) {
                    sender.sendMessage(ChatColor.YELLOW + playerName + " balance: " + ChatColor.AQUA + DatabaseAPI.getInstance().getData(EnumData.GEMS, Bukkit.getPlayer(playerName).getUniqueId()));
                }
                break;
        }
        return false;
    }
}
