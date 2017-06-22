package net.dungeonrealms.game.command.party;

import com.google.common.collect.Lists;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.game.affair.Affair;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Nick on 11/9/2015.
 */
public class CommandPLeave extends BaseCommand {
    public CommandPLeave() {
        super("pleave", "/<command>", "Remove player from party", null, Lists.newArrayList("pquit"));
    }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {

        if (s instanceof ConsoleCommandSender) return false;

        Player player = (Player) s;
        
        if (!Affair.isInParty(player)) {
        	player.sendMessage(ChatColor.RED + "You are not in a party.");
        	return true;
        }
        
        Affair.getParty(player).removePlayer(player, false);
        return true;
    }
}
