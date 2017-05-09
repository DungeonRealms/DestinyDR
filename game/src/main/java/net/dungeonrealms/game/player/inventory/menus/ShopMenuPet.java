package net.dungeonrealms.game.player.inventory.menus;

import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.item.items.core.ShopItem;
import net.dungeonrealms.game.item.items.core.ShopItem.ShopItemClick;
import net.dungeonrealms.game.item.items.functional.ecash.ItemPetSelector;
import net.dungeonrealms.game.player.inventory.ShopMenu;
import net.dungeonrealms.game.player.menu.CraftingMenu;
import net.dungeonrealms.game.world.entity.type.pet.EnumPets;
import net.dungeonrealms.game.world.entity.type.pet.PetData;

public class ShopMenuPet extends ShopMenu {

	public ShopMenuPet(Player player) {
		super(player, "E-Cash Pets", 2);
	}

	@Override
	protected void setItems() {
		addItem(BACK);
		
		ShopItemClick buyPet = (player, item) -> {
			PlayerWrapper wrapper = PlayerWrapper.getWrapper(player);
			ItemPetSelector selector = (ItemPetSelector) item.getSoldItem();
			Set<EnumPets> pets = wrapper.getPetsUnlocked().keySet();
			EnumPets buy = selector.getPet();
			
			if (pets.contains(buy)) {
				player.sendMessage(ChatColor.RED + "You already own this pet.");
				return false;
			}
			
			wrapper.getPetsUnlocked().put(buy, new PetData(null, true));
			player.sendMessage(ChatColor.GREEN + "You have purchased the " + buy.getDisplayName() + " pet.");
            CraftingMenu.addPetItem(player);
			
			return true;
		};
		
        for (EnumPets pet : EnumPets.values()) 
            if (pet.isSpecial())
            	addItem(new ShopItem(new ItemPetSelector(pet), buyPet)).setECash(449);
	}
}
