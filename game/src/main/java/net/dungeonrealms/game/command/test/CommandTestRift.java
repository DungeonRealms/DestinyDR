package net.dungeonrealms.game.command.test;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.game.mechanic.dungeons.DungeonManager;
import net.dungeonrealms.game.mechanic.dungeons.DungeonType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

/**
 * Created by Rar349 on 6/7/2017.
 */
public class CommandTestRift extends BaseCommand {
    public CommandTestRift() {
        super("drrift");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(!(sender instanceof Player)) return false;
        DungeonManager.createDungeon(DungeonType.T1_ELITE_RIFT, Arrays.asList((Player)sender));
        return true;
    }
}
