package net.dungeonrealms.game.command.moderation;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.data.EnumOperators;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

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
            if (Rank.isGM(player)) {
                if (args.length == 1) {
                    UUID uuid = null;
                    try {
                        uuid = UUID.fromString(DatabaseAPI.getInstance().getUUIDFromName(args[0]));
                    } catch (Exception ignored) {
                    }
                    // Check
                    if (uuid != null) {
                        DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.IS_PLAYING, false, true, true);
                        sender.sendMessage(ChatColor.GREEN + "Fixed " + args[0] + "'s vnetty-session_ID");
                    } else {
                        sender.sendMessage(ChatColor.RED + "Player is not existent in the Mongo database");
                        return true;
                    }
                }
            } else {
                sender.sendMessage(ChatColor.RED + "Command: /session <player> | Fixes a player's vnetty-session_ID");
                return true;
            }
        }
        return false;
    }
}
