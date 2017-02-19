package net.dungeonrealms.game.quests;

public enum NPCAnimation {
	Nothing("Stand Still"),
	Nearest("Face Nearest Player");
	
	private final String displayName;
	NPCAnimation(String display){
		this.displayName = display;
	}
	
	public String getDisplayName(){
		return this.displayName;
	}
}
