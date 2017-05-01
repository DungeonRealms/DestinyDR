package net.dungeonrealms.game.command.dungeon;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.game.mechanic.dungeons.BossType;
import net.dungeonrealms.game.mechanic.dungeons.Dungeon;
import net.dungeonrealms.game.mechanic.dungeons.DungeonManager;

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
        if (!Rank.isGM(player))
        	return true;
        
        if (!DungeonManager.isDungeon(player)) {
        	player.sendMessage(ChatColor.RED + "You must be in a dungeon to use this.");
        	return true;
        }

        if (args.length == 0)
        	player.sendMessage(ChatColor.RED + "Syntax: /bspawn <boss>");
        
        for (BossType bt : BossType.values()) {
        	if (!bt.getName().toLowerCase().contains(args[0].toLowerCase()))
        		continue;
        	Dungeon d = DungeonManager.getDungeon(player.getWorld());
        	d.getSpawnedBosses().remove(bt); // Allow respawning the boss if it despawned or something.
        	d.spawnBoss(bt, player.getLocation());
        	GameAPI.sendDevMessage(ChatColor.GREEN + "Spawned " + player.getName() + " " + bt.getName() + " on {SERVER}");
        	return true;
        }
        
        player.sendMessage(ChatColor.RED + "Boss not found.");
        return true;
    }
}
