package net.dungeonrealms.game.commands;

import net.dungeonrealms.common.game.commands.BasicCommand;
import net.dungeonrealms.game.player.json.JSONMessage;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Alan on 7/25/2016.
 */
public class CommandVote extends BasicCommand {
    public CommandVote(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        final JSONMessage normal = new JSONMessage(ChatColor.AQUA + "To vote for 15 ECASH and 5% EXP click ");
        normal.addURL(ChatColor.AQUA.toString() + ChatColor.BOLD + ChatColor.UNDERLINE + "HERE", ChatColor.AQUA, "http://minecraftservers.org/vote/174212");
        normal.addText(ChatColor.AQUA + ". You can vote each day to get the rewards!");
        normal.sendToPlayer((Player)sender);
        return true;
    }
}