package net.dungeonrealms.game.world.entities.types.monsters.boss;

import net.dungeonrealms.game.world.entities.EnumEntityType;
import net.dungeonrealms.game.world.entities.types.monsters.EnumBoss;
import net.dungeonrealms.game.world.entities.types.monsters.EnumMonster;
import net.dungeonrealms.game.world.entities.types.monsters.base.DRWitherSkeleton;
import net.minecraft.server.v1_9_R2.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * Created by Chase on Oct 20, 2015
 */
public class Aceron extends DRWitherSkeleton implements Boss{

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
	public EnumBoss getEnumBoss() {
		return null;
	}

	@Override
	public void onBossDeath() {
		
	}
	@Override
	public void onBossHit(EntityDamageByEntityEvent event) {
		LivingEntity en = (LivingEntity) event.getEntity();		
	}

	@Override
	protected void setStats() {

	}
}
