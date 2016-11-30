package net.dungeonrealms.lobby.command;

import net.dungeonrealms.common.frontend.command.BaseCommand;
import net.dungeonrealms.common.old.game.database.player.rank.Rank;
import net.dungeonrealms.common.old.network.bungeecord.BungeeUtils;
import net.dungeonrealms.lobby.ServerLobby;
import net.dungeonrealms.lobby.misc.gui.ShardGUI;
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

        if (args.length == 0 || !Rank.isGM(player)) {
            new ShardGUI(player).openInventory(player);
            return true;
        }


        if (args.length > 0) {
            player.sendMessage(ChatColor.YELLOW + "Sending you to " + ChatColor.BOLD + ChatColor.UNDERLINE + args[0] + ChatColor.YELLOW + "...");

            Bukkit.getScheduler().scheduleSyncDelayedTask(ServerLobby.getServerLobby(),
                    () -> BungeeUtils.sendToServer(player.getName(), args[0]), 10);
        }

        return true;
    }

}
