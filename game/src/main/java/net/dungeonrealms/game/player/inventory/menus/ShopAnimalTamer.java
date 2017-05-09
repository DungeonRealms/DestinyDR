package net.dungeonrealms.game.player.inventory.menus;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.achievements.Achievements;
import net.dungeonrealms.game.achievements.Achievements.EnumAchievements;
import net.dungeonrealms.game.item.items.core.ShopItem;
import net.dungeonrealms.game.item.items.core.ShopItem.ShopItemClick;
import net.dungeonrealms.game.item.items.functional.ecash.ItemMountSelector;
import net.dungeonrealms.game.item.items.functional.ecash.ItemMuleMount;
import net.dungeonrealms.game.mechanic.data.HorseTier;
import net.dungeonrealms.game.mechanic.data.MuleTier;
import net.dungeonrealms.game.player.inventory.ShopMenu;
import net.dungeonrealms.game.player.menu.CraftingMenu;
import net.dungeonrealms.game.world.entity.type.mounts.EnumMounts;
import net.dungeonrealms.game.world.entity.util.MountUtils;

public class ShopAnimalTamer extends ShopMenu {

	public ShopAnimalTamer(Player player) {
		super(player, "Animal Tamer", 1);
	}

	@Override
	protected void setItems() {
		ShopItemClick cb = (player, item) -> buyMount(player, item);
		for (HorseTier horse : HorseTier.values())
			if (horse != HorseTier.MULE)
				addItem(new ShopItem(new ItemMountSelector(horse), cb)).setPrice(horse.getPrice());
		
		setIndex(8);
		addItem(new ShopItem(new ItemMuleMount(MuleTier.OLD), cb)).setPrice(5000);
	}
	
	private boolean buyMount(Player player, ShopItem item) {
		PlayerWrapper pw = PlayerWrapper.getWrapper(player);
		EnumMounts mount = (item.getSoldItem() instanceof ItemMountSelector) ? ((ItemMountSelector) item.getSoldItem()).getTier().getMount() : EnumMounts.MULE;
        
		if (pw.getMountsUnlocked().contains(mount)) {
            player.sendMessage(ChatColor.RED + "You already own this mount!");
            return false;
        }
		
		if (!MountUtils.hasMountPrerequisites(mount, pw.getMountsUnlocked())) {
			player.sendMessage(ChatColor.RED + "You must own the previous mount to upgrade.");
			return false;
		}
		
		pw.getMountsUnlocked().add(mount);
        
		if (mount != EnumMounts.MULE) {
			pw.setActiveMount(mount);
            Achievements.giveAchievement(player, EnumAchievements.MOUNT_OWNER);
            CraftingMenu.addMountItem(player);
        } else {
            CraftingMenu.addMuleItem(player);
        }
        
        player.sendMessage(ChatColor.GREEN + "You have purchased the " + mount.getDisplayName() + ChatColor.GREEN + " mount.");
        Bukkit.getScheduler().runTask(DungeonRealms.getInstance(), player::closeInventory);
        
		return true;
	}
}
