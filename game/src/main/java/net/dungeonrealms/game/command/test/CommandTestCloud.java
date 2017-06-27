package net.dungeonrealms.game.command.test;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.game.mechanic.dungeons.*;
import net.dungeonrealms.game.mechanic.dungeons.rifts.EliteRift;
import net.dungeonrealms.game.player.cosmetics.particles.impl.CloudParticleEffect;
import net.dungeonrealms.game.player.cosmetics.particles.impl.HaloParticleEffect;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

import static net.dungeonrealms.game.mechanic.dungeons.DungeonManager.*;

/**
 * Created by Rar349 on 6/7/2017.
 */
public class CommandTestCloud extends BaseCommand {
    public CommandTestCloud() {
        super("drcloud");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(!(sender instanceof Player)) return false;
        if(!Rank.isDev((Player)sender)) return false;
        Player player = (Player)sender;
        //CloudParticleEffect particle = new CloudParticleEffect(player.getEyeLocation(), 1);
        HaloParticleEffect particle = new HaloParticleEffect(player.getLocation());
        Bukkit.getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), () -> {
            particle.tick();
        },1L,1L);
        return true;
    }
}
