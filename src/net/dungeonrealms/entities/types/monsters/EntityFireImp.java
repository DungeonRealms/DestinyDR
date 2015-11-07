package net.dungeonrealms.entities.types.monsters;

import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.ItemStack;

import net.dungeonrealms.entities.EnumEntityType;
import net.dungeonrealms.entities.types.monsters.base.DRSkeleton;
import net.dungeonrealms.items.Item.ItemTier;
import net.dungeonrealms.items.Item.ItemType;
import net.dungeonrealms.items.ItemGenerator;
import net.dungeonrealms.items.armor.Armor;
import net.dungeonrealms.items.armor.ArmorGenerator;
import net.dungeonrealms.mastery.MetadataUtils;
import net.minecraft.server.v1_8_R3.EntityLiving;
import net.minecraft.server.v1_8_R3.EntityWitherSkull;
import net.minecraft.server.v1_8_R3.MathHelper;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.World;

/**
 * Created by Chase on Sep 21, 2015
 */
public class EntityFireImp extends DRSkeleton {

    /**
     * @param world
     * @param mobName
     * @param mobHead
     * @param tier
     * @param entityType
     */

    private int tier;

    public EntityFireImp(World world){
    	super(world);
    }
    
    public EntityFireImp(World world, int tier, EnumEntityType entityType) {
        super(world, EnumMonster.FireImp, tier, entityType);
        this.tier = tier;
        this.setEquipment(0, CraftItemStack.asNMSCopy(new ItemGenerator().getDefinedStack(ItemType.STAFF, ItemTier.getByTier(tier), ItemGenerator.getRandomItemModifier())));
    }

    @Override
    public void setArmor(int tier) {
        ItemStack leggings = new ItemStack(new ArmorGenerator().getDefinedStack(Armor.EquipmentType.LEGGINGS, Armor.ArmorTier.getByTier(tier), ArmorGenerator.getRandomItemModifier()));
        ItemStack chestplate = new ItemStack(new ArmorGenerator().getDefinedStack(Armor.EquipmentType.CHESTPLATE, Armor.ArmorTier.getByTier(tier), ArmorGenerator.getRandomItemModifier()));
        ItemStack boots = new ItemStack(new ArmorGenerator().getDefinedStack(Armor.EquipmentType.BOOTS, Armor.ArmorTier.getByTier(tier), ArmorGenerator.getRandomItemModifier()));
        this.setEquipment(1, CraftItemStack.asNMSCopy(boots));
        this.setEquipment(2, CraftItemStack.asNMSCopy(leggings));
        this.setEquipment(3, CraftItemStack.asNMSCopy(chestplate));
        this.setEquipment(4, getHead());
    }

    @Override
    protected void getRareDrop() {

    }
    
	@Override
	public EnumMonster getEnum() {
		return this.monsterType;
	}

    @Override
    public void setStats() {

    }

    @Override
    public void a(EntityLiving entity, float f) {
        double d0 = entity.locX - this.locX;
        float f1 = MathHelper.c(f) * 0.5F;
        double d1 = entity.getBoundingBox().b + entity.length / 2.0F - (this.locY + this.length / 2.0F);
        double d2 = entity.locZ - this.locZ;
        EntityWitherSkull entityWitherSkull = new EntityWitherSkull(this.world, this, d0 + this.random.nextGaussian() * f1, d1, d2 + this.random.nextGaussian() * f1);
        entityWitherSkull.locY = this.locY + this.length / 2.0F + 0.5D;
        Projectile projectileWitherSkull = (Projectile) entityWitherSkull.getBukkitEntity();
        projectileWitherSkull.setVelocity(projectileWitherSkull.getVelocity().multiply(1.35));
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = this.getEquipment(0);
        NBTTagCompound tag = nmsItem.getTag();
        MetadataUtils.registerProjectileMetadata(tag, projectileWitherSkull, tier);
        this.makeSound("random.bow", 1.0F, 1.0F / (0.8F));
        this.world.addEntity(entityWitherSkull);
    }

}
