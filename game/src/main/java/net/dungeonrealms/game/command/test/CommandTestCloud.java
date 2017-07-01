package net.dungeonrealms.game.command.test;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.player.cosmetics.particles.SpecialParticleEffect;
import net.dungeonrealms.game.player.cosmetics.particles.SpecialParticles;
import net.dungeonrealms.game.player.cosmetics.particles.impl.FourthOfJulyAuraEffect;
import net.dungeonrealms.game.player.cosmetics.particles.impl.FourthOfJulySpiral;
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
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
        SpecialParticleEffect effect = SpecialParticles.constrauctEffectFromName(SpecialParticles.FOURTH_AURA.getInternalName(), player);
        wrapper.setActiveSpecialEffect(effect);
        wrapper.setActiveChestEffect(SpecialParticles.FOURTH_AURA);
        wrapper.setActiveRealmEffect(SpecialParticles.FOURTH_AURA);
        return true;
    }
}
