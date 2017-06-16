package net.dungeonrealms.game.command;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.common.util.ChatUtil;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.player.chat.Chat;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Class written by APOLLOSOFTWARE.IO on 6/23/2016
 */

public class CommandRealm extends BaseCommand {

    public CommandRealm(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }

        Player player = (Player) sender;
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);

        if (args.length == 0) {
            player.sendMessage(usage);
            return true;
        }

        StringBuilder newTitle = new StringBuilder(args[0]);
        for (int arg = 1; arg < args.length; arg++) newTitle.append(" ").append(args[arg]);

        if (newTitle.toString().contains("$")) {
            player.sendMessage(ChatColor.RED + "Title contains illegal character '$'.");
            return true;
        }

        String fixedTitle = SQLDatabaseAPI.filterSQLInjection(Chat.checkForBannedWords(newTitle.toString()));

        if (ChatUtil.containsIllegal(fixedTitle)) {
            player.sendMessage(ChatColor.RED + "Your realm title must not contain illegal characters!");
            return true;
        }
        player.sendMessage("");
        player.sendMessage(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "                       " + "* REALM TITLE SET *");
        player.sendMessage(ChatColor.GRAY + "\"" + fixedTitle + "\"");
        player.sendMessage("");

        wrapper.setRealmTitle(fixedTitle);
        return true;
    }
}
