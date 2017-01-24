package net.dungeonrealms.game.command.moderation;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Created by Brad on 25/12/2016.
 */
public class CommandGemsee extends BaseCommand {
    public CommandGemsee(String command, String usage, String description, List<String> aliases) {
        super(command, usage, description, aliases);
    }

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
        Bukkit.getScheduler().scheduleAsyncDelayedTask(DungeonRealms.getInstance(), () -> {
           String uuid = DatabaseAPI.getInstance().getUUIDFromName(playerName);
           if(uuid == null || uuid.equals("")){
               sender.sendMessage(ChatColor.RED + "No player found with that name.");
               return;
           }

            UUID p_uuid = UUID.fromString(uuid);

            sender.sendMessage(ChatColor.YELLOW + playerName + " balance: " + ChatColor.AQUA + DatabaseAPI.getInstance().getData(EnumData.GEMS, p_uuid));

        });
        return false;
    }
}
