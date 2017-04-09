package net.dungeonrealms.game.command.moderation;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.game.mastery.ItemSerialization;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.player.banks.Storage;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.*;

/**
 * Created by Brad on 25/12/2016.
 */
public class CommandBanksee extends BaseCommand {
    public CommandBanksee() {
        super("banksee", "/<command> [args]", "View a player's bank.", Collections.singletonList("mbs"));
    }

    public static Map<UUID, UUID> offline_armor_watchers = new HashMap<>();
    public static Map<UUID, UUID> offline_bank_watchers = new HashMap<>();
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
            sender.openInventory(storage.inv);
        } else {
            if (DatabaseAPI.getInstance().getUUIDFromName(playerName).equals("")) {
                sender.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + playerName + ChatColor.RED + " does not exist in our database.");
                return true;
            }

            UUID p_uuid = UUID.fromString(DatabaseAPI.getInstance().getUUIDFromName(playerName));

            // check if they're logged in on another shard
            if ((Boolean)DatabaseAPI.getInstance().getData(EnumData.IS_PLAYING, p_uuid)) {
                String shard = DatabaseAPI.getInstance().getFormattedShardName(p_uuid);
                sender.sendMessage(ChatColor.RED + "That player is currently playing on shard " + shard + ". " +
                        "Please banksee on that shard to avoid concurrent modification.");
                return true;
            }

            String source = (String) DatabaseAPI.getInstance().getData(EnumData.INVENTORY_STORAGE, p_uuid);
            Inventory inv = null;
            if (source != null && source.length() > 0 && !source.equalsIgnoreCase("null")) {
                inv = ItemSerialization.fromString(source);
            } else {
                sender.sendMessage(ChatColor.RED + "That player's storage is empty.");
                return true;
            }

            offline_bank_watchers.put(sender.getUniqueId(), p_uuid);
            sender.openInventory(inv);
        }
        return false;
    }
}
