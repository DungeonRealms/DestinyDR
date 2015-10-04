package net.dungeonrealms.entities.types.monsters;

import net.dungeonrealms.banks.BankMechanics;
import net.dungeonrealms.entities.types.MeleeEntityZombie;
import net.dungeonrealms.entities.utils.EntityStats;
import net.dungeonrealms.enums.EnumEntityType;
import net.dungeonrealms.mastery.MetadataUtils;
import net.dungeonrealms.mastery.Utils;
import net.minecraft.server.v1_8_R3.Item;
import net.minecraft.server.v1_8_R3.World;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Chase on Oct 2, 2015
 */
public class BasicMeleeMonster extends MeleeEntityZombie {

	/**
	 * @param world
	 * @param mobName
	 * @param mobHead
	 * @param tier
	 */
	public BasicMeleeMonster(World world, String mobName, String mobHead, int tier) {
		super(world, mobName, mobHead, tier, EnumEntityType.HOSTILE_MOB, true);
		int level = Utils.getRandomFromTier(tier);
		MetadataUtils.registerEntityMetadata(this, entityType, tier, level);
		EntityStats.setMonsterStats(this, level, tier);
//		String special = getSpecial();
//		String cc = getChatColor(special);
//		if (!special.equalsIgnoreCase("null"))
//			this.getBukkitEntity().setCustomName(ChatColor.LIGHT_PURPLE.toString() + "[" + level + "] "
//				+ ChatColor.RESET + getPrefix() + mobName + special + getSuffix());
	}

	/**
	 * @param special
	 * @return
	 */
	private String getChatColor(String special) {
		switch (special) {
		case "Ice":
			return ChatColor.BLUE.toString();
		case "Poisonous":
			return ChatColor.GREEN.toString();
		case "Fire":
			return ChatColor.RED.toString();
		default:
			return "null";
		}
	}

	private String getSpecial() {

		/*
		 * public MobEffect(int i, int j, int k, boolean flag) { this.effectId =
		 * i; this.duration = j; this.amplification = k; this.ambient = flag; }
		 * 
		 */

		switch (name) {
		case "naga":
		case "golem":
			return "Ice";
		default:
			return "null";
		}
	}

	public BasicMeleeMonster(World world) {
		super(world);
	}

	@Override
	protected Item getLoot() {
		ItemStack item = BankMechanics.gem.clone();
		item.setAmount(this.random.nextInt(5));
		this.world.getWorld().dropItemNaturally(this.getBukkitEntity().getLocation(), item);
		return null;
	}

	@Override
	protected void getRareDrop() {

	}

	@Override
	protected void setStats() {

	}

	@Override
	public String getPrefix() {
		String[] adjectives = null;
		switch (name) {
		case "naga":
			adjectives = new String[] { "Weak" };
			break;
		case "troll":
			adjectives = new String[] { "" };
			break;
		}
		List<String> list = Arrays.asList(adjectives);
		Collections.shuffle(list);
		return list.get(0);

	}

	@Override
	public String getSuffix() {
		// TODO Auto-generated method stub
		return null;
	}

}
