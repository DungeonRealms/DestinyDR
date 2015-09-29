/**
 * 
 */
package net.dungeonrealms.entities.types.monsters;

import net.dungeonrealms.items.ItemGenerator;
import net.dungeonrealms.mastery.MetadataUtils;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import net.dungeonrealms.banks.BankMechanics;
import net.dungeonrealms.entities.types.RangedEntitySkeleton;
import net.dungeonrealms.enums.EnumEntityType;

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

	private int tier;

	public EntityFireImp(World world, int tier, EnumEntityType entityType) {
		super(world, "Fire Imp", "Satan", tier, entityType);
		this.tier = tier;
		this.setEquipment(0, CraftItemStack.asNMSCopy(new ItemGenerator().next()));
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
		LeatherArmorMeta lch = (LeatherArmorMeta) lchest.getItemMeta();
		lch.setColor(Color.fromRGB(176, 23, 23));
		lchest.setItemMeta(lch);
		// weapon, boots, legs, chest, helmet/head
		// this.setEquipment(0, CraftItemStack.asNMSCopy(weapon));
		this.setEquipment(1, CraftItemStack.asNMSCopy(boots));
		this.setEquipment(2, CraftItemStack.asNMSCopy(leggings));
		this.setEquipment(3, CraftItemStack.asNMSCopy(chestplate));
		this.setEquipment(4, getHead());
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
	public void setStats() {

	}

	@Override
	public void a(EntityLiving entity, float f) {
		double d0 = entity.locX - this.locX;
		float f1 = MathHelper.c(f) * 0.5F;
		double d1 = entity.getBoundingBox().b + (double) (entity.length / 2.0F)
			- (this.locY + (double) (this.length / 2.0F));
		double d2 = entity.locZ - this.locZ;
		EntityWitherSkull entityWitherSkull = new EntityWitherSkull(this.world, this,
			d0 + this.random.nextGaussian() * (double) f1, d1, d2 + this.random.nextGaussian() * (double) f1);
		entityWitherSkull.locY = this.locY + (double) (this.length / 2.0F) + 0.5D;
		Projectile projectileWitherSkull = (Projectile) entityWitherSkull.getBukkitEntity();
		projectileWitherSkull.setVelocity(projectileWitherSkull.getVelocity().multiply(1.35));
		net.minecraft.server.v1_8_R3.ItemStack nmsItem = this.getEquipment(0);
		NBTTagCompound tag = nmsItem.getTag();
		MetadataUtils.registerProjectileMetadata(tag, projectileWitherSkull, tier);
		this.makeSound("random.bow", 1.0F, 1.0F / (0.8F));
		this.world.addEntity(entityWitherSkull);
	}

}
