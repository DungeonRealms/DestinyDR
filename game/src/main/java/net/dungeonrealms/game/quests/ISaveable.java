package net.dungeonrealms.game.quests;

import com.google.gson.JsonObject;

/**
 * This interface allows easy saving of the applied class from a StoreBase
 * 
 * @author Kneesnap
 */
public interface ISaveable {

	public void fromFile(JsonObject obj);
	
	public JsonObject toJSON();
	
	/**
	 * Returns the file name to save the file as.
	 * Returns null if the file should not be saved. (Such as how DialogueLines are not their own files, they're saved in NPCs)
	 * @author Kneesnap
	 * @return File Name
	 */
	public String getFileName();
}
