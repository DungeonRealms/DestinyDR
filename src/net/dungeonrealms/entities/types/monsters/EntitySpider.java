package net.dungeonrealms.entities.types.monsters;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.dungeonrealms.entities.types.MeleeEntityZombie;
import net.dungeonrealms.enums.EnumEntityType;
import net.minecraft.server.v1_8_R3.Item;
import net.minecraft.server.v1_8_R3.World;

/**
 * Created by Chase on Oct 2, 2015
 */
public class EntitySpider extends MeleeEntityZombie {

	/**
	 * @param world
	 * @param mobName
	 * @param tier
	 */
	public EntitySpider(World world, String mobName, int tier) {
		super(world, getFirst() + " Spider", "Steve", tier, EnumEntityType.HOSTILE_MOB, true);
	}

	public EntitySpider(World world){
		super(world);
	}
	
	/**
	 * @return
	 */
	private static String getFirst() {
		String[] array = new String[]{"Scary", "Spookey", "Hairy"};
		List<String> list = Arrays.asList(array);
		Collections.shuffle(list);
		return list.get(0);
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
