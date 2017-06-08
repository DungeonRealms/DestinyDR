package net.dungeonrealms.game.command.test;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.game.mechanic.dungeons.*;
import net.dungeonrealms.game.mechanic.dungeons.rifts.EliteRift;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

import static net.dungeonrealms.game.mechanic.dungeons.DungeonManager.*;

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
        EliteRift d = (EliteRift)createDungeon(DungeonType.ELITE_RIFT, Arrays.asList((Player)sender));
        d.setOurTier(5);
        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
            d.spawnBoss(BossType.RiftEliteBoss);
        }, 20);
        return true;
    }
}
