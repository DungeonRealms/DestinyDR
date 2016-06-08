package net.dungeonrealms.game.world.entities.types.monsters;

import net.dungeonrealms.API;
import net.dungeonrealms.game.miscellaneous.SkullTextures;
import net.dungeonrealms.game.world.anticheat.AntiCheat;
import net.dungeonrealms.game.world.entities.EnumEntityType;
import net.dungeonrealms.game.world.entities.types.monsters.base.DRSkeleton;
import net.dungeonrealms.game.world.items.DamageAPI;
import net.dungeonrealms.game.world.items.Item.ItemTier;
import net.dungeonrealms.game.world.items.Item.ItemType;
import net.dungeonrealms.game.world.items.itemgenerator.ItemGenerator;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

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
        this.tier = tier;
        this.setEquipment(4, CraftItemStack.asNMSCopy(SkullTextures.DEVIL.getSkull()));
        ItemStack weapon =  new ItemGenerator().setType(ItemType.STAFF).setRarity(API.getItemRarity()).setTier(ItemTier.getByTier(tier)).generateItem().getItem();
        AntiCheat.getInstance().applyAntiDupe(weapon);
        this.setEquipment(0, CraftItemStack.asNMSCopy(weapon));
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

}
