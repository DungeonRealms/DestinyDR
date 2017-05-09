package net.dungeonrealms.game.world.loot;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.bukkit.inventory.ItemStack;

import lombok.AllArgsConstructor;
import net.dungeonrealms.game.item.items.core.ItemGeneric;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.world.loot.LootType.LootGenerator;

/**
 * Represents possible loot from a loot chest.
 * 
 * Created May 8th, 2017.
 * @author Kneesnap
 */
public class LootTable {
	
	private List<PossibleLoot> loot = new ArrayList<>();
	
	public void addLoot(PossibleLoot pl) {
		this.loot.add(pl);
	}
	
	/**
	 * Generate a list of items from the loot data.
	 */
	public List<ItemStack> generateLoot() {
		List<ItemStack> items = new ArrayList<>();
		
		if (loot.isEmpty()) // Don't deadlock.
			return items;
		
		for (PossibleLoot pl : loot) {
			ItemGeneric ig = pl.generate();
			if (ig == null) // Chance roll failed :/
				continue;
			
			ig.removeEpoch();
			items.add(ig.generateItem());
		}
		
		return items.isEmpty() ? generateLoot() : items;
	}
	
	public List<String> getFriendlyList() {
		return loot.stream().map(l -> l.toString()).collect(Collectors.toList());
	}
	
	@AllArgsConstructor
	public static class PossibleLoot {
		
		private LootGenerator generator;
		private double chance;
		private int minAmt;
		private int maxAmt;
		private int meta;
		
		/**
		 * Attempts to generate loot.
		 * Returns null if the chance was not met.
		 */
		public ItemGeneric generate() {
			return this.chance >= new Random().nextInt(1000) ? generator.getLoot(Utils.randInt(minAmt, maxAmt), meta) : null;
		}
		
		@Override
		public String toString() {
			return (chance / 10) + "% " + LootType.getName(generator) + (meta != 0 ? " (" + meta + ")" : "");
		}
	}
}
