package net.dungeonrealms.loot;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.items.Item.ItemModifier;
import net.dungeonrealms.items.Item.ItemTier;
import net.dungeonrealms.items.Item.ItemType;
import net.dungeonrealms.items.ItemGenerator;
import net.dungeonrealms.items.armor.Armor.ArmorModifier;
import net.dungeonrealms.items.armor.Armor.ArmorTier;
import net.dungeonrealms.items.armor.Armor.EquipmentType;
import net.dungeonrealms.items.armor.ArmorGenerator;
import net.dungeonrealms.loot.types.LootType;
import net.dungeonrealms.mastery.Utils;
import net.dungeonrealms.mechanics.generic.EnumPriority;
import net.dungeonrealms.mechanics.generic.GenericMechanic;

/**
 * Created by Chase on Oct 9, 2015
 */
public class LootManager implements GenericMechanic {

	public static List<LootSpawner> LOOT_SPAWNERS = new ArrayList<>();
	public static List<String> SPAWNER_CONFIG = new ArrayList<>();
	/**
	 * Weapons/Armor 1% Glyphs .2%
	 * 
	 * 
	 * 
	 * Health Potion %75 Food 75% TP Books 10%
	 * 
	 */

	/**
	 * 
	 */
	public static void loadLootSpawners() {
		loadLootItems();
		SPAWNER_CONFIG = DungeonRealms.getInstance().getConfig().getStringList("loot");
		for (String line : SPAWNER_CONFIG) {
			String[] cords = line.split("=")[0].split(",");
			int x,y,z;
			x = Integer.parseInt(cords[0]);
			y = Integer.parseInt(cords[1]);
			z = Integer.parseInt(cords[2]);
			World world = Bukkit.getWorlds().get(0);
			Location loc = new Location(world, x, y, z);
			Block chest = world.getBlockAt(loc);
			chest.setType(Material.CHEST);
			String lootType = line.split("=")[1];
			lootType = lootType.substring(1, lootType.length());
			lootType = lootType.split("@")[0];
			 if(LootType.getLootType(lootType) == null){
				 Utils.log.info(lootType + " NULL");
			 	 continue;
			 }
			long spawn_delay = Math.round(Double.parseDouble(line.substring(line.lastIndexOf("@") + 1, line.indexOf("#"))));
			Utils.log.info("Loot placed at " + loc);
			LootSpawner spawner = new LootSpawner(chest, spawn_delay, LootType.getLootType(lootType));
			LOOT_SPAWNERS.add(spawner);
		}
	}

	/**
	 * @return
	 */
	static double getDelayMultiplier() {
		int player_count = Bukkit.getOnlinePlayers().size() - 1;
		if (player_count <= 10) {
			return 1.25D;
		}
		if (player_count <= 30) {
			return 0.75D;
		}
		if (player_count <= 50) {
			return 0.50D;
		}
		if (player_count <= 70) {
			return 0.40D;
		}
		if (player_count <= 100) {
			return 0.39D;
		}
		if (player_count <= 150) {
			return 0.20D;
		}
		return 0.20D; // player_count is greater than 100.
	}

	/**
	 * Creates ItemStack Array from templates in the loot folder.
	 */
	private static void loadLootItems() {
		LootType.initLoot();
	}

	public static boolean checkLocationForLootSpawner(Location location) {
		for (LootSpawner lootSpawner : LOOT_SPAWNERS) {
			if (lootSpawner.location.distanceSquared(location) <= 2) {
				return true;
			}
		}
		return false;
	}
	
	public static ItemStack generateRandomTierItem(int tier) {
		ItemStack i = null;
		
	    int armor_range_check = new Random().nextInt(100);
	    ItemModifier rarity = null;
	    ArmorModifier armorRarity = null;
        if (armor_range_check <= 80) {
            rarity = ItemModifier.COMMON;
            armorRarity = ArmorModifier.COMMON;
        } else if (armor_range_check < 95) {
            rarity = ItemModifier.UNCOMMON;
            armorRarity = ArmorModifier.UNCOMMON;
        } else if (armor_range_check < 98) {
            rarity = ItemModifier.RARE;
            armorRarity = ArmorModifier.RARE;
        } else if (armor_range_check == 99) {
            rarity = ItemModifier.UNIQUE;
            armorRarity = ArmorModifier.UNIQUE;
        }
        if (rarity == null) {
            rarity = ItemModifier.COMMON;
            armorRarity = ArmorModifier.COMMON;
        }
        
		int r = new Random().nextInt(9);
		if (r == 0) {
			return new ItemGenerator().next(ItemType.SWORD, ItemTier.getByTier(tier), rarity);
		} else if (r == 1) {
			return new ItemGenerator().next(ItemType.AXE, ItemTier.getByTier(tier), rarity);
		} else if (r == 2) {
			return new ItemGenerator().next(ItemType.BOW, ItemTier.getByTier(tier), rarity);
		} else if (r == 3) {
			return new ArmorGenerator().getArmor(EquipmentType.HELMET, ArmorTier.getByTier(tier), armorRarity);
		} else if (r == 4) {
			return new ArmorGenerator().getArmor(EquipmentType.BOOTS, ArmorTier.getByTier(tier), armorRarity);
		} else if (r == 5) {
			return new ArmorGenerator().getArmor(EquipmentType.LEGGINGS, ArmorTier.getByTier(tier), armorRarity);
		} else if (r == 6) {
			return new ArmorGenerator().getArmor(EquipmentType.CHESTPLATE, ArmorTier.getByTier(tier), armorRarity);
		} else if (r == 7) {
			return new ItemGenerator().next(ItemType.POLE_ARM, ItemTier.getByTier(tier), rarity);
		} else if (r == 8) {
			return new ItemGenerator().next(ItemType.STAFF, ItemTier.getByTier(tier), rarity);
		}
		return i;
	}

	@Override
	public EnumPriority startPriority() {
		return EnumPriority.ARCHBISHOPS;
	}

	@Override
	public void startInitialization() {
		Utils.log.info("LOADING ALL LOOT CHESTS...");
		loadLootSpawners();
	}

	@Override
	public void stopInvocation() {

	}

	/**
	 * @param location
	 * @return
	 */
	public static LootSpawner getSpawner(Location location) {
        for (LootSpawner loot : LootManager.LOOT_SPAWNERS) {
            if (loot.location.getX() == location.getX() && loot.location.getY() == location.getY() && loot.location.getZ() == location.getZ()) {
            	return loot;
            }
        }
        return null;
	}
}
