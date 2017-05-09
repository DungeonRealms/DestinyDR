package net.dungeonrealms.game.command.menu;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.game.listener.NPCMenu;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Created by Brad on 29/06/2016.
 */

public class CommandEcash extends BaseCommand {

    public CommandEcash(String command, String usage, String description, List<String> aliases) {
        super(command, usage, description, aliases);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return false;

        if (DungeonRealms.isEvent()) {
            sender.sendMessage(ChatColor.RED + "You cannot access the E-Cash Vendor on this shard.");
            return false;
        }

        NPCMenu.ECASH_VENDOR.open((Player) sender);

        return true;
    }
}
