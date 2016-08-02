package net.dungeonrealms.game.commands.party;

import net.dungeonrealms.common.game.commands.BasicCommand;
import net.dungeonrealms.game.affair.Affair;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Created by Nick on 11/9/2015.
 */
public class CommandPLeave extends BasicCommand {
    public CommandPLeave(String command, String usage, String description, List<String> aliases) {
        super(command, usage, description, aliases);
    }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {

        if (s instanceof ConsoleCommandSender) return false;

        Player player = (Player) s;

        if (Affair.getInstance().isInParty(player)) {
            Affair.getInstance().removeMember(player, false);
        } else {
            player.sendMessage(ChatColor.RED + "You are not in a party.");
        }

        return false;
    }
}
