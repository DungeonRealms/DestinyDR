package net.dungeonrealms.game.command;

import com.google.common.collect.Lists;
import net.dungeonrealms.common.game.command.BaseCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;

/**
 * Created by Rar349 on 7/14/2017.
 */
public class CommandLobby extends BaseCommand {

    public CommandLobby() {
        super("lobby", "/lobby", "Send you to the lobby",Collections.singletonList("hub"));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(!(sender instanceof Player)) return true;
        return true;
    }
}
