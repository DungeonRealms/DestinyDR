package net.dungeonrealms.game.commands;

import net.dungeonrealms.game.commands.generic.BasicCommand;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.mongo.EnumOperators;
import net.dungeonrealms.game.player.rank.Rank;
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
            DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$INC, EnumData.ECASH, 2000, true);
            player.sendMessage(ChatColor.YELLOW + "You also obtained 2000 ECash in order to assist us in testing out ECash!");
        } else {
            player.sendMessage(ChatColor.RED + "You've already redeemed your rank!\n Note: You are considered a 'Founder' and will receive 'Founder Perks' shortly before or after open beta release!");
        }

        return false;
    }
}
