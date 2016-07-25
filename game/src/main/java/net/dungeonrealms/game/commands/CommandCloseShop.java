package net.dungeonrealms.game.commands;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.commands.BasicCommand;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.common.game.database.type.EnumData;
import net.dungeonrealms.common.game.database.type.EnumOperators;
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

        if (args.length == 1 && (commandSender instanceof Player && Rank.isGM((Player) commandSender))) {
            String playerName = args[0];
            if (!isPlayer(playerName))
                return false;

            boolean hasShop = (boolean) DatabaseAPI.getInstance().getData(EnumData.HASSHOP, UUID.fromString(DatabaseAPI.getInstance().getUUIDFromName(playerName)));
            if (!hasShop) {
                return false;
            }

            String uuidString = DatabaseAPI.getInstance().getUUIDFromName(playerName);
            UUID uuid = UUID.fromString(uuidString);
            GameAPI.sendNetworkMessage("Shop", "close:" + " ," + playerName);
            DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.HASSHOP, false, true);

            return false;
        } else {
            Player player = (Player) commandSender;
            GameAPI.sendNetworkMessage("Shop", "close:" + " ," + player.getName());
            String uuidString = DatabaseAPI.getInstance().getUUIDFromName(player.getName());
            UUID uuid = UUID.fromString(uuidString);
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
        return !uuid.equals("");
    }

}
