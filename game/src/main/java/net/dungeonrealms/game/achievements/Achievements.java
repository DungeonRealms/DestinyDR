package net.dungeonrealms.game.achievements;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.game.database.player.Rank.PlayerRank;
import net.dungeonrealms.common.game.database.sql.QueryType;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.mechanic.ParticleAPI;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Created by Nick on 8/29/2015.
 */
public class Achievements {

    @Getter private static Achievements instance = new Achievements();

    public static boolean hasAchievement(Player player, EnumAchievements ach) {
    	return hasAchievement(player.getUniqueId(), ach);
    }
    
    public static boolean hasAchievement(UUID uuid, EnumAchievements achievement) {
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(uuid);
        return wrapper != null && wrapper.getAchievements().contains(achievement);
    }

    private static int getAchievementCount(UUID uuid, AchievementCategory category) {
    	int count = 0;
    	for (EnumAchievements ach : EnumAchievements.getByCategory(category))
    		if (hasAchievement(uuid, ach))
    			count++;
    	return count;
    }
    
    public static void giveAchievement(Player p, EnumAchievements achievement) {
    	giveAchievement(p.getUniqueId(), achievement);
    }

    /**
     * Gives a player an achievement, performs internal hasCheck.
     *
     * @param uuid
     * @param achievement
     * @since 1.0
     */
    public static void giveAchievement(UUID uuid, EnumAchievements achievement) {
    	
    	if (!Bukkit.isPrimaryThread()) {
    		Bukkit.getScheduler().runTask(DungeonRealms.getInstance(), () -> giveAchievement(uuid, achievement));
    		return;
    	}
    	
        // Achievements are disabled on the event shard.
        if (DungeonRealms.isEvent() || hasAchievement(uuid, achievement))
            return;

        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(uuid);
        if(wrapper == null) return;

        // Why wouldnt this be async?
        wrapper.getAchievements().add(achievement);
        wrapper.runQuery(QueryType.SET_ACHIEVEMENTS, wrapper.getAchievements(), wrapper.getCharacterID());
        
        Player player = Bukkit.getPlayer(uuid);
        if (player == null)
        	return;
        player.sendMessage(ChatColor.DARK_AQUA.toString() + ChatColor.BOLD.toString() + ">> " + ChatColor.DARK_AQUA.toString() + "Achievement Unlocked:" + ChatColor.DARK_AQUA.toString() + " '" + ChatColor.GRAY + achievement.getName() + ChatColor.DARK_AQUA.toString() + "'!");
        player.sendMessage(ChatColor.GRAY.toString() + ChatColor.ITALIC + achievement.getMessage());
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1F, 1F);
        ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.TOWN_AURA, player.getLocation().add(0, 2, 0), new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat(), 1F, 10);
        if (achievement.getReward() > 0)
            wrapper.addExperience(achievement.getReward(), false, true);
        
        for (EnumAchievementCount count : EnumAchievementCount.values())
        	if (count.getRequirement() == wrapper.getAchievements().size())
        		giveAchievement(uuid, count.getAchievement());
        
        int expCount = getAchievementCount(uuid, AchievementCategory.EXPLORE);
        
        if (expCount == 10) {
        	giveAchievement(uuid, EnumAchievements.TOURIST);
        } else if (expCount == 20) {
        	giveAchievement(uuid, EnumAchievements.ADVENTURER);
        }
        
        if (achievement == EnumAchievements.FISHINGROD_LEVEL_V && hasAchievement(uuid, EnumAchievements.PICKAXE_LEVEL_V))
        	giveAchievement(uuid, EnumAchievements.SKILL_MASTER);
        
        if (achievement == EnumAchievements.PICKAXE_LEVEL_V && hasAchievement(uuid, EnumAchievements.FISHINGROD_LEVEL_V))
        	giveAchievement(uuid, EnumAchievements.SKILL_MASTER);
    }
    
    @AllArgsConstructor @Getter
	public enum AchievementCategory {
		
		EXPLORE("Exploration", "exploration", Material.MAP),
		SOCIAL("Social", "socialization", Material.WRITTEN_BOOK),
		MONEY("Currency", "currency", Material.EMERALD),
		COMBAT("Combat", "combat", Material.GOLD_SWORD),
		REALM("Realm", "your realm", Material.NETHER_STAR),
		//EVENT("Event", "event participation", Material.GOLD_INGOT),
		DUNGEON("Dungeon", "dungeons", Material.COMMAND),
		CHARACTER("Character", "character customization", Material.ARMOR_STAND);
		
		private String name;
		private String description;
		private Material icon;
	}

    @AllArgsConstructor @Getter
    public enum EnumAchievements {

    	FIRST_LOGIN("First Login", "Welcome to DungeonRealms!", 100),

    	// Exploration
    	TUTORIAL_ISLAND("Tutorial Island", "Explorer: Tutorial Island", 100, AchievementCategory.EXPLORE),
    	HARRISONS_FIELD("Harrisons Fields", "Explorer: Harrisons Fields", 500, AchievementCategory.EXPLORE),
    	PLAINS_OF_CYRENE("Plains of Cyrene", "Explorer: Plains of Cyrene", 500, AchievementCategory.EXPLORE),
    	DARKOAK("Darkoak", "Explorer: Darkoak", 500, AchievementCategory.EXPLORE),
    	JAGGED_ROCKS("Jagged Rocks", "Explorer: Jagged Rocks", 500, AchievementCategory.EXPLORE),
    	SKULLNECK("Skullneck", "Explorer: Skullneck", 1000, AchievementCategory.EXPLORE),
    	TROLLINGOR("Trollingor", "Explorer: Trollingor", 2500, AchievementCategory.EXPLORE),
    	CRYSTALPEAK_TOWER("Crystalpeak Tower", "Explorer: Crystalpeak Tower", 5000, AchievementCategory.EXPLORE),
    	HELMCHEN("Helmchen", "Explorer: Helmchen", 1000, AchievementCategory.EXPLORE),
    	AL_SAHRA("Al Sahra", "Explorer: Al Sahra", 1000, AchievementCategory.EXPLORE),
    	TRIPOLI("Tripoli", "Explorer: Tripoli", 1000, AchievementCategory.EXPLORE),
    	DREADWOOD("Dreadwood", "Explorer: Dreadwood", 500, AchievementCategory.EXPLORE),
    	GLOOMY_HOLLOWS("Gloomy Hallows", "Explorer: Gloomy Hallows", 1000, AchievementCategory.EXPLORE),
    	CREST_GUARD("Crest Guard", "Explorer: Crest Guard", 2500, AchievementCategory.EXPLORE),
    	FROZEN_NORTH("The Frozen North", "Explorer: The Frozen North", 5000, AchievementCategory.EXPLORE),
    	UNDER_WORLD("The Lost City of Avalon", "Explorer: The Lost City of Avalon", 10000, AchievementCategory.EXPLORE),
    	CHIEF("Chief's Glory", "Explorer: Chief's Glory", 7500, AchievementCategory.EXPLORE),
    	DEAD_PEAKS("Deadpeaks", "Explorer: Deadpeaks", 1000, AchievementCategory.EXPLORE),
    	MURE("Mure", "Explorer: Mure", 2500, AchievementCategory.EXPLORE),
    	SEBRATA("Sebrata", "Explorer: Sebrata", 2500, AchievementCategory.EXPLORE),
    	CYRENNICA("Cyrennica", "Explorer: Cyrennica", 500, AchievementCategory.EXPLORE),
    	TOURIST("Tourist", "Begin to explore the world of Andalucia", 1000, AchievementCategory.EXPLORE),
    	ADVENTURER("Adventurer", "Explore many of the important areas withing Andalucia", 2000, AchievementCategory.EXPLORE),
    	OPEN_LOOT_CHEST("A chest within a chest", "You've opened your first loot chest.", 250, AchievementCategory.EXPLORE),
    	CARTOGRAPHER("Cartographer", "Obtain a map of the area.", 250, AchievementCategory.EXPLORE),
        CAKE_IS_A_LIE("The Cake is a Lie", "Discovered the truth about the cake.", 250, AchievementCategory.EXPLORE),
    	
    	// Character
    	NOVICE("DungeonRealms Novice", "You've unlocked 10 or more achievements.", 1500, AchievementCategory.CHARACTER),
    	APPRENTICE("DungeonRealms Apprentice", "You've unlocked 25 or more achievements.", 2500, AchievementCategory.CHARACTER),
    	ADEPT("DungeonRealms Adept", "You've unlocked 50 or more achievements.", 5000, AchievementCategory.CHARACTER),
    	EXPERT("DungeonRealms Expert", "You've unlocked 100 or more achievements.", 10000, AchievementCategory.CHARACTER),
    	MASTER("DungeonRealms Master", "You've unlocked 200 or more achievements.", 20000, AchievementCategory.CHARACTER),
    	LEAP_OF_FAITH("Leap of Faith", "You've taken a leap of faith.", 1000, AchievementCategory.CHARACTER),
    	VOTE("Vote for the Server", "Vote for DungeonRealms!", 1000, AchievementCategory.CHARACTER),
    	VOTE_AS_SUB("Vote as Subscriber", "Vote for Dungeon Realms while being a Subscriber", 500, AchievementCategory.CHARACTER),
    	VOTE_AS_SUB_PLUS("Vote as Subscriber+", "Vote for Dungeon Realms while being a Subscriber+", 1000, AchievementCategory.CHARACTER),
    	SKILL_MASTER("Skill Master", "Obtained both a level 100 fishing rod and pickaxe.", 25000, AchievementCategory.CHARACTER),
    	OLD_TIMER("Old Timer", "You helped shape Dungeon Realms during the Beta.", 500, AchievementCategory.CHARACTER),
        PET_COMPANION("A Companion", "You've purchased your first pet, take care of it.", 250, AchievementCategory.CHARACTER),
        ANIMAL_TAMER("Animal Tamer", "You've mastered the art of animal taming.", 500, AchievementCategory.CHARACTER),
        SUBSCRIBER("Subscriber", "Thank you for purchasing Subscriber.", 500, AchievementCategory.CHARACTER),
        SUBSCRIBER_PLUS("Subscriber+", "Thank you for purchasing Subscriber+.", 750, AchievementCategory.CHARACTER),
        SUBSCRIBER_PLUS_PLUS("Subscriber++", "Thank you for purchasing Subscriber++.", 1000, AchievementCategory.CHARACTER),
        LEVEL_10("Level 10", "Reach level 10.", 1000, AchievementCategory.CHARACTER),
        LEVEL_25("Level 25", "Reach level 25.", 2500, AchievementCategory.CHARACTER),
        LEVEL_50("Level 50", "Reach level 50.", 5000, AchievementCategory.CHARACTER),
        LEVEL_100("Level 100", "Reach level 100.", 10000, AchievementCategory.CHARACTER),
        MOUNT_OWNER("Saddle Up!", "Own a mount.", 250, AchievementCategory.CHARACTER),
        PICKAXE_LEVEL_I("Pickaxe Level I", "Obtained a level 20 Pickaxe.", 500, AchievementCategory.CHARACTER),
        PICKAXE_LEVEL_II("Pickaxe Level II", "Obtained a level 40 Pickaxe.", 1000, AchievementCategory.CHARACTER),
        PICKAXE_LEVEL_III("Pickaxe Level III", "Obtained a level 60 Pickaxe.", 2000, AchievementCategory.CHARACTER),
        PICKAXE_LEVEL_IV("Pickaxe Level IV", "Obtained a level 80 Pickaxe.", 3500, AchievementCategory.CHARACTER),
        PICKAXE_LEVEL_V("Pickaxe Level V", "Obtained a level 100 Pickaxe.", 5000, AchievementCategory.CHARACTER),
        FISHINGROD_LEVEL_I("Fishing Rod Level I", "Obtained a level 20 Fishing Rod.", 500, AchievementCategory.CHARACTER),
        FISHINGROD_LEVEL_II("Fishing Rod Level II", "Obtained a level 40 Fishing Rod.", 1000, AchievementCategory.CHARACTER),
        FISHINGROD_LEVEL_III("Fishing Rod Level III", "Obtained a level 60 Fishing Rod.", 2000, AchievementCategory.CHARACTER),
        FISHINGROD_LEVEL_IV("Fishing Rod Level IV", "Obtained a level 80 Fishing Rod.", 3500, AchievementCategory.CHARACTER),
        FISHINGROD_LEVEL_V("Fishing Rod Level IV", "Obtained a level 100 Fishing Rod.", 5000, AchievementCategory.CHARACTER),
    	
        PLAYER_MOD("Player Moderator", "You're the eyes and ears of Dungeon Realms.", 500, true, AchievementCategory.CHARACTER),
        SUPPORT_AGENT("Support Agent", "Thank you for helping the players.", 500, true, AchievementCategory.CHARACTER),
        GAME_MASTER("Game Master", "You keep Dungeon Realms under check.", 500, true, AchievementCategory.CHARACTER),
        DEVELOPER("Developer", "Thank you for helping build Dungeon Realms.", 500, true, AchievementCategory.CHARACTER),
        
    	// Social
    	PLAY_WITH_DEV("Play with a Developer", "You're playing with a Developer.", 450, AchievementCategory.SOCIAL),
    	MESSAGE_YOURSELF("Message Yourself", "That's not quite right, try messaging someone else.", 640, AchievementCategory.SOCIAL),
    	SEND_A_PM("Send a Private Message", "Send your first private message.", 200, AchievementCategory.SOCIAL),
    	DUELIST("Duelist", "Challenge someone to a duel.", 400, AchievementCategory.SOCIAL),
    	PM_DEV("Message a Developer", "Message a developer for the first time.", 450, AchievementCategory.SOCIAL),
    	CREATE_A_GUILD("Create a Guild", "Create a guild.", 750, AchievementCategory.SOCIAL),
    	GUILD_MEMBER("Guild Member", "You're part of a guild now.", 250, AchievementCategory.SOCIAL),
        GUILD_OFFICER("Guild Officer", "Helping to ensure order within your guild.", 250, AchievementCategory.SOCIAL),
        GUILD_CREATOR("Guild Creator", "You've created a guild, best of luck.", 250, AchievementCategory.SOCIAL),
        GUILD_REPESENT("Represent", "Equip a Guild Banner.", 250, AchievementCategory.SOCIAL),
        PARTY_MAKER("Party Maker", "Create your very own party.", 250, AchievementCategory.SOCIAL),
        PARTY_UP("Party Up", "Join a party.", 250, AchievementCategory.SOCIAL),
    	
    	// Realm
        REALM_EXPANSION_I("Expanding I", "You've begun work on your realm.", 500, AchievementCategory.REALM),
        REALM_EXPANSION_II("Expanding II", "You've taken the initiative to expand your realm.", 1000, AchievementCategory.REALM),
        REALM_EXPANSION_III("Expanding III", "Your realm is looking very nice.", 2500, AchievementCategory.REALM),
        REALM_EXPANSION_IV("Expanding IV", "Your realm is truly a sight to behold.", 5000, AchievementCategory.REALM),
        
        // Money
        ACQUIRE_CURRENCY_I("Acquire Currency I", "You have gems to spend.", 250, AchievementCategory.MONEY),
        ACQUIRE_CURRENCY_II("Acquire Currency II", "More money each day.", 500, AchievementCategory.MONEY),
        ACQUIRE_CURRENCY_III("Acquire Currency III", "A large pile of gems.", 1000, AchievementCategory.MONEY),
        ACQUIRE_CURRENCY_IV("Acquire Currency IV", "Gem hoarder...", 1500, AchievementCategory.MONEY),
        ACQUIRE_CURRENCY_V("Acquire Currency V", "Wow, that's a lot of gems", 2500, AchievementCategory.MONEY),
        ACQUIRE_CURRENCY_VI("Acquire Currency VI", "I'm not sure you need all of these.", 5000, AchievementCategory.MONEY),
        ACQUIRE_CURRENCY_VII("Acquire Currency VII", "You should start your own bank.", 7500, AchievementCategory.MONEY),
        ACQUIRE_CURRENCY_VIII("Acquire Currency VIII", "You're now one of the elites in Andalucia", 10000, AchievementCategory.MONEY),
        SHOP_CREATOR("Shop Creator", "Create your own shop.", 250, AchievementCategory.MONEY),
        SHOP_UPGRADE_I("Shop Upgrade I", "Upgrade your shop.", 250, AchievementCategory.MONEY),
        SHOP_MERCHANT("Merchant", "Sell an item from your shop.", 250, AchievementCategory.MONEY),
        
        
        // Combat
        INFECTED("Infected!", "Killed someone who was infected.", 500, AchievementCategory.COMBAT),
        MONSTER_HUNTER_I("Monster Hunter I", "Defeated 100 monsters.", 250, AchievementCategory.COMBAT),
        MONSTER_HUNTER_II("Monster Hunter II", "Defeated 300 monsters.", 500, AchievementCategory.COMBAT),
        MONSTER_HUNTER_III("Monster Hunter III", "Defeated 500 monsters.", 1000, AchievementCategory.COMBAT),
        MONSTER_HUNTER_IV("Monster Hunter IV", "Defeated 1000 monsters.", 3000, AchievementCategory.COMBAT),
        MONSTER_HUNTER_V("Monster Hunter V", "Defeated 1500 monsters.", 6000, AchievementCategory.COMBAT),
        MONSTER_HUNTER_VI("Monster Hunter VI", "Defeated 2000 monsters.", 10000, AchievementCategory.COMBAT),
        MAN_HUNTER_I("Man Hunter I", "Defeated 1 player.", 250, AchievementCategory.COMBAT),
        MAN_HUNTER_II("Man Hunter II", "Defeated 3 players.", 500, AchievementCategory.COMBAT),
        MAN_HUNTER_III("Man Hunter III", "Defeated 5 players.", 1000, AchievementCategory.COMBAT),
        MAN_HUNTER_IV("Man Hunter IV", "Defeated 10 players.", 3000, AchievementCategory.COMBAT),
        MAN_HUNTER_V("Man Hunter V", "Defeated 15 players.", 6000, AchievementCategory.COMBAT),
        MAN_HUNTER_VI("Man Hunter VI", "Defeated 20 players.", 10000, AchievementCategory.COMBAT),
        ENFORCER_OF_JUSTICE_I("Enforcer of Justice I", "Defeated 1 chaotic player.", 300, AchievementCategory.COMBAT),
        ENFORCER_OF_JUSTICE_iI("Enforcer of Justice II", "Defeated 3 chaotic players.", 2500, AchievementCategory.COMBAT),
        ENFORCER_OF_JUSTICE_Iii("Enforcer of Justice III", "Defeated 5 chaotic players.", 5000, AchievementCategory.COMBAT),
        HERO("Hero", "Heroically killed an outlaw.", 250, AchievementCategory.COMBAT),
        
        // Dungeons:
        BANDIT_TROVE("Mayel The Cruel", "Defeat Mayel The Cruel.", 15000, AchievementCategory.DUNGEON),
        VARENGLADE("Burick The Fanatic", "Defeat Burick The Fanatic.", 50000, AchievementCategory.DUNGEON),
        INFERNAL_ABYSS("The Infernal Abyss", "Defeat The Infernal Abyss.", 75000, AchievementCategory.DUNGEON);
        //DEPTHS_OF_ACERON("Depths of Aceron", "Defeat Aceron.", 75000, AchievementCategory.DUNGEON),
        
        // Event
        //EVENT_PARTICIPANT_1("Championship of Cyrennica 2017", "Participated in the Championship of Cyrennica 2017 Event.", 0, AchievementCategory.EVENT);

        private String name;
        private String message;
        private int reward;
        private boolean hide;
        private AchievementCategory category;
        
        EnumAchievements(String name, String message, int reward) {
        	this(name, message, reward, false);
        }
        
        EnumAchievements(String name, String message, int reward, AchievementCategory c) {
        	this(name, message, reward, false, c);
        }
        
        EnumAchievements(String name, String message, int reward, boolean hide) {
        	this(name, message, reward, hide, null);
        }
        
        public static List<EnumAchievements> getByCategory(AchievementCategory category) {
        	List<EnumAchievements> list = new ArrayList<>();
        	for (EnumAchievements ach : values())
        		if (ach.getCategory() == category)
        			list.add(ach);
        	return list;
        }
    }
    
    public enum EnumAchievementMonsterKill {
    	MONSTER_HUNTER_I(100),
    	MONSTER_HUNTER_II(300),
    	MONSTER_HUNTER_III(5000),
    	MONSTER_HUNTER_IV(1000),
    	MONSTER_HUNTER_VI(2000);
    	
    	@Getter private int killRequirement;
    	
    	EnumAchievementMonsterKill(int req) {
    		this.killRequirement = req;
    	}
    	
    	public EnumAchievements getAchievement() {
    		return EnumAchievements.valueOf(name());
    	}
    	
    }
    
    @Getter
    public enum EnumRankAchievement {
    	
    	SUB(PlayerRank.SUB, EnumAchievements.SUBSCRIBER),
    	SUB_P(PlayerRank.SUB_PLUS, EnumAchievements.SUBSCRIBER_PLUS),
    	SUB_P_P(PlayerRank.SUB_PLUS_PLUS, EnumAchievements.SUBSCRIBER_PLUS_PLUS),
    	SUPPORT(PlayerRank.SUPPORT, EnumAchievements.SUPPORT_AGENT),
    	PMOD(PlayerRank.PMOD, EnumAchievements.PLAYER_MOD),
    	GM(PlayerRank.TRIALGM, EnumAchievements.GAME_MASTER),
    	DEV(PlayerRank.DEV, EnumAchievements.DEVELOPER, EnumAchievements.INFECTED);
    	
    	private PlayerRank minRank;
    	private EnumAchievements[] achievements;
    	
    	EnumRankAchievement(PlayerRank minRank, EnumAchievements... ach) {
    		this.minRank = minRank;
    		this.achievements = ach;
    	}
    }
    
    @AllArgsConstructor @Getter
    public enum EnumAchievementCount {
    	NOVICE(10),
    	APPRENTICE(20),
        ADEPT(50),
        EXPERT(100),
        MASTER(200);
    	
    	private int requirement;
    	
    	public EnumAchievements getAchievement() {
    		return EnumAchievements.valueOf(name());
    	}
    }
    
    @AllArgsConstructor
    public enum EnumAchievementMoney {
    	ACQUIRE_CURRENCY_I(100),
    	ACQUIRE_CURRENCY_II(1000),
    	ACQUIRE_CURRENCY_III(5000),
    	ACQUIRE_CURRENCY_IV(10000),
    	ACQUIRE_CURRENCY_V(50000),
    	ACQUIRE_CURRENCY_VI(100000),
    	ACQUIRE_CURRENCY_VII(500000),
    	ACQUIRE_CURRENCY_VIII(1000000);
    	
    	@Getter private int moneyRequirement;
    	
    	public EnumAchievements getAchievement() {
    		return EnumAchievements.valueOf(name());
    	}
    }
    
    @AllArgsConstructor
    public enum EnumAchievementLevel {
    	LEVEL_10(10),
    	LEVEL_25(25),
    	LEVEL_50(50),
    	LEVEL_100(100);
    	
    	@Getter private int levelRequirement;
    	
    	public EnumAchievements getAchievement() {
    		return EnumAchievements.valueOf(name());
    	}
    }
}
