package net.dungeonrealms.commands;

import net.dungeonrealms.commands.generic.BasicCommand;
import net.dungeonrealms.rank.Rank;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Nick on 11/9/2015.
 */
public class CommandRedeem extends BasicCommand {
    public CommandRedeem(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (sender instanceof ConsoleCommandSender) return false;

        Player player = (Player) sender;

        if (!Rank.getInstance().getRank(player.getUniqueId()).getName().equalsIgnoreCase("SUB")) {
            Rank.getInstance().setRank(player.getUniqueId(), "SUB");
        } else {
            player.sendMessage(ChatColor.RED + "You've already redeemed your rank!\n Note: You are considered a 'Founder' and will receive 'Founder Perks' shortly before or after open beta release!");
        }

        return false;
    }
}
