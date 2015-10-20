package net.dungeonrealms.commands;

import net.dungeonrealms.mechanics.DungeonManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;

/**
 * Created by Nick on 10/20/2015.
 */
public class CommandInvoke implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender s, Command cmd, String string, String[] args) {

        if (s instanceof ConsoleCommandSender) return false;

        Player player = (Player) s;

        if(args.length > 0) {
            if(args[0].equalsIgnoreCase("bandittrove")) {
                DungeonManager.getInstance().createNewInstance(DungeonManager.DungeonType.BANDIT_TROVE, Collections.singletonList(player));
            }
        }

        return false;
    }
}
