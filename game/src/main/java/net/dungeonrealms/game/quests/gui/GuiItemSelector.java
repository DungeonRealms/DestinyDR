package net.dungeonrealms.game.quests.gui;

import java.util.List;

import net.dungeonrealms.game.quests.QuestItem;
import net.dungeonrealms.game.quests.QuestStage;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GuiItemSelector extends GuiBase {

	private QuestStage stage;
	private List<QuestItem> items;
	
	public GuiItemSelector(Player player, QuestStage stage, List<QuestItem> items) {
		super(player, "Select Item", 1);
		this.stage = stage;
		this.items = items;
	}
	
	@Override
	protected void createGUI(){
		for(int i = 0; i < 8; i++){
			if(this.items.size() > i){
				//Editing existing item.
				QuestItem questItem = this.items.get(i);
				this.setSlot(i, questItem.createItem(player), (evt) -> {
					if(evt.isRightClick()){
						this.items.remove(evt.getRawSlot());
						player.sendMessage(ChatColor.RED + "Item Deleted.");
						new GuiItemSelector(player, stage, items);
						return;
					}
					new GuiItemEditor(player, stage, items, questItem);
				});
			}else{
				//Creating new item
				this.setSlot(i, Material.BARRIER, ChatColor.GREEN + "Create Item", new String[] {"Click here to create a new Quest Item"}, (evt) -> {
					QuestItem qi = new QuestItem(new ItemStack(Material.STONE));
					this.items.add(qi);
					new GuiItemEditor(player, stage, items, qi);
				});
			}
		}
		this.setSlot(8, GO_BACK, (evt) -> new GuiStageEditor(player, stage));
	}
}
