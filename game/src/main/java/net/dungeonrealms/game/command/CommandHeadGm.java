package net.dungeonrealms.game.command;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.game.player.inventory.PlayerMenus;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Brad on 26/12/2016.
 */

public class CommandHeadGm extends BaseCommand {
    public CommandHeadGm() {
        super("headgm", "/<command> [password]", "Displays the Head Game Master toggles.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof Player) || !Rank.isHeadGM((Player) sender)) return false;

        // We have a security precaution in-place, the word for arg[0] must match otherwise the user doesn't know the key.
        // Without the correct key they won't be able to access to head GM tools and we can pretend they've not got access.
        if (args.length != 1 || !args[0].equalsIgnoreCase("nitro")) return false;

        Player player = (Player) sender;
        PlayerMenus.openHeadGameMasterTogglesMenu(player);

        return true;
    }

}
