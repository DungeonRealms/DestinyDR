package net.dungeonrealms.game.commands.testcommands;

import net.dungeonrealms.game.commands.BasicCommand;
import net.dungeonrealms.game.database.player.Rank;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Created by Brad on 12/06/2016.
 */

public class CommandTestingHall extends BasicCommand {

    public CommandTestingHall(String command, String usage, String description, List<String> aliases) {
        super(command, usage, description, aliases);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player)sender;

        if (!Rank.isDev(player)) return false;

        player.teleport(new Location(Bukkit.getWorlds().get(0), -405.5, 2, -833.5, 90.0F, 1.0F));
        player.sendMessage(ChatColor.GREEN + "You have been teleported to the " + ChatColor.BOLD + ChatColor.UNDERLINE + "Dungeon Realms Testing Hall" + ChatColor.GREEN + ".");

        return true;
    }

}
