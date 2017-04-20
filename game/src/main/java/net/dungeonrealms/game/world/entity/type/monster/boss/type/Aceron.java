package net.dungeonrealms.game.world.entity.type.monster.boss.type;

import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.world.entity.EnumEntityType;
import net.dungeonrealms.game.world.entity.type.monster.base.DRWitherSkeleton;
import net.dungeonrealms.game.world.entity.type.monster.boss.DungeonBoss;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumDungeonBoss;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.minecraft.server.v1_9_R2.Entity;
import net.minecraft.server.v1_9_R2.World;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * Created by Chase on Oct 20, 2015
 */
public class Aceron extends DRWitherSkeleton implements DungeonBoss {

    /**
     * @param world
     * @param mon
     */
    public Aceron(World world, EnumMonster mon) {
        super(world, mon, 5, EnumEntityType.HOSTILE_MOB);
        this.setSize(0.7F, 2.4F);
        this.fireProof = true;
        this.setSkeletonType(1);
    }

    @Override
    public EnumDungeonBoss getEnumBoss() {
        return null;
    }

    @Override
    public void onBossDeath() {

    }

    @Override
    public void collide(Entity e) {
    }

    @Override
    public void onBossAttack(EntityDamageByEntityEvent event) {
        LivingEntity en = (LivingEntity) event.getEntity();
    }

    @Override
    protected void setStats() {

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
	public void addKillStat(PlayerWrapper gp) {
		// TODO Auto-generated method stub
	}
}
