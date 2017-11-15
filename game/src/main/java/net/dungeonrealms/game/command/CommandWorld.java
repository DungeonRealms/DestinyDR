package net.dungeonrealms.game.command;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.game.world.WorldType;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Allows moving between world types.
 * Created by Kneesnap on 11/14/2017.
 */
public class CommandWorld extends BaseCommand {
    public CommandWorld() {
        super("world", "<world>", "Teleport to another world.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player) || !Rank.isBuilder((Player) sender))
            return false;

        if (args.length < 1) {
            sender.sendMessage("Syntax: /world <world>");
            sender.sendMessage("Valid Worlds: " + Arrays.stream(WorldType.values()).map(WorldType::name).map(StringUtils::capitalize).collect(Collectors.joining(", ")));
            return false;
        }

        WorldType world = WorldType.getWorld(args[0]);
        if (world == null) {
            sender.sendMessage("Unknown World.");
            return false;
        }

        ((Player) sender).teleport(world.getWorld().getSpawnLocation());
        sender.sendMessage("Travelling to " + StringUtils.capitalize(world.name()) + ".");
        return true;
    }
}
