package net.dungeonrealms.monsters.entities;

import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.EntityZombie;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagInt;
import net.minecraft.server.v1_8_R3.NBTTagString;
import net.minecraft.server.v1_8_R3.World;

/**
 * Created by Nick on 9/17/2015.
 */
public class EntityPirate extends EntityZombie {
	public EntityPirate(World world) {
		super(world);

		NBTTagCompound tag = this.getNBTTag() == null ? new NBTTagCompound() : this.getNBTTag();
		this.c(tag);
		tag.set("type", new NBTTagString("mob"));
		tag.set("level", new NBTTagInt(1));
		this.f(tag);
		this.setCustomName(ChatColor.GOLD + "Pirate");
		this.setCustomNameVisible(true);
		setArmor(1);
	}

	public ItemStack[] getTierArmor(int tier) {
		if (tier == 1) {
			return new ItemStack[] { new ItemStack(Material.LEATHER_BOOTS, 1),
					new ItemStack(Material.LEATHER_LEGGINGS, 1), new ItemStack(Material.LEATHER_CHESTPLATE, 1),
					new ItemStack(Material.LEATHER_HELMET, 1) };
		}
		return null;
	}

	protected net.minecraft.server.v1_8_R3.ItemStack getHead(String name) {
		ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
		SkullMeta meta = (SkullMeta) head.getItemMeta();
		meta.setOwner(name);
		head.setItemMeta(meta);
		return CraftItemStack.asNMSCopy(head);
	}

	public void setArmor(int tier) {
		ItemStack[] armor = getTierArmor(tier);
		// weapon, boots, legs, chest, helmet/head
		ItemStack weapon = getTierWeapon(tier);
		this.setEquipment(0, CraftItemStack.asNMSCopy(weapon));
		this.setEquipment(1, CraftItemStack.asNMSCopy(armor[0]));
		this.setEquipment(2, CraftItemStack.asNMSCopy(armor[1]));
		this.setEquipment(3, CraftItemStack.asNMSCopy(armor[2]));
		this.setEquipment(4, this.getHead("samsamsam1234"));
	}

	ItemStack getTierWeapon(int tier) {
		return new ItemStack(Material.WOOD_SWORD, 1);
	}

	@Override
	protected String z() {
		return "mob.zombie.say";
	}

	@Override
	protected String bo() {
		return "random.bowhit";
	}

	@Override
	protected String bp() {
		return "mob.zombie.death";
	}
}
