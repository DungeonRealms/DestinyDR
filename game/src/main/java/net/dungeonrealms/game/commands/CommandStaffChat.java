package net.dungeonrealms.game.commands;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.commands.BasicCommand;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.game.player.chat.GameChat;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Created by Brad on 16/06/2016.
 */

public class CommandStaffChat extends BasicCommand {

    public CommandStaffChat(String command, String usage, String description, List<String> aliases) {
        super(command, usage, description, aliases);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return false;
        Player player = (Player) sender;

        if (!Rank.isPMOD(player) && !Rank.isSupport(player)) return false;

        StringBuilder message;
        if (args.length > 0) {
            message = new StringBuilder(args[0]);

            for (int arg = 1; arg < args.length; arg++)
                message.append(" ").append(args[arg]);

            GameAPI.sendNetworkMessage("StaffMessage", "&6<SC> &6(" + DungeonRealms.getInstance().shardid + ") " + GameChat.getPreMessage((Player) sender) + "&6" + message);
        } else sender.sendMessage("/sc|staffchat|s [message]");
        return true;
    }
}
