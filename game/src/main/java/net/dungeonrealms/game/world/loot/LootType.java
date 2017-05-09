package net.dungeonrealms.game.world.loot;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import net.dungeonrealms.game.item.items.core.ItemGeneric;
import net.dungeonrealms.game.item.items.core.VanillaItem;
import net.dungeonrealms.game.item.items.functional.ItemEnchantArmor;
import net.dungeonrealms.game.item.items.functional.ItemEnchantWeapon;
import net.dungeonrealms.game.item.items.functional.ItemMoney;
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

	@Getter
	private static Map<String, LootGenerator> generators = new HashMap<>();
	
	static {
		//  REGISTER LOOT GENERATORS  //
		add("EnchantArmor", (amt, meta) -> new ItemEnchantArmor(meta));
		add("EnchantWeapon", (amt, meta) -> new ItemEnchantWeapon(meta));
		add("Gems", (amt, meta) -> ItemMoney.createMoney(amt, "Loot Chest"));
		add("Potion", (amt, meta) -> new PotionItem(meta).setSplash(amt > 0));
		add("Orb", (amt, meta) -> new ItemOrb());
		add("RealmChest", (amt, meta) -> new ItemRealmChest());
		add("Teleport", (amt, meta) -> new ItemTeleportBook());
		add("Vanilla", (amt, meta) -> new VanillaItem(new ItemStack(Material.getMaterial(meta), amt > 0 ? amt : 1)));
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
		public ItemGeneric getLoot(int amount, int meta);
	}
	
	public static String getName(LootGenerator g) {
		for (String name : generators.keySet())
			if (generators.get(name) == g)
				return name;
		return "Unknown Generator";
	}
}
