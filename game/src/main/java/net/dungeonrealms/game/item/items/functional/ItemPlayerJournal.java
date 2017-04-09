package net.dungeonrealms.game.item.items.functional;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.ItemUsage;
import net.dungeonrealms.game.item.event.ItemClickEvent;
import net.dungeonrealms.game.item.event.ItemConsumeEvent;
import net.dungeonrealms.game.item.event.ItemInventoryEvent;
import net.dungeonrealms.game.mechanic.ItemManager;
import net.dungeonrealms.game.quests.Quests;
import net.dungeonrealms.game.quests.objectives.ObjectiveOpenJournal;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemPlayerJournal extends FunctionalItem {
	
	public ItemPlayerJournal() {
		super(ItemType.PLAYER_JOURNAL);
		setUndroppable(true);
	}
	
	public ItemPlayerJournal(ItemStack item) {
		super(item);
	}

	@Override
	public void onClick(ItemClickEvent evt) {
		//Open a real character journal.
		//Not saving a full one in a player's inventory will save CPU power and storage space.
		GameAPI.openBook(evt.getPlayer(), ItemManager.createCharacterJournal(evt.getPlayer()));
		Quests.getInstance().triggerObjective(evt.getPlayer(), ObjectiveOpenJournal.class);
	}

	@Override
	public void onConsume(ItemConsumeEvent evt) {}

	@Override
	public void onInventoryClick(ItemInventoryEvent evt) {}

	@Override
	protected String getDisplayName() {
		return ChatColor.GREEN + "" + ChatColor.BOLD + "Character Journal";
	}

	@Override
	protected String[] getLore() {
		return null;
	}

	@Override
	protected ItemUsage[] getUsage() {
		return INTERACT_RIGHT_CLICK;
	}

	@Override
	protected ItemStack getStack() {
		return new ItemStack(Material.WRITTEN_BOOK);
	}
}
