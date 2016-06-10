package net.dungeonrealms.game.world.entities.types.monsters;

import net.dungeonrealms.API;
import net.dungeonrealms.game.miscellaneous.SkullTextures;
import net.dungeonrealms.game.world.anticheat.AntiCheat;
import net.dungeonrealms.game.world.entities.EnumEntityType;
import net.dungeonrealms.game.world.entities.types.monsters.base.DRSkeleton;
import net.dungeonrealms.game.world.items.DamageAPI;
import net.dungeonrealms.game.world.items.Item;
import net.dungeonrealms.game.world.items.itemgenerator.ItemGenerator;
import net.minecraft.server.v1_8_R3.EntityLiving;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.World;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Chase on Oct 17, 2015
 */
public class BasicEntitySkeleton extends DRSkeleton {
    private int tier;

    /**
     * @param world
     * @param tier
     */
    public BasicEntitySkeleton(World world, int tier, EnumMonster type) {
        super(world, EnumMonster.Skeleton, tier, EnumEntityType.HOSTILE_MOB);
        this.tier = tier;
        ItemStack weapon = new ItemGenerator().setType(Item.ItemType.BOW).setRarity(API.getItemRarity(false)).setTier(Item.ItemTier.getByTier(tier)).generateItem().getItem();
        AntiCheat.getInstance().applyAntiDupe(weapon);
        this.setEquipment(0, CraftItemStack.asNMSCopy(weapon));
        if (type == EnumMonster.FrozenSkeleton) {
            this.setEquipment(4, CraftItemStack.asNMSCopy(SkullTextures.FROZEN_SKELETON.getSkull()));
        } else {
            this.setEquipment(4, CraftItemStack.asNMSCopy(SkullTextures.SKELETON.getSkull()));
        }
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
