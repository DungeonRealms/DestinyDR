package net.dungeonrealms.game.command;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.game.world.teleportation.TeleportAPI;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Brad on 08/06/2016.
 */
public class CommandSpawn extends BaseCommand {

    public CommandSpawn(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player) || !Rank.isGM((Player) sender)) {
            return false;
        }

        Player player = (Player) sender;

        Location respawnLocation = TeleportAPI.getLocationFromString("cyrennica");
        player.teleport(respawnLocation);

        return true;
    }

}
