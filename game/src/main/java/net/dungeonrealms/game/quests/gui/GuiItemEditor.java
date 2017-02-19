package net.dungeonrealms.game.quests.gui;

import java.util.List;

import net.dungeonrealms.game.player.chat.Chat;
import net.dungeonrealms.game.quests.QuestItem;
import net.dungeonrealms.game.quests.QuestStage;
import net.dungeonrealms.game.world.item.itemgenerator.ItemGenerator;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GuiItemEditor extends GuiBase {
	
	private QuestItem questItem;
	private List<QuestItem> allItems;
	private QuestStage stage;

	public GuiItemEditor(Player player, QuestStage stage, List<QuestItem> items, QuestItem qi) {
		super(player, "Item Editor", 1);
		this.questItem = qi;
		this.allItems = items;
		this.stage = stage;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void createGUI(){
		ItemStack item = this.questItem.createItem(player);
			
		this.setSlot(0, item, (evt) -> evt.getWhoClicked().setItemOnCursor(evt.getCurrentItem().clone()));
			
		if(!this.questItem.isDRItem()){
			this.setSlot(3, Material.NAME_TAG, ChatColor.GOLD + "Set Display Name", new String[] {"Click here to set the display name."}, (evt) -> {
				player.sendMessage(ChatColor.YELLOW + "Please enter the display name of this item.");
				Chat.listenForMessage(player, (event) -> {
					this.questItem.setDisplayName(ChatColor.translateAlternateColorCodes('&', ChatColor.AQUA + event.getMessage()));
					player.sendMessage(ChatColor.GREEN + "Item Display Name set as " + this.questItem.getDisplayName());
					new GuiItemEditor(player, stage, allItems, questItem);
				}, p -> new GuiItemEditor(player, stage, allItems, questItem));
			});
		
			this.setSlot(4, item.getType(), ChatColor.GREEN + "Set Item Type", new String[] {"Click here to set the item type."}, (evt) -> {
				player.sendMessage(ChatColor.YELLOW + "Please enter the item material.");
				player.sendMessage(ChatColor.YELLOW + "Type \"held\" to select your held item.");
				
				Chat.listenForMessage(player, (event) -> {
					try{
						Material mat = null;
						if(event.getMessage().equalsIgnoreCase("held")){
							ItemStack i = player.getInventory().getItemInMainHand();
							if(i != null){
								mat = i.getType();
							}else{
								player.sendMessage(ChatColor.RED + "You are not holding an item.");
							}
						}else{
							mat = Material.valueOf(event.getMessage().toUpperCase());
						}
						if(mat != null){
							this.questItem.setType(mat);
							player.sendMessage(ChatColor.GREEN + "Material Type set to " + this.questItem.getType().name());
						}
					}catch(Exception e){
						player.sendMessage(ChatColor.RED + "Material not found.");
					}
					new GuiItemEditor(player, stage, allItems, questItem);
				}, p -> new GuiItemEditor(player, stage, allItems, questItem));
			});
		
			this.setSlot(5, Material.WOOL, item.getAmount(), ChatColor.GREEN + "Set Item Amount", new String[] {"Click here to set item quantity"}, (evt) -> {
				player.sendMessage(ChatColor.YELLOW + "Please enter the itemstack size.");
				Chat.listenForNumber(player, 1, 64, (num) -> {
					this.questItem.setAmount(num);
					player.sendMessage(ChatColor.GREEN + "Item Amount set to " + this.questItem.getAmount());
					new GuiItemEditor(player, stage, allItems, questItem);
				}, (p) -> new GuiItemEditor(player, stage, allItems, questItem));
			});
			
			this.setSlot(6, Material.GOLD_BLOCK, ChatColor.YELLOW + "Load DR Item", new String[] {"Click here to load a custom DungeonRealms Item.", "An example of this would be: duranorhelm"}, (evt) -> {
				player.sendMessage(ChatColor.YELLOW + "Please enter the custom item name you'd like to load.");
				Chat.listenForMessage(player, (event) -> {
					ItemStack load = ItemGenerator.getNamedItem(event.getMessage());
					if(load == null){
						player.sendMessage(ChatColor.RED + "Item Not Found.");
						new GuiItemEditor(player, stage, allItems, questItem);
						return;
					}
					player.sendMessage(ChatColor.GREEN + "Custom Item Loaded.");
					this.questItem.setDRItemName(event.getMessage());
					new GuiItemEditor(player, stage, allItems, questItem);
				}, p -> new GuiItemEditor(player, stage, allItems, questItem));
			});
			
			this.setSlot(7, Material.BUCKET, ChatColor.AQUA + "Load Item", new String [] {"Place an item on its slot to load it."}, (evt) -> {
				if(evt.getCursor() != null){
					this.questItem.loadItem(evt.getCursor());
					evt.getWhoClicked().setItemOnCursor(null);
					evt.getWhoClicked().sendMessage(ChatColor.GREEN + "Item Loaded");
					new GuiItemEditor(player, stage, this.allItems, this.questItem);
				}
			});
		}else{
			this.setSlot(4, Material.BARRIER, ChatColor.RED + "Reset Item", new String[] {"Click here to reset this item back to default."}, (evt) -> {
				this.questItem.setDRItemName(null);
				player.sendMessage(ChatColor.GREEN + "Item Reset.");
				new GuiItemEditor(player, stage, allItems, questItem);
				return;
			});
		}
		
		this.setSlot(2, Material.WOOL, 1, this.questItem.isSoulbound() ? (short)DyeColor.GREEN.getWoolData() : (short)DyeColor.RED.getWoolData(), ChatColor.GOLD + "", new String[] {"Click to toggle Soulbound Status", "Current Status: "  + (this.questItem.isSoulbound() ? ChatColor.GREEN + "" : ChatColor.RED + "Not ") + "Soulbound"}, (evt) -> {
			this.questItem.setSoulbound(!this.questItem.isSoulbound());
			new GuiItemEditor(player, stage, allItems, questItem);
		});
		
		this.setSlot(8, GO_BACK, (evt) -> new GuiItemSelector(player, stage, allItems));
	}
}
