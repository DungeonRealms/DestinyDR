package net.dungeonrealms.game.quests;

import java.util.Date;

import org.bukkit.entity.Player;

public enum QuestInterval {
	ONCE("Once", -1),
	DAILY("Every Day", 1),
	WEEKLY("Every Week", 7),
	MONTHLY("Every Month", 30);
	
	private final String displayName;
	private final int interval;
	
	QuestInterval(String s, int i){
		this.displayName = s;
		this.interval = i;
	}
	
	public String getDisplayName(){
		return this.displayName;
	}
	
	public int getInterval(){
		return this.interval;
	}
	
	/**
	 * Returns if a quest cooldown has expired or not.
	 * @param Player
	 * @param Quest
	 * @return If the player can do the quest
	 */
	public boolean hasCooldownEnded(Player player, Quest quest){
		return getCooldown(player, quest) >= 0 && this.getInterval() > 0;
	}
	
	/**
	 * Returns milliseconds until a player can do a quest again.
	 * (Positive if cooldown is over)
	 * (Negative if cooldown is not over)
	 * 
	 * @param Player
	 * @param Quest
	 * @author Kneesnap
	 */
	public long getCooldown(Player player, Quest quest){
		return this == QuestInterval.ONCE ? this.getInterval() : (new Date().getTime() - (Quests.getInstance().playerDataMap.get(player).getQuestProgress(quest).getLastCompletionTime() + (this.interval * (24 * 60 * 60 * 1000) )) );
	}
}