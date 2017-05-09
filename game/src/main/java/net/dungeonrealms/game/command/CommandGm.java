package net.dungeonrealms.game.command;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.game.player.inventory.PlayerMenus;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Brad on 23/06/2016.
 */

public class CommandGm extends BaseCommand {
    public CommandGm() {
        super("gm", "/<command> [args]", "Displays the Game Master toggles.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof Player) || !Rank.isTrialGM((Player) sender)) return false;

        Player player = (Player) sender;
        PlayerMenus.openGameMasterTogglesMenu(player);

        return true;
    }

}
