package net.dungeonrealms.game.command;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.mastery.ItemSerialization;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.player.banks.Storage;
import net.dungeonrealms.game.world.entity.util.MountUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Date;
import java.util.UUID;

public class CommandLookup extends BaseCommand {

    public CommandLookup() {
        super("lookup", "/<command> <username>", "Looks up data for a specified username.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player && !Rank.isGM((Player) sender))
            return false;

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Syntax: /" + label + " <player>");
            return true;
        }

        SQLDatabaseAPI.getInstance().getUUIDFromName(args[0], false, (uuid) -> {
            if (uuid == null) {
                sender.sendMessage(ChatColor.RED + args[0] + " was not found in the database.");
                return;
            }
            PlayerWrapper.getPlayerWrapper(uuid, false, true, (wrapper) -> {
                boolean isPlaying = wrapper.isPlaying();
                String server = wrapper.getShardPlayingOn();

                sender.sendMessage(ChatColor.GREEN + "Generated report for " + ChatColor.GOLD + wrapper.getUsername() + ChatColor.GREEN + ":");
                sender.sendMessage(ChatColor.GREEN + "Server: " + ChatColor.AQUA + (isPlaying ? server : "Offline"));
                sender.sendMessage(ChatColor.GREEN + "Rank: " + ChatColor.AQUA + wrapper.getRank());
                sender.sendMessage(ChatColor.GREEN + "Level: " + ChatColor.YELLOW + wrapper.getLevel());
                sender.sendMessage(ChatColor.GREEN + "Time Played: " + ChatColor.YELLOW + GameAPI.formatTime(1000 * 60 * wrapper.getPlayerGameStats().getTimePlayed()));
                if (!isPlaying) {
                    long lastSeen = wrapper.getLastLogout();
                    sender.sendMessage(ChatColor.GREEN + "Last Seen: " + ChatColor.YELLOW + GameAPI.formatTime(new Date().getTime() - lastSeen));
                }
                sender.sendMessage(ChatColor.GREEN + "Gems: " + ChatColor.YELLOW + wrapper.getGems());
                sender.sendMessage(ChatColor.GREEN + "E-Cash: " + ChatColor.YELLOW + wrapper.getEcash());

                Storage storage = BankMechanics.getInstance().getStorage(uuid);
                if(storage != null) {
                    sender.sendMessage(ChatColor.GREEN + "Bank: " + formatInventoryData(storage.inv));
                    sender.sendMessage(ChatColor.GREEN + "Inventory: " + formatInventoryData(wrapper.getPendingInventory()));
                    sender.sendMessage(ChatColor.GREEN + "Collection Bin: " + formatInventoryData(storage.collection_bin));
                }
                sender.sendMessage(ChatColor.GREEN + "Mule: " + formatInventoryData(MountUtils.inventories.get(uuid)));
            });

        });
//        Bukkit.getScheduler().runTaskAsynchronously(DungeonRealms.getInstance(), () -> {
//            String id = DatabaseAPI.getInstance().getUUIDFromName(args[0]);
//
//            if (id.equals("")) {
//                sender.sendMessage(ChatColor.RED + args[0] + " was not found in the database.");
//                return;
//            }
//
//            UUID uuid = UUID.fromString(id);
//
//            boolean isPlaying = (boolean) DatabaseAPI.getInstance().getData(EnumData.IS_PLAYING, uuid);
//            String server = DatabaseAPI.getInstance().getFormattedShardName(uuid);
//
//            sender.sendMessage(ChatColor.GREEN + "Generated report for " + ChatColor.GOLD + DatabaseAPI.getInstance().getData(EnumData.USERNAME, uuid) + ChatColor.GREEN + ":");
//            sender.sendMessage(ChatColor.GREEN + "Server: " + ChatColor.AQUA + (isPlaying ? server : "Offline"));
//            sender.sendMessage(ChatColor.GREEN + "Rank: " + ChatColor.AQUA + DatabaseAPI.getInstance().getData(EnumData.RANK, uuid));
//            sender.sendMessage(ChatColor.GREEN + "Level: " + ChatColor.YELLOW + DatabaseAPI.getInstance().getData(EnumData.LEVEL, uuid));
//            sender.sendMessage(ChatColor.GREEN + "Time Played: " + ChatColor.YELLOW + GameAPI.formatTime(1000 * 60 * (int) DatabaseAPI.getInstance().getData(EnumData.TIME_PLAYED, uuid)));
//            if (!isPlaying) {
//                long lastSeen = (long) DatabaseAPI.getInstance().getData(EnumData.LAST_LOGOUT, uuid);
//                sender.sendMessage(ChatColor.GREEN + "Last Seen: " + ChatColor.YELLOW + GameAPI.formatTime(new Date().getTime() - lastSeen));
//            }
//            sender.sendMessage(ChatColor.GREEN + "Gems: " + ChatColor.YELLOW + DatabaseAPI.getInstance().getData(EnumData.GEMS, uuid));
//            sender.sendMessage(ChatColor.GREEN + "E-Cash: " + ChatColor.YELLOW + DatabaseAPI.getInstance().getData(EnumData.ECASH, uuid));
//            sender.sendMessage(ChatColor.GREEN + "Bank: " + loadInventory(uuid, EnumData.INVENTORY_STORAGE));
//            sender.sendMessage(ChatColor.GREEN + "Inventory: " + loadInventory(uuid, EnumData.INVENTORY, 36));
//            sender.sendMessage(ChatColor.GREEN + "Collection Bin: " + loadInventory(uuid, EnumData.INVENTORY_COLLECTION_BIN));
//            sender.sendMessage(ChatColor.GREEN + "Mule: " + loadInventory(uuid, EnumData.INVENTORY_MULE));
//
//            //Punishments
//            // - Bans
//            // - Mutes
//
//        });

        return true;
    }



    private String formatInventoryData(Inventory inv) {
        int items = 0;
        int gemTotal = 0;

        //  FILL VARIABLES  //
        if (inv != null) {
            for (ItemStack item : inv.getContents()) {
                if (item == null || item.getType() == Material.AIR)
                    continue;
                items++;

                if (BankMechanics.isMoney(item))
                    gemTotal += BankMechanics.getGemWorth(item);
            }
        }

        //  GENERATE USER-FRIENDLY STRING  //
        if (items == 0)
            return ChatColor.LIGHT_PURPLE + "Empty";

        String ret = ChatColor.LIGHT_PURPLE + "" + items + " Items";

        if (gemTotal != 0)
            ret += ChatColor.RED + " (" + gemTotal + "g)";

        return ret;
    }

}
