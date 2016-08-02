package net.dungeonrealms.game.world.entity.type.monster.base;

import lombok.Getter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.anticheat.AntiCheat;
import net.dungeonrealms.game.miscellaneous.SkullTextures;
import net.dungeonrealms.game.world.entity.EnumEntityType;
import net.dungeonrealms.game.world.entity.type.monster.DRMonster;
import net.dungeonrealms.game.world.entity.type.monster.EnumMonster;
import net.dungeonrealms.game.world.item.Item;
import net.dungeonrealms.game.world.item.itemgenerator.ItemGenerator;
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
 * Created by Chase on Oct 4, 2015
 */
public abstract class DRBlaze extends EntityBlaze implements DRMonster {

	protected String name;
	protected String mobHead;
	protected EnumEntityType entityType;
	protected EnumMonster monsterType;
	@Getter
	protected Map<String, Integer[]> attributes = new HashMap<>();

	public DRBlaze(World world, EnumMonster monster, int tier, EnumEntityType entityType, boolean setArmor) {
		this(world);
		this.getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(20d);
		this.getAttributeInstance(GenericAttributes.c).setValue(1.00d);
		this.monsterType = monster;
		this.name = monster.name;
		this.mobHead = monster.mobHead;
		this.entityType = entityType;
		if (setArmor) {
            setArmor(tier);
        }
        setStats();
		this.getBukkitEntity().setCustomNameVisible(true);
        String customName = monster.getPrefix().trim() + " " + name.trim()  + " " + monster.getSuffix().trim()  + " ";
        this.setCustomName(customName);
        this.getBukkitEntity().setMetadata("customname", new FixedMetadataValue(DungeonRealms.getInstance(), customName));
		this.goalSelector.a(7, new PathfinderGoalRandomStroll(this, 1.0D));
		this.targetSelector.a(5, new PathfinderGoalNearestAttackableTarget(this, EntityHuman.class, true));
		this.noDamageTicks = 0;
		this.maxNoDamageTicks = 0;
	}

	protected DRBlaze(World world) {
		super(world);
	}

	protected abstract void setStats();

	protected String getCustomEntityName() {
		return this.name;
	}

	public void setArmor(int tier) {
		ItemStack[] armor = GameAPI.getTierArmor(tier);
		// weapon, boots, legs, chest, helmet/head
		ItemStack weapon = getTierWeapon(tier);
		LivingEntity livingEntity = (LivingEntity) this.getBukkitEntity();
		boolean armorMissing = false;
		int chance = 6 + tier;
		if (tier >= 3 || random.nextInt(10) <= chance) {
			ItemStack armor0 = AntiCheat.getInstance().applyAntiDupe(armor[0]);
			livingEntity.getEquipment().setBoots(armor0);
			this.setEquipment(EnumItemSlot.FEET, CraftItemStack.asNMSCopy(armor0));
		} else {
			armorMissing = true;
		}
		if (tier >= 3 || random.nextInt(10) <= chance || armorMissing) {
			ItemStack armor1 = AntiCheat.getInstance().applyAntiDupe(armor[1]);
			livingEntity.getEquipment().setLeggings(armor1);
			this.setEquipment(EnumItemSlot.LEGS, CraftItemStack.asNMSCopy(armor1));
			armorMissing = false;
		} else {
			armorMissing = true;
		}
		if (tier >= 3 || random.nextInt(10) <= chance || armorMissing) {
			ItemStack armor2 = AntiCheat.getInstance().applyAntiDupe(armor[2]);
			livingEntity.getEquipment().setChestplate(armor2);
			this.setEquipment(EnumItemSlot.CHEST, CraftItemStack.asNMSCopy(armor2));
		}
		this.setEquipment(EnumItemSlot.MAINHAND, CraftItemStack.asNMSCopy(weapon));
		livingEntity.getEquipment().setItemInMainHand(weapon);
		livingEntity.getEquipment().setHelmet(SkullTextures.DEVIL.getSkull());
	}

    private ItemStack getTierWeapon(int tier) {
        ItemStack item = new ItemGenerator().setTier(Item.ItemTier.getByTier(tier)).setType(Item.ItemType.STAFF)
                .setRarity(GameAPI.getItemRarity(false)).generateItem().getItem();
        AntiCheat.getInstance().applyAntiDupe(item);
        return item;
    }

    @Override
	public void onMonsterAttack(Player p) {
    	
    	
    }

	@Override
	public void collide(Entity e) {
	}
    
	@Override
	public abstract EnumMonster getEnum();

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
