package net.dungeonrealms.game.command;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.game.handler.TipHandler;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * CommandTip - Dish out a random tip.
 * 
 * Redone on May 8th, 2017.
 * @author Kneesnap
 */
public class CommandTips extends BaseCommand {

    public CommandTips(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player) || Rank.isTrialGM((Player) sender))
        	TipHandler.showTip();
        return true;
    }
}
