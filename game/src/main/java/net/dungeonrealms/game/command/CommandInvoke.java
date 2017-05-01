package net.dungeonrealms.game.command;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.game.mechanic.dungeons.Dungeon;
import net.dungeonrealms.game.mechanic.dungeons.DungeonManager;
import net.dungeonrealms.game.mechanic.dungeons.DungeonType;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

/**
 * Created by Nick on 10/20/2015.
 */
public class CommandInvoke extends BaseCommand {
    public CommandInvoke() {
        super("invoke", "/<command> [args]", "The invoke command.");
    }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String string, String[] args) {

        if (!(s instanceof Player) || !Rank.isTrialGM((Player)s))
        		return true;

        Player player = (Player) s;
        
        if (args.length == 0) {
        	player.sendMessage(ChatColor.RED + "Syntax: /invoke <dungeon>");
        	return true;
        }
        
        DungeonType dt = null;
        for (DungeonType type : DungeonType.values())
        	if (type.getInternalName().toLowerCase().contains(args[0].toLowerCase()))
        		dt = type;
        
        if (dt == null) {
        	player.sendMessage(ChatColor.RED + "Dungeon '" + args[0] + "' not found.");
        	return true;
        }
        
        Dungeon dungeon = DungeonManager.createDungeon(dt, Arrays.asList(player));
        
        if(args.length > 1 && args[1].equalsIgnoreCase("edit") && Rank.isHeadGM(player)) {
            dungeon.setEditMode(true);
            player.sendMessage(ChatColor.AQUA + "You have entered EDIT MODE.");
            player.sendMessage(ChatColor.AQUA + "All changes upon leaving of this instance will be saved to disk.");
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERDRAGON_GROWL, 1, 1);
        }
        return true;
    }
}
