/**
 * 
 */
package net.dungeonrealms.entities.types.monsters;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import net.dungeonrealms.entities.types.RangedEntitySkeleton;
import net.dungeonrealms.enums.EnumEntityType;
import net.minecraft.server.v1_8_R3.EntityLiving;
import net.minecraft.server.v1_8_R3.EntitySmallFireball;
import net.minecraft.server.v1_8_R3.Item;
import net.minecraft.server.v1_8_R3.ItemArmor;
import net.minecraft.server.v1_8_R3.Items;
import net.minecraft.server.v1_8_R3.MathHelper;
import net.minecraft.server.v1_8_R3.World;

/**
 * Created by Chase on Sep 21, 2015
 */
public class EntityFireImp extends RangedEntitySkeleton {

	/**
	 * @param world
	 * @param mobName
	 * @param mobHead
	 * @param tier
	 * @param entityType
	 */
	public EntityFireImp(World world, int tier, EnumEntityType entityType) {
		super(world, "Fire Imp", "Satan", tier, entityType);
		this.setEquipment(0, CraftItemStack.asNMSCopy(new ItemStack(Material.WOOD_HOE, 1)));
	}

	@Override
	public void setArmor(int tier) {
		ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS, 1);
		ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE, 1);
		ItemStack boots = new ItemStack(Material.LEATHER_BOOTS, 1);
		LeatherArmorMeta lam = (LeatherArmorMeta) leggings.getItemMeta();
		lam.setColor(Color.RED);
		leggings.setItemMeta(lam);
		chestplate.setItemMeta(lam);
		boots.setItemMeta(lam);
		ItemStack lchest = new ItemStack(Material.LEATHER_CHESTPLATE, 1);
      LeatherArmorMeta lch = (LeatherArmorMeta)lchest.getItemMeta();
      lch.setColor(Color.fromRGB(176, 23, 23));
      lchest.setItemMeta(lch);
		// weapon, boots, legs, chest, helmet/head
		ItemStack weapon = getTierWeapon(tier);
		this.setEquipment(0, CraftItemStack.asNMSCopy(weapon));
		this.setEquipment(1, CraftItemStack.asNMSCopy(boots));
		this.setEquipment(2, CraftItemStack.asNMSCopy(leggings));
		this.setEquipment(3, CraftItemStack.asNMSCopy(chestplate));
		this.setEquipment(4, getHead());
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

	@Override
	public void a(EntityLiving entity, float f) {
		double d0 = entity.locX - this.locX;
		float f1 = MathHelper.c(f) * 0.5F;
		double d1 = entity.getBoundingBox().b + (double) (entity.length / 2.0F)
			- (this.locY + (double) (this.length / 2.0F));
		double d2 = entity.locZ - this.locZ;

		EntitySmallFireball entitysmallfireball = new EntitySmallFireball(this.world, this,
			d0 + this.random.nextGaussian() * (double) f1, d1, d2 + this.random.nextGaussian() * (double) f1);
		entitysmallfireball.locY = this.locY + (double) (this.length / 2.0F) + 0.5D;
		this.makeSound("random.bow", 1.0F, 1.0F / (0.8F));
		this.world.addEntity(entitysmallfireball);
		// entityarrow.setLocation(this.locX, this.locY + 3, this.locZ, 1, 1);
		// this.world.addEntity(entityarrow);
		// Vector v = this.getBukkitEntity().getVelocity();
		// entityarrow.setDirection(v.getX(), v.getY(), v.getZ());
	}

}
