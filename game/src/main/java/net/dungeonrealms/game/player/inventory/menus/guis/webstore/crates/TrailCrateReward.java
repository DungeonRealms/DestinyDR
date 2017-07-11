package net.dungeonrealms.game.player.inventory.menus.guis.webstore.crates;

import net.dungeonrealms.common.game.util.ChatColor;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.mechanic.ParticleAPI;
import org.bukkit.entity.Player;

/**
 * Created by Rar349 on 7/9/2017.
 */
public class TrailCrateReward extends AbstractCrateReward {

    private ParticleAPI.ParticleEffect effect;

    public TrailCrateReward(ParticleAPI.ParticleEffect effect) {
        super(effect.getMaterial(), ChatColor.GREEN + effect.getDisplayName(), ChatColor.GRAY + "A cool trail!");
        this.effect = effect;
    }

    @Override
    public void giveReward(Player player) {
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
        wrapper.getParticles().add(effect);
    }

    @Override
    public boolean canReceiveReward(Player player) {
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
        return !wrapper.getParticles().contains(effect);
    }
}
