package net.dungeonrealms.entities.types.monsters;

import net.dungeonrealms.entities.EnumEntityType;
import net.dungeonrealms.entities.types.monsters.base.DRSkeleton;
import net.dungeonrealms.items.DamageAPI;
import net.dungeonrealms.items.Item.ItemTier;
import net.dungeonrealms.items.Item.ItemType;
import net.dungeonrealms.items.ItemGenerator;
import net.minecraft.server.v1_8_R3.EntityLiving;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.World;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;

/**
 * Created by Chase on Oct 2, 2015
 */
public class BasicMageMonster extends DRSkeleton {
    /**
     * @param world
     * @param mobName
     * @param mobHead
     * @param tier
     * @param entityType
     */

    private int tier;

    public BasicMageMonster(World world, EnumMonster mons, int tier) {
        super(world, mons, tier, EnumEntityType.HOSTILE_MOB);
        this.tier = tier;
        this.setEquipment(0, CraftItemStack.asNMSCopy(new ItemGenerator().getDefinedStack(ItemType.STAFF, ItemTier.getByTier(tier), ItemGenerator.getRandomItemModifier())));
    }

    public BasicMageMonster(World world) {
        super(world);
    }

    @Override
    protected void getRareDrop() {

    }

    @Override
    public void setStats() {

    }
	@Override
	public EnumMonster getEnum() {
		return this.monsterType;
	}
    @Override
    public void a(EntityLiving entity, float f) {
        /*double d0 = entity.locX - this.locX;
        float f1 = MathHelper.c(f) * 0.5F;
        double d1 = entity.getBoundingBox().b + entity.length / 2.0F - (this.locY + this.length / 2.0F);
        double d2 = entity.locZ - this.locZ;
        EntityWitherSkull entityWitherSkull = new EntityWitherSkull(this.world, this,d0 + this.random.nextGaussian() * f1, d1, d2 + this.random.nextGaussian() * f1);
        entityWitherSkull.locY = this.locY + this.length / 2.0F + 0.5D;
        Projectile projectileWitherSkull = (Projectile) entityWitherSkull.getBukkitEntity();
        projectileWitherSkull.setVelocity(projectileWitherSkull.getVelocity().multiply(1.35));
        MetadataUtils.registerProjectileMetadata(tag, projectileWitherSkull, tier);
        this.makeSound("random.bow", 1.0F, 1.0F / (0.8F));
        this.world.addEntity(entityWitherSkull);*/

        net.minecraft.server.v1_8_R3.ItemStack nmsItem = this.getEquipment(0);
        NBTTagCompound tag = nmsItem.getTag();
        DamageAPI.fireStaffProjectileMob((CraftLivingEntity) this.getBukkitEntity(), tag, (CraftLivingEntity) entity.getBukkitEntity());
    }

}
