package net.dungeonrealms.game.command.moderation;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.sql.QueryType;
import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Created by Giovanni on 25-12-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class CommandSessionID extends BaseCommand {

    public CommandSessionID(String command, String usage, String description, List<String> aliases) {
        super(command, usage, description, aliases);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (Rank.isTrialGM(player)) {
                if (args.length == 1) {

                    SQLDatabaseAPI.getInstance().getUUIDFromName(args[0], false, (uuid) -> {
                        if (uuid == null) {
                            sender.sendMessage(ChatColor.RED + "The player could not be found, have they played Dungeon Realms before?");
                            return;
                        }

                        Integer accountID = SQLDatabaseAPI.getInstance().getAccountIdFromUUID(uuid);
                        if(accountID == null){
                            sender.sendMessage(ChatColor.RED + "Account ID not found for " + uuid);
                            return;
                        }

                        SQLDatabaseAPI.getInstance().addQuery(QueryType.SET_ONLINE_USER, 0, accountID);
                });
//                    UUID uuid = null;
//                    try {
//                        uuid = UUID.fromString(DatabaseAPI.getInstance().getUUIDFromName(args[0]));
//                    } catch (Exception ignored) {
//                    }
//                    // Check
//                    if (uuid != null) {
//                        DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.IS_PLAYING, false, true, true);
//                        sender.sendMessage(ChatColor.GREEN + "Fixed " + args[0] + "'s session ID.");
//                    } else {
//                        sender.sendMessage(ChatColor.RED + "The player could not be found, have they played Dungeon Realms before?");
//                        return true;
//                    }
                }
            } else {
                sender.sendMessage(ChatColor.RED + "Command: /session <player> | Fixes a player's session ID.");
                return true;
            }
        }
        return false;
    }
}
