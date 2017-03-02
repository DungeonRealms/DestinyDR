package net.dungeonrealms.game.quests.objectives;

import net.dungeonrealms.game.quests.QuestNPC;
import net.dungeonrealms.game.quests.QuestStage;
import net.dungeonrealms.game.quests.gui.GuiBase;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.google.gson.JsonObject;

public class ObjectiveOpenJournal implements QuestObjective {

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
		return "Use Journal";
	}

	@Override
	public Material getIcon() {
		return Material.BOOK;
	}

	@Override
	public String[] getDescription() {
		return new String[] {"Requires a player to open their character journal."};
	}

	@Override
	public String getTaskDescription(Player player, QuestStage stage) {
		return "Open your journal.";
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
