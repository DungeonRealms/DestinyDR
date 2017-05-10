package net.dungeonrealms.game.command;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.common.game.database.player.Rank.PlayerRank;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.database.UpdateType;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Brad on 09/06/2016.
 */
public class CommandSetRank extends BaseCommand {
    public CommandSetRank(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (sender instanceof Player && !Rank.isGM((Player) sender))
        	return false;
        
        PlayerWrapper executor = PlayerWrapper.getWrapper((Player) sender);
        PlayerRank newRank = PlayerRank.getFromPrefix(args[1]);
        
        if (newRank == null) {
        	sender.sendMessage(ChatColor.RED + "Invalid usage: /setrank <name> <rank>");
        	String ranks = "";
        	for (int i = 0; i <= executor.getRank().ordinal(); i++) {
        		PlayerRank r = PlayerRank.values()[i];
        		ranks += r.getChatColor() + (r == PlayerRank.DEFAULT ? "DEFAULT" : r.getPrefix()) + ChatColor.GREEN + " | ";
        	}
        	
            sender.sendMessage(ChatColor.GREEN + ChatColor.BOLD.toString() + "Ranks: " + ranks);
        }
        
        if (newRank.ordinal() > executor.getRank().ordinal()) {
        	sender.sendMessage(ChatColor.RED + "You are not authorized to set a player to this rank.");
        	return true;
        }
        
        SQLDatabaseAPI.getInstance().getUUIDFromName(args[0], false, uuid -> {
            if (uuid == null) {
                sender.sendMessage(ChatColor.RED + "This player has never logged into Dungeon Realms!");
                return;
            }

            PlayerWrapper.getPlayerWrapper(uuid, false, true, wrapper -> {
                
                PlayerRank currentRank = wrapper.getRank();
                
                if (currentRank.ordinal() > executor.getRank().ordinal()) {
                	sender.sendMessage(ChatColor.RED + "You do not have permission to change this user's rank.");
                	return;
                }
                
                if (newRank.isSUB())
                	wrapper.setRankExpiration(0);

                sender.sendMessage(ChatColor.GREEN + "Setting rank of " + ChatColor.BOLD + ChatColor.UNDERLINE + args[0] + ChatColor.GREEN + " to " + ChatColor.BOLD + ChatColor.UNDERLINE + (newRank == PlayerRank.DEFAULT ? "DEFAULT" : newRank.getPrefix()) + ChatColor.GREEN + ".");
                wrapper.setRank(newRank);

                Rank.setRank(uuid, newRank.getInternalName(), done -> GameAPI.updatePlayerData(uuid, UpdateType.RANK));
            });
        });

        return true;
    }

}
