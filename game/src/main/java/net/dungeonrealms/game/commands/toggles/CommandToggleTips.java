package net.dungeonrealms.game.commands.toggles;

import net.dungeonrealms.game.commands.generic.BasicCommand;
import net.dungeonrealms.game.mechanics.PlayerManager;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Kieran Quigley (Proxying) on 18-Jun-16.
 */
public class CommandToggleTips extends BasicCommand {
    public CommandToggleTips(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return false;
        Player player = (Player) sender;

        PlayerManager.PlayerToggles toggle = PlayerManager.PlayerToggles.TIPS;
        toggle.setToggleState(player, !(boolean) DatabaseAPI.getInstance().getData(toggle.getDbField(), player.getUniqueId()));

        return true;
    }
}
