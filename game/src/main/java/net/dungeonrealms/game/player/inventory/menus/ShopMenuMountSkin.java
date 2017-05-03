package net.dungeonrealms.game.player.inventory.menus;

import java.util.List;
import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.*;
import net.dungeonrealms.game.item.items.functional.ecash.ItemMountSkin;
import net.dungeonrealms.game.player.inventory.ShopMenu;
import net.dungeonrealms.game.world.entity.type.mounts.EnumMountSkins;

public class ShopMenuMountSkin extends ShopMenu {

	public ShopMenuMountSkin(Player player) {
		super(player, "E-Cash Skins", 1);
	}

	@Override
	protected void setItems() {
		addItem(BACK);
		
		for (EnumMountSkins s : EnumMountSkins.values()) {
			addItem(new ItemMountSkin(s)).setECash(1250).setOnClick((player, shop) -> {
                List<String> playerSkins = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.MOUNT_SKINS, player.getUniqueId());
                
                if (!playerSkins.isEmpty() && playerSkins.contains(s.name())) {
                	player.sendMessage(ChatColor.RED + "You already own the " + ChatColor.BOLD + ChatColor.UNDERLINE + s.getDisplayName() + ChatColor.RED + " mount skin.");
                	return false;
                }
                
                DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$PUSH, EnumData.MOUNT_SKINS, s.name(), true);
                DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.ACTIVE_MOUNT_SKIN, s.name(), true);
                player.sendMessage(ChatColor.GREEN + "You have purchased the " + s.getDisplayName() + " mount skin.");
                player.closeInventory();
                return true;
			});
		}
	}
}
