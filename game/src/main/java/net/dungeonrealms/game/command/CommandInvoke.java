package net.dungeonrealms.game.command;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.game.affair.Affair;
import net.dungeonrealms.game.mechanic.dungeons.DungeonManager;
import net.dungeonrealms.game.mechanic.dungeons.DungeonManager.DungeonType;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Nick on 10/20/2015.
 */
public class CommandInvoke extends BaseCommand {
    public CommandInvoke(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String string, String[] args) {

        if (s instanceof ConsoleCommandSender) return false;

        Player player = (Player) s;
        if (!Rank.isTrialGM(player)) {
            return false;
        }
        
        if (args.length > 0) {
            if (DungeonManager.getInstance().canCreateInstance()) {

            	DungeonManager.DungeonObject object = null;
            	for(DungeonType type : DungeonManager.DungeonType.values())
                	if(type.name().contains(args[0].toUpperCase()))
                		object = invokeDungeon(player, type);
                
                if(object != null){
                    if(args.length == 2 && args[1].equalsIgnoreCase("edit") && Rank.isHeadGM(player)){
                        object.setEditMode(true);
                        player.sendMessage(ChatColor.RED + "You have entered EDIT MODE.");
                        player.sendMessage(ChatColor.RED + "All changes upon leaving of this instance will be saved to disk.");
                        player.playSound(player.getLocation(), Sound.ENTITY_ENDERDRAGON_GROWL, 1, 1);
                    }
                }else{
                	player.sendMessage(ChatColor.RED + "Dungeon not found.");
                }
            } else {
                player.sendMessage(ChatColor.RED + "There are no dungeons available at this time.");
            }
        }else{
        	player.sendMessage(ChatColor.RED + "Syntax: /invoke <dungeon>");
        }
        return false;
    }
    
    private DungeonManager.DungeonObject invokeDungeon(Player player, DungeonManager.DungeonType type) {
    	if (!Affair.getInstance().isInParty(player))
    		Affair.getInstance().createParty(player);
    	Map<Player, Boolean> partyList = new HashMap<>();
    	for (Player player1 : Affair.getInstance().getParty(player).get().getMembers())
    		partyList.put(player1, player1.getLocation().distanceSquared(player.getLocation()) <= 200);
    	partyList.put(player, true);
    	return DungeonManager.getInstance().createNewInstance(type, partyList);
    }
}
