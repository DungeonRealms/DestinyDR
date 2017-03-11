package net.dungeonrealms.game.command.dungeon;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.game.mechanic.DungeonManager;
import net.dungeonrealms.game.mechanic.DungeonManager.DungeonType;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Alan on 8/2/2016.
 */
public class BossSpawn extends BaseCommand {
    public BossSpawn(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        final Player player = (Player) sender;
        if (!(Rank.isGM(player)))
        	return true;

        if (args.length == 0)
        	player.sendMessage(ChatColor.RED + "Syntax: /bspawn <boss>");
        
        for(DungeonType type : DungeonManager.DungeonType.values()){
        	if(type.getBossName().toUpperCase().contains(args[0].toUpperCase())){
        		type.spawnBoss(player.getLocation(), true);
        		GameAPI.sendDevMessage(ChatColor.GREEN.toString() + ChatColor.BOLD  +"<DEV> " + ChatColor.GREEN + "Spawned " + type.getBossName() + ChatColor.GREEN + " boss on {SERVER} in world " + ChatColor.BOLD + player.getWorld().getName());
        		return true;
        	}
        }
        player.sendMessage(ChatColor.RED + "Boss not found.");
        return true;
    }
}
