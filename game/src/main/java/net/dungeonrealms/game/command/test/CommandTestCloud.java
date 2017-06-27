package net.dungeonrealms.game.command.test;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.game.player.cosmetics.particles.impl.CloudParticleEffect;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Created by Rar349 on 6/7/2017.
 */
public class CommandTestCloud extends BaseCommand {
    public CommandTestCloud() {
        super("drcloud");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return false;
        if (!Rank.isDev((Player) sender)) return false;
        Player player = (Player) sender;
        CloudParticleEffect particle = new CloudParticleEffect(player.getEyeLocation(), 1);
//        HaloParticleEffect particle = new HaloParticleEffect(player.getLocation());
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }
                particle.tick();
            }
        }.runTaskTimer(DungeonRealms.getInstance(), 1L, 1L);
        return true;
    }
}
