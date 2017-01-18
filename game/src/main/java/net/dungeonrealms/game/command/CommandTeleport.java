package net.dungeonrealms.game.command;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.game.player.json.JSONMessage;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Brad on 21/08/2016.
 */

public class CommandTeleport extends BaseCommand {
    public CommandTeleport(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof Player) || !Rank.isGM((Player) sender)) return false;

        Player player = (Player) sender;

        String tpLocations[][] = {
                { "Cyrennica", "-378 85 357" },
                { "Harrison Field", "-594 59 687" },
                { "Dark Oak", "280 59 1132" },
                { "Trollsbane Tavern", "962 95 1069" },
                { "Tripoli", "-1320 91 370" },
                { "Gloomy Hollows", "-590 44 0" },
                { "Crestguard Keep", "-1428 116 -489" },
                { "Deadpeaks", "-1173 106 1030" },
                { "Tutorial", "-726 78 350" },
                { "Avalon Peaks", "-1263 103 -516" },
                { "Lost City of Avalon", "-221 153 -3489" },
                { "Varenglade", "-673 58 1399" },
                { "Bandit Trove", "-978 60 745" },
                { "Infernal Abyss", "-280 25 -478" },
                { "Cop'jak", "-80 100 1270" },
                { "Kilatan", "-400 40 -3480" },
                { "Blayshan", "-280 85 -200" },
                { "Maltai", "-1440 60 1290" },
                { "Throne Room", "-1565 75 1312" },
                { "Lord Taylor", "-1040 120 1260" },
                { "Mure", "-1240 90 200" },
                { "Skullneck", "600 90 890" },
                { "Crystal Peaks Tower", "980 90 910" },
                { "Crest Watch", "-522 60 -420" },
               // { "-", "0 0 0" },
        };

        final JSONMessage message = new JSONMessage("\n" + ChatColor.GREEN + ChatColor.BOLD.toString() + "Please select a location to teleport to:\n", ChatColor.GREEN);
        for (int i = 0; i < tpLocations.length; i++) {
            message.addRunCommand(ChatColor.GRAY + "[" + ChatColor.GREEN + tpLocations[i][0] + ChatColor.GRAY + "]" + ((i+1) % 4 == 0 ? "\n" : " "), ChatColor.GRAY, "/tp " + tpLocations[i][1]);
        }
        message.sendToPlayer(player);

        return true;
    }
}
