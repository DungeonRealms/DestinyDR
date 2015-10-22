package net.dungeonrealms.entities.types.monsters.boss;

import org.bukkit.entity.LivingEntity;

import net.dungeonrealms.entities.types.monsters.EntityWitherSkeleton;
import net.dungeonrealms.entities.types.monsters.EnumBoss;
import net.dungeonrealms.entities.types.monsters.EnumMonster;
import net.minecraft.server.v1_8_R3.World;

/**
 * Created by Chase on Oct 20, 2015
 */
public class Aceron extends EntityWitherSkeleton implements Boss{

	/**
	 * @param world
	 * @param mon
	 * @param tier
	 */
	public Aceron(World world, EnumMonster mon) {
		super(world, mon, 5);
	}

	@Override
	public EnumBoss getEnumBoss() {
		return null;
	}

	@Override
	public void onBossDeath() {
		
	}

	@Override
	public void onBossHit(LivingEntity en) {
		
	}

}
