package net.dungeonrealms.game.quests;

public enum Trigger {
	NPC("NPC", "Talking to an NPC"),
	LOCATION("Location", "Entering an area."),
	NONE("None", "Please Select an NPC or create a location objective.");
	
	private final String name;
	private final String whatTriggers;
	
	Trigger(String n, String t){
		this.name = n;
		this.whatTriggers = t;
	}
	
	public String getName(){
		return this.name;
	}
	
	public String getWhatTriggers(){
		return this.whatTriggers;
	}
}
