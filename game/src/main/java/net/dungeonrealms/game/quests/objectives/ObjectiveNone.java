package net.dungeonrealms.game.quests.objectives;

import net.dungeonrealms.game.quests.QuestNPC;
import net.dungeonrealms.game.quests.QuestStage;
import net.dungeonrealms.game.quests.gui.GuiBase;
import net.dungeonrealms.game.quests.gui.GuiStageEditor;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.google.gson.JsonObject;

public class ObjectiveNone implements QuestObjective {

	@Override
	public GuiBase createEditorGUI(Player player, QuestStage stage) {
		return new GuiStageEditor(player, stage);
	}

	@Override
	public boolean isCompleted(Player player, QuestStage stage, QuestNPC currentNPC) {
		return true;
	}

	@Override
	public String getName() {
		return "None";
	}

	@Override
	public Material getIcon() {
		return Material.BARRIER;
	}

	@Override
	public String[] getDescription() {
		return new String[] {"No Objective. (End of Quest?)"};
	}

	@Override
	public String getTaskDescription(Player player, QuestStage stage) {
		return "No Objective";
	}

	@Override
	public JsonObject saveJSON() {
		return new JsonObject();
	}

	@Override
	public void loadJSON(JsonObject o) {
		
	}

	@Override
	public void setQuestStage(QuestStage qs) {}
}
