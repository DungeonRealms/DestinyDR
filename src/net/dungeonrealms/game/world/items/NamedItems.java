package net.dungeonrealms.game.world.items;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.dungeonrealms.API;
import net.dungeonrealms.game.mechanics.generic.EnumPriority;
import net.dungeonrealms.game.mechanics.generic.GenericMechanic;
import net.dungeonrealms.game.world.items.Item.ItemModifier;
import net.dungeonrealms.game.world.items.Item.ItemTier;
import net.dungeonrealms.game.world.items.Item.ItemType;
import net.md_5.bungee.api.ChatColor;

/**
 * Created by Chase on Dec 4, 2015
 */
public class NamedItems implements GenericMechanic{

	public static ItemStack blayshanAxe;
	
	
	public static void loadNamedItems(){
		blayshanAxe = editItem(new ItemGenerator().getDefinedStack(ItemType.AXE, ItemTier.TIER_4, ItemModifier.UNIQUE), API.getTierColor(4) + "Blayshan's Wicked Axe", ChatColor.GRAY + "An Axe with the face of the cursed Blayshan carved into it.");
	}
	
    /**
	 * @param editItem
	 * @param i
	 * @param j
	 * @return
	 */
	private static ItemStack setItemStats(ItemStack item, int dmg, int stat) {
		if(dmg > 0){
			net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(item);
			nms.getTag().setInt("damage", dmg);
			item = CraftItemStack.asBukkitCopy(nms);
			ItemMeta meta =  item.getItemMeta();
			ArrayList<String> lore = (ArrayList<String>) meta.getLore();
			int i = 0;
			for(String s : lore){
				if(s.contains("DMG:")){
	                int damageRandomizer = ItemGenerator.getRandomDamageVariable(4);
	                String newDmg = ChatColor.WHITE.toString() + "DMG" + ": " + ChatColor.RED.toString() + Math.round((dmg - (dmg / damageRandomizer))) + ChatColor.WHITE + " - " + ChatColor.RED + Math.round((dmg + (dmg / damageRandomizer)));
					lore.set(i, newDmg);
					break;
				}
			}
		}
		return null;
	}

	public static ItemStack editItem(ItemStack itemStack, String name, String lore) {
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(name);
        ArrayList<String> itemLore = (ArrayList<String>) meta.getLore();
        if(lore != null){
        	itemLore.add(lore);
        	meta.setLore(itemLore);
        }
        itemStack.setItemMeta(meta);
        itemStack.setAmount(1);
        return itemStack;
    }

	@Override
	public EnumPriority startPriority() {
		return EnumPriority.PRIESTS;
	}

	@Override
	public void startInitialization() {
		loadNamedItems();
	}

	@Override
	public void stopInvocation() {
		
	}
}
