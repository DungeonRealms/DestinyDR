package net.dungeonrealms.game.quests.objectives;

import net.dungeonrealms.game.quests.QuestNPC;
import net.dungeonrealms.game.quests.QuestStage;
import net.dungeonrealms.game.quests.Quests;
import net.dungeonrealms.game.quests.gui.GuiBase;
import net.dungeonrealms.game.quests.gui.GuiStageEditor;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.google.gson.JsonObject;

public class ObjectiveNextNPC implements QuestObjective {
	
	@Override
	public GuiBase createEditorGUI(Player player, QuestStage stage) {
		return new GuiStageEditor(player, stage);
	}

	@Override
	public boolean isCompleted(Player player, QuestStage stage, QuestNPC currentNPC) {
		return currentNPC == stage.getNPC();
	}

	@Override
	public String getName() {
		return "Speak";
	}

	@Override
	public Material getIcon() {
		return Material.LEASH;
	}

	@Override
	public String[] getDescription() {
		return new String[] {"Speak to the next NPC."};
	}

	@Override
	public String getTaskDescription(Player player, QuestStage stage) {
		if(stage == null || stage.getNPC() == null)
			return "Cannot find next NPC in Quest.";
		return "Speak to " + stage.getNPC().getName() + " " + Quests.getRegionDirections(stage.getNPC().getLocation());
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
