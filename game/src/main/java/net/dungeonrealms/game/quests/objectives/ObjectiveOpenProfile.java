package net.dungeonrealms.game.quests.objectives;

import net.dungeonrealms.game.quests.QuestNPC;
import net.dungeonrealms.game.quests.QuestStage;
import net.dungeonrealms.game.quests.gui.GuiBase;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.google.gson.JsonObject;

public class ObjectiveOpenProfile implements QuestObjective {

	@Override
	public GuiBase createEditorGUI(Player player, QuestStage stage) {
		return null;
	}

	@Override
	public boolean isCompleted(Player player, QuestStage stage,
			QuestNPC currentNPC) {
		return false;
	}

	@Override
	public String getName() {
		return "Open Profile";
	}

	@Override
	public Material getIcon() {
		return Material.SKULL_ITEM;
	}

	@Override
	public String[] getDescription() {
		return new String[] {"Require a player to interact with their profile."};
	}

	@Override
	public String getTaskDescription(Player player, QuestStage stage) {
		return "Open your Character Profile with /profile.";
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
