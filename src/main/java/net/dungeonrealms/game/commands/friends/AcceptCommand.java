package net.dungeonrealms.game.commands.friends;

import net.dungeonrealms.game.commands.generic.BasicCommand;
import net.dungeonrealms.game.handlers.FriendHandler;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Created by chase on 7/7/2016.
 */
public class AcceptCommand extends BasicCommand {

    public AcceptCommand(String command, String usage, String description, List<String> aliases) {
        super(command, usage, description, aliases);
    }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String string, String[] args) {
        if (s instanceof ConsoleCommandSender) return false;
        Player player = (Player) s;

        String name = args[0];

        if (!FriendHandler.getInstance().isPendingFrom(player.getUniqueId(), name)) {
            player.sendMessage(ChatColor.RED + "You're not pending a request from that user.");
            return false;
        }

        FriendHandler.getInstance().acceptFriend(player.getUniqueId(), name);
        return false;
    }
}
