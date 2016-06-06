package net.dungeonrealms.game.world.entities.types.monsters;

import net.dungeonrealms.game.miscellaneous.SkullCreator;
import net.dungeonrealms.game.world.entities.EnumEntityType;
import net.dungeonrealms.game.world.entities.types.monsters.base.DRSkeleton;
import net.dungeonrealms.game.world.items.DamageAPI;
import net.minecraft.server.v1_8_R3.EntityLiving;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.World;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;

/**
 * Created by Chase on Oct 17, 2015
 */
public class BasicEntitySkeleton extends DRSkeleton {
    private int tier;

	/**
	 * @param world
	 * @param tier
	 */
	public BasicEntitySkeleton(World world, int tier) {
		super(world, EnumMonster.Skeleton, tier, EnumEntityType.HOSTILE_MOB);
        this.tier = tier;
        this.setEquipment(4, CraftItemStack.asNMSCopy(SkullCreator.SkullTextures.SKELETON.getSkull()));
	}

	/**
	 * @param world
	 */
	public BasicEntitySkeleton(World world) {
		super(world);
	}

    @Override
    public void a(EntityLiving entityliving, float f) {
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = this.getEquipment(0);
        NBTTagCompound tag = nmsItem.getTag();
        DamageAPI.fireArrowFromMob((CraftLivingEntity) this.getBukkitEntity(), tag, (CraftLivingEntity) entityliving.getBukkitEntity());
    }

    @Override
    public void setStats() {

    }

    @Override
    protected void getRareDrop() {
    }

    @Override
    protected String z() {
        return "";
    }

    @Override
    protected String bo() {
        return "game.player.hurt";
    }

    @Override
    protected String bp() {
        return "";
    }

	@Override
	public EnumMonster getEnum() {
		return this.monsterType;
	}
}
