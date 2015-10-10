package net.dungeonrealms.mechanics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.banks.BankMechanics;
import net.dungeonrealms.items.Item.ItemTier;
import net.dungeonrealms.items.ItemGenerator;
import net.dungeonrealms.spawning.LootSpawner;

/**
 * Created by Chase on Oct 9, 2015
 */
public class LootManager {

	public static ArrayList<LootSpawner> spawners = new ArrayList<>();
	public static ArrayList<String> spawnerConfig = new ArrayList<>();
	public static ArrayList<ItemStack> tier1Loot = new ArrayList<>();
	public static ArrayList<ItemStack> tier2Loot = new ArrayList<>();
	public static ArrayList<ItemStack> tier3Loot = new ArrayList<>();
	public static ArrayList<ItemStack> tier4Loot = new ArrayList<>();
	public static ArrayList<ItemStack> tier5Loot = new ArrayList<>();

	/**
	 * Manually load of all items to an ArrayList.
	 */
	private static void loadLootItems() {
		ItemStack money = BankMechanics.gem.clone();
		ItemStack weapon = null;

		// Tier 1 Loot
		for (int i = 0; i < 10; i++) {
			weapon = new ItemGenerator().next(ItemTier.TIER_1);
			tier1Loot.add(weapon);
		}
		money.setAmount(10);
		tier1Loot.add(money);

		// Tier 2 Loot
		for (int i = 0; i < 10; i++) {
			weapon = new ItemGenerator().next(ItemTier.TIER_2);
			tier2Loot.add(weapon);
		}
		money.setAmount(20);
		tier2Loot.add(money);

		// Tier 3 Loot
		for (int i = 0; i < 10; i++) {
			weapon = new ItemGenerator().next(ItemTier.TIER_3);
			tier3Loot.add(weapon);
		}
		money.setAmount(30);
		tier3Loot.add(money);

		// Tier 4 Loot
		for (int i = 0; i < 10; i++) {
			weapon = new ItemGenerator().next(ItemTier.TIER_4);
			tier4Loot.add(weapon);
		}
		money.setAmount(40);
		tier4Loot.add(money);

		// Tier 5 Loot
		for (int i = 0; i < 10; i++) {
			weapon = new ItemGenerator().next(ItemTier.TIER_5);
			tier5Loot.add(weapon);
		}
		money.setAmount(50);
		tier5Loot.add(money);
	}

	/**
	 * returns an array of Items that are dedicated to the tier.
	 * 
	 * @param tier
	 * @return
	 */
	public static ItemStack[] getLoot(int tier) {
		int num = new Random().nextInt(3);
		if (num == 0)
			num += 1;
		ItemStack[] items = new ItemStack[num];
		switch (tier) {
		case 1:
			for (int i = 0; i < num; i++)
				items[i] = tier1Loot.get(new Random().nextInt(tier1Loot.size()));
			break;
		case 2:
			for (int i = 0; i < num; i++)
				items[i] = tier2Loot.get(new Random().nextInt(tier2Loot.size()));
			break;
		case 3:
			for (int i = 0; i < num; i++)
				items[i] = tier3Loot.get(new Random().nextInt(tier3Loot.size()));
			break;
		case 4:
			for (int i = 0; i < num; i++)
				items[i] = tier4Loot.get(new Random().nextInt(tier4Loot.size()));
			break;
		case 5:
			for (int i = 0; i < num; i++)
				items[i] = tier5Loot.get(new Random().nextInt(tier5Loot.size()));
			break;
		}
		return items;
	}

	/**
	 * Initialization of loot spawners from config.
	 */
	public static void loadLootSpawners() {
		loadLootItems();
		spawnerConfig = (ArrayList<String>) DungeonRealms.getInstance().getConfig().getStringList("loot");
		for (String line : spawnerConfig) {
			int tier = Integer.parseInt(line.split(":")[1]);
			double x, y, z = 0.0;
			String[] location = line.split(":")[0].split(",");
			x = Double.parseDouble(location[0]);
			y = Double.parseDouble(location[1]);
			z = Double.parseDouble(location[2]);
			World world = Bukkit.getWorlds().get(0);
			Location loc = new Location(world, x, y, z);
			Block chest = world.getBlockAt(loc);
			chest.setType(Material.CHEST);
			LootSpawner lootSpawner = new LootSpawner(loc, tier, chest);
			spawners.add(lootSpawner);
		}

	}
}
