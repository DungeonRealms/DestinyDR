package net.dungeonrealms.game.command;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.command.BaseCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandRiftTime extends BaseCommand{
    public CommandRiftTime(){
        super("rifttime", "/<command>", "Gives time of the current rift.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            GameAPI.sendRiftMessage((Player)sender);
        }
        return true;
    }
}
