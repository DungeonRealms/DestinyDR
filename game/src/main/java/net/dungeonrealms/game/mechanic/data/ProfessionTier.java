package net.dungeonrealms.game.mechanic.data;

import net.dungeonrealms.game.achievements.Achievements.EnumAchievements;

public interface ProfessionTier {
	
	/**
	 * Get the name for this tier's item.
	 * @return
	 */
	public String getItemName();
	
	/**
	 * Return the description shown in the item's lore.
	 * @return
	 */
	public String getDescription();
	
	/**
	 * Get the achievement for getting this tier.
	 * @return
	 */
	public EnumAchievements getAchievement();
	
	public int getTier();
}
