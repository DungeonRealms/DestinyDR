package net.dungeonrealms.game.mongo.achievements;

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
        player.sendMessage(ChatColor.DARK_AQUA.toString() + ChatColor.BOLD.toString() + ">> " + ChatColor.DARK_AQUA.toString() + ChatColor.UNDERLINE.toString() + "Achievement Unlocked:" + ChatColor.DARK_AQUA.toString() + " '" + ChatColor.GRAY + achievement.getName() + ChatColor.DARK_AQUA.toString() + "'!");
        player.playSound(player.getLocation(), Sound.LEVEL_UP, 1F, 1F);
        try {
            ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.TOWN_AURA, player.getLocation().add(0, 2, 0), new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat(), 1F, 10);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (achievement.getReward() > 0) {
            API.getGamePlayer(player).addExperience(achievement.getReward(), false);
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
    }

    public enum EnumAchievements {

        FIRST_LOGIN(0, "First Login", new String[]{
                "Congratulations You've logged in!",
        }, 100, "achievement.first_login"),

        VILLAGE_SAFE(1, "Harridons Fields", new String[]{
                "Explorer: harrisons Fields",
        }, 500, "achievement.explorer_harrisons_fields"),

        PLAINS_OF_CYRENE(2, "Plains of Cyrene", new String[]{
                "Explorer: Plains of Cyrene",
        }, 500, "achievement.explorer_plains_of_cyrene"),

        DARK_OAK_WILD2(3, "Darkoak", new String[]{
                "Explorer: Darkoak",
        }, 500, "achievement.explorer_darkoak"),

        INFRONT_OF_TAVERN(4, "Jagged Rocks", new String[]{
                "Explorer: Jagged Rocks",
        }, 500, "achievement.explorer_jagged_rocks"),

        GOBLIN_CITY(5, "Skullneck", new String[]{
                "Explorer: Skullneck",
        }, 500, "achievement.explorer_skull_neck"),

        TROLL_CITY1(6, "Trollingor", new String[]{
                "Explorer: Trollingor",
        }, 500, "achievement.explorer_trollingor"),

        CRYSTALPEAKT(7, "Crystalpeak Tower", new String[]{
                "Explorer: Crystalpeak Tower",
        }, 500, "achievement.explorer_crystalpeak_tower"),

        TRANSITIONAL_13(8, "Helmchen", new String[]{
                "Explorer: Helmchen",
        }, 500, "achievement.explorer_helmchen"),

        ALSAHRA(9, "Al Sahra", new String[]{
                "Explorer: Al Sahra",
        }, 500, "achievement.explorer_al_sahra"),

        SAVANNAH_SAFEZONE(10, "Tripoli", new String[]{
                "Explorer: Tripoli",
        }, 500, "achievement.explorer_tripoli"),

        SWAMP_VILLAGE2(11, "Dreadwood", new String[]{
                "Explorer: Dreadwood",
        }, 500, "achievement.explorer_dreadwood"),

        SWAMP1(12, "Gloomy Hallows", new String[]{
                "Explorer: Gloomy Hallows",
        }, 500, "achievement.explorer_gloomy_hallows"),

        CREST_GUARD(13, "Avalon Peaks", new String[]{
                "Explorer: Avalon Peaks",
        }, 500, "achievement.explorer_avalon_peaks"),

        CS_TRIP_6(14, "The Frozen North", new String[]{
                "Explorer: The Frozen North",
        }, 500, "achievement.explorer_the_frozen_north"),

        UNDER_WORLD(15, "The Lost City of Avalon", new String[]{
                "Explorer: The Lost City of Avalon",
        }, 500, "achievement.explorer_the_lost_city_of_avalon"),

        CHIEF(16, "Chief's Glory", new String[]{
                "Explorer: Chief's Glory",
        }, 500, "achievement.explorer_chiefs_glory"),

        DEAD_PEAKS(17, "Deadpeaks", new String[]{
                "Explorer: Deadpeaks",
        }, 500, "achievement.explorer_deadpeaks"),

        MURE(18, "Mure", new String[]{
                "Explorer: Mure",
        }, 500, "achievement.explorer_mure"),

        SEBRATA(19, "Sebrata", new String[]{
                "Explorer: Sebrata",
        }, 500, "achievement.explorer_sebrata"),

        FIERY_DUNGEON(20, "The Infernal Abyss", new String[]{
                "Explorer: The Infernal Abyss",
        }, 500, "achievement.explorer_the_infernal_abyss"),

        TUTORIAL_ISLAND(21, "Tutorial Island", new String[]{
                "Explorer: Tutorial Island",
        }, 100, "achievement.explorer_tutorial_island"),

        NOVICE(22, "Dungeon Realms Novice", new String[]{
                "You've unlocked 10 or more achievements.",
        }, 500, "achievement.novice"),

        APPRENTICE(23, "Dungeon Realms Apprentice", new String[]{
                "You've unlocked 25 or more achievements.",
        }, 1000, "achievement.apprentice"),

        ADEPT(24, "Dungeon Realms Adept", new String[]{
                "You've unlocked 50 or more achievements.",
        }, 2000, "achievement.adept"),

        EXPERT(25, "Dungeon Realms Expert", new String[]{
                "You've unlocked 100 or more achievements.",
        }, 4000, "achievement.expert"),

        MASTER(22, "Dungeon Realms Master", new String[]{
                "You've unlocked 200 or more achievements.",
        }, 8000, "achievement.master"),

        PLAY_WITH_DEV(23, "Play with Developer", new String[]{
                "You're playing with a Developer!",
        }, 450, "achievement.play_with_dev"),

        MESSAGE_YOURSELF(24, "Message yourself", new String[]{
                "That's not quite right, try messaging someone else."
        }, 640, "achievement.message_your_self"),

        SEND_A_PM(25, "Send a private message", new String[]{
                "You've just sent your first private message."
        }, 200, "achievement.send_a_pm"),

        DUELER(26, "Dueler", new String[]{
                "You just challenged someone to a duel.",
        }, 400, "achievement.dueler"),

        GUILD_INVITE_YOURSELF(27, "Krazy Kat.", new String[]{
                "Invite yourself to your own guild!",
        }, 400, "achievement.krazy_kat"),

        VOTE(28, "Vote for the Server", new String[]{
                "Vote for Dungeon Realms!",
        }, 1000, "achievement.you_voted"),

        VOTE_AS_SUB(29, "Vote as Subscriber", new String[]{
                "Vote for Dungeon Realms while being a Subscriber."
        }, 500, "achievement.vote_as_subscriber"),

        VOTE_AS_SUB_PLUS(30, "Vote as Subscriber+", new String[]{
                "Vote for Dungeon Realms while being a Subscriber+"
        }, 1000, "achievement.vote_as_subscriber+"),

        PM_DEV(31, "Message a Developer", new String[]{
                "You attempted to message a Developer. Please contact a Game Master for game assistance."
        }, 450, "achievement.pmdev"),

        CREATE_A_GUILD(32, "Create a Guild", new String[]{
                "Congratulations on the creation of your new guild!."
        }, 750, "achievement.create_a_guild"),

        TOURIST(33, "Tourist", new String[]{
                "You've begun to explore the world of Andalucia"
        }, 1000, "achievement.tourist"),

        ADVENTURER(34, "Adventurer", new String[]{
                "You've explored many of the important areas within Andalucia"
        }, 2000, "achievement.adventurer"),

        OPEN_LOOT_CHEST(35, "A chest within a chest", new String[]{
                "You've opened your first loot chest."
        }, 250, "achievement.open_loot_chest"),

        REALM_EXPANSION_1(36, "Expanding I", new String[]{
                "You've begun work on your realm."
        }, 250, "achievement.realm_expansion1"),

        REALM_EXPANSION_2(37, "Expanding II", new String[]{
                "You've taken the initiative to expand your realm."
        }, 350, "achievement.realm_expansion2"),

        REALM_EXPANSION_3(38, "Expanding III", new String[]{
                "Your realm is looking very nice."
        }, 450, "achievement.realm_expansion3"),

        REALM_EXPANSION_4(39, "Expanding IV", new String[]{
                "Your realm is truly a sight to behold."
        }, 550, "achievement.realm_expansion4"),

        ACQUIRE_CURRENCY_1(40, "Acquire Currency I", new String[]{
                "You have gems to spend."
        }, 250, "achievement.currency_acquire1"),

        ACQUIRE_CURRENCY_2(41, "Acquire Currency II", new String[]{
                "More money each day."
        }, 350, "achievement.currency_acquire2"),

        ACQUIRE_CURRENCY_3(43, "Acquire Currency III", new String[]{
                "A large pile of gems."
        }, 450, "achievement.currency_acquire3"),

        ACQUIRE_CURRENCY_4(44, "Acquire Currency IV", new String[]{
                "Gem hoarder..."
        }, 550, "achievement.currency_acquire4"),

        ACQUIRE_CURRENCY_5(45, "Acquire Currency V", new String[]{
                "Wow, that's a lot of gems"
        }, 650, "achievement.currency_acquire5"),

        ACQUIRE_CURRENCY_6(46, "Acquire Currency VI", new String[]{
                "I'm not sure you need all of these."
        }, 750, "achievement.currency_acquire6"),

        ACQUIRE_CURRENCY_7(47, "Acquire Currency VII", new String[]{
                "You should start your own bank."
        }, 850, "achievement.currency_acquire7"),

        ACQUIRE_CURRENCY_8(48, "Acquire Currency VIII", new String[]{
                "You're now one of the elites in Andalucia"
        }, 950, "achievement.currency_acquire8"),

        OLD_TIMER(49, "Old Timer", new String[]{
                "You helped shape Dungeon Realms during the Beta."
        }, 500, "achievement.old_timer"),

        PET_COMPANION(50, "A Companion", new String[]{
                "You've purchased your first pet, take care of it."
        }, 250, "achievement.pet_owner"),

        ANIMAL_TAMER(51, "Animal Tamer", new String[]{
                "You've mastered the art of animal taming."
        }, 500, "achievement.pet_master"),

        SUBSCRIBER(52, "Subscriber", new String[]{
                "Thank you for purchasing Subscriber."
        }, 500, "achievement.donate_subscriber"),

        SUBSCRIBER_PLUS(53, "Subscriber+", new String[]{
                "Thank you for purchasing Subscriber+."
        }, 750, "achievement.donate_subscriber+"),

        SUBSCRIBER_PLUS_PLUS(54, "Subscriber++", new String[]{
                "Thank you for purchasing Subscriber++."
        }, 1000, "achievement.donate_subscriber++"),

        PLAYER_MOD(55, "Player Moderator", new String[]{
                "You're the eyes and ears of Dungeon Realms."
        }, 500, "achievement.staff_pmod"),

        GAME_MASTER(56, "Game Master", new String[]{
                "You keep Dungeon Realms under check."
        }, 500, "achievement.staff_gm"),

        GUILD_MEMBER(57, "Guild Member", new String[]{
                "You're part of a guild now."
        }, 250, "achievement.guild_member"),

        GUILD_OFFICER(58, "Guild Officer", new String[]{
                "Helping to ensure order within your guild."
        }, 250, "achievement.guild_officer"),

        GUILD_CREATOR(59, "Guild Creator", new String[]{
                "You've created a guild, best of luck."
        }, 250, "achievement.guild_creator"),

        PARTY_UP(60, "Party Up", new String[]{
                "Join a party."
        }, 250, "achievement.party_join"),

        LEVEL_10(61, "Level 10", new String[]{
                "Reach level 10."
        }, 250, "achievement.level_10"),

        LEVEL_25(62, "Level 25", new String[]{
                "Reach level 25."
        }, 500, "achievement.level_25"),

        LEVEL_50(63, "Level 50", new String[]{
                "Reach level 50."
        }, 500, "achievement.level_50"),

        LEVEL_100(64, "Level 100", new String[]{
                "Reach level 100."
        }, 1000, "achievement.level_100"),

        SHOP_CREATOR(65, "Shop Creator", new String[]{
                "Create your own shop."
        }, 250, "achievement.shop_created"),

        SHOP_UPGRADE_1(66, "Shop Upgrade I", new String[]{
                "Upgrade your shop."
        }, 250, "achievement.shop_upgrade1"),

        SHOP_MERCHANT(67, "Merchant", new String[]{
                "Sell an item from your shop."
        }, 250, "achievement.shop_merchant"),

        CARTOGRAPHER(68, "Cartographer", new String[]{
                "Obtain a map of the area."
        }, 250, "achievement.cartographer"),

        MOUNT_OWNER(69, "Saddle Up!", new String[]{
                "Own a mount."
        }, 250, "achievement.mount_owner");

        private int id;
        private String name;
        private String[] message;
        private int reward;
        private String rawName;

        EnumAchievements(int id, String name, String[] message, int reward, String rawName) {
            this.id = id;
            this.name = name;
            this.message = message;
            this.reward = reward;
            this.rawName = rawName;
        }

        public int getId() {
            return id;
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

        public static EnumAchievements getById(int id) {
            for (EnumAchievements ea : values()) {
                if (ea.getId() == id) {
                    return ea;
                }
            }
            return getById(0);
        }
    }


}
