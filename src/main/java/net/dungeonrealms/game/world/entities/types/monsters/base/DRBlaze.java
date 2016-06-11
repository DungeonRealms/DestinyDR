package net.dungeonrealms.game.world.entities.types.monsters.base;

import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.miscellaneous.SkullTextures;
import net.dungeonrealms.game.world.anticheat.AntiCheat;
import net.dungeonrealms.game.world.entities.EnumEntityType;
import net.dungeonrealms.game.world.entities.types.monsters.DRMonster;
import net.dungeonrealms.game.world.entities.types.monsters.EnumMonster;
import net.dungeonrealms.game.world.items.Item.ItemTier;
import net.dungeonrealms.game.world.items.Item.ItemType;
import net.dungeonrealms.game.world.items.itemgenerator.ItemGenerator;
import net.minecraft.server.v1_8_R3.EntityBlaze;
import net.minecraft.server.v1_8_R3.GenericAttributes;
import net.minecraft.server.v1_8_R3.PathfinderGoalRandomStroll;
import net.minecraft.server.v1_8_R3.World;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Random;

/**
 * Created by Chase on Oct 4, 2015
 */
public abstract class DRBlaze extends EntityBlaze implements DRMonster {

	protected String name;
	protected String mobHead;
	protected EnumEntityType entityType;
	protected EnumMonster monsterType;

	public DRBlaze(World world, EnumMonster monster, int tier, EnumEntityType entityType, boolean setArmor) {
		this(world);
        this.getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(16d);
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.29D);
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
	}

	protected DRBlaze(World world) {
		super(world);
	}

	protected abstract void setStats();

	protected String getCustomEntityName() {
		return this.name;
	}
	
    private void setArmor(int tier) {
        ItemStack[] armor = API.getTierArmor(tier);
		boolean armorMissing = false;
		if (random.nextInt(10) <= 5) {
			ItemStack armor0 = AntiCheat.getInstance().applyAntiDupe(armor[0]);
			this.setEquipment(1, CraftItemStack.asNMSCopy(armor0));
		} else {
			armorMissing = true;
		}
		if (random.nextInt(10) <= 5 || armorMissing) {
			ItemStack armor1 = AntiCheat.getInstance().applyAntiDupe(armor[1]);
			this.setEquipment(2, CraftItemStack.asNMSCopy(armor1));
			armorMissing = false;
		} else {
			armorMissing = true;
		}
		if (random.nextInt(10) <= 5 || armorMissing) {
			ItemStack armor2 = AntiCheat.getInstance().applyAntiDupe(armor[2]);
			this.setEquipment(3, CraftItemStack.asNMSCopy(armor2));
		}

        // weapon, boots, legs, chest, helmet/head
        ItemStack weapon = getTierWeapon(tier);
        this.setEquipment(0, CraftItemStack.asNMSCopy(weapon));
        this.setEquipment(4, CraftItemStack.asNMSCopy(SkullTextures.DEVIL.getSkull()));
    }

    private ItemStack getTierWeapon(int tier) {
        ItemStack item = new ItemGenerator().setTier(ItemTier.getByTier(tier)).setType(ItemType.STAFF)
                .setRarity(API.getItemRarity(false)).generateItem().getItem();
        AntiCheat.getInstance().applyAntiDupe(item);
        return item;
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
	public void onMonsterAttack(Player p) {
    	
    	
    }
    
	@Override
	public abstract EnumMonster getEnum();

	@Override
	public void onMonsterDeath(Player killer) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), ()->{
			this.checkItemDrop(this.getBukkitEntity().getMetadata("tier").get(0).asInt(), monsterType, this.getBukkitEntity(), killer);
			if (new Random().nextInt(99) < 3) {
				this.getRareDrop();
			}
		});
	}
}
