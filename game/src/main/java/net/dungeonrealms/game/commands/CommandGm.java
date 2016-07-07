package net.dungeonrealms.game.commands;

import net.dungeonrealms.game.commands.generic.BasicCommand;
import net.dungeonrealms.game.player.inventory.PlayerMenus;
import net.dungeonrealms.game.player.rank.Rank;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Brad on 23/06/2016.
 */

public class CommandGm extends BasicCommand {
    public CommandGm(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof Player) || !Rank.isGM((Player) sender)) return false;

        Player player = (Player) sender;
        PlayerMenus.openGameMasterTogglesMenu(player);

        return true;
    }

}
