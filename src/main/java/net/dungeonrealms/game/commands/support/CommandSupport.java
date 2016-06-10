package net.dungeonrealms.game.commands.support;

import net.dungeonrealms.game.commands.generic.BasicCommand;
import net.dungeonrealms.game.player.inventory.PlayerMenus;
import net.dungeonrealms.game.player.rank.Rank;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Brad on 09/06/2016.
 */

public class CommandSupport extends BasicCommand {

    public CommandSupport(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return false;
        Player player = (Player) sender;
        if (!Rank.isSupport(player)) return false;
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Invalid usage: /support <name>");
            return false;
        } else if (args[0].equalsIgnoreCase(player.getDisplayName())) {
            player.sendMessage(ChatColor.RED + "You " + ChatColor.BOLD + ChatColor.UNDERLINE.toString() + "CANNOT" + ChatColor.RED + " manage your own account.");
            return false;
        }

        PlayerMenus.openSupportMenu(player, args[0]);
        return true;
    }

}
