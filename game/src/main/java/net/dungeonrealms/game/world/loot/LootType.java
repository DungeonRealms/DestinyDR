package net.dungeonrealms.game.world.loot;

import java.util.HashMap;
import java.util.Map;

import net.dungeonrealms.game.item.items.functional.ItemEnchantArmor;
import net.dungeonrealms.game.item.items.functional.ItemEnchantWeapon;
import net.dungeonrealms.game.item.items.functional.ItemGem;
import net.dungeonrealms.game.item.items.functional.ItemGemNote;
import net.dungeonrealms.game.item.items.functional.ItemOrb;
import net.dungeonrealms.game.item.items.functional.ItemRealmChest;
import net.dungeonrealms.game.item.items.functional.ItemTeleportBook;
import net.dungeonrealms.game.item.items.functional.PotionItem;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * LootType - Contains the loot chest item generators.
 * 
 * Created April 29th, 2017.
 * @author Kneesnap
 */
@SuppressWarnings("deprecation")
public class LootType {

	private static Map<String, LootGenerator> generators = new HashMap<>();
	
	static {
		//  REGISTER LOOT GENERATORS  //
		add("EnchantArmor", (amt, meta) -> new ItemEnchantArmor(meta).generateItem());
		add("EnchantWeapon", (amt, meta) -> new ItemEnchantWeapon(meta).generateItem());
		add("Gems", (amt, meta) -> amt > 64 ? new ItemGemNote("Loot Chest", amt).generateItem() : new ItemGem(amt).generateItem());
		add("Potion", (amt, meta) -> new PotionItem(meta).setSplash(amt > 0).generateItem());
		add("Orb", (amt, meta) -> new ItemOrb().generateItem());
		add("RealmChest", (amt, meta) -> new ItemRealmChest().generateItem());
		add("Teleport", (amt, meta) -> new ItemTeleportBook().generateItem());
		add("Vanilla", (amt, meta) -> new ItemStack(Material.getMaterial(meta), amt));
	}
	
	private static void add(String type, LootGenerator g) {
		generators.put(type, g);
	}
	
	public static boolean isValid(String name) {
		return getGenerator(name) != null;
	}
	
	public static LootGenerator getGenerator(String name) {
		for (String key : generators.keySet())
			if (key.equalsIgnoreCase(name))
				return generators.get(key);
		return null;
	}
	
	public interface LootGenerator {
		public ItemStack getLoot(int amount, int meta);
	}
}
