package net.dungeonrealms.old.game.command;

import net.dungeonrealms.common.frontend.command.BaseCommand;
import net.dungeonrealms.common.old.game.database.DatabaseAPI;
import net.dungeonrealms.common.old.game.database.data.EnumData;
import net.dungeonrealms.common.old.game.database.player.rank.Rank;
import net.dungeonrealms.old.game.mastery.ItemSerialization;
import net.dungeonrealms.old.game.player.banks.BankMechanics;
import net.dungeonrealms.old.game.player.banks.Storage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * Created by Chase on Nov 11, 2015
 */
public class CommandModeration extends BaseCommand {
    public CommandModeration(String command, String usage, String description, List<String> aliases) {
        super(command, usage, description, aliases);
    }

    public static Map<UUID, UUID> offline_inv_watchers = new HashMap<>();
    public static Map<UUID, UUID> offline_armor_watchers = new HashMap<>();
    public static Map<UUID, UUID> offline_bank_watchers = new HashMap<>();
    public static Map<UUID, UUID> offline_bin_watchers = new HashMap<>();

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
                        if (stack == null) continue;
                        inv.addItem(stack);
                    }
                    inv.setItem(8, player.getEquipment().getItemInMainHand());
                    sender.openInventory(inv);
                } else {
                    if (DatabaseAPI.getInstance().getUUIDFromName(playerName).equals("")) {
                        sender.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + playerName + ChatColor.RED + " does not exist in our database.");
                        return true;
                    }

                    UUID p_uuid = UUID.fromString(DatabaseAPI.getInstance().getUUIDFromName(playerName));
                    Inventory inventoryView = Bukkit.createInventory(null, 9, playerName + "'s Offline Armor View (Last slot is offhand)");

                    List<String> playerArmor = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.ARMOR, p_uuid);
                    if (playerArmor == null) break;
                    int i = -1;
                    ItemStack[] armorContents = new ItemStack[4];
                    ItemStack offHand = new ItemStack(Material.AIR);
                    for (String armor : playerArmor) {
                        i++;
                        if (i <= 3) { //Normal armor piece
                            if (armor.equals("null") || armor.equals("")) {
                                inventoryView.addItem(new ItemStack(Material.AIR));
                            } else {
                                inventoryView.addItem(ItemSerialization.itemStackFromBase64(armor));
                            }

                        } else {
                            if (armor.equals("null") || armor.equals("")) {
                                inventoryView.addItem(new ItemStack(Material.AIR));
                            } else {
                                inventoryView.addItem(ItemSerialization.itemStackFromBase64(armor));
                            }
                        }
                    }

                    offline_armor_watchers.put(sender.getUniqueId(), p_uuid);
                    sender.openInventory(inventoryView);
                }
                break;
            case "mulesee":
                break;
            case "hide":
                sender.sendMessage(ChatColor.YELLOW + "Please use " + ChatColor.BOLD + ChatColor.UNDERLINE + "/gm" + ChatColor.YELLOW + ".");
                // @todo: remove this later on.
                break;
            case "storagesee":
            case "banksee":
                playerName = args[1];
                if (Bukkit.getPlayer(playerName) != null) {
                    Storage storage = BankMechanics.getInstance().getStorage(Bukkit.getPlayer(playerName).getUniqueId());
                    sender.openInventory(storage.inv);
                } else {
                    if (DatabaseAPI.getInstance().getUUIDFromName(playerName).equals("")) {
                        sender.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + playerName + ChatColor.RED + " does not exist in our database.");
                        return true;
                    }

                    UUID p_uuid = UUID.fromString(DatabaseAPI.getInstance().getUUIDFromName(playerName));

                    // check if they're logged in on another shard
                    if ((Boolean) DatabaseAPI.getInstance().getData(EnumData.IS_PLAYING, p_uuid)) {
                        String shard = DatabaseAPI.getInstance().getFormattedShardName(p_uuid);
                        sender.sendMessage(ChatColor.RED + "That player is currently playing on shard " + shard + ". " +
                                "Please banksee on that shard to avoid concurrent modification.");
                        break;
                    }

                    String source = (String) DatabaseAPI.getInstance().getData(EnumData.INVENTORY_STORAGE, p_uuid);
                    Inventory inv = null;
                    if (source != null && source.length() > 0 && !source.equalsIgnoreCase("null")) {
                        inv = ItemSerialization.fromString(source);
                    } else {
                        sender.sendMessage(ChatColor.RED + "That player's storage is empty.");
                        break;
                    }

                    offline_bank_watchers.put(sender.getUniqueId(), p_uuid);
                    sender.openInventory(inv);
                }
                break;
            case "binsee":
                playerName = args[1];
                if (Bukkit.getPlayer(playerName) != null) {
                    Storage storage = BankMechanics.getInstance().getStorage(Bukkit.getPlayer(playerName).getUniqueId());
                    sender.openInventory(storage.collection_bin);
                } else {
                    if (DatabaseAPI.getInstance().getUUIDFromName(playerName).equals("")) {
                        sender.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + playerName + ChatColor.RED + " does not exist in our database.");
                        return true;
                    }

                    UUID p_uuid = UUID.fromString(DatabaseAPI.getInstance().getUUIDFromName(playerName));

                    // check if they're logged in on another shard
                    if ((Boolean) DatabaseAPI.getInstance().getData(EnumData.IS_PLAYING, p_uuid)) {
                        String shard = DatabaseAPI.getInstance().getFormattedShardName(p_uuid);
                        sender.sendMessage(ChatColor.RED + "That player is currently playing on shard " + shard + ". " +
                                "Please banksee on that shard to avoid concurrent modification.");
                        break;
                    }

                    String stringInv = (String) DatabaseAPI.getInstance().getData(EnumData.INVENTORY_COLLECTION_BIN, p_uuid);
                    Inventory inv = null;
                    if (stringInv.length() > 1) {
                        inv = ItemSerialization.fromString(stringInv);
                        for (ItemStack item : inv.getContents()) {
                            if (item != null && item.getType() == Material.AIR) {
                                inv.addItem(item);
                            }
                        }
                        Player p = Bukkit.getPlayer(p_uuid);
                        if (p != null)
                            p.sendMessage(ChatColor.RED + "You have items in your collection bin!");
                    } else {
                        sender.sendMessage(ChatColor.RED + "That player's collection bin is empty.");
                        break;
                    }

                    offline_bin_watchers.put(sender.getUniqueId(), p_uuid);
                    sender.openInventory(inv);
                }
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
