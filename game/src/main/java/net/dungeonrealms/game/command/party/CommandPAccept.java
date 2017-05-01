package net.dungeonrealms.game.command.party;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.game.affair.Affair;
import net.dungeonrealms.game.affair.party.Party;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Nick on 11/9/2015.
 */
public class CommandPAccept extends BaseCommand {
	
    public CommandPAccept() {
        super("paccept", "/<command>", "Accept a party invite.");
    }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {
        if (!(s instanceof Player))
        	return true;

        Player player = (Player) s;
        Party party = Affair.getInvitations().get(player);
        
        if (party == null) {
        	player.sendMessage(ChatColor.RED + "You do not have any pending invitations!");
        	return true;
        }
        
        party.addMember(player);
        return true;
    }
}
