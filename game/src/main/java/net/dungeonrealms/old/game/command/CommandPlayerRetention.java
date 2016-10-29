package net.dungeonrealms.old.game.command;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.common.game.punishment.TimeFormat;
import net.dungeonrealms.old.game.mastery.Utils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.concurrent.ExecutionException;

/**
 * Class written by APOLLOSOFTWARE.IO on 9/5/2016
 */
public class CommandPlayerRetention extends BaseCommand {

    public CommandPlayerRetention(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!Rank.isGM(player)) return false;
        }

        if (args.length == 0) {
            sender.sendMessage("/pretention <time>");
            return true;
        }

        long duration = 0;
        boolean isNull = true;

        for (TimeFormat d : TimeFormat.values())
            if (d.getKey().equalsIgnoreCase(args[0].substring(args[0].length() - 1))) {
                String n = args[0].substring(0, args[0].length() - 1);
                if (!Utils.isInt(n)) continue;
                duration = Integer.parseInt(n) * d.convert();
                isNull = false;
                break;
            }

        if (!Utils.isInt(args[0]) && isNull) {
            sender.sendMessage(ChatColor.RED + args[0] + " is not a valid number.");
            return true;
        }

        if (isNull)
            duration = Integer.parseInt(args[0]);

        if (duration < -1) {
            sender.sendMessage(ChatColor.RED + args[0] + " is not a valid number.");
            return true;
        }

        long finalDuration = duration;
        GameAPI.submitAsyncCallback(() -> GameAPI.calculatePlayerRetention(finalDuration), retention -> {
            try {
                sender.sendMessage(ChatColor.RED + "Player Retention: " + retention.get() + " players.");
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });
        return true;
    }

}
