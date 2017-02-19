package net.dungeonrealms.game.world.teleportation;

import static net.dungeonrealms.game.achievements.Achievements.EnumAchievements.*;

import org.bukkit.entity.Player;

import net.dungeonrealms.game.achievements.Achievements;
import net.dungeonrealms.game.achievements.Achievements.EnumAchievements;

/**
 * An easier way to identify parts of the world.
 * Code should gradually be changed to use this enum instead of
 * spaghetti switches.
 * 
 * Created Febuary 15th, 2017.
 * @author Kneesnap
 */
public enum WorldRegion {
	
	CYRENNICA("Cyrennica", "cityofcyrennica", EnumAchievements.CYRENNICA),
	HARRISON("Harrison's Field", "villagesafe", HARRISONS_FIELD),
	PLAINS_OF_CYRENNE("Cyrenne", "plainsofcyrenne", PLAINS_OF_CYRENE),
	DARK_OAK("Dark Oak", "darkoakwild2", DARKOAK),
	JAGGED_ROCKS("Jagged Rocks", "infrontoftavern", EnumAchievements.JAGGED_ROCKS),
	SKULLNECK("Skullneck", "goblincity", GOBLIN_CITY),
	TROLLINGOR("Trollingor", "trollcity1", EnumAchievements.TROLLINGOR),
	CRYSTALPEAK("Crystal Peak", "crystalpeakt", CRYSTALPEAK_TOWER),
	HELMCHEN("Helmchen", "transitional3", EnumAchievements.HELMCHEN),
	AL_SAHRA("Al Sahra", "alsahra", EnumAchievements.AL_SAHRA),
	TRIPOLI("Tripoli", "savannahsafezone", EnumAchievements.TRIPOLI),
	DREADWOOD("Dreadwood", "swampvillage_2", EnumAchievements.DREADWOOD),
	GLOOMY("Gloomy Hollows", "swamp_1", GLOOMY_HOLLOWS),
	CRESTGUARD("Crest Guard", "crestguard", CREST_GUARD),
	FROZEN_NORTH("Frozen North", "cstrip6", EnumAchievements.FROZEN_NORTH),
	AVALON("Avalon Peaks", "underworld", UNDER_WORLD),
	CHIEFS("Chiefs", "cheifs", CHIEF),
	DEADPEAKS("Deadpeaks", "deadpeaks", DEAD_PEAKS),
	MURE("Mure", "mure", EnumAchievements.MURE),
	SEBRATA("Sebrata", "sebrata", EnumAchievements.SEBRATA),
	PORTAL_EASTER_EGG("Oak's Portal", "achievement_easteregg_portal_cakelie", CAKE_IS_A_LIE),
	
	INFERNAL_ABYSS("The Infernal Abyss", "fireydungeon", EnumAchievements.FIERY_DUNGEON);
	
	private final String wgRegion;
	private final Achievements.EnumAchievements achievement;
	private final String displayName;
	
	WorldRegion(String displayName, String worldGuardName, Achievements.EnumAchievements achievement){
		this.wgRegion = worldGuardName;
		this.achievement = achievement;
		this.displayName = displayName;
	}
	
	public String getRegionName(){
		return this.wgRegion;
	}
	
	public void giveAchievement(Player player){
		if(this.achievement != null)
			Achievements.getInstance().giveAchievement(player.getUniqueId(), this.achievement);
	}
	
	public String getDisplayName(){
		return this.displayName;
	}
	
	public static WorldRegion getByRegionName(String regionName){
		for(WorldRegion region : WorldRegion.values())
			if(region.getRegionName().equals(regionName.toLowerCase()))
				return region;
		return null;
	}
}
