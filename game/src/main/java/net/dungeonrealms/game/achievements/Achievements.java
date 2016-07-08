package net.dungeonrealms.game.achievements;

import net.dungeonrealms.API;
import net.dungeonrealms.game.mechanics.ParticleAPI;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.mongo.EnumOperators;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

/**
 * Created by Nick on 8/29/2015.
 */
@SuppressWarnings("unchecked")
public class Achievements {

    private static Achievements instance = null;

    public static Achievements getInstance() {
        if (instance == null) {
            instance = new Achievements();
        }
        return instance;
    }

    /**
     * Checks if a player has the achievement.
     *
     * @param uuid
     * @param achievement
     * @return
     * @since 1.0
     */
    public boolean hasAchievement(UUID uuid, EnumAchievements achievement) {
        return ((ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.ACHIEVEMENTS, uuid)).contains(achievement.getMongoName());
    }

    private int explorerAchievementCount(UUID uuid) {
        int count = 0;
        for (String achievement : (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.ACHIEVEMENTS, uuid)) {
            if (achievement.toLowerCase().contains("explorer_")) {
                count++;
            }
        }
        return count;
    }

    /**
     * Gives a player an achievement, performs internal hasCheck.
     *
     * @param uuid
     * @param achievement
     * @since 1.0
     */
    public void giveAchievement(UUID uuid, EnumAchievements achievement) {
        if (hasAchievement(uuid, achievement)) return;
        DatabaseAPI.getInstance().update(uuid, EnumOperators.$PUSH, EnumData.ACHIEVEMENTS, achievement.getMongoName(), true);
        if (Bukkit.getPlayer(uuid) == null) return;
        Player player = Bukkit.getPlayer(uuid);
        player.sendMessage(ChatColor.DARK_AQUA.toString() + ChatColor.BOLD.toString() + ">> " + ChatColor.DARK_AQUA.toString() + "Achievement Unlocked:" + ChatColor.DARK_AQUA.toString() + " '" + ChatColor.GRAY + achievement.getName() + ChatColor.DARK_AQUA.toString() + "'!");
        player.sendMessage(ChatColor.GRAY.toString() + ChatColor.ITALIC + achievement.getMessage()[0]);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1F, 1F);
        try {
            ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.TOWN_AURA, player.getLocation().add(0, 2, 0), new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat(), 1F, 10);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (achievement.getReward() > 0) {
            API.getGamePlayer(player).addExperience(achievement.getReward(), false, true);
        }
        switch (((ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.ACHIEVEMENTS, uuid)).size()) {
            case 10:
                giveAchievement(uuid, EnumAchievements.NOVICE);
                break;
            case 20:
                giveAchievement(uuid, EnumAchievements.APPRENTICE);
                break;
            case 50:
                giveAchievement(uuid, EnumAchievements.ADEPT);
                break;
            case 100:
                giveAchievement(uuid, EnumAchievements.EXPERT);
                break;
            case 200:
                giveAchievement(uuid, EnumAchievements.MASTER);
                break;
            default:
                break;
        }
        switch (explorerAchievementCount(uuid)) {
            case 10:
                giveAchievement(uuid, EnumAchievements.TOURIST);
                break;
            case 20:
                giveAchievement(uuid, EnumAchievements.ADVENTURER);
                break;
            default:
                break;
        }
        if (achievement == EnumAchievements.FISHINGROD_LEVEL_V) {
            if (hasAchievement(player.getUniqueId(), EnumAchievements.PICKAXE_LEVEL_V)) {
                giveAchievement(player.getUniqueId(), EnumAchievements.SKILL_MASTER);
            }
        } else if (achievement == EnumAchievements.PICKAXE_LEVEL_V) {
            if (hasAchievement(player.getUniqueId(), EnumAchievements.FISHINGROD_LEVEL_V)) {
                giveAchievement(player.getUniqueId(), EnumAchievements.SKILL_MASTER);
            }
        }
    }

    public enum EnumAchievements {

        FIRST_LOGIN("First Login", new String[]{
                "Welcome to Dungeon Realms!",
        }, 100, "achievement.first_login", false),

        TUTORIAL_ISLAND("Tutorial Island", new String[]{
                "Explorer: Tutorial Island",
        }, 100, "achievement.explorer_tutorial_island", false),

        HARRISONS_FIELD("Harrisons Fields", new String[]{
                "Explorer: Harrisons Fields",
        }, 500, "achievement.explorer_harrisons_fields", false),

        PLAINS_OF_CYRENE("Plains of Cyrene", new String[]{
                "Explorer: Plains of Cyrene",
        }, 500, "achievement.explorer_plains_of_cyrene", false),

        DARKOAK("Darkoak", new String[]{
                "Explorer: Darkoak",
        }, 500, "achievement.explorer_darkoak", false),

        JAGGED_ROCKS("Jagged Rocks", new String[]{
                "Explorer: Jagged Rocks",
        }, 500, "achievement.explorer_jagged_rocks", false),

        GOBLIN_CITY("Skullneck", new String[]{
                "Explorer: Skullneck",
        }, 1000, "achievement.explorer_skull_neck", false),

        TROLLINGOR("Trollingor", new String[]{
                "Explorer: Trollingor",
        }, 2500, "achievement.explorer_trollingor", false),

        CRYSTALPEAK_TOWER("Crystalpeak Tower", new String[]{
                "Explorer: Crystalpeak Tower",
        }, 5000, "achievement.explorer_crystalpeak_tower", false),

        HELMCHEN("Helmchen", new String[]{
                "Explorer: Helmchen",
        }, 1000, "achievement.explorer_helmchen", false),

        AL_SAHRA("Al Sahra", new String[]{
                "Explorer: Al Sahra",
        }, 1000, "achievement.explorer_al_sahra", false),

        TRIPOLI("Tripoli", new String[]{
                "Explorer: Tripoli",
        }, 1000, "achievement.explorer_tripoli", false),

        DREADWOOD("Dreadwood", new String[]{
                "Explorer: Dreadwood",
        }, 500, "achievement.explorer_dreadwood", false),

        GLOOMY_HOLLOWS("Gloomy Hallows", new String[]{
                "Explorer: Gloomy Hallows",
        }, 1000, "achievement.explorer_gloomy_hallows", false),

        CREST_GUARD("Crest Guard", new String[]{
                "Explorer: Crest Guard",
        }, 2500, "achievement.explorer_crest_guard", false),

        FROZEN_NORTH("The Frozen North", new String[]{
                "Explorer: The Frozen North",
        }, 5000, "achievement.explorer_the_frozen_north", false),

        UNDER_WORLD("The Lost City of Avalon", new String[]{
                "Explorer: The Lost City of Avalon",
        }, 10000, "achievement.explorer_the_lost_city_of_avalon", false),

        CHIEF("Chief's Glory", new String[]{
                "Explorer: Chief's Glory",
        }, 7500, "achievement.explorer_chiefs_glory", false),

        DEAD_PEAKS("Deadpeaks", new String[]{
                "Explorer: Deadpeaks",
        }, 1000, "achievement.explorer_deadpeaks", false),

        MURE("Mure", new String[]{
                "Explorer: Mure",
        }, 2500, "achievement.explorer_mure", false),

        SEBRATA("Sebrata", new String[]{
                "Explorer: Sebrata",
        }, 2500, "achievement.explorer_sebrata", false),

        CYRENNICA("Cyrennica", new String[]{
                "Explorer: Cyrennica",
        }, 500, "achievement.explorer_cyrennica", false),

        FIERY_DUNGEON("The Infernal Abyss", new String[]{
                "Explorer: The Infernal Abyss",
        }, 5000, "achievement.explorer_the_infernal_abyss", false),

        NOVICE("Dungeon Realms Novice", new String[]{
                "You've unlocked 10 or more achievements.",
        }, 1500, "achievement.character_novice", false),

        APPRENTICE("Dungeon Realms Apprentice", new String[]{
                "You've unlocked 25 or more achievements.",
        }, 2500, "achievement.character_apprentice", false),

        ADEPT("Dungeon Realms Adept", new String[]{
                "You've unlocked 50 or more achievements.",
        }, 5000, "achievement.character_adept", false),

        EXPERT("Dungeon Realms Expert", new String[]{
                "You've unlocked 100 or more achievements.",
        }, 10000, "achievement.character_expert", false),

        MASTER("Dungeon Realms Master", new String[]{
                "You've unlocked 200 or more achievements.",
        }, 20000, "achievement.character_master", false),

        LEAP_OF_FAITH("Leap of Faith", new String[]{
                "You've taken a leap of faith.",
        }, 1000, "achievement.character_leap_of_faith", false),

        PLAY_WITH_DEV("Play with Developer", new String[]{
                "You're playing with a Developer!",
        }, 450, "achievement.social_play_with_dev", false),

        MESSAGE_YOURSELF("Message yourself", new String[]{
                "That's not quite right, try messaging someone else."
        }, 640, "achievement.social_message_your_self", false),

        SEND_A_PM("Send a private message", new String[]{
                "You've just sent your first private message."
        }, 200, "achievement.social_send_a_pm", false),

        DUELER("Dueler", new String[]{
                "You just challenged someone to a duel.",
        }, 400, "achievement.social_dueler", false),

        GUILD_INVITE_YOURSELF("Krazy Kat.", new String[]{
                "Invite yourself to your own guild!",
        }, 400, "achievement.social_krazy_kat", false),

        VOTE("Vote for the Server", new String[]{
                "Vote for Dungeon Realms!",
        }, 1000, "achievement.character_you_voted", false),

        VOTE_AS_SUB("Vote as Subscriber", new String[]{
                "Vote for Dungeon Realms while being a Subscriber."
        }, 500, "achievement.character_vote_as_subscriber", false),

        VOTE_AS_SUB_PLUS("Vote as Subscriber+", new String[]{
                "Vote for Dungeon Realms while being a Subscriber+"
        }, 1000, "achievement.character_vote_as_subscriber+", false),

        PM_DEV( "Message a Developer", new String[]{
                "You attempted to message a Developer, please contact a Game Master for game assistance."
        }, 450, "achievement.social_pmdev", false),

        CREATE_A_GUILD("Create a Guild", new String[]{
                "Congratulations on the creation of your new guild!"
        }, 750, "achievement.social_create_a_guild", false),

        TOURIST("Tourist", new String[]{
                "You've begun to explore the world of Andalucia"
        }, 1000, "achievement.explorer_tourist", false),

        ADVENTURER("Adventurer", new String[]{
                "You've explored many of the important areas within Andalucia"
        }, 2000, "achievement.explorer_adventurer", false),

        OPEN_LOOT_CHEST("A chest within a chest", new String[]{
                "You've opened your first loot chest."
        }, 250, "achievement.explorer_open_loot_chest", false),

        REALM_EXPANSION_I("Expanding I", new String[]{
                "You've begun work on your realm."
        }, 500, "achievement.realm_expansion_i", false),

        REALM_EXPANSION_II("Expanding II", new String[]{
                "You've taken the initiative to expand your realm."
        }, 1000, "achievement.realm_expansion_ii", false),

        REALM_EXPANSION_III("Expanding III", new String[]{
                "Your realm is looking very nice."
        }, 2500, "achievement.realm_expansion_iii", false),

        REALM_EXPANSION_IV("Expanding IV", new String[]{
                "Your realm is truly a sight to behold."
        }, 5000, "achievement.realm_expansion_iv", false),

        ACQUIRE_CURRENCY_I("Acquire Currency I", new String[]{
                "You have gems to spend."
        }, 250, "achievement.currency_acquire_i", false),

        ACQUIRE_CURRENCY_II("Acquire Currency II", new String[]{
                "More money each day."
        }, 500, "achievement.currency_acquire_ii", false),

        ACQUIRE_CURRENCY_III("Acquire Currency III", new String[]{
                "A large pile of gems."
        }, 1000, "achievement.currency_acquire_iii", false),

        ACQUIRE_CURRENCY_IV("Acquire Currency IV", new String[]{
                "Gem hoarder..."
        }, 1500, "achievement.currency_acquire_iv", false),

        ACQUIRE_CURRENCY_V("Acquire Currency V", new String[]{
                "Wow, that's a lot of gems"
        }, 2500, "achievement.currency_acquire_v", false),

        ACQUIRE_CURRENCY_VI("Acquire Currency VI", new String[]{
                "I'm not sure you need all of these."
        }, 5000, "achievement.currency_acquire_vi", false),

        ACQUIRE_CURRENCY_VII("Acquire Currency VII", new String[]{
                "You should start your own bank."
        }, 7500, "achievement.currency_acquire_vii", false),

        ACQUIRE_CURRENCY_VIII("Acquire Currency VIII", new String[]{
                "You're now one of the elites in Andalucia"
        }, 10000, "achievement.currency_acquire_vii", false),

        OLD_TIMER("Old Timer", new String[]{
                "You helped shape Dungeon Realms during the Beta."
        }, 500, "achievement.character_old_timer", false),

        PET_COMPANION("A Companion", new String[]{
                "You've purchased your first pet, take care of it."
        }, 250, "achievement.character_pet_owner", false),

        ANIMAL_TAMER("Animal Tamer", new String[]{
                "You've mastered the art of animal taming."
        }, 500, "achievement.character_pet_master", false),

        SUBSCRIBER("Subscriber", new String[]{
                "Thank you for purchasing Subscriber."
        }, 500, "achievement.character_donate_subscriber", false),

        SUBSCRIBER_PLUS("Subscriber+", new String[]{
                "Thank you for purchasing Subscriber+."
        }, 750, "achievement.character_donate_subscriber+", false),

        SUBSCRIBER_PLUS_PLUS("Subscriber++", new String[]{
                "Thank you for purchasing Subscriber++."
        }, 1000, "achievement.character_donate_subscriber++", false),

        PLAYER_MOD("Player Moderator", new String[]{
                "You're the eyes and ears of Dungeon Realms."
        }, 500, "achievement.character_staff_pmod", true),

        SUPPORT_AGENT("Support Agent", new String[]{
                "Thank you for helping the players."
        }, 500, "achievement.character_staff_support", true),

        GAME_MASTER("Game Master", new String[]{
                "You keep Dungeon Realms under check."
        }, 500, "achievement.character_staff_gm", true),

        DEVELOPER("Developer", new String[]{
                "Thank you for helping build Dungeon Realms."
        }, 500, "achievement.character_staff_dev", true),

        GUILD_MEMBER("Guild Member", new String[]{
                "You're part of a guild now."
        }, 250, "achievement.social_guild_member", false),

        GUILD_OFFICER("Guild Officer", new String[]{
                "Helping to ensure order within your guild."
        }, 250, "achievement.social_guild_officer", false),

        GUILD_CREATOR("Guild Creator", new String[]{
                "You've created a guild, best of luck."
        }, 250, "achievement.social_guild_creator", false),

        GUILD_REPESENT("Represent", new String[]{
                "Equip a Guild Banner."
        }, 250, "achievement.social_guild_represent", false),

        PARTY_MAKER("Party Maker", new String[]{
                "Create your very own party."
        }, 250, "achievement.social_party_create", false),

        PARTY_UP("Party Up", new String[]{
                "Join a party."
        }, 250, "achievement.social_party_join", false),

        LEVEL_10("Level 10", new String[]{
                "Reach level 10."
        }, 1000, "achievement.character_level_10", false),

        LEVEL_25("Level 25", new String[]{
                "Reach level 25."
        }, 2500, "achievement.character_level_25", false),

        LEVEL_50("Level 50", new String[]{
                "Reach level 50."
        }, 5000, "achievement.character_level_50", false),

        LEVEL_100("Level 100", new String[]{
                "Reach level 100."
        }, 10000, "achievement.character_level_100", false),

        SHOP_CREATOR("Shop Creator", new String[]{
                "Create your own shop."
        }, 250, "achievement.currency_shop_created", false),

        SHOP_UPGRADE_I("Shop Upgrade I", new String[]{
                "Upgrade your shop."
        }, 250, "achievement.currency_shop_upgrade_i", false),

        SHOP_MERCHANT("Merchant", new String[]{
                "Sell an item from your shop."
        }, 250, "achievement.currency_shop_merchant", false),

        CARTOGRAPHER("Cartographer", new String[]{
                "Obtain a map of the area."
        }, 250, "achievement.explorer_cartographer", false),

        MOUNT_OWNER("Saddle Up!", new String[]{
                "Own a mount."
        }, 250, "achievement.character_mount_owner", false),

        CAKE_IS_A_LIE("The Cake is a Lie", new String[]{
                "Discovered the truth about the cake."
        }, 250, "achievement.explorer_easteregg_portal_cakelie", false),

        INFECTED("Infected!", new String[]{
                "Killed someone who was infected."
        }, 500, "achievement.combat_infected", false),

        BANDIT_TROVE("Mayel The Cruel", new String[]{
                "Defeated Mayel The Cruel."
        }, 15000, "achievement.combat_bandit_trove", false),

        VARENGLADE("Burick The Fanatic", new String[]{
                "Defeated Burick The Fanatic."
        }, 50000, "achievement.combat_varenglade", false),

        INFERNAL_ABYSS("The Infernal Abyss", new String[]{
                "Defeated The Infernal Abyss."
        }, 75000, "achievement.combat_infernal_abyss", false),

        //Isn't implemented. Part of the incomplete "OneWolfe" dunegon.
        DEPTHS_OF_ACERON("Depths of Aceron", new String[]{
                "Defeated Aceron."
        }, 75000, "achievement.combat_depths_of_aceron", true),

        MONSTER_HUNTER_I("Monster Hunter I", new String[]{
                "Defeated 100 monsters."
        }, 250, "achievement.combat_monster_hunter_i", false),

        MONSTER_HUNTER_II("Monster Hunter II", new String[]{
                "Defeated 300 monsters."
        }, 500, "achievement.combat_monster_hunter_ii", false),

        MONSTER_HUNTER_III("Monster Hunter III", new String[]{
                "Defeated 500 monsters."
        }, 1000, "achievement.combat_monster_hunter_iii", false),

        MONSTER_HUNTER_IV("Monster Hunter IV", new String[]{
                "Defeated 1000 monsters."
        }, 3000, "achievement.combat_monster_hunter_iv", false),

        MONSTER_HUNTER_V("Monster Hunter V", new String[]{
                "Defeated 1500 monsters."
        }, 6000, "achievement.combat_monster_hunter_v", false),

        MONSTER_HUNTER_VI("Monster Hunter VI", new String[]{
                "Defeated 2000 monsters."
        }, 10000, "achievement.combat_monster_hunter_vi", false),

        MAN_HUNTER_I("Man Hunter I", new String[]{
                "Defeated 1 player."
        }, 250, "achievement.combat_man_hunter_i", false),

        MAN_HUNTER_II("Man Hunter II", new String[]{
                "Defeated 3 players."
        }, 500, "achievement.combat_man_hunter_ii", false),

        MAN_HUNTER_III("Man Hunter III", new String[]{
                "Defeated 5 players."
        }, 1000, "achievement.combat_man_hunter_iii", false),

        MAN_HUNTER_IV("Man Hunter IV", new String[]{
                "Defeated 10 players."
        }, 3000, "achievement.combat_man_hunter_iv", false),

        MAN_HUNTER_V("Man Hunter V", new String[]{
                "Defeated 15 players."
        }, 6000, "achievement.combat_man_hunter_v", false),

        MAN_HUNTER_VI("Man Hunter VI", new String[]{
                "Defeated 20 players."
        }, 10000, "achievement.combat_man_hunter_vi", false),

        ENFORCER_OF_JUSTICE_I("Enforcer of Justice I", new String[]{
            "Defeated 1 chaotic player."
        }, 300, "achievement.combat_enforcer_of_justice_i", false),

        ENFORCER_OF_JUSTICE_iI("Enforcer of Justice II", new String[]{
            "Defeated 3 chaotic players."
        }, 2500, "achievement.combat_enforcer_of_justice_ii", false),

        ENFORCER_OF_JUSTICE_Iii("Enforcer of Justice III", new String[]{
            "Defeated 5 chaotic players."
        }, 5000, "achievement.combat_enforcer_of_justice_iii", false),

        HERO("Hero", new String[]{
                "Heroically killed an outlaw."
        }, 250, "achievement.combat_hero_i", false),

        PICKAXE_LEVEL_I("Pickaxe Level I", new String[]{
            "Obtained a level 20 Pickaxe."
        }, 500, "achievement.character_pickaxe_level_i", false),

        PICKAXE_LEVEL_II("Pickaxe Level II", new String[]{
                "Obtained a level 40 Pickaxe."
        }, 1000, "achievement.character_pickaxe_level_ii", false),

        PICKAXE_LEVEL_III("Pickaxe Level III", new String[]{
                "Obtained a level 60 Pickaxe."
        }, 2000, "achievement.character_pickaxe_level_iii", false),

        PICKAXE_LEVEL_IV("Pickaxe Level IV", new String[]{
                "Obtained a level 80 Pickaxe."
        }, 3500, "achievement.character_pickaxe_level_iv", false),

        PICKAXE_LEVEL_V("Pickaxe Level V", new String[]{
                "Obtained a level 100 Pickaxe."
        }, 5000, "achievement.character_pickaxe_level_v", false),

        FISHINGROD_LEVEL_I("Fishing Rod Level I", new String[]{
                "Obtained a level 20 Fishing Rod."
        }, 500, "achievement.character_fishingrod_level_i", false),

        FISHINGROD_LEVEL_II("Fishing Rod Level II", new String[]{
                "Obtained a level 40 Fishing Rod."
        }, 1000, "achievement.character_fishingrod_level_ii", false),

        FISHINGROD_LEVEL_III("Fishing Rod Level III", new String[]{
                "Obtained a level 60 Fishing Rod."
        }, 2000, "achievement.character_fishingrod_level_iii", false),

        FISHINGROD_LEVEL_IV("Fishing Rod Level IV", new String[]{
                "Obtained a level 80 Fishing Rod."
        }, 3500, "achievement.character_fishingrod_level_iv", false),

        FISHINGROD_LEVEL_V("Fishing Rod Level IV", new String[]{
                "Obtained a level 100 Fishing Rod."
        }, 5000, "achievement.character_fishingrod_level_v", false),

        SKILL_MASTER("Skill Master", new String[]{
            "Obtained both a level 100 Fishing Rod and Pickaxe."
        }, 25000, "achievement.character_skill_master", false);

        private String name;
        private String[] message;
        private int reward;
        private String rawName;
        private boolean hideWhenIncomplete;

        EnumAchievements(String name, String[] message, int reward, String rawName, boolean hideWhenIncomplete) {
            this.name = name;
            this.message = message;
            this.reward = reward;
            this.rawName = rawName;
            this.hideWhenIncomplete = hideWhenIncomplete;
        }

        public String getName() {
            return name;
        }

        public String[] getMessage() {
            return message;
        }

        public int getReward() {
            return reward;
        }

        public String getMongoName() {
            return rawName;
        }

        public boolean getHide() {
            return hideWhenIncomplete;
        }
    }


}
