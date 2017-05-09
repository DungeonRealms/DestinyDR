package net.dungeonrealms.game.command;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.sql.QueryType;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.database.UpdateType;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Class written by APOLLOSOFTWARE.IO on 6/23/2016
 */

public class CommandRealmFix extends BaseCommand {

    public CommandRealmFix(String command, String usage, String description) {
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

        SQLDatabaseAPI.getInstance().getUUIDFromName(args[0], false, uuid -> {
            if (uuid == null) {
                sender.sendMessage(ChatColor.RED + "This player has never logged into Dungeon Realms");
                return;
            }

            PlayerWrapper.getPlayerWrapper(uuid, false, true, wrapper -> {
                if (wrapper == null) {
                    sender.sendMessage(ChatColor.RED + "Could not load player data!");
                    return;
                }

                wrapper.setUploadingRealm(false);
                wrapper.setUpgradingRealm(false);

                SQLDatabaseAPI.getInstance().executeUpdate(results -> {
                    sender.sendMessage(ChatColor.GREEN + "Successfully fixed " + args[0] + "'s realm.");
                    //Update their realm status..
                    GameAPI.updatePlayerData(uuid, UpdateType.REALM);
                }, QueryType.SET_REALM_INFO.getQuery(0, 0, wrapper.getRealmTier(), wrapper.getCharacterID()));
            });

        });


        return true;
    }
}
