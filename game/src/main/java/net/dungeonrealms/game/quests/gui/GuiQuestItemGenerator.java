package net.dungeonrealms.game.quests.gui;

import java.util.List;
import java.util.function.Consumer;

import net.dungeonrealms.game.mechanic.ItemManager;
import net.dungeonrealms.game.player.chat.Chat;
import net.dungeonrealms.game.quests.QuestItem;
import net.dungeonrealms.game.quests.QuestItem.ItemOption;
import net.dungeonrealms.game.quests.QuestStage;
import net.dungeonrealms.game.world.item.Item.ItemRarity;
import net.dungeonrealms.game.world.item.Item.ItemTier;
import net.dungeonrealms.game.world.item.Item.ItemType;
import net.dungeonrealms.game.world.item.itemgenerator.ItemGenerator;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.ChatColor;

public class GuiQuestItemGenerator extends GuiBase {
	
	private List<QuestItem> items;
	private QuestItem item;
	private QuestStage stage;
	
	public GuiQuestItemGenerator(Player player, QuestStage stage, List<QuestItem> items, QuestItem qi) {
		super(player, "Choose Item.", 3);
		this.stage = stage;
		this.items = items;
		this.item = qi;
	}
	
	@Override
	public void createGUI(){
		ItemGenerator armor = new ItemGenerator();
		armor.setRarity(item.getRarity());
		armor.setTier(item.getTier());
		armor.setType(ItemType.CHESTPLATE);
		
		this.setSlot(0, armor.generateItem().getItem().getType(), ChatColor.GREEN + "Armor", new String[] {"Click here to mark this item as armor."}, setType(ItemOption.ARMOR));
		
		ItemGenerator weapon = new ItemGenerator();
		weapon.setRarity(item.getRarity());
		weapon.setTier(item.getTier());
		weapon.setType(ItemType.SWORD);
		
		this.setSlot(1, weapon.generateItem().getItem().getType(), ChatColor.GREEN + "Weapon", new String[] {"Click here to mark this item as a weapon"}, setType(ItemOption.WEAPON));
		
		this.setSlot(2, ItemManager.createPickaxe(item.getTier().getTierId()).getType(), ChatColor.GREEN + "Pickaxe", new String[] {"Click here to mark this item as a pickaxe."}, setType(ItemOption.PICKAXE));
		
		this.setSlot(3, Material.FISHING_ROD, ChatColor.GREEN + "Fishing Rod", new String[] {"Click here to mark this item as a fishing rod."}, setType(ItemOption.FISHING_ROD));
		
		this.setSlot(4, Material.MAGMA_CREAM, ChatColor.GREEN + "Orb of Alteration", new String[] {"Click here to mark this item as an orb of alteration."}, setType(ItemOption.ORB));
		
		this.setSlot(5, Material.MAP, ChatColor.GREEN + "Weapon Enchant", new String[] {"Click here to mark this item as a weapon enchant"}, setType(ItemOption.ARMOR_ENCH_SCROLL));
		
		this.setSlot(6, Material.MAP, ChatColor.GREEN + "Armor Enchant", new String[] {"Click here to mark this item as an armor enchant."}, setType(ItemOption.WEAPON_ENCH_SCROLL));
		
		this.setSlot(7, Material.MAP, ChatColor.GREEN + "Protection Scroll", new String[] {"Click here to mark this item as a protection scroll."}, setType(ItemOption.PROT_SCROLL));
		
		this.setSlot(8, Material.EMERALD, ChatColor.GREEN + "Gem Note", new String[] {"Click here to mark this item as a gem note."}, setType(ItemOption.GEM_NOTE));
		
		for(ItemTier tier : ItemTier.values()){
			this.setSlot(9 + tier.getTierId() - 1, tier.getMaterial(), tier.getTierColor() + "Tier " + tier.getTierId(), new String[] {"Click here to set the item as tier " + tier.getTierId() + "."}, evt -> {
				player.sendMessage(ChatColor.GREEN + "Tier Updated.");
				item.setItemTier(tier);
				new GuiQuestItemGenerator(player, stage, items, item);
			});
		}
		
		for(ItemRarity rarity : ItemRarity.values()){
			this.setSlot(18 + rarity.ordinal(), rarity.getMaterial(), rarity.getColor() + rarity.getName(), new String[] {"Click here to change the item rarity."}, evt -> {
				player.sendMessage(ChatColor.GREEN + "Rarity Updated.");
				item.setItemRarity(rarity);
				new GuiQuestItemGenerator(player, stage, items, item);
			});
		}
		
		this.setSlot(this.getSize() - 1, GO_BACK, (evt) -> new GuiItemEditor(player, stage, items, item));
	}
	
	private Consumer<InventoryClickEvent> setType(QuestItem.ItemOption opt){
		return (evt) -> {
			evt.getWhoClicked().sendMessage(ChatColor.GREEN + "Item type set to " + opt.name());
			if(opt == ItemOption.GEM_NOTE){
				evt.getWhoClicked().sendMessage(ChatColor.GREEN + "Please enter the amount of gems.");
				Chat.listenForNumber(player, 1, 64, (num) -> {
					item.setAmount(num);
					evt.getWhoClicked().sendMessage(ChatColor.GREEN + "Gem amount set.");
					new GuiQuestItemGenerator(player, stage, items, item);
				}, () -> new GuiQuestItemGenerator(player, stage, items, item));
			}
			this.item.setGenerationType(opt);
		};
	}
}
