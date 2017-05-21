package net.dungeonrealms.game.world.entity.type.monster.boss.type.subboss;

import net.dungeonrealms.game.handler.HealthHandler;
import net.dungeonrealms.game.mechanic.dungeons.BossType;
import net.dungeonrealms.game.mechanic.dungeons.DungeonBoss;
import net.dungeonrealms.game.world.entity.type.monster.base.DRGhast;
import net.dungeonrealms.game.world.entity.type.monster.boss.InfernalAbyss;
import net.dungeonrealms.game.world.item.DamageAPI;
import net.minecraft.server.v1_9_R2.World;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 * Infernal Ghast - Infernal Abyss subboss.
 * 
 * Redone on April 28th, 2017.
 * @author Kneesnap
 */
public class InfernalGhast extends DRGhast implements DungeonBoss {

    public InfernalGhast(World world) {
        super(world);
        DamageAPI.setArmorBonus(getBukkit(), 50);
    }
    
    private InfernalAbyss getMainBoss() {
    	return (InfernalAbyss) getDungeon().getBoss();
    }

    public void init() {
        HealthHandler.initHP(getBukkit(), getMainBoss().getHP());
        getBukkit().setPassenger(getMainBoss().getBukkit());
    }

    @Override
    public void onBossDeath(Player player) {
    	playSound(Sound.ENTITY_GHAST_SCREAM, 2F, 1F);
    }
    
    @Override
    public void onBossAttacked(Player attacker) {
    	if (getPercentHP() > 0.5F)
    		return;
    	
    	// We're under 50% health, it's time to die and let infernal take over.
    	getBukkit().eject();
    	getMainBoss().doFinalForm(getHP());
    	die();
    }

	@Override
	public BossType getBossType() {
		return BossType.InfernalGhast;
	}
}
