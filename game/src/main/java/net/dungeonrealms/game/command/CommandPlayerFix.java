package net.dungeonrealms.game.command;

import com.mongodb.client.model.Filters;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.DatabaseInstance;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.data.EnumOperators;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.common.network.ShardInfo;

import org.bson.Document;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * Created by Nick on 12/2/2015.
 */
public class CommandPlayerFix extends BaseCommand {

	public CommandPlayerFix() {
		super("playerfix", "/<command> <username>", "Sets a player's state to offline so he can login.", Collections.singletonList("pfix"));
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player && !Rank.isGM((Player) sender)) {
			return true;
		}

        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Syntax: /pfix <playername|shard|all>");
            return true;
        }

        if (args[0].equalsIgnoreCase("all")) {
            GameAPI.submitAsyncCallback(() -> DatabaseInstance.playerData.updateMany(Filters.eq("info.isPlaying", true),
                    new Document(EnumOperators.$SET.getUO(), new Document("info.isPlaying", false))), result -> {
                try {
                    if (result.get().wasAcknowledged()) {
                        sender.sendMessage(ChatColor.YELLOW + "Set " + result.get().getModifiedCount() + " players' " +
                                "statuses to offline.");
                    } else {
                        sender.sendMessage(ChatColor.RED + "Operation failed: database error.");
                    }
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            });
            return true;
        }

        boolean wholeShard = false;
        for (ShardInfo shard : ShardInfo.values()) {
            if (args[0].equalsIgnoreCase(shard.getPseudoName())) {
                wholeShard = true;
            }
        }

        if (wholeShard) {
            String shard = args[0].toLowerCase();
            GameAPI.submitAsyncCallback(() -> DatabaseInstance.playerData.updateMany(Filters.eq("info.current", shard),
                    new Document(EnumOperators.$SET.getUO(), new Document("info.isPlaying", false))), result -> {
                try {
                    if (result.get().wasAcknowledged()) {
                        sender.sendMessage(ChatColor.YELLOW + "Set " + result.get().getModifiedCount() + " players' " +
                                "statuses to offline from " +
                                "shard " + shard);
                    } else {
                        sender.sendMessage(ChatColor.RED + "Operation failed: database error.");
                    }
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            });
        } else {
            String playerName = args[0];
            DatabaseAPI.getInstance().update(UUID.fromString(DatabaseAPI.getInstance().getUUIDFromName(playerName)),
                    EnumOperators.$SET, EnumData.IS_PLAYING, false, true, doAfter -> {
                        if (doAfter.wasAcknowledged())
                            sender.sendMessage(ChatColor.YELLOW + "Set status of player " + playerName + " to offline.");
                        else
                            sender.sendMessage(ChatColor.RED + "Operation failed: database error.");
                    });
        }

		return false;
	}
}