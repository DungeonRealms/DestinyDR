package net.dungeonrealms.entities.types.monsters;

import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.ItemStack;

import net.dungeonrealms.banks.BankMechanics;
import net.dungeonrealms.entities.types.RangedEntitySkeleton;
import net.dungeonrealms.enums.EnumEntityType;
import net.dungeonrealms.items.Item.ItemTier;
import net.dungeonrealms.items.Item.ItemType;
import net.dungeonrealms.items.ItemGenerator;
import net.dungeonrealms.mastery.MetadataUtils;
import net.minecraft.server.v1_8_R3.EntityLiving;
import net.minecraft.server.v1_8_R3.EntityWitherSkull;
import net.minecraft.server.v1_8_R3.Item;
import net.minecraft.server.v1_8_R3.MathHelper;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.World;

/**
 * Created by Chase on Oct 2, 2015
 */
public class BasicMageMonster extends RangedEntitySkeleton{
   /**
    * @param world
    * @param mobName
    * @param mobHead
    * @param tier
    * @param entityType
    */

   private int tier;

   public BasicMageMonster(World world, String mobName, String mobHead, int tier) {
       super(world, mobName, mobHead, tier, EnumEntityType.HOSTILE_MOB);
       this.tier = tier;
       this.setEquipment(0, CraftItemStack.asNMSCopy(new ItemGenerator().getDefinedStack(ItemType.STAFF, ItemTier.getById(tier), ItemGenerator.getRandomItemModifier())));
   }
	public BasicMageMonster(World world){
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
