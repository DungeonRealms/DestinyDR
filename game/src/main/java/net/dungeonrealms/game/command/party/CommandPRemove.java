package net.dungeonrealms.game.command.party;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.game.affair.Affair;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Created by Nick on 11/9/2015.
 */
public class CommandPRemove extends BaseCommand {
    public CommandPRemove(String command, String usage, String description, List<String> aliases) {
        super(command, usage, description, aliases);
    }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {

        if (s instanceof ConsoleCommandSender) return false;

        Player player = (Player) s;

        if (!Affair.getInstance().isInParty(player)) {
            player.sendMessage(ChatColor.RED + "You must be in a party.");
            return true;
        }

        if (args.length == 1) {
            if (Affair.getInstance().isOwner(player)) {
                if (Bukkit.getPlayer(args[0]) == null) {
                    player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + args[0] + ChatColor.RED + " is offline.");
                } else {
                    Affair.getInstance().removeMember(Bukkit.getPlayer(args[0]), true);
                }
            } else {
                player.sendMessage(new String[] {
                        ChatColor.RED + "You are NOT the leader of your party.",
                        ChatColor.GRAY + "Type " + ChatColor.BOLD + "/pquit" + ChatColor.GRAY + " to quit your current party."
                });
            }
            return true;
        } else {
            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "Invalid Syntax." + ChatColor.RED + " /pkick <player>");
        }
        return false;
    }
}
