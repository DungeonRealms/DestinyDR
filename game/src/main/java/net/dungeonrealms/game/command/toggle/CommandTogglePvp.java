package net.dungeonrealms.game.command.toggle;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.mechanic.PlayerManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Brad on 11/06/2016.
 */

public class CommandTogglePvp extends BaseCommand {

    public CommandTogglePvp(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return false;
        Player player = (Player) sender;

        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
        if(wrapper == null) return false;

        wrapper.getToggles().setPvp(!wrapper.getToggles().isPvp());

        return true;
    }

}