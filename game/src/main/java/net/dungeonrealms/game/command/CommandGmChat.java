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

public class CommandGmChat extends BaseCommand {

    public CommandGmChat() {
        super("gmchat", "/<command> [message]", "Send a message to the GM chat.", Arrays.asList("gc"));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player))
            return false;

        Player player = (Player) sender;
        if (!Rank.isTrialGM(player))
            return false;

        StringBuilder message;
        if (args.length > 0) {
            message = new StringBuilder(args[0]);

            for (int arg = 1; arg < args.length; arg++)
                message.append(" ").append(args[arg]);

            GameAPI.sendNetworkMessage("IGN_GMMessage", ChatColor.AQUA + "<GM> (" + DungeonRealms.getInstance().shardid + ") " + PlayerWrapper.getWrapper((Player) sender).getChatName() + ChatColor.AQUA + message);
        } else sender.sendMessage("/gc|gmchat [message]");

        return true;
    }
}
