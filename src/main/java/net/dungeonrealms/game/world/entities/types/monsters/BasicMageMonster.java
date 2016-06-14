package net.dungeonrealms.game.world.entities.types.monsters;

import net.dungeonrealms.API;
import net.dungeonrealms.game.miscellaneous.SkullTextures;
import net.dungeonrealms.game.world.entities.EnumEntityType;
import net.dungeonrealms.game.world.entities.types.monsters.base.DRSkeleton;
import net.dungeonrealms.game.world.items.DamageAPI;
import net.dungeonrealms.game.world.items.Item.ItemTier;
import net.dungeonrealms.game.world.items.Item.ItemType;
import net.dungeonrealms.game.world.items.itemgenerator.ItemGenerator;
import net.minecraft.server.v1_9_R2.EntityLiving;
import net.minecraft.server.v1_9_R2.EnumItemSlot;
import net.minecraft.server.v1_9_R2.NBTTagCompound;
import net.minecraft.server.v1_9_R2.World;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.LivingEntity;

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
        LivingEntity livingEntity = (LivingEntity) this.getBukkitEntity();
        org.bukkit.inventory.ItemStack weapon = new ItemGenerator().setType(ItemType.STAFF).setTier(ItemTier.getByTier(tier)).setRarity(API.getItemRarity(false)).generateItem().getItem();
        this.setEquipment(EnumItemSlot.MAINHAND, CraftItemStack.asNMSCopy(weapon));
        livingEntity.getEquipment().setItemInMainHand(weapon);
        switch (mons) {
            case Naga:
                this.setEquipment(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(SkullTextures.NAGA.getSkull()));
                livingEntity.getEquipment().setHelmet(SkullTextures.NAGA.getSkull());
                break;
            case Mage:
                this.setEquipment(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(SkullTextures.MAGE.getSkull()));
                livingEntity.getEquipment().setHelmet(SkullTextures.MAGE.getSkull());
                break;
            case Daemon2:
                this.setEquipment(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(SkullTextures.DEVIL.getSkull()));
                livingEntity.getEquipment().setHelmet(SkullTextures.DEVIL.getSkull());
                break;
        }
    }

    public BasicMageMonster(World world) {
        super(world);
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
        net.minecraft.server.v1_9_R2.ItemStack nmsItem = this.getEquipment(EnumItemSlot.MAINHAND);
        NBTTagCompound tag = nmsItem.getTag();
        DamageAPI.fireStaffProjectileMob((CraftLivingEntity) this.getBukkitEntity(), tag, (CraftLivingEntity) entity.getBukkitEntity());
    }

}
