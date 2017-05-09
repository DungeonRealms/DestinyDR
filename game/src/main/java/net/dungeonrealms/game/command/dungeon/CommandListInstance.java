package net.dungeonrealms.game.command.dungeon;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.game.mechanic.dungeons.Dungeon;
import net.dungeonrealms.game.mechanic.dungeons.DungeonManager;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandListInstance extends BaseCommand {

    public CommandListInstance() {
        super("listinstance", "/<command>", "List all instances.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!Rank.isTrialGM((Player) sender))
        	return true;

        sender.sendMessage(ChatColor.GREEN + "Listing all " + DungeonManager.getDungeons().size() + " active dungeons:");
        sender.sendMessage("");
        
        for (Dungeon d : DungeonManager.getDungeons()) {
        	sender.sendMessage(ChatColor.GOLD + "Dungeon: " + d.getType().getDisplayName());
        	sender.sendMessage(ChatColor.GOLD + "Players: " + d.getAllPlayers().size());
        	sender.sendMessage(ChatColor.GOLD + "Time: " + d.getTime());
        	sender.sendMessage("");
        }
        
        return true;
    }
}
