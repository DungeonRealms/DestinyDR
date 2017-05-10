package net.dungeonrealms.game.command.moderation;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.player.banks.Storage;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Brad on 25/12/2016.
 */
public class CommandBinsee extends BaseCommand {
    public CommandBinsee() {
        super("binsee", "/<command> <player>", "View a player's collection bin.", Collections.singletonList("mbns"));
    }

    public static Map<UUID, UUID> offline_bin_watchers = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String string, String[] args) {
        if (s instanceof ConsoleCommandSender) return false;
        Player sender = (Player) s;

        if (!Rank.isGM(sender)) return false;

        if (args.length == 0) {
            s.sendMessage(usage);
            return true;
        }

        String playerName = args[0];
        if (Bukkit.getPlayer(playerName) != null) {
            Storage storage = BankMechanics.getStorage(Bukkit.getPlayer(playerName).getUniqueId());
            if(storage == null || storage.collection_bin == null || storage.collection_bin.getContents().length <= 0) {
                sender.sendMessage(ChatColor.RED + "This person does not have anything in their collection bin!");
                return true;
            }
            sender.openInventory(storage.collection_bin);
        } else {

            SQLDatabaseAPI.getInstance().getUUIDFromName(playerName, false, (uuid) -> {
                if (uuid == null) {
                    Bukkit.getLogger().info(playerName + " does not exist in our database.");
                    return;
                }

                PlayerWrapper.getPlayerWrapper(uuid, false, false, (wrapper) -> {
                    if (wrapper.isPlaying()) {
                        sender.sendMessage(ChatColor.RED + "That player is currently playing on shard " + wrapper.getFormattedShardName() + ". " +
                                "Please binsee on that shard to avoid concurrent modification.");
                        return;
                    }


                    if (wrapper.getPendingBankStorage() != null && wrapper.getPendingBankStorage().collection_bin != null) {
                        sender.openInventory(wrapper.getPendingBankStorage().collection_bin);
                        offline_bin_watchers.put(sender.getUniqueId(), uuid);
                    } else {
                        sender.sendMessage(ChatColor.RED + "That players Collection Bin is empty.");
                    }
                });
            });
        }
        return false;
    }
}
