package net.dungeonrealms.game.player.inventory.menus;

import org.bukkit.entity.Player;

import net.dungeonrealms.game.item.items.functional.ItemHealingFood;
import net.dungeonrealms.game.item.items.functional.ItemHealingFood.EnumHealingFood;
import net.dungeonrealms.game.player.inventory.ShopMenu;

public class ShopFoodVendor extends ShopMenu {

	public ShopFoodVendor(Player player) {
		super(player, "Food Vendor", 18);
	}

	protected void setItems() {
		for(EnumHealingFood food : EnumHealingFood.values())
        	addItem(new ItemHealingFood(food)).setPrice(food.getPrice());
	}
}
