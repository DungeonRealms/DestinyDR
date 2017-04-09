package net.dungeonrealms.game.item.items.functional.ecash;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.ItemUsage;
import net.dungeonrealms.game.item.event.ItemClickEvent;
import net.dungeonrealms.game.item.event.ItemConsumeEvent;
import net.dungeonrealms.game.item.event.ItemInventoryEvent;
import net.dungeonrealms.game.item.items.functional.FunctionalItem;
import net.dungeonrealms.game.player.chat.Chat;

public class ItemRetrainingBook extends FunctionalItem {

	public ItemRetrainingBook() {
		super(ItemType.RETRAINING_BOOK);
		setPermUntradeable(true);
	}
	
	@Override
	public void onClick(ItemClickEvent evt) {
		Player player = evt.getPlayer();
		player.sendMessage(ChatColor.GREEN + "Reset stat points? Type 'yes' or 'y' to confirm.");
		evt.setUsed(true);
		
		Chat.promptPlayerConfirmation(player, () -> {
			GameAPI.getGamePlayer(player).getStats().unallocateAllPoints();
			player.sendMessage(ChatColor.YELLOW + "All Stat Points have been unallocated!");
		}, () -> {
			GameAPI.giveOrDropItem(player, getItem());
			player.sendMessage(ChatColor.RED + "Action cancelled.");
		});
	}

	@Override
	protected String getDisplayName() {
		return ChatColor.GREEN + "Retraining Book";
	}

	@Override
	protected String[] getLore() {
		return new String[] {
				"Right click to reset your stat",
				"allocated points to free points.",
				ChatColor.DARK_GRAY + "One time use."
		};
	}

	@Override
	public void onConsume(ItemConsumeEvent evt) {}

	@Override
	public void onInventoryClick(ItemInventoryEvent evt) {}

	@Override
	protected ItemUsage[] getUsage() {
		return INTERACT;
	}

	@Override
	protected ItemStack getStack() {
		return new ItemStack(Material.ENCHANTED_BOOK);
	}

}
