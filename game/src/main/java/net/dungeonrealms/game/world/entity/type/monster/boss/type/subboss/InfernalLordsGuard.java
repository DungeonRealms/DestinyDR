package net.dungeonrealms.game.world.entity.type.monster.boss.type.subboss;

import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.items.core.ItemArmor;
import net.dungeonrealms.game.item.items.core.ItemWeapon;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.dungeonrealms.game.world.entity.type.monster.type.melee.MeleeWitherSkeleton;
import net.dungeonrealms.game.world.item.Item.ItemRarity;
import net.dungeonrealms.game.world.item.Item.ItemTier;
import net.minecraft.server.v1_9_R2.*;

import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Chase on Oct 21, 2015
 */
public class InfernalLordsGuard extends MeleeWitherSkeleton {

    public boolean died = false;

    public InfernalLordsGuard(World world, int tier) {
        super(world, 4, EnumMonster.LordsGuard);
        
        this.setOnFire(Integer.MAX_VALUE);
        this.getBukkitEntity().setCustomName(ChatColor.RED.toString() + ChatColor.BOLD + ChatColor.UNDERLINE.toString() + "The Infernal Lords Guard");
        getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(65);
    }

    
    @Override
    public ItemStack getWeapon() {
    	return makeItem(new ItemWeapon(ItemType.POLEARM, ItemType.SWORD).setRarity(ItemRarity.COMMON));
    }


    public void setGear() {
    	super.setGear();
    	ItemArmor am = new ItemArmor();
        am.setTier(ItemTier.getByTier(4)).setRarity(ItemRarity.COMMON);
        ((LivingEntity)getBukkitEntity()).getEquipment().setArmorContents(am.generateArmorSet());
    }


    @Override
    protected void r() {
        this.goalSelector.a(1, new PathfinderGoalFloat(this));
        this.goalSelector.a(5, new PathfinderGoalRandomStroll(this, 1.2D, 20));
        this.goalSelector.a(6, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
        this.goalSelector.a(6, new PathfinderGoalRandomLookaround(this));
        this.targetSelector.a(1, new PathfinderGoalHurtByTarget(this, false, new Class[0]));
        this.targetSelector.a(2, new PathfinderGoalNearestAttackableTarget(this, EntityHuman.class, true));
    }

    @Override
    public void onMonsterDeath(Player killer) {
        for (Player p : this.getBukkitEntity().getWorld().getPlayers())
            p.sendMessage(ChatColor.RED.toString() + "The Infernal Lords Guard" + ChatColor.RESET.toString() + ": " + "I have failed you...");
        super.onMonsterDeath(null);
    }
}
