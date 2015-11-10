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

        if (Bukkit.getPlayer(args[0]) == null) {
            player.sendMessage(ChatColor.RED + "You must specify a player!");
            return true;
        }

        if (Affair.getInstance().isInParty(player)) {
            Affair.AffairO party = Affair.getInstance().getParty(player).get();
            if (party.getOwner().equals(player)) {
                Affair.getInstance().removeMember(Bukkit.getPlayer(args[0]));
            } else {
                player.sendMessage(ChatColor.RED + "You must be a party owner!");
            }

        } else {
            player.sendMessage(ChatColor.RED + "You are not in a party!");
        }

        return false;
    }
}
