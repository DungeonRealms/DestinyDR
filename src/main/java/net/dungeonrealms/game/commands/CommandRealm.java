package net.dungeonrealms.game.commands;

import net.dungeonrealms.game.commands.generic.BasicCommand;
import net.dungeonrealms.game.player.chat.Chat;
import net.dungeonrealms.game.world.realms.Realms;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Class written by APOLLOSOFTWARE.IO on 6/23/2016
 */

public class CommandRealm extends BasicCommand {

    public CommandRealm(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage(usage);
            return true;
        }

        StringBuilder newTitle = new StringBuilder(args[0]);
        for (int arg = 1; arg < args.length; arg++) newTitle.append(" ").append(args[arg]);

        if (newTitle.length() > 16) {
            player.sendMessage(ChatColor.RED + "Your realm title cannot be longer than 11 characters");
            return true;
        }

        if (newTitle.toString().contains("$")) {
            player.sendMessage(ChatColor.RED + "Title contains illegal character '$'.");
            return true;
        }

        String fixedTitle = Chat.getInstance().checkForBannedWords(newTitle.toString());

        player.sendMessage("");
        player.sendMessage(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "                       " + "* REALM TITLE SET *");
        player.sendMessage(ChatColor.GRAY + "\"" + fixedTitle + "\"");
        player.sendMessage("");

        Realms.getInstance().setRealmTitle(player.getUniqueId(), fixedTitle);
        Realms.getInstance().updateRealmHologram(player.getUniqueId());

        return true;
    }
}
