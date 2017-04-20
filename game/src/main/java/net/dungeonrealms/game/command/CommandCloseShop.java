package net.dungeonrealms.game.command;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.data.EnumOperators;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.player.banks.BankMechanics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

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
                    UUID uuid = other.getUniqueId();
                    GameAPI.sendNetworkMessage("Shop", "close:" + " ," + playerName);
                    otherWrapper.setShopOpened(false);
                    commandSender.sendMessage(ChatColor.GREEN + "Safely closed " + playerName + "'s shop");
                    return false;
                }
            }
        } else {
            //  A player types /closeshop
            Player player = (Player) commandSender;
            if (GameAPI.isShop(player.getOpenInventory())) {
                player.sendMessage(ChatColor.RED + "Illegal inventory session ID");
                player.sendMessage(ChatColor.RED + "Close your shop GUI to close your shop");
                return false;
            } else {
                GameAPI.sendNetworkMessage("Shop", "close:" + " ," + player.getName());
                DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.HASSHOP, false, true);
                player.sendMessage(ChatColor.GRAY + "Checking shards for open shop..");
                Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                    GameAPI.updatePlayerData(player.getUniqueId());
                    BankMechanics.getInstance().getStorage(player.getUniqueId()).update();
                }, 20);
                player.sendMessage(ChatColor.GREEN + "Process finished, your shop has been closed safely");
            }
        }

        return false;
    }


}