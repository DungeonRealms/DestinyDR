/**
 * 
 */
package net.dungeonrealms.entities.types.monsters;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.metadata.FixedMetadataValue;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.entities.types.MeleeEntityZombie;
import net.dungeonrealms.enums.EnumEntityType;
import net.dungeonrealms.mastery.Utils;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.Item;
import net.minecraft.server.v1_8_R3.World;

/**
 * Created by Chase on Sep 21, 2015
 */
public class EntityBandit extends MeleeEntityZombie {
	public EntityBandit(World world) {
		super(world);
	}

	/**
	 * @param world
	 * @param mobName
	 * @param mobHead
	 * @param tier
	 * @param entityType
	 */
	public EntityBandit(World world, int tier, EnumEntityType entityType) {
		super(world, getRandomName(false), getRandomHead(), tier, entityType);
		checkPoison();
	}

	/**
	 * 
	 */
	private void checkPoison() {
		if (Utils.randInt(1, 6) == 4) {
			this.getBukkitEntity().setMetadata("special",
				new FixedMetadataValue(DungeonRealms.getInstance(), "poison"));
			this.setCustomName(ChatColor.GREEN.toString() + ChatColor.UNDERLINE.toString() + "" + getRandomName(true));
		}
	}

	/**
	 * @return
	 */
	private static String getRandomName(boolean bool) {
		String[] adjectives = new String[] { "Clumsy", "Lazy", "Old", "Ugly", "Pretty", "Dumb", "Friendly", "Sleepy",
			"Majestic", "Intrigued", "Dignified", "Couragous", "Timid", "Gloomy", "Noble", "Naive" };
		List<String> list = Arrays.asList(adjectives);
		Collections.shuffle(list);
		if (bool)
			return list.get(0) + " Poisonous " + " Bandit";
		return list.get(0) + " Bandit";
	}

	/**
	 * @return
	 */
	private static String getRandomHead() {
		return "Steve";
	}

	@Override
	protected Item getLoot() {
		return null;
	}

	@Override
	protected void getRareDrop() {

	}

	@Override
	public void setStats() {

	}

}
