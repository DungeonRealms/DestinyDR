package net.dungeonrealms.game.item.items.functional;

import java.util.Arrays;
import java.util.List;

import lombok.Getter;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.ItemUsage;
import net.dungeonrealms.game.item.event.ItemClickEvent;
import net.dungeonrealms.game.item.event.ItemInventoryEvent;
import net.dungeonrealms.game.player.chat.Chat;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ItemGemNote extends ItemMoney {

	@Getter
	private List<String> signers;
	
	public static final int MAX_SIZE = 100000;
	
	public ItemGemNote(String signer, int size) {
		super(ItemType.GEM_NOTE, size);
		addSigner(signer);
	}
	
	public ItemGemNote(ItemStack item) {
		super(item);
	}
	
	@Override
	public void loadItem() {
		this.signers = Arrays.asList(getTagString("signers").split(","));
		super.loadItem();
	}
	
	@Override
	public void updateItem() {
		if (getGemValue() > 50000)
			GameAPI.sendNetworkMessage("IGN_GMMessage", ChatColor.GOLD + "[WARNING] " + ChatColor.WHITE + getLastSigner() + " created a Bank Note worth " + ChatColor.GREEN + getGemValue() + ChatColor.WHITE + " on {SERVER}.");
		
		setTagString("signers", String.join(",", this.signers));
		super.updateItem();
	}
	
	/**
	 * Adds a signer to this gem note.
	 * Checks whether the name is already added first.
	 */
	public void addSigner(String s) {
		if (!this.signers.contains(s))
			this.signers.add(s);
	}
	
	/**
	 * Combines the signers from another gem note.
	 */
	public void combineSigners(ItemGemNote note) {
		note.getSigners().forEach(this::addSigner);
	}
	
	/**
	 * Get the last signer of this gem note.
	 */
	public String getLastSigner() {
		return this.signers.get(this.signers.size() - 1);
	}

	@Override
	public int getMaxStorage() {
		return MAX_SIZE;
	}

	@Override
	protected String getDisplayName() {
		return ChatColor.GREEN + "Gem Note";
	}

	@Override
	protected String[] getLore() {
		return new String[] { ChatColor.WHITE + "" + ChatColor.BOLD + "Value: " + ChatColor.WHITE + getGemValue() + " Gems",
				"Exchange at any bank for GEM(s)",
				signers.size() == 0 ? "Signed by " + ChatColor.WHITE + signers.get(0) : ""};
	}

	@Override
	public void onInventoryClick(ItemInventoryEvent evt) {
		if (!isGemNote(evt.getSwappedItem()))
			return;
		
		ItemGemNote combine = new ItemGemNote(evt.getSwappedItem());
		int combinedGems = combine.getGemValue() + getGemValue();
		
		if(combinedGems > getMaxStorage()) {
			evt.getPlayer().sendMessage(ChatColor.RED + "You cannot create a banknote of this value.");
			return;
		}
		
		evt.setCancelled(true);
		evt.setUsed(true);
		evt.getPlayer().sendMessage(ChatColor.GRAY + "You have combined bank notes " + ChatColor.ITALIC
				+ getGemValue() + "G + " + combine.getGemValue() + "G " + ChatColor.GRAY
				+ "with the value of " + ChatColor.BOLD + combinedGems + "G");
		
		//  COMBINE SIGNERS  //
		combine.combineSigners(this);
		combine.setGemValue(combinedGems);
		evt.setSwappedItem(combine.generateItem());
	}
	
	@Override
	public void onClick(ItemClickEvent evt) {
		Player player = evt.getPlayer();
		
		player.sendMessage(ChatColor.GRAY + "This bank note is worth " + ChatColor.GREEN + getGemValue() + " Gems." + ChatColor.GRAY + " Please enter the amount");
        player.sendMessage(ChatColor.GRAY + "you'd like to sign an additional bank note for. Alternatively,");
        player.sendMessage(ChatColor.GRAY + "type" + ChatColor.RED + " 'cancel' " + ChatColor.GRAY + "to stop this operation.");
        player.getInventory().setItemInMainHand(null);
        
        Chat.listenForNumber(player, 1, getGemValue(), num -> {
        	int newValue = getGemValue() - num;
        	
        	if (newValue > 0) {
        		ItemGemNote newNote = new ItemGemNote(player.getName(), newValue);
        		newNote.combineSigners(this);
        		GameAPI.giveOrDropItem(player, newNote.generateItem());
        	}
        	
        	setGemValue(num);
        	GameAPI.giveOrDropItem(player, generateItem());
        }, () -> {
        	GameAPI.giveOrDropItem(player, getItem());
        	player.sendMessage(ChatColor.RED + "Bank Note Split - " + ChatColor.BOLD + "CANCELLED");
        });
	}
	
	@Override
	protected ItemUsage[] getUsage() {
		return new ItemUsage[] {ItemUsage.LEFT_CLICK_BLOCK, ItemUsage.INVENTORY_SWAP_PLACE};
	}

	@Override
	protected ItemStack getStack() {
		return new ItemStack(Material.PAPER);
	}
	
	public static boolean isGemNote(ItemStack item) {
		return isType(item, ItemType.GEM_NOTE);
	}
}
