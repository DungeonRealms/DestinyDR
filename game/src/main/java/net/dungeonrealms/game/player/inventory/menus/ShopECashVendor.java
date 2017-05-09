package net.dungeonrealms.game.player.inventory.menus;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.item.items.core.VanillaItem;
import net.dungeonrealms.game.listener.NPCMenu;
import net.dungeonrealms.game.miscellaneous.ItemBuilder;
import net.dungeonrealms.game.player.inventory.ShopMenu;
import net.dungeonrealms.game.player.json.JSONMessage;

public class ShopECashVendor extends ShopMenu {

	public ShopECashVendor(Player player) {
		super(player, "E-Cash Vendor", 18);
	}
	
	private void addEcashItem(int slot, Material mat, String type, NPCMenu menu) {
		ItemStack stack = new ItemStack(mat);
		ItemMeta meta = stack.getItemMeta();
		meta.setDisplayName(ChatColor.GOLD + type);
		meta.setLore(Arrays.asList(ChatColor.GRAY + "View the available E-Cash " + type + ".", ChatColor.GRAY + "Display Item"));
		stack.setItemMeta(meta);
		setIndex(slot);
		addItem(new VanillaItem(stack), (player, item) -> {menu.open(player); return false;});
	}

	@Override
	protected void setItems() {
		
		addEcashItem(1, Material.MONSTER_EGG, "Pets", NPCMenu.PET_VENDOR);
		addEcashItem(3, Material.GLOWSTONE_DUST, "Effects", NPCMenu.EFFECT_VENDOR);
		addEcashItem(5, Material.SKULL_ITEM, "Skins", NPCMenu.SKIN_VENDOR);
		addEcashItem(7, Material.INK_SACK, "Miscellaneous Items", NPCMenu.ECASH_MISC);
		
		ItemStack stack = new ItemBuilder().setItem(new ItemStack(Material.EMERALD), ChatColor.GREEN + "Our Store", new String[]{
            ChatColor.AQUA + "Click here to visit our store!",
            ChatColor.GRAY + "Display Item"}).build();
		setIndex(9);
		addItem(new VanillaItem(stack), (player, item) -> giveLink(player));
		
		getInventory().setItem(17, new ItemBuilder().setItem(new ItemStack(Material.GOLDEN_APPLE), ChatColor.GREEN + "Current E-Cash", new String[]{
            ChatColor.AQUA + "Your E-Cash Balance is: " + ChatColor.YELLOW.toString() + ChatColor.BOLD + PlayerWrapper.getWrapper(getPlayer()).getEcash(),
            ChatColor.GRAY + "Display Item"}).build());
	}
	
	private boolean giveLink(Player player) {
		Bukkit.getScheduler().runTask(DungeonRealms.getInstance(), player::closeInventory);
		final JSONMessage normal4 = new JSONMessage(ChatColor.GOLD + "To Purchase E-Cash from our Shop, Click ", ChatColor.GOLD);
		normal4.addURL(ChatColor.AQUA.toString() + ChatColor.BOLD + ChatColor.UNDERLINE + "HERE", ChatColor.AQUA, "http://www.dungeonrealms.net/store");
		normal4.sendToPlayer(player);
		return false;
	}
}
