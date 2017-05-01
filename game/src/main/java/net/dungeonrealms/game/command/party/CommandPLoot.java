package net.dungeonrealms.game.command.party;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.game.affair.Affair;
import net.dungeonrealms.game.affair.party.PLootMenu;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandPLoot extends BaseCommand {

    public CommandPLoot() {
        super("ploot", "/<command>", "Change party loot mode.", "partyloot");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player)) 
        	return true;
        Player player = (Player) sender;
        
        if (!Affair.isInParty(player)) {
        	player.sendMessage(ChatColor.RED + "You must be in a party to use this.");
        	return true;
        }
        
        try {
        	new PLootMenu(player, Affair.getParty(player)).open(player);
        } catch (Exception e) {
        	e.printStackTrace();
        	player.sendMessage(ChatColor.RED + "There was an error while running this command.");
        }
        return false;
    }
}
