package net.dungeonrealms.game.commands;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.commands.BasicCommand;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.data.EnumOperators;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.game.achievements.Achievements;
import net.dungeonrealms.game.player.json.JSONMessage;
import org.bukkit.Bukkit;
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
        Player player = (Player) sender;

        String rank = Rank.getInstance().getRank(player.getUniqueId());

        String preMessage = "";
        switch (rank.toLowerCase()) {
            case "default":
                preMessage = ChatColor.AQUA + "To vote for 15 ECASH & 5% EXP, click ";
                break;
            case "sub":
                preMessage = ChatColor.AQUA + "To vote for 25 ECASH & 5% EXP, click ";
                break;
            case "sub+":
            case "sub++":
                preMessage = ChatColor.AQUA + "To vote for 25 ECASH & 5% EXP, click ";
                break;
            default:
                preMessage = ChatColor.AQUA + "To vote for 15 ECASH & 5% EXP, click ";
                break;
        }
        final JSONMessage message = new JSONMessage(preMessage, ChatColor.AQUA);
        message.addURL(ChatColor.AQUA.toString() + ChatColor.BOLD + ChatColor.UNDERLINE + "HERE", ChatColor.AQUA, "http://minecraftservers.org/vote/174212");
        message.sendToPlayer(player);
        return true;
    }
}