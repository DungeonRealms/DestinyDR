package net.dungeonrealms.game.listener;

import net.dungeonrealms.game.player.inventory.menus.guis.PetSelectionGUI;
import net.dungeonrealms.game.player.inventory.menus.guis.SalesManagerGUI;
import org.bukkit.entity.Player;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.player.inventory.ShopMenu;
import net.dungeonrealms.game.player.inventory.menus.*;
import net.md_5.bungee.api.ChatColor;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum NPCMenu {

	ANIMAL_TAMER("Animal Tamer", ShopAnimalTamer.class, false),
	DUNGEONEER("Dungeoneer", ShopDungeoneer.class, false),
	SKILL_TRAINER("Skill Trainer", ShopProfessionVendor.class, false),
	FOOD_VENDOR("Food Vendor", ShopFoodVendor.class, false),
	ITEM_VENDOR("Item Vendor", ShopItemVendor.class, false),
	INNKEEPER("Innkeeper", ShopHearthstoneLocation.class, false),
	ECASH_VENDOR("E-Cash Vendor", ShopECashVendor.class, false),
	PET_VENDOR("Pet Vendor", PetSelectionGUI.class, false),
//	ECASH_MISC("E-Cash Miscellaneous", ShopMenuMisc.class, false),
	SKIN_VENDOR("Skin Vendor", ShopMenuMountSkin.class, false),
	EFFECT_VENDOR("Effect Vendor", ShopMenuParticleEffect.class, false),
	SALES_MANAGER("Sales Manager", SalesManagerGUI.class, false);
	
	@Getter private String npcName;
	private Class<? extends ShopMenu> shopCls;
	@Getter private boolean allowedOnEvent;

	public ShopMenu open(Player player) {
		if (DungeonRealms.isEvent() && !isAllowedOnEvent()) {
			player.sendMessage(ChatColor.RED + "You cannot use this interface on this shard.");
			return null;
		}
		try {
			return (ShopMenu) shopCls.getDeclaredConstructor(Player.class).newInstance(player);
		} catch (Exception e) {
			e.printStackTrace();
			Utils.log.info("Failed to construct shop menu for " + name() + "...");
		}
		return null;
	}
	
	public static NPCMenu getMenu(String name) {
		for (NPCMenu menu : values())
			if (menu.getNpcName().equals(name))
				return menu;
		return null;
	}
}
