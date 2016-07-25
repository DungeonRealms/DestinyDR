package net.dungeonrealms.game.commands;

import net.dungeonrealms.common.game.commands.BasicCommand;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.type.EnumData;
import net.dungeonrealms.common.game.database.type.EnumOperators;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Class written by APOLLOSOFTWARE.IO on 6/23/2016
 */

public class CommandRealmFix extends BasicCommand {

    public CommandRealmFix(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return false;

        Player player = (Player) sender;

        DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.REALM_UPLOAD, false, true);
        player.sendMessage(ChatColor.GRAY.toString() + "Realm fixed");
        return true;
    }
}
