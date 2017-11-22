package net.dungeonrealms.game.command;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.command.BaseCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandRules extends BaseCommand{
    public CommandRules(){
        super("rules", "/<command>", "Gives link to the rules page.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            GameAPI.sendRulesMessage((Player) sender);
        }
        return true;
    }
}
