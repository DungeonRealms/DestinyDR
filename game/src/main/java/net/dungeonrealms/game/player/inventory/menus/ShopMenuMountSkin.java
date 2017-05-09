package net.dungeonrealms.game.player.inventory.menus;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import net.dungeonrealms.database.PlayerWrapper;
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
				PlayerWrapper wrapper = PlayerWrapper.getWrapper(player);
				List<EnumMountSkins> skins = wrapper.getMountSkins();
                
                if (skins.contains(s)) {
                	player.sendMessage(ChatColor.RED + "You already own the " + ChatColor.BOLD + ChatColor.UNDERLINE + s.getDisplayName() + ChatColor.RED + " mount skin.");
                	return false;
                }
                
                wrapper.getMountSkins().add(s);
                wrapper.setActiveMountSkin(s);
                player.sendMessage(ChatColor.GREEN + "You have purchased the " + s.getDisplayName() + " mount skin.");
                player.closeInventory();
                return true;
			});
		}
	}
}
