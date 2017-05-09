package net.dungeonrealms.game.command;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.database.PlayerWrapper;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

/**
 * Created by Brad on 16/06/2016.
 */

public class CommandStaffChat extends BaseCommand {

    public CommandStaffChat() {
        super("staffchat", "/<command> [message]", "Send a message to the staff chat.", Arrays.asList("sc", "s"));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return false;
        Player player = (Player) sender;

        if (!Rank.isPMOD(player))
        	return false;
        
        if (args.length == 0) {
        	sender.sendMessage("/sc|staffchat|s [message]");
        	return true;
        }

        GameAPI.sendNetworkMessage("StaffMessage", "&6<SC> &6(" + DungeonRealms.getInstance().shardid + ") " + PlayerWrapper.getWrapper((Player) sender).getChatName() + "&6" + String.join(" ", args));
        return true;
    }
}
