package net.dungeonrealms.game.command;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.database.PlayerWrapper;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

/**
 * Created by Brad on 16/06/2016.
 */
public class CommandDevChat extends BaseCommand {

    public CommandDevChat() {
        super("devchat", "/<command> [message]", "Send a message to the developer chat.", Arrays.asList("dc", "d"));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player))
            return false;

        Player player = (Player) sender;
        if (!Rank.isDev(player))
            return false;

        StringBuilder message;
        if (args.length > 0) {
            message = new StringBuilder(args[0]);

            for (int arg = 1; arg < args.length; arg++)
                message.append(" ").append(args[arg]);

            GameAPI.sendIngameDevMessage(ChatColor.DARK_AQUA + "<DC> (" + DungeonRealms.getInstance().shardid + ") " + PlayerWrapper.getWrapper((Player) sender).getChatName() + ChatColor.DARK_AQUA + message);
        } else sender.sendMessage("/dc|devchat|d [message]");

        return true;
    }
}
