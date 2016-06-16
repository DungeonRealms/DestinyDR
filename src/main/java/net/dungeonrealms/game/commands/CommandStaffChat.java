package net.dungeonrealms.game.commands;

import net.dungeonrealms.game.commands.generic.BasicCommand;
import net.dungeonrealms.game.player.rank.Rank;
import org.bukkit.ChatColor;
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

        player.sendMessage(ChatColor.RED + "Currently unavailable, coming soon.");
        // @todo: Create SC!

        return true;
    }
}
