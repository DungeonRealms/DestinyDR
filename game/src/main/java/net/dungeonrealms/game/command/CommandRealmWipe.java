package net.dungeonrealms.game.command;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.data.EnumOperators;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.game.world.realms.Realms;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Class written by APOLLOSOFTWARE.IO on 6/23/2016
 */

public class CommandRealmWipe extends BaseCommand {

    public CommandRealmWipe(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!Rank.isTrialGM(player)) return false;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + usage);
            return true;
        }

        if (DatabaseAPI.getInstance().getUUIDFromName(args[0]).equals("")) {
            sender.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + args[0] + ChatColor.RED + " does not exist in our database.");
            return true;
        }

        UUID p_uuid = UUID.fromString(DatabaseAPI.getInstance().getUUIDFromName(args[0]));

        GameAPI.submitAsyncCallback(() -> {
                    Realms.getInstance().wipeRealm(p_uuid);
                    return true;
                }, callback -> {
                    DatabaseAPI.getInstance().update(p_uuid, EnumOperators.$SET, EnumData.REALM_TIER, 1, true);
                    DatabaseAPI.getInstance().update(p_uuid, EnumOperators.$SET, EnumData.REALM_UPLOAD, false, true);
                    DatabaseAPI.getInstance().update(p_uuid, EnumOperators.$SET, EnumData.REALM_UPGRADE, false, true);

                    sender.sendMessage(ChatColor.GRAY.toString() + "Realm wiped.");
                    GameAPI.updatePlayerData(p_uuid);
                }
        );


        return true;
    }
}
