package net.dungeonrealms.game.commands;

import net.dungeonrealms.game.commands.generic.BasicCommand;
import net.dungeonrealms.game.handlers.TipHandler;
import net.dungeonrealms.game.player.rank.Rank;
import org.bukkit.ChatColor;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Kieran Quigley (Proxying) on 18-Jun-16.
 */
public class CommandTips extends BasicCommand {

    public CommandTips(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length != 0) {
            sender.sendMessage(ChatColor.RED + "Invalid arguments, try /tips.");
            return true;
        }
        if (sender instanceof ConsoleCommandSender || sender instanceof BlockCommandSender) {
            TipHandler.getInstance().displayTipToPlayers();
            return true;
        }
        if (sender instanceof Player) {
            if (Rank.isGM((Player) sender)) {
                TipHandler.getInstance().displayTipToPlayers();
                return true;
            }
        }
        return true;
    }
}
