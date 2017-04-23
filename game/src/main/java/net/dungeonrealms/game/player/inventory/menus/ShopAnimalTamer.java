package net.dungeonrealms.game.player.inventory.menus;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.data.EnumOperators;
import net.dungeonrealms.game.achievements.Achievements;
import net.dungeonrealms.game.item.items.core.ShopItem;
import net.dungeonrealms.game.item.items.core.ShopItem.ShopItemClick;
import net.dungeonrealms.game.item.items.functional.ecash.ItemMountSelector;
import net.dungeonrealms.game.item.items.functional.ecash.ItemMuleMount;
import net.dungeonrealms.game.mechanic.data.HorseTier;
import net.dungeonrealms.game.player.inventory.ShopMenu;
import net.dungeonrealms.game.player.menu.CraftingMenu;
import net.dungeonrealms.game.world.entity.type.mounts.EnumMounts;
import net.dungeonrealms.game.world.entity.type.mounts.mule.MuleTier;
import net.dungeonrealms.game.world.entity.util.MountUtils;

public class ShopAnimalTamer extends ShopMenu {

	public ShopAnimalTamer(Player player) {
		super(player, "Animal Tamer", 1);
	}

	@Override
	protected void setItems() {
		ShopItemClick cb = (player, item) -> buyMount(player, item);
		for (HorseTier horse : HorseTier.values())
			addItem(new ShopItem(new ItemMountSelector(horse), cb)).setPrice(horse.getPrice());
		
		setIndex(8);
		addItem(new ShopItem(new ItemMuleMount(MuleTier.OLD), cb)).setPrice(5000);
	}
	
	@SuppressWarnings("unchecked")
	private boolean buyMount(Player player, ShopItem item) {
		List<String> playerMounts = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.MOUNTS, player.getUniqueId());
		EnumMounts mount = (item.getSoldItem() instanceof ItemMountSelector) ? ((ItemMountSelector) item.getSoldItem()).getTier().getMount() : EnumMounts.MULE;
		
		String rawName = mount.name();
        
		if (playerMounts.contains(rawName)) {
            player.sendMessage(ChatColor.RED + "You already own this mount!");
            return false;
        }
		
		if (!MountUtils.hasMountPrerequisites(mount, playerMounts)) {
			player.sendMessage(ChatColor.RED + "You must own the previous mount to upgrade.");
			return false;
		}
		
		DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$PUSH, EnumData.MOUNTS, mount.name(), true);
        
		if (mount != EnumMounts.MULE) {
            DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.ACTIVE_MOUNT, mount.name(), true);
            Achievements.getInstance().giveAchievement(player.getUniqueId(), Achievements.EnumAchievements.MOUNT_OWNER);
            CraftingMenu.addMountItem(player);
        } else {
            CraftingMenu.addMuleItem(player);
        }
        
        player.sendMessage(ChatColor.GREEN + "You have purchased the " + mount.getDisplayName() + ChatColor.GREEN + " mount.");
        Bukkit.getScheduler().runTask(DungeonRealms.getInstance(), player::closeInventory);
        
		return true;
	}
}
