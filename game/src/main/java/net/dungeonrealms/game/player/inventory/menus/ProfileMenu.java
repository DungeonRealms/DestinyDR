package net.dungeonrealms.game.player.inventory.menus;

import org.bukkit.entity.Player;

import net.dungeonrealms.game.player.inventory.ShopMenu;

public abstract class ProfileMenu extends ShopMenu {

	public ProfileMenu(Player player, String title, int rows) {
		super(player, title, rows);
	}
	
	/*@Override
	public NPCMenu getLastMenu() {
		return NPCMenu.PROFILE;
	}*/

}
