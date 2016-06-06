package net.dungeonrealms.game.world.entities.types.monsters;

import net.dungeonrealms.game.miscellaneous.SkullCreator;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import net.dungeonrealms.API;
import net.dungeonrealms.game.world.entities.EnumEntityType;
import net.dungeonrealms.game.world.entities.types.monsters.base.DRSkeleton;
import net.dungeonrealms.game.world.items.DamageAPI;
import net.dungeonrealms.game.world.items.Item.ItemTier;
import net.dungeonrealms.game.world.items.Item.ItemType;
import net.dungeonrealms.game.world.items.itemgenerator.ItemGenerator;
import net.minecraft.server.v1_8_R3.EntityHuman;
import net.minecraft.server.v1_8_R3.EntityLiving;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.PathfinderGoalArrowAttack;
import net.minecraft.server.v1_8_R3.PathfinderGoalFloat;
import net.minecraft.server.v1_8_R3.PathfinderGoalHurtByTarget;
import net.minecraft.server.v1_8_R3.PathfinderGoalLookAtPlayer;
import net.minecraft.server.v1_8_R3.PathfinderGoalNearestAttackableTarget;
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
    
    @SuppressWarnings("unchecked")
    public EntityFireImp(World world, int tier, EnumEntityType entityType) {
        super(world, EnumMonster.FireImp, tier, entityType);
        this.goalSelector.a(1, new PathfinderGoalFloat(this));
        this.goalSelector.a(4, new PathfinderGoalArrowAttack(this, 1.0D, 20, 60, 15.0F));
        this.goalSelector.a(7, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
        this.targetSelector.a(1, new PathfinderGoalHurtByTarget(this, false));
        this.targetSelector.a(5, new PathfinderGoalNearestAttackableTarget(this, EntityHuman.class, true));
        this.tier = tier;
        this.setEquipment(4, CraftItemStack.asNMSCopy(SkullCreator.SkullTextures.IMP.getSkull()));
    }

    @Override
    public void setArmor(int tier) {
        ItemStack leggings = new ItemGenerator().setType(ItemType.LEGGINGS).setTier(ItemTier.getByTier(tier)).setRarity(API.getItemRarity()).getItem();
        ItemStack chestplate = new ItemGenerator().setType(ItemType.CHESTPLATE).setTier(ItemTier.getByTier(tier)).setRarity(API.getItemRarity()).getItem();
        ItemStack boots = new ItemGenerator().setType(ItemType.BOOTS).setTier(ItemTier.getByTier(tier)).setRarity(API.getItemRarity()).getItem();
        ItemStack staff = new ItemGenerator().setType(ItemType.STAFF).setTier(ItemTier.getByTier(tier)).setRarity(API.getItemRarity()).getItem();
        this.setEquipment(0, CraftItemStack.asNMSCopy(staff));
        this.setEquipment(1, CraftItemStack.asNMSCopy(boots));
        this.setEquipment(2, CraftItemStack.asNMSCopy(leggings));
        this.setEquipment(3, CraftItemStack.asNMSCopy(chestplate));
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
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = this.getEquipment(0);
        NBTTagCompound tag = nmsItem.getTag();
        DamageAPI.fireStaffProjectileMob((CraftLivingEntity) this.getBukkitEntity(), tag, (CraftLivingEntity) entity.getBukkitEntity());
    }

}
