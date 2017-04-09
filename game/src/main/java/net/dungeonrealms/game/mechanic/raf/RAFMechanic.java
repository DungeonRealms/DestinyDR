package net.dungeonrealms.game.mechanic.raf;

import lombok.Getter;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.data.EnumOperators;
import net.dungeonrealms.game.achievements.Achievements;
import net.dungeonrealms.game.achievements.Achievements.EnumAchievements;
import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;

public class RAFMechanic implements GenericMechanic {

	@Getter
	private static final RAFMechanic instance = new RAFMechanic();
	
	@Override
	public void startInitialization() {
		Bukkit.getScheduler().runTaskTimerAsynchronously(DungeonRealms.getInstance(), this::checkAllPlayers, 0, 60L * 20);
	}

	@Override
	public void stopInvocation() {
		
	}
	
	/**
	 * Cycles through all players on the server and handles giving out RAF rewards based on their progress.
	 */
	private void checkAllPlayers() {
		for (Player player : Bukkit.getOnlinePlayers())
			if (!(Boolean)DatabaseAPI.getInstance().getData(EnumData.RAF_CHECKED, player.getUniqueId())
					&& DatabaseAPI.getInstance().getData(EnumData.RAF_REFERRER, player.getUniqueId()) != null
					&& hasCompletedRequirements(player))
				handleRAFCompletion(player);
	}
	
	/**
	 * Handles when a player completes the RAF requirements.
	 * @param player
	 */
	private void handleRAFCompletion(Player player) {
		DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.RAF_CHECKED, true, true);
		
	}
	
	/**
	 * Has a player completed their RAF requirements?
	 * @param toCheck
	 * @param referee - Player to refer this new player.
	 */
	public boolean hasCompletedRequirements(Player toCheck) {
		double hoursPlayed = (double)((Integer)DatabaseAPI.getInstance().getData(EnumData.TIME_PLAYED, toCheck.getUniqueId()) / 60D);
		int level = (Integer)DatabaseAPI.getInstance().getData(EnumData.LEVEL, toCheck.getUniqueId());
		
		int achievementCount = 0;
		//Calculate achievement count.
		for(EnumAchievements ach : EnumAchievements.values())
			if(Achievements.getInstance().hasAchievement(toCheck.getUniqueId(), ach))
				achievementCount++;
		
		return hoursPlayed >= 5 && level >= 25 && achievementCount >= 15;
	}
	
	@Override
	public EnumPriority startPriority() {
		return EnumPriority.CATHOLICS;
	}
}
