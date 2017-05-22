package net.dungeonrealms.game.command;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.common.game.database.player.PlayerRank;
import net.dungeonrealms.database.PlayerWrapper;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

/**
 * Created by Brad on 16/06/2016.
 */
public class CommandStaffChat extends BaseCommand {

	private PlayerRank rank;
	private String prefix;
	
	public CommandStaffChat(String name, PlayerRank minRank) {
		this(name, minRank, minRank.getPrefix());
	}
	
    public CommandStaffChat(String name, PlayerRank rank, String prefix) {
        super(name + "chat", "/<command> <message>", "Send a message to the " + name + " chat.", Arrays.asList(name.substring(0, 1) + "c"));
        this.rank = rank;
        this.prefix = prefix;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return false;
        Player player = (Player) sender;

        if (!Rank.getRank(player).isAtLeast(rank))
        	return true;
        
        if (args.length == 0) {
        	sender.sendMessage(getUsage());
        	return true;
        }

        GameAPI.sendStaffMessage(rank, ChatColor.GOLD + "<" + this.prefix + "> ({SERVER}&) " + PlayerWrapper.getWrapper((Player) sender).getChatName() + "&6: " + String.join(" ", args));
        return true;
    }
}
