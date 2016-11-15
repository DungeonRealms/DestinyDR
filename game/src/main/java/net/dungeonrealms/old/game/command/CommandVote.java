package net.dungeonrealms.old.game.command;

import net.dungeonrealms.common.frontend.command.BaseCommand;
import net.dungeonrealms.common.old.game.database.player.rank.Rank;
import net.dungeonrealms.old.game.player.json.JSONMessage;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Alan on 7/25/2016.
 */
public class CommandVote extends BaseCommand {
    public CommandVote(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;

        int ecashAmount = 15;
        if (Rank.isSubscriberPlus(player)) {
           ecashAmount = 25;
        } else if (Rank.isSubscriber(player)) {
           ecashAmount = 20;
        }

        final JSONMessage message = new JSONMessage("To vote for " + ecashAmount + " ECASH & 5% EXP, click ", ChatColor.AQUA);
        message.addURL(ChatColor.AQUA.toString() + ChatColor.BOLD + ChatColor.UNDERLINE + "HERE", ChatColor.AQUA, "http://minecraftservers.org/vote/174212");
        message.sendToPlayer(player);
        return true;
    }
}