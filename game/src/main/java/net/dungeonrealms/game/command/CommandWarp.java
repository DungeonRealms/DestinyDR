package net.dungeonrealms.game.command;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.game.world.teleportation.TeleportLocation;
import net.dungeonrealms.game.world.teleportation.Teleportation;
import net.dungeonrealms.game.world.teleportation.Teleportation.EnumTeleportType;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Kneesnap on 02/27/2017.
 */
public class CommandWarp extends BaseCommand {

    public CommandWarp() {
        super("warp", "/<command> <location>", "Warp to a defined region on the map.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player) || !Rank.isTrialGM((Player) sender))
            return false;
        
        if (args.length == 0) {
        	sender.sendMessage(ChatColor.RED + "/" + label + " <location>");
        	return false;
        }
        
        try{
        	TeleportLocation location = TeleportLocation.valueOf(args[0].toUpperCase());
        	Teleportation.getInstance().teleportPlayer(((Player)sender).getUniqueId(), EnumTeleportType.TELEPORT_BOOK, location);
        }catch(Exception e){
        	sender.sendMessage(ChatColor.RED + "Location not found.");
        	return false;
        }
        
        return true;
    }

}
