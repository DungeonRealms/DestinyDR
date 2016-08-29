package net.dungeonrealms.game.command;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Created by Alan Lu (dartaran) on 17-Jul-16.
 */
public class CommandBroadcast extends BaseCommand {

    public CommandBroadcast(String command, String usage, String description, List<String> aliases) {
        super(command, usage, description, aliases);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player && !Rank.isGM((Player) sender)) return true;
        String message = "";
        for (int i = 0; i < args.length; i++) {
            message += args[i] + " ";
        }
        message.trim();
        GameAPI.sendNetworkMessage("Broadcast", message);
        return false;
    }
}
