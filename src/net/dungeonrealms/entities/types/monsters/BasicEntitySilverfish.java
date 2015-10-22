package net.dungeonrealms.entities.types.monsters;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import net.dungeonrealms.entities.EnumEntityType;
import net.dungeonrealms.entities.utils.EntityStats;
import net.dungeonrealms.items.ItemGenerator;
import net.dungeonrealms.items.armor.ArmorGenerator;
import net.dungeonrealms.mastery.MetadataUtils;
import net.dungeonrealms.mastery.Utils;
import net.minecraft.server.v1_8_R3.EntitySilverfish;
import net.minecraft.server.v1_8_R3.World;

/**
 * Created by Chase on Oct 21, 2015
 */
public class BasicEntitySilverfish extends EntitySilverfish {

	public EnumMonster enumMonster;

	public BasicEntitySilverfish(World world, EnumMonster type, int tier) {
		super(world);
		this.enumMonster = type;
		int level = Utils.getRandomFromTier(tier);
		MetadataUtils.registerEntityMetadata(this, EnumEntityType.HOSTILE_MOB, tier, level);
		EntityStats.setMonsterRandomStats(this, level, tier);
		this.getBukkitEntity().setCustomName(ChatColor.LIGHT_PURPLE.toString() + "[" + level + "] " + ChatColor.RESET
		        + type.getPrefix() + " " + type.name + " " + type.getSuffix());
		setArmor(tier);
	}

	protected void setArmor(int tier) {
		ItemStack[] armor = getTierArmor(tier);
		// weapon, boots, legs, chest, helmet/head
		ItemStack weapon = getTierWeapon(tier);
		this.setEquipment(0, CraftItemStack.asNMSCopy(weapon));
		this.setEquipment(1, CraftItemStack.asNMSCopy(armor[0]));
		this.setEquipment(2, CraftItemStack.asNMSCopy(armor[1]));
		this.setEquipment(3, CraftItemStack.asNMSCopy(armor[2]));
		this.setEquipment(4, this.getHead());
	}

	protected String getCustomEntityName() {
		return this.enumMonster.name;
	}

	protected net.minecraft.server.v1_8_R3.ItemStack getHead() {
		ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
		SkullMeta meta = (SkullMeta) head.getItemMeta();
		meta.setOwner(enumMonster.mobHead);
		head.setItemMeta(meta);
		return CraftItemStack.asNMSCopy(head);
	}

	private ItemStack getTierWeapon(int tier) {
		return new ItemGenerator().next(net.dungeonrealms.items.Item.ItemType.BOW,
		        net.dungeonrealms.items.Item.ItemTier.getByTier(tier));
	}

	private ItemStack[] getTierArmor(int tier) {
		return new ArmorGenerator().nextTier(tier);
	}
}
