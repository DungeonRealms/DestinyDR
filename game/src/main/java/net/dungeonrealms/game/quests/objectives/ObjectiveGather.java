package net.dungeonrealms.game.quests.objectives;

import java.util.ArrayList;
import java.util.List;

import net.dungeonrealms.game.quests.QuestItem;
import net.dungeonrealms.game.quests.QuestNPC;
import net.dungeonrealms.game.quests.QuestStage;
import net.dungeonrealms.game.quests.gui.GuiBase;
import net.dungeonrealms.game.quests.gui.GuiItemSelector;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class ObjectiveGather implements QuestObjective {
	
	private List<QuestItem> itemsToGather = new ArrayList<QuestItem>();
	
	@Override
	public boolean isCompleted(Player player, QuestStage stage, QuestNPC currentNPC) {
		
		//  CHECKS IF ALL ITEMS ARE PRESENT  //
		for(QuestItem qi : itemsToGather)
			if(!this.doesPlayerHave(player, qi, false))
				return false;
		
		//  REMOVES THE ITEMS  //
		for(QuestItem qi : itemsToGather)
			this.doesPlayerHave(player, qi, true);
		player.playSound(player.getLocation(), Sound.BLOCK_CLOTH_BREAK, 1, 1.333F);
		return true;
	}
	
	private boolean doesPlayerHave(Player player, QuestItem qi, boolean remove){
		for(int i = 0; i < player.getInventory().getSize(); i++){
			ItemStack item = player.getInventory().getItem(i);
			if(qi.doesItemMatch(item)){
				if(remove){
					ItemStack newItem = item.clone();
					newItem.setAmount(newItem.getAmount() - qi.createItem(player).getAmount());
					if(newItem.getAmount() <= 0)
						newItem = null;
					player.getInventory().setItem(i, newItem);
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public String getName() {
		return "Fetch";
	}
	
	public List<QuestItem> getItems(){
		return this.itemsToGather;
	}

	@Override
	public String getTaskDescription(Player player, QuestStage stage) {
		String ret = "";
		for(QuestItem qi : this.itemsToGather){
			if(player == null || !this.doesPlayerHave(player, qi, false)){
				ItemStack item = qi.createItem(player);
				ret += " and " + item.getAmount() + " " + ChatColor.stripColor(item.getItemMeta().getDisplayName());
			}
		}
		if(player == null || ret.equals(""))
			return "Deliver items to " + stage.getNPC().getName();
		return "Deliver " +  ret.substring(" and ".length()) + " to " + stage.getNPC().getName();//TODO: Don't show the name of the NPC in question if talking to that NPC, since this is supposed to be in first person.
	}

	@Override
	public JsonObject saveJSON() {
		JsonObject obj = new JsonObject();
		JsonArray items = new JsonArray();
		for(QuestItem qi : this.itemsToGather)
			items.add(qi.toJSON());
		obj.add("items", items);
		return obj;
	}

	@Override
	public void loadJSON(JsonObject o) {
		JsonArray items = o.get("items").getAsJsonArray();
		for(int i = 0; i < items.size(); i++){
			JsonObject obj = items.get(i).getAsJsonObject();
			this.itemsToGather.add(new QuestItem(obj));
		}
	}
	
	@Override
	public GuiBase createEditorGUI(Player player, QuestStage stage) {
		return new GuiItemSelector(player, stage, this.itemsToGather);
	}

	@Override
	public Material getIcon() {
		return Material.BREAD;
	}

	@Override
	public String[] getDescription() {
		return new String[] {"Player must gather items."};
	}

	@Override
	public void setQuestStage(QuestStage qs) {}
}
