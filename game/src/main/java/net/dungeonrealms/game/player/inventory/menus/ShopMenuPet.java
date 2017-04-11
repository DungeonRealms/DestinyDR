package net.dungeonrealms.game.player.inventory.menus;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.data.EnumOperators;
import net.dungeonrealms.game.item.items.core.ShopItem;
import net.dungeonrealms.game.item.items.core.ShopItem.ShopItemClick;
import net.dungeonrealms.game.item.items.functional.ecash.ItemPetSelector;
import net.dungeonrealms.game.player.inventory.ShopMenu;
import net.dungeonrealms.game.player.menu.CraftingMenu;
import net.dungeonrealms.game.world.entity.type.pet.EnumPets;

public class ShopMenuPet extends ShopMenu {

	public ShopMenuPet(Player player) {
		super(player, "E-Cash Pets", 2);
	}

	@Override
	protected void setItems() {
		addItem(BACK);
		
		ShopItemClick buyPet = (player, item) -> {
			List<String> playerPets = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.PETS, player.getUniqueId());
			ItemPetSelector petSelector = (ItemPetSelector) item.getSoldItem();
            EnumPets newPet = petSelector.getPet();
			
            if (!playerPets.isEmpty()) {
                for (String pet : playerPets) {
                	if (newPet.getName().equalsIgnoreCase(pet)) {
                        player.sendMessage(ChatColor.RED + "You already own the " + ChatColor.BOLD + ChatColor.UNDERLINE + newPet.getDisplayName() + ChatColor.RED + " pet.");
                        return false;
                    }
                }
            }

            DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$PUSH, EnumData.PETS, newPet.getName(), true);
            DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.ACTIVE_PET, newPet.getName(), true);
            player.sendMessage(ChatColor.GREEN + "You have purchased the " + newPet.getDisplayName() + " pet.");
            CraftingMenu.addPetItem(player);
            
			return true;
		};
		
        for (EnumPets pet : EnumPets.values()) 
            if (pet.isSpecial())
            	addItem(new ShopItem(new ItemPetSelector(pet), buyPet)).setECash(459);
	}
}
