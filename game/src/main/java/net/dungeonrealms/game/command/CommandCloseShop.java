package net.dungeonrealms.game.command;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.player.banks.BankMechanics;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandCloseShop extends BaseCommand {

    public CommandCloseShop() {
        super("closeshop", "/<command>", "Close your shop");
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (!(commandSender instanceof Player)) return false;
        Player sender = (Player) commandSender;
        if (args.length == 1 && (Rank.isTrialGM((Player) commandSender))) {
            String playerName = args[0];
            Player other = Bukkit.getPlayer(playerName);
            if (other == null) {
                sender.sendMessage(ChatColor.RED + "Player is not on this shard!");
                return false;
            }

            PlayerWrapper otherWrapper = PlayerWrapper.getPlayerWrapper(other);

            if (otherWrapper == null) {
                sender.sendMessage(ChatColor.RED + "Player is not on this shard!");
                return false;
            }

            boolean hasShop = otherWrapper.isShopOpened();
            if (!hasShop) {
                commandSender.sendMessage(ChatColor.RED + "Shop not detected");
                return false;
            }

            if (other.isOnline()) {
                // Check if the player is viewing his shop
                if (GameAPI.isShop(other.getOpenInventory())) {
                    // Don't allow the shop to be removed if the owner is viewing his shop GUI
                    commandSender.sendMessage(ChatColor.RED + other.getName() + " is currently viewing his shop, action denied");
                    return false;
                } else {
                    // If he isn't viewing his shop GUI, continue
                    GameAPI.sendNetworkMessage("Shop", "close:" + " ," + playerName);
                    otherWrapper.setShopOpened(false);
                    commandSender.sendMessage(ChatColor.GREEN + "Safely closed " + playerName + "'s shop");
                    return false;
                }
            }
        } else {
            Player player = (Player) commandSender;
            PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
            if (GameAPI.isShop(player.getOpenInventory())) {
                player.sendMessage(ChatColor.RED + "Illegal inventory session ID");
                player.sendMessage(ChatColor.RED + "Close your shop GUI to close your shop");
                return false;
            } else {
                Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                    //Update the collection bin..
                    BankMechanics.getStorage(player).update(false, true, bin -> {
                        wrapper.setShopOpened(false);
                        player.sendMessage(ChatColor.GREEN + "Process finished, your shop has been closed safely.");
                    });
                }, 20);
            }
        }

        return false;
    }


}