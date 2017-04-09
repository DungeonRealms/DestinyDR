package net.dungeonrealms.game.command;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.data.EnumOperators;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.game.player.banks.BankMechanics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Created by Giovanni on 26-12-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class CommandCloseShop extends BaseCommand {

    public CommandCloseShop() {
        super("closeshop", "/<command>", "Close your shop");
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {

        if (args.length == 1 && (commandSender instanceof Player && Rank.isTrialGM((Player) commandSender))) {
            String playerName = args[0];
            if (!isPlayer(playerName)) {
                commandSender.sendMessage(ChatColor.RED + "Player does not exist in our database");
                return false;
            }

            boolean hasShop = (boolean) DatabaseAPI.getInstance().getData(EnumData.HASSHOP, UUID.fromString(DatabaseAPI.getInstance().getUUIDFromName(playerName)));
            if (!hasShop) {
                commandSender.sendMessage(ChatColor.RED + "Shop not detected");
                return false;
            }
            // A gm is closing a player's shop
            Player player;
            try {
                player = Bukkit.getPlayer(playerName);
                // Trigger exception
                player.sendMessage("");
            } catch (Exception e) {
                commandSender.sendMessage(ChatColor.RED + "You cannot close a player's shop if he is not on the same shard as you");
                String shard = DatabaseAPI.getInstance().getFormattedShardName(UUID.fromString(DatabaseAPI.getInstance().getUUIDFromName(playerName)));
                if (shard != null) {
                    commandSender.sendMessage(ChatColor.RED + "Player is on shard: " + ChatColor.BOLD + shard);
                } else {
                    commandSender.sendMessage(ChatColor.RED + "Player is not online on the network (" + playerName + ")");
                }
                return false;
            }

            if (player != null && player.isOnline()) {
                // Check if the player is viewing his shop
                if (GameAPI.isShop(player.getOpenInventory())) {
                    // Don't allow the shop to be removed if the owner is viewing his shop GUI
                    commandSender.sendMessage(ChatColor.RED + player.getName() + " is currently viewing his shop, action denied");
                    commandSender.sendMessage(ChatColor.RED + "This it to prevent duplication exploitation, please manually close the shop");
                    return false;
                } else {
                    // If he isn't viewing his shop GUI, continue
                    UUID uuid = UUID.fromString(DatabaseAPI.getInstance().getUUIDFromName(playerName));
                    GameAPI.sendNetworkMessage("Shop", "close:" + " ," + playerName);
                    DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.HASSHOP, false, true);
                    commandSender.sendMessage(ChatColor.GREEN + "Safely closed " + playerName + "'s shop");
                    return false;
                }
            }
            return false;
        } else {
            //  A player types /closeshop
            if (commandSender instanceof Player) {
                Player player = (Player) commandSender;
                if (GameAPI.isShop(player.getOpenInventory())) {
                    // Uh oh, someone is hacking? He is performing a command whilst having his shop GUI open
                    player.sendMessage(ChatColor.RED + "Illegal inventory session ID");
                    player.sendMessage(ChatColor.RED + "Close your shop GUI to close your shop");
                    return false;
                } else {
                    GameAPI.sendNetworkMessage("Shop", "close:" + " ," + player.getName());
                    DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.HASSHOP, false, true);
                    player.sendMessage(ChatColor.GRAY + "Checking shards for open shop..");
                    Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                        GameAPI.updatePlayerData(player.getUniqueId());
                        BankMechanics.getStorage(player.getUniqueId()).update();
                    }, 20);
                    player.sendMessage(ChatColor.GREEN + "Process finished, your shop has been closed safely");
                }
            }
        }
        return false;
    }

    private boolean isPlayer(String player) {
        String uuid = DatabaseAPI.getInstance().getUUIDFromName(player);
        return !uuid.equals("");
    }

}