package net.dungeonrealms.lobby.commands;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.common.network.bungeecord.BungeeUtils;
import net.dungeonrealms.lobby.Lobby;
import net.dungeonrealms.lobby.ShardSelector;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Class written by APOLLOSOFTWARE.IO on 7/13/2016
 */
public class CommandShard extends BaseCommand {

    public CommandShard(String command, String usage, String description, List<String> aliases) {
        super(command, usage, description, aliases);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return false;
        Player player = (Player) sender;

        if (!Lobby.getInstance().isLoggedIn(player)) {
            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + " >> " + ChatColor.RED + "You must login before you can do this.");
            return false;
        }

        if (args.length == 0 || !Rank.isTrialGM(player)) {
            if (Lobby.chatCallbacks.containsKey(player.getUniqueId())) {
                player.sendMessage(ChatColor.RED + "Please finish entering your Pack Bypass code.");
                player.sendMessage(ChatColor.GRAY + "Since you decided to not use our Custom Resource Pack, you must enter a code before logging in.");
                player.sendMessage(ChatColor.GRAY + "If you'd like to not have to enter this code every time, simply install our resource pack by using /pack.");
                return true;
            }
            if(player.hasMetadata("savingData")) {
                player.sendMessage(ChatColor.RED + "Your data is still saving! Please try again!");
                return true;
            }
            new ShardSelector(player).open(player);
            //CharacterSelector.openCharacterSelector(player);
            return true;
        }


        if (args.length > 0) {
            player.sendMessage(ChatColor.YELLOW + "Sending you to " + ChatColor.BOLD + ChatColor.UNDERLINE + args[0] + ChatColor.YELLOW + "...");

            Bukkit.getScheduler().scheduleSyncDelayedTask(Lobby.getInstance(),
                    () -> BungeeUtils.sendToServer(player.getName(), args[0]), 10);
        }

        return true;
    }

}
