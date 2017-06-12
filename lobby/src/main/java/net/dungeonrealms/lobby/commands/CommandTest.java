package net.dungeonrealms.lobby.commands;

import net.dungeonrealms.common.game.database.player.rank.Rank;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.network.bungeecord.BungeeUtils;
import net.dungeonrealms.lobby.Lobby;
import net.md_5.bungee.api.ChatColor;

public class CommandTest extends BaseCommand {

    public CommandTest() {
        super("gotest", "/<command>", "Send yourself to the test server.");
    }

    public static boolean subOnly = true;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player) || !Lobby.getInstance().isLoggedIn((Player)sender))
            return false;

        // Player must a PMOD+.
        if (subOnly && !Rank.isSubscriber((Player) sender)) {
            sender.sendMessage(ChatColor.RED + "You must be a" + ChatColor.UNDERLINE + " subscriber" + ChatColor.RED + " to connect to this shard!");
            return false;
        }

        // Send the user to the new test lobby.
        sender.sendMessage(ChatColor.YELLOW + ChatColor.ITALIC.toString() + "Attempting to send you to the new test server...");
        BungeeUtils.sendToServer(sender.getName(), "newhub");

        return true;
    }
}
