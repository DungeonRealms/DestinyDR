package net.dungeonrealms.commands;

import net.dungeonrealms.commands.generic.BasicCommand;
import net.dungeonrealms.party.Affair;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Nick on 11/9/2015.
 */
public class CommandPRemove extends BasicCommand {
    public CommandPRemove(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {

        if (s instanceof ConsoleCommandSender) return false;

        Player player = (Player) s;

        if (!Affair.getInstance().isInParty(player)) {
            player.sendMessage(ChatColor.RED + "You must be in a party to invoke this!");
            return true;
        }

        if (args.length == 1) {
            if (Affair.getInstance().isOwner(player)) {
                if (Bukkit.getPlayer(args[0]) == null) {
                    player.sendMessage(ChatColor.RED + "You must specify a player!");
                } else {
                    Affair.getInstance().removeMember(Bukkit.getPlayer(args[0]));
                }
            } else {
                player.sendMessage(ChatColor.RED + "You must be of the rank [Party Owner] to invoke this!");
            }
            return true;
        } else {
            player.sendMessage(ChatColor.RED + "/premove <playerName>");
        }
        return false;
    }
}
