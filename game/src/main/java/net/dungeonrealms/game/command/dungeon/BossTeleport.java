package net.dungeonrealms.game.command.dungeon;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.game.mechanic.dungeons.Dungeon;
import net.dungeonrealms.game.mechanic.dungeons.DungeonManager;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Kieran Quigley (Proxying) on 17-Jun-16.
 */
public class BossTeleport extends BaseCommand {
    public BossTeleport(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof BlockCommandSender))
        	return true;

        BlockCommandSender bcs = (BlockCommandSender) sender;
        
        if (!DungeonManager.isDungeon(bcs.getBlock().getLocation())) {
        	sender.sendMessage(ChatColor.RED + "This command can only be run in a dungeon.");
        	return true;
        }
        
        Dungeon d = DungeonManager.getDungeon(bcs.getBlock().getWorld());

        if (!d.canBossSpawn(true) && (d.getWorld().getPlayers().stream().filter(p -> Rank.isDev(p)).count() != d.getWorld().getPlayers().size())) {
        	d.announce(ChatColor.RED + "You must kill " + ChatColor.GOLD + d.getKillsLeft() + ChatColor.RED + " more mobs.");
        	return true;
        }
        
        Location spawn = d.getType().getBoss().getLocation(d.getWorld());

        float yaw = args.length >= 4 ? Float.parseFloat(args[3]) : 0;
        float pitch = args.length >= 5 ? Float.parseFloat(args[4]) : 0;

        if (args.length >= 3)
        	spawn = new Location(bcs.getBlock().getWorld(), Double.parseDouble(args[0]), Double.parseDouble(args[1]), Double.parseDouble(args[2]), yaw, pitch);

        for (Player player : bcs.getBlock().getWorld().getPlayers()) {
            player.teleport(spawn.clone().add(0, 2, 0));
            player.setFallDistance(0.0F);
        }
        
        d.spawnBoss(d.getType().getBoss());
        bcs.getBlock().setType(Material.AIR); // Remove the command block.
        return true;
    }
}
