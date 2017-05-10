package net.dungeonrealms.game.player.inventory.menus;

import net.dungeonrealms.game.listener.NPCMenu;
import org.bukkit.entity.Player;

import net.dungeonrealms.game.player.inventory.ShopMenu;

public abstract class ProfileMenu extends ShopMenu {

	public ProfileMenu(Player player, String title, int rows) {
		super(player, title, rows);
	}

	@Override
	protected NPCMenu getLastMenu() {
		return super.getLastMenu();
	}

	/*@Override
	public NPCMenu getLastMenu() {
		return NPCMenu.PROFILE;
	}*/

}
