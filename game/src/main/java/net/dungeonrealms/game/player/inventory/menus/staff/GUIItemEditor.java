package net.dungeonrealms.game.player.inventory.menus.staff;

import lombok.Getter;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.dungeonrealms.game.item.items.core.ItemGear;
import net.dungeonrealms.game.item.items.core.ItemGeneric.ItemData;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.player.chat.Chat;
import net.dungeonrealms.game.player.inventory.menus.GUIItem;
import net.dungeonrealms.game.player.inventory.menus.GUIMenu;
import net.dungeonrealms.game.world.item.Item.*;
import net.dungeonrealms.game.world.item.itemgenerator.ItemGenerator;
import net.dungeonrealms.game.world.item.itemgenerator.engine.ModifierRange;

/**
 * Open a GUI that allows for editting item attributes.
 * @author Kneesnap
 */
@Getter
public class GUIItemEditor extends GUIMenu {
	
	private ItemGear gear;
	
	public GUIItemEditor(Player player, ItemGear gear) {
		super(player, fitSize(gear.getGeneratedItemType().getAttributeBank().getAttributes().length) + 27, "Item Editor");
		this.gear = gear;
		open(player, null);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void setItems() {
		// Add Attribute Items.
		for (AttributeType at : gear.getGeneratedItemType().getAttributeBank().getAttributes()) {
			ModifierRange value = gear.getAttributes().getAttribute(at);
			GUIItem gi = new GUIItem(Material.WOOL, (value.getValue() != 0 ? DyeColor.LIME : DyeColor.RED).getWoolData());
			gi.setName(Utils.capitalizeWords(at.getPrefix().split(":")[0]))
					.setLore(
					"Value: " + ChatColor.YELLOW + value.toString(),
					"",
					"Left-Click: Set Value",
					"Right-Click: Remove Value");
			
			gi.setClick(e -> {
				if (e.isLeftClick()) {
					player.sendMessage(ChatColor.YELLOW + "Please enter the " + (at.isRange() ? "minimum value for this range" : "new value") + ".");
					Chat.listenForNumber(player, 0, 10000, min -> {
						if (at.isRange()) {
							player.sendMessage(ChatColor.YELLOW + "Please enter the maximum value for this range.");
							Chat.listenForNumber(player, min, 10000, max -> {
								gear.getAttributes().setStatRange(at, min, max);
								player.sendMessage(ChatColor.GREEN + "Attribute set.");
								open();
							}, this::open);
							return;
						}
						
						gear.getAttributes().setStat(at, min);
						player.sendMessage(ChatColor.GREEN + "Attribute set.");
						open();
					}, this::open);
				} else if (e.isRightClick()) {
					gear.getAttributes().remove(at);
					player.sendMessage(ChatColor.RED + "Attribute reset.");
					reconstructGUI(player);
				}
			});
			
			addItem(gi);
		}
		nextRow();
		
		skipSlot(5);
		ItemStack g = getGear().generateItem();
		addItem(new GUIItem(g).setClick((evt) -> {
			if (evt.isRightClick()) {
				getPlayer().sendMessage(ChatColor.GREEN + "Please enter the name you'd like to save this item as.");
				Chat.listenForMessage(getPlayer(), e -> {
					ItemGenerator.saveItem(g, e.getMessage());
					player.sendMessage(ChatColor.GREEN + "Saved");
					reconstructGUI(getPlayer());
				}, this::open);
				return;
			}
			getPlayer().getInventory().addItem(getGear().generateItem());
			reconstructGUI(getPlayer());
		}));
		
		nextRow();
		skipSlot(3);
		
		// Set Tier.
		addItem(new GUIItem(GeneratedItemType.SWORD.getTier(gear.getTier()))
				.setName(ChatColor.AQUA + "Set Tier")
				.setLore("Current Tier: " + gear.getTier().getColor() + gear.getTier().getId())
				.setClick(e -> {
					player.sendMessage(ChatColor.YELLOW + "Please enter the tier of this item.");
					Chat.listenForNumber(player, 1, ItemTier.values().length, num -> {
						gear.setTier(num);
						player.sendMessage(ChatColor.GREEN + "Tier updated.");
						open();
					}, this::open);
				}));
		
		// Rarity
		if (gear.getRarity() != null) {
			addItem(new GUIItem(gear.getRarity().getMaterial())
				.setName(ChatColor.AQUA + "Set Rarity")
				.setLore("Current Rarity: " + gear.getRarity().getName())
				.setClick(e -> {
					player.sendMessage(ChatColor.YELLOW + "Please enter the rarity of this item.");
					Chat.listenForMessage(player, evt -> {
						ItemRarity ir = ItemRarity.getByName(evt.getMessage());
						if (ir == null) {
							player.sendMessage(ChatColor.RED + "Unknown tier.");
						} else {
							gear.setRarity(ir);
							player.sendMessage(ChatColor.GREEN + "Rarity updated.");
						}
						open();
					}, this::open);
			}));
		} else {
			skipSlot(1);
		}
		
		skipSlot(1);
		
		// Set Name
		addItem(new GUIItem(Material.NAME_TAG).setName(ChatColor.AQUA + "Set Name").setLore("Click here to change display name.")
				.setClick(e -> {
					player.sendMessage(ChatColor.GREEN + "Enter the new name of this item.");
					Chat.listenForMessage(player, evt -> {
						gear.setCustomName(evt.getMessage());
						player.sendMessage(ChatColor.GREEN + "Named changed.");
						open();
					}, this::open);
				}));
		
		// Set Lore
		addItem(new GUIItem(Material.MAP).setName(ChatColor.AQUA + "Set Lore").setLore("Click here to change item lore.").setClick(e -> {
			player.sendMessage(ChatColor.GREEN + "Enter the new lore for this item.");
			Chat.listenForMessage(player, evt -> {
				gear.setCustomLore(evt.getMessage());
				player.sendMessage(ChatColor.GREEN + "Lore updated.");
				open();
			}, this::open);
		}));
		
		nextRow();
		skipSlot(3);
		for (ItemData data : gear.getDataMap().keySet()) {
			if (!data.isShowInGUI())
				continue;
			
			addItem(new GUIItem(gear.getSData(data) ? Material.EMERALD_BLOCK : Material.LAPIS_BLOCK)
				.setName(ChatColor.BLUE + "Set " + data.getDisplay())
				.setLore("Click here to toggle " + data.getDisplay() + " status.")
				.setClick(e -> {
					gear.setData(data, !gear.getSData(data));
					player.sendMessage(ChatColor.GREEN + "Toggled " + data.getDisplay());
					reconstructGUI(player);
				}));
		}
	}
}
