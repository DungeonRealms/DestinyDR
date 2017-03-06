package net.dungeonrealms.game.quests.objectives;

import net.dungeonrealms.game.quests.QuestNPC;
import net.dungeonrealms.game.quests.QuestStage;
import net.dungeonrealms.game.quests.gui.GuiBase;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.google.gson.JsonObject;

/**
 * Quest Objectives
 * @author Kneesnap
 */
public interface QuestObjective {

	/**
	 * Returns a GUI where you can edit this objective's data
	 */
	public GuiBase createEditorGUI(Player player, QuestStage stage);
	
	/**
	 * Has this quest objective been completed?
	 * 
	 * @param Player
	 */
	public boolean isCompleted(Player player, QuestStage stage, QuestNPC currentNPC);
	
	/**
	 * Returns the objective name (Used when editting quests)
	 */
	public String getName();
	
	/**
	 * Returns the icon of this objective (For the GUI)
	 * @return Icon
	 */
	public Material getIcon();
	
	/**
	 * Returns the description to show in the gui.
	 */
	public String[] getDescription();
	
	/**
	 * Returns the objective description shown to players.
	 * Ie: "Slay 30 bandits"
	 */
	public String getTaskDescription(Player player, QuestStage stage);
	
	/**
	 * Saves the Objective data to JSON
	 */
	public JsonObject saveJSON();
	
	/**
	 * Loads the Objective data from JSON
	 */
	public void loadJSON(JsonObject o);
	
	/**
	 * Sets the queststage this belongs to.
	 * This is used in Objectives such as ObjectiveKill which needs to know that the player is on the correct step
	 * to tell them how close they are to killing their goal.
	 */
	public void setQuestStage(QuestStage qs);
	
	/**
	 * Calls when the objective is active.
	 */
	default void onStart(Player player){
		
	}
	
	/**
	 * Calls when the objective is completed.
	 */
	default void onEnd(Player player){
		
	}
}
