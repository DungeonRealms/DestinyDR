package net.dungeonrealms.game.command.toggle;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.game.mechanic.PlayerManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Created by Brad on 11/06/2016.
 */

public class CommandToggleDebug extends BaseCommand {

    public CommandToggleDebug(String command, String usage, String description, List<String> aliases) {
        super(command, usage, description, aliases);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return false;
        Player player = (Player) sender;

        PlayerManager.PlayerToggles toggle = PlayerManager.PlayerToggles.DEBUG;
        toggle.setToggleState(player, !(boolean) DatabaseAPI.getInstance().getData(toggle.getDbField(), player.getUniqueId()));

        return true;
    }

}