package net.dungeonrealms.entities.types.monsters.base;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.banks.BankMechanics;
import net.dungeonrealms.entities.EnumEntityType;
import net.dungeonrealms.entities.types.monsters.EnumMonster;
import net.dungeonrealms.entities.types.monsters.Monster;
import net.dungeonrealms.items.ItemGenerator;
import net.dungeonrealms.items.armor.ArmorGenerator;
import net.minecraft.server.v1_8_R3.Item;
import net.minecraft.server.v1_8_R3.World;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.FixedMetadataValue;

import java.lang.reflect.Field;
import java.util.Random;

/**
 * Created by Chase on Oct 4, 2015
 */
public abstract class DRBlaze extends net.minecraft.server.v1_8_R3.EntityBlaze implements Monster {

	protected String name;
	protected String mobHead;
	protected EnumEntityType entityType;
	protected EnumMonster monsterType;
	public DRBlaze(World world, EnumMonster monster, int tier, EnumEntityType entityType, boolean setArmor) {
		this(world);
		monsterType = monster;
		this.name = monster.name;
		this.mobHead = monster.mobHead;
		this.entityType = entityType;
		if (setArmor)
			setArmor(tier);
		this.getBukkitEntity().setCustomNameVisible(true);
        String customName = monster.getPrefix() + " " + name + " " + monster.getSuffix() + " ";
        this.setCustomName(customName);
        this.getBukkitEntity().setMetadata("customname", new FixedMetadataValue(DungeonRealms.getInstance(), customName));
		setStats();
	}
	
	@Override
	protected abstract void getRareDrop();

	protected DRBlaze(World world) {
		super(world);
	}

	protected abstract void setStats();

	public static Object getPrivateField(String fieldName, Class clazz, Object object) {
		Field field;
		Object o = null;
		try {
			field = clazz.getDeclaredField(fieldName);
			field.setAccessible(true);
			o = field.get(object);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return o;
	}
	protected String getCustomEntityName() {
		return this.name;
	}
	protected net.minecraft.server.v1_8_R3.ItemStack getHead() {
		ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
		SkullMeta meta = (SkullMeta) head.getItemMeta();
		meta.setOwner(mobHead);
		head.setItemMeta(meta);
		return CraftItemStack.asNMSCopy(head);
	}
	/**
	 * set monster armor tier.
	 * @param tier
	 */
	private void setArmor(int tier) {
		ItemStack[] armor = getTierArmor(tier);
		// weapon, boots, legs, chest, helmet/head
		ItemStack weapon = getTierWeapon(tier);
		this.setEquipment(0, CraftItemStack.asNMSCopy(weapon));
		this.setEquipment(1, CraftItemStack.asNMSCopy(armor[0]));
		this.setEquipment(2, CraftItemStack.asNMSCopy(armor[1]));
		this.setEquipment(3, CraftItemStack.asNMSCopy(armor[2]));
		this.setEquipment(4, this.getHead());
	}

	private ItemStack getTierWeapon(int tier) {
		return new ItemGenerator().next(net.dungeonrealms.items.Item.ItemType.STAFF, net.dungeonrealms.items.Item.ItemTier.getByTier(tier));
	}


	/**
	 * get monster tier Armor as ItemsStack array
	 * 
	 * @param tier
	 * @return
	 */
	private ItemStack[] getTierArmor(int tier) {
		return new ArmorGenerator().nextTier(tier);
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
		return "mob.ghast.scream";
	}
	
    
    @Override
	public void onMonsterAttack(Player p) {
    	
    	
    }
    
	public abstract EnumMonster getEnum();

	@Override
	public void onMonsterDeath() {
		Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), ()->{
			this.checkItemDrop(this.getBukkitEntity().getMetadata("tier").get(0).asInt(), monsterType, this.getBukkitEntity().getLocation());
			if (new Random().nextInt(99) < 3) {
				this.getRareDrop();
			}
		});
	}
}
