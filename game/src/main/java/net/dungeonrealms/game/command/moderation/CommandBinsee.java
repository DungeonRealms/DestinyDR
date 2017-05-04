package net.dungeonrealms.game.command.moderation;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.rank.Rank;
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
            Storage storage = BankMechanics.getInstance().getStorage(Bukkit.getPlayer(playerName).getUniqueId());
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
                                "Please banksee on that shard to avoid concurrent modification.");
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
//
//            UUID p_uuid = UUID.fromString(DatabaseAPI.getInstance().getUUIDFromName(playerName));
//
//            // check if they're logged in on another shard
//            if ((Boolean) DatabaseAPI.getInstance().getData(EnumData.IS_PLAYING, p_uuid)) {
//                String shard = DatabaseAPI.getInstance().getFormattedShardName(p_uuid);
//                sender.sendMessage(ChatColor.RED + "That player is currently playing on shard " + shard + ". " +
//                        "Please banksee on that shard to avoid concurrent modification.");
//                return false;
//            }
//
//            String stringInv = (String) DatabaseAPI.getInstance().getData(EnumData.INVENTORY_COLLECTION_BIN, p_uuid);
//            Inventory inv = null;
//            if (stringInv.length() > 1) {
//                inv = ItemSerialization.fromString(stringInv);
//                for (ItemStack item : inv.getContents()) {
//                    if (item != null && item.getType() == Material.AIR) {
//                        inv.addItem(item);
//                    }
//                }
//                Player p = Bukkit.getPlayer(p_uuid);
//                if (p != null)
//                    p.sendMessage(ChatColor.RED + "You have items in your collection bin!");
//            } else {
//                sender.sendMessage(ChatColor.RED + "That player's collection bin is empty.");
//                return false;
//            }
//
//            offline_bin_watchers.put(sender.getUniqueId(), p_uuid);
//            sender.openInventory(inv);
        }
        return false;
    }
}
