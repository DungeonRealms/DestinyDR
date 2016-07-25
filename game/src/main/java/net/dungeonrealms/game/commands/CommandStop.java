package net.dungeonrealms.game.commands;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.commands.BasicCommand;
import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.game.world.realms.Realms;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Created by Chase on Nov 6, 2015
 */
public class CommandStop extends BasicCommand {
    public CommandStop(String command, String usage, String description, List<String> aliases) {
        super(command, usage, description, aliases);
    }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String string, String[] args) {
        if (s instanceof Player) {
            Player player = (Player) s;
            if (!Rank.isDev(player)) return false;
        }

        if (Realms.getInstance().realmsAreUpgrading()) {
            s.sendMessage(ChatColor.RED + "Realms are still being upgraded!");
            return true;
        }

        if (args.length == 1)
            if (args[0].equalsIgnoreCase("all")) GameAPI.sendStopAllServersPacket();

        GameAPI.stopGame();
        return false;
    }
}
