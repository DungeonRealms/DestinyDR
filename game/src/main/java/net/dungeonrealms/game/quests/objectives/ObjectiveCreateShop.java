package net.dungeonrealms.game.quests.objectives;

import net.dungeonrealms.game.quests.QuestNPC;
import net.dungeonrealms.game.quests.QuestStage;
import net.dungeonrealms.game.quests.gui.GuiBase;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.google.gson.JsonObject;

public class ObjectiveCreateShop implements QuestObjective {

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
		return "Create Shop";
	}

	@Override
	public Material getIcon() {
		return Material.CHEST;
	}

	@Override
	public String[] getDescription() {
		return new String[] {"Requires the player to create a shop."};
	}

	@Override
	public String getTaskDescription(Player player, QuestStage stage) {
		return "Create a Shop";
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
}
