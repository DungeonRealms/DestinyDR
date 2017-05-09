package net.dungeonrealms.game.command;

import lombok.Cleanup;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.sql.QueryType;
import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.common.network.ShardInfo;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;

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

		    SQLDatabaseAPI.getInstance().getSqlQueries().add("UPDATE users SET is_online = 0 WHERE is_online = 1;");

//            GameAPI.submitAsyncCallback(() -> DatabaseInstance.playerData.updateMany(Filters.eq("info.isPlaying", true),
//                    new Document(EnumOperators.$SET.getUO(), new Document("info.isPlaying", false))), result -> {
//                try {
//                    if (result.get().wasAcknowledged()) {
//                        sender.sendMessage(ChatColor.YELLOW + "Set " + result.get().getModifiedCount() + " players' " +
//                                "statuses to offline.");
//                    } else {
//                        sender.sendMessage(ChatColor.RED + "Operation failed: database error.");
//                    }
//                } catch (InterruptedException | ExecutionException e) {
//                    e.printStackTrace();
//                }
//            });
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

            CompletableFuture.runAsync(() -> {
                try{
                    @Cleanup PreparedStatement statement = SQLDatabaseAPI.getInstance().getDatabase().getConnection().
                            prepareStatement(QueryType.FIX_WHOLE_SHARD.getQuery(shard));
                    int updates = statement.executeUpdate();
                    sender.sendMessage(ChatColor.RED + "Updated " + updates + " players with currentShard = " + shard);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }, ForkJoinPool.commonPool());
//            GameAPI.submitAsyncCallback(() -> DatabaseInstance.playerData.updateMany(Filters.eq("info.current", shard),
//                    new Document(EnumOperators.$SET.getUO(), new Document("info.isPlaying", false))), result -> {
//                try {
//                    if (result.get().wasAcknowledged()) {
//                        sender.sendMessage(ChatColor.YELLOW + "Set " + result.get().getModifiedCount() + " players' " +
//                                "statuses to offline from " +
//                                "shard " + shard);
//                    } else {
//                        sender.sendMessage(ChatColor.RED + "Operation failed: database error.");
//                    }
//                } catch (InterruptedException | ExecutionException e) {
//                    e.printStackTrace();
//                }
//            });
        } else {
            String playerName = args[0];

            SQLDatabaseAPI.getInstance().getUUIDFromName(playerName, false, (uuid) -> {
                if(uuid == null) {
                    sender.sendMessage(ChatColor.RED + "Unable to find player for name.");
                    return;
                }

                Integer accountID = SQLDatabaseAPI.getInstance().getAccountIdFromUUID(uuid);
                if(accountID == null){
                    sender.sendMessage(ChatColor.RED + "Unable to find accountID for " + uuid.toString());
                    return;
                }

                SQLDatabaseAPI.getInstance().getSqlQueries().add("UPDATE users SET is_online = 0, currentShard = null WHERE account_id = '" + accountID + "';");
            });
        }

		return false;
	}
}