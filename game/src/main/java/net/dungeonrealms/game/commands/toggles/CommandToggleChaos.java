package net.dungeonrealms.game.commands.toggles;

import net.dungeonrealms.game.commands.generic.BasicCommand;
import net.dungeonrealms.game.database.DatabaseAPI;
import net.dungeonrealms.game.mechanics.PlayerManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Brad on 11/06/2016.
 */

public class CommandToggleChaos extends BasicCommand {

    public CommandToggleChaos(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return false;
        Player player = (Player) sender;

        PlayerManager.PlayerToggles toggle = PlayerManager.PlayerToggles.CHAOTIC_PREVENTION;
        toggle.setToggleState(player, !(boolean) DatabaseAPI.getInstance().getData(toggle.getDbField(), player.getUniqueId()));

        return true;
    }

}