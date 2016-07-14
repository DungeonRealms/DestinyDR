package net.dungeonrealms.game.commands;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.database.DatabaseAPI;
import net.dungeonrealms.game.database.type.EnumData;
import net.dungeonrealms.game.database.type.EnumOperators;
import net.dungeonrealms.game.player.banks.BankMechanics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

/**
 * Created by chase on 7/8/2016.
 */
public class CommandCloseShop extends BasicCommand {

    public CommandCloseShop(String command, String usage, String description, List<String> aliases) {
        super(command, usage, description, aliases);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {

        if (args.length == 1) {
            if (!commandSender.hasPermission("dungeonrealms.closeShops"))
                return false;
            String playerName = args[0];
            if (!isPlayer(playerName))
                return false;
            boolean hasShop = (boolean) DatabaseAPI.getInstance().getData(EnumData.HASSHOP, UUID.fromString(DatabaseAPI.getInstance().getUUIDFromName(playerName)));
            if (!hasShop) {
                return false;
            }

            if (commandSender instanceof Player) {
                Player player = (Player) commandSender;
                ByteArrayDataOutput shopClose = ByteStreams.newDataOutput();
                shopClose.writeUTF("Shop");
                shopClose.writeUTF("close:" + " ," + playerName);
                String uuidString = DatabaseAPI.getInstance().getUUIDFromName(playerName);

                UUID uuid = UUID.fromString(uuidString);
                player.sendPluginMessage(DungeonRealms.getInstance(), "DungeonRealms", shopClose.toByteArray());
                DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.HASSHOP, false, true);

            }

            return false;
        } else {
            Player player = (Player) commandSender;
            ByteArrayDataOutput shopClose = ByteStreams.newDataOutput();
            shopClose.writeUTF("Shop");
            shopClose.writeUTF("close:" + " ," + player.getName());
            String uuidString = DatabaseAPI.getInstance().getUUIDFromName(player.getName());
            UUID uuid = UUID.fromString(uuidString);
            player.sendPluginMessage(DungeonRealms.getInstance(), "DungeonRealms", shopClose.toByteArray());
            DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.HASSHOP, false, true);
            player.sendMessage(ChatColor.GRAY + "Checking shards for open shop..");
            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                GameAPI.updatePlayerData(uuid);
                BankMechanics.getInstance().getStorage(uuid).update();
            }, 20);
        }
        return false;
    }

    private boolean isPlayer(String player) {
        String uuid = DatabaseAPI.getInstance().getUUIDFromName(player);
        return uuid.equals("") ? false : true;
    }

}
