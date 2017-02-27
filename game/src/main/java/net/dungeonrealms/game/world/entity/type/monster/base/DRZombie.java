package net.dungeonrealms.game.world.entity.type.monster.base;

import lombok.Getter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.anticheat.AntiDuplication;
import net.dungeonrealms.game.world.entity.EnumEntityType;
import net.dungeonrealms.game.world.entity.type.monster.DRMonster;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.minecraft.server.v1_9_R2.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Xwaffle on 8/29/2015.
 */

public abstract class DRZombie extends EntityZombie implements DRMonster {

    protected String name;
    protected String mobHead;
    protected EnumEntityType entityType;
    protected EnumMonster monsterType;
    public int tier;
    @Getter
    protected Map<String, Integer[]> attributes = new HashMap<>();

    protected DRZombie(World world, EnumMonster monster, int tier, EnumEntityType entityType) {
        this(world);
        this.tier  = tier;
        this.getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(20d);
        this.getAttributeInstance(GenericAttributes.c).setValue(1.00d);
        this.monsterType = monster;
        
        this.name = monster.name;
        this.mobHead = monster.mobHead;
        this.entityType = entityType;
        setArmor(tier);
        setStats();
        String customName = monster.getPrefix().trim() + " " + name.trim() + " " + monster.getSuffix().trim();
        this.setCustomName(customName);
        this.getBukkitEntity().setMetadata("customname", new FixedMetadataValue(DungeonRealms.getInstance(), customName));
        LivingEntity livingEntity = (LivingEntity) this.getBukkitEntity();
        this.setEquipment(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(monster.getSkullItem(monster)));
        livingEntity.getEquipment().setHelmet(monster.getSkullItem(monster));
        this.noDamageTicks = 0;
        this.maxNoDamageTicks = 0;
    }

    protected DRZombie(World world) {
        super(world);
    }

    protected abstract void setStats();


    protected String getCustomEntityName() {
        return this.name;
    }

    public void setArmor(int tier) {
        ItemStack[] armor = GameAPI.getTierArmor(tier);
        // weapon, boots, legs, chest, helmet/head
        LivingEntity livingEntity = (LivingEntity) this.getBukkitEntity();
        boolean armorMissing = false;
        int chance = 6 + tier;
        if (tier >= 3 || random.nextInt(10) <= chance) {
            ItemStack armor0 = AntiDuplication.getInstance().applyAntiDupe(armor[0]);
            livingEntity.getEquipment().setBoots(armor0);
            this.setEquipment(EnumItemSlot.FEET, CraftItemStack.asNMSCopy(armor0));
        } else {
            armorMissing = true;
        }
        if (tier >= 3 || random.nextInt(10) <= chance || armorMissing) {
            ItemStack armor1 = AntiDuplication.getInstance().applyAntiDupe(armor[1]);
            livingEntity.getEquipment().setLeggings(armor1);
            this.setEquipment(EnumItemSlot.LEGS, CraftItemStack.asNMSCopy(armor1));
            armorMissing = false;
        } else {
            armorMissing = true;
        }
        if (tier >= 3 || random.nextInt(10) <= chance || armorMissing) {
            ItemStack armor2 = AntiDuplication.getInstance().applyAntiDupe(armor[2]);
            livingEntity.getEquipment().setChestplate(armor2);
            this.setEquipment(EnumItemSlot.CHEST, CraftItemStack.asNMSCopy(armor2));
        }
    }

    public void setWeapon(int tier) {
    }

    @Override
    public void collide(Entity e) {}

	@Override
	public void onMonsterAttack(Player p) {
	}
	
	@Override
	public EnumMonster getEnum(){
		return this.monsterType;
	}
	
	@Override
	public void onMonsterDeath(Player killer) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), ()->{
		this.checkItemDrop(this.getBukkitEntity().getMetadata("tier").get(0).asInt(), monsterType, this.getBukkitEntity(), killer);
		});
	}

    @Override
    public void enderTeleportTo(double d0, double d1, double d2) {
        //Test for EnderPearl TP Cancel.
    }
}
