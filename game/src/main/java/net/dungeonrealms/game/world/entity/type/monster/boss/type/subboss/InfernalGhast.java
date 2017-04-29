package net.dungeonrealms.game.world.entity.type.monster.boss.type.subboss;

import net.dungeonrealms.game.handler.HealthHandler;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.mechanic.dungeons.BossType;
import net.dungeonrealms.game.mechanic.dungeons.DungeonBoss;
import net.dungeonrealms.game.world.entity.type.monster.base.DRGhast;
import net.dungeonrealms.game.world.entity.type.monster.boss.type.InfernalAbyss;
import net.dungeonrealms.game.world.item.DamageAPI;
import net.minecraft.server.v1_9_R2.World;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * Infernal Ghast - Infernal Abyss subboss.
 * 
 * Redone on April 28th, 2017.
 * @author Kneesnap
 */
public class InfernalGhast extends DRGhast implements DungeonBoss {

    public InfernalGhast(World world) {
        super(world);
        createEntity(100);
        DamageAPI.setArmorBonus(getBukkit(), 50);
    }
    
    private InfernalAbyss getMainBoss() {
    	return (InfernalAbyss) getDungeon().getBoss();
    }

    public void init(int hp) {
        HealthHandler.setMaxHP(getBukkit(), hp);
        HealthHandler.setMonsterHP(getBukkit(), hp);
        this.getBukkitEntity().setPassenger(getMainBoss().getBukkit());
    }

    @Override
    public void onBossDeath(Player player) {
    	getDungeon().getAllPlayers().forEach(p -> p.playSound(getBukkit().getLocation(), Sound.ENTITY_GHAST_SCREAM, 2F, 1F));
    }

    @Override
    public void onBossAttack(EntityDamageByEntityEvent event) {
    	double cHP = HealthHandler.getMonsterHP(getBukkit());
        if (cHP <= HealthHandler.getMonsterMaxHP(getBukkit()) / 2) { // If the ghast is at less then 50% health, it's time for infernal's final form.
        	getBukkit().eject();
            getMainBoss().doFinalForm(cHP);
            die();
        }
    }

	@Override
	public int getGemDrop() {
		return 0;
	}

	@Override
	public int getXPDrop() {
		return 0;
	}

	@Override
	public String[] getItems() {
		return null;
	}

	@Override
	public void addKillStat(GamePlayer gp) {
		
	}

	@Override
	public BossType getBossType() {
		return BossType.InfernalGhast;
	}
}
