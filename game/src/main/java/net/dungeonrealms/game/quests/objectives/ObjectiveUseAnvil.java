package net.dungeonrealms.game.quests.objectives;

import net.dungeonrealms.game.quests.QuestNPC;
import net.dungeonrealms.game.quests.QuestStage;
import net.dungeonrealms.game.quests.gui.GuiBase;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.google.gson.JsonObject;

public class ObjectiveUseAnvil implements QuestObjective {
	
	@Override
	public GuiBase createEditorGUI(Player player, QuestStage stage) {
		return null;
	}

	@Override
	public boolean isCompleted(Player player, QuestStage stage, QuestNPC currentNPC) {
		return false;
	}

	@Override
	public String getName() {
		return "Repair";
	}

	@Override
	public Material getIcon() {
		return Material.ANVIL;
	}

	@Override
	public String[] getDescription() {
		return new String[] {"Require a player to repair an item with an anvil"};
	}

	@Override
	public String getTaskDescription(Player player, QuestStage stage) {
		return "Repair an item.";
	}

	@Override
	public JsonObject saveJSON() {
		return new JsonObject();
	}

	@Override
	public void loadJSON(JsonObject o) {
		
	}

	@Override
	public void setQuestStage(QuestStage qs) {
		
	}
	
	public void onStart(Player player){
		
	}
	
	public void onEnd(Player player){
		
	}
}
