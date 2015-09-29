package net.dungeonrealms.entities.types.monsters;

import net.dungeonrealms.entities.types.MeleeEntityZombie;
import net.dungeonrealms.entities.utils.EntityStats;
import net.dungeonrealms.enums.EnumEntityType;
import net.dungeonrealms.items.ItemGenerator;
import net.dungeonrealms.mastery.MetadataUtils;
import net.dungeonrealms.mastery.Utils;
import net.dungeonrealms.mechanics.XRandom;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.Item;
import net.minecraft.server.v1_8_R3.World;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;

/**
 * Created by Chase on Sep 28, 2015
 */
public class EntityGolem extends MeleeEntityZombie {

	public EntityGolem(World world, int tier, EnumEntityType entityType) {
		super(world, "Golem", null, tier, entityType, true);
		this.setEquipment(0, CraftItemStack.asNMSCopy(new ItemGenerator().next()));
		int level = Utils.getRandomFromTier(tier);
		MetadataUtils.registerEntityMetadata(this, entityType, tier, level);
		EntityStats.setMonsterStats(this, level, tier);
		this.setCustomName(ChatColor.GOLD + "Golem");
		this.setCustomNameVisible(true);
	}
	/**
	 * @param world
	 */
	public EntityGolem(World world) {
		super(world);
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
