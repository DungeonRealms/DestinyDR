package net.dungeonrealms.game.command;

import com.mongodb.client.model.Filters;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.DatabaseDriver;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.data.EnumOperators;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.common.network.ShardInfo;
import org.bson.Document;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * Created by Nick on 12/2/2015.
 */
public class CommandPlayerFix extends BaseCommand {

	public CommandPlayerFix(String command, String usage, String description, List<String> aliases) {
		super(command, usage, description, aliases);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player && !Rank.isGM((Player) sender)) {
			return true;
		}

        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Syntax: /pfix playername|shard");
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
            GameAPI.submitAsyncCallback(() -> DatabaseDriver.playerData.updateMany(Filters.eq("info.current", shard),
                    new Document(EnumOperators.$SET.getUO(), new Document("info.current", false))), result -> {
                try {
                    if (result.get().wasAcknowledged()) {
                        sender.sendMessage(ChatColor.YELLOW + "Set " + result.get().getModifiedCount() + " players' " +
                                "statuses to offline from " +
                                "shard " + shard);
                    } else {
                        sender.sendMessage(ChatColor.RED + "Operation failed: database error.");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
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