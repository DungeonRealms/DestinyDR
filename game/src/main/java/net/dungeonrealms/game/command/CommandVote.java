package net.dungeonrealms.game.command;


import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.game.player.json.JSONMessage;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Alan on 7/25/2016.
 */
public class CommandVote extends BaseCommand {
    public CommandVote() {
        super("vote", "/<command>", "Gives the link to vote for rewards.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return true;

        GameAPI.sendVoteMessage((Player)sender);
        return true;
    }
}