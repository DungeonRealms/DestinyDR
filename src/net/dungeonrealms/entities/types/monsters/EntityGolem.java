package net.dungeonrealms.entities.types.monsters;

import net.dungeonrealms.entities.types.MeleeEntityZombie;
import net.dungeonrealms.enums.EnumEntityType;
import net.dungeonrealms.mechanics.XRandom;
import net.minecraft.server.v1_8_R3.Item;
import net.minecraft.server.v1_8_R3.World;

/**
 * Created by Chase on Sep 28, 2015
 */
public class EntityGolem extends MeleeEntityZombie {

	/**
	 * @param world
	 */
	public EntityGolem(World world) {
		super(world, setName(), null, getLvl() + 3, EnumEntityType.HOSTILE_MOB, false);
		mobHead = "Steve";
		this.setEquipment(4, getHead());
	}

	/**
	 * @return
	 */
	private static int getLvl() {
		return new XRandom().nextInt(2);
	}

	/**
	 * @return
	 */
	private static String setName() {
		return "Enchanted Iron Golem";
	}

	@Override
	protected Item getLoot() {
		return null;
	}

	@Override
	protected void getRareDrop() {

	}

	@Override
	protected void setStats() {

	}

}
