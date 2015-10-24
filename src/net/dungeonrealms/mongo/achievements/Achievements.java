package net.dungeonrealms.mongo.achievements;

import com.mongodb.client.result.UpdateResult;
import net.dungeonrealms.core.Callback;
import net.dungeonrealms.mastery.GamePlayer;
import net.dungeonrealms.mongo.DatabaseAPI;
import net.dungeonrealms.mongo.EnumData;
import net.dungeonrealms.mongo.EnumOperators;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
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
    private boolean hasAchievement(UUID uuid, EnumAchievements achievement) {
        return ((ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.ACHIEVEMENTS, uuid)).contains(achievement.getMongoName());
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
        DatabaseAPI.getInstance().update(uuid, EnumOperators.$PUSH, "collectibles.achievements", achievement.getMongoName(), true, new Callback<UpdateResult>(UpdateResult.class) {
            @Override
            public void callback(Throwable failCause, UpdateResult result) {
                if (result.wasAcknowledged()) {
                    if (Bukkit.getPlayer(uuid) != null) {
                        Player player = Bukkit.getPlayer(uuid);
                        player.sendMessage(ChatColor.GREEN + "[Achievement Earned] " + ChatColor.YELLOW + achievement.getMessage()[0]);
                        new GamePlayer(player).addExperience(achievement.getReward());
                    }
                    switch (((ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.ACHIEVEMENTS, uuid)).size()) {
                        case 10:
                            Achievements.getInstance().giveAchievement(uuid, EnumAchievements.NOVICE);
                            break;
                        case 20:
                            Achievements.getInstance().giveAchievement(uuid, EnumAchievements.APPRENTICE);
                            break;
                        case 50:
                            Achievements.getInstance().giveAchievement(uuid, EnumAchievements.ADEPT);
                            break;
                        case 100:
                            Achievements.getInstance().giveAchievement(uuid, EnumAchievements.EXPERT);
                            break;
                        case 200:
                            Achievements.getInstance().giveAchievement(uuid, EnumAchievements.MASTER);
                            break;
                    }
                }
            }
        });
    }

    public enum EnumAchievements {

        FIRST_LOGIN(0, "First Login", new String[]{
                "Congratulations You've logged in!",
        }, 100, "achievement.first_login"),

        VILLAGE_SAFE(1, "Harrisons Fields", new String[]{
                "Explorer: harrisons Fields",
        }, 100, "achievement.harrisons_fields"),

        PLAINS_OF_CYRENE(2, "Plains of Cyrene", new String[]{
                "Explorer: Plains of Cyrene",
        }, 100, "achievement.plains_of_cyrene"),

        DARK_OAK_WILD2(3, "Darkoak", new String[]{
                "Explorer: Darkoak",
        }, 100, "achievement.darkoak"),

        INFRONT_OF_TAVERN(4, "Jagged Rocks", new String[]{
                "Explorer: Jagged Rocks",
        }, 100, "achievement.jagged_rocks"),

        GOBLIN_CITY(5, "Skullneck", new String[]{
                "Explorer: Skullneck",
        }, 100, "achievement.skull_neck"),

        TROLL_CITY1(6, "Trollingor", new String[]{
                "Explorer: Trollingor",
        }, 100, "achievement.trollingor"),

        CRYSTALPEAKT(7, "Crystalpeak Tower", new String[]{
                "Explorer: Crystalpeak Tower",
        }, 100, "achievement.crystalpeak_tower"),

        TRANSITIONAL_13(8, "Helmchen", new String[]{
                "Explorer: Helmchen",
        }, 100, "achievement.helmchen"),

        ALSAHRA(9, "Al Sahra", new String[]{
                "Explorer: Al Sahra",
        }, 100, "achievement.al_sahra"),

        SAVANNAH_SAFEZONE(10, "Tripoli", new String[]{
                "Explorer: Tripoli",
        }, 100, "achievement.tripoli"),

        SWAMP_VILLAGE2(11, "Dreadwood", new String[]{
                "Explorer: Dreadwood",
        }, 100, "achievement.dreadwood"),

        SWAMP1(12, "Gloomy Hallows", new String[]{
                "Explorer: Gloomy Hallows",
        }, 100, "achievement.gloomy_hallows"),

        CREST_GUARD(13, "Avalon Peaks", new String[]{
                "Explorer: Avalon Peaks",
        }, 100, "achievement.avalon_peaks"),

        CS_TRIP_6(14, "The Frozen North", new String[]{
                "Explorer: The Frozen North",
        }, 100, "achievement.the_frozen_north"),

        UNDER_WORLD(15, "The Lost City of Avalon", new String[]{
                "Explorer: The Lost City of Avalon",
        }, 100, "achievement.the_lost_city_of_avalon"),

        CHIEF(16, "Cheif's Glory\"", new String[]{
                "Explorer: Cheif's Glory\"",
        }, 100, "achievement.chiefs_glory"),

        DEAD_PEAKS(17, "Deadpeaks", new String[]{
                "Explorer: Deadpeaks",
        }, 100, "achievement.deadpeaks"),

        MURE(18, "Mure", new String[]{
                "Explorer: Mure",
        }, 100, "achievement.mure"),

        SEBRATA(19, "Sebrata", new String[]{
                "Explorer: Sebrata",
        }, 100, "achievement.sebrata"),

        FIREY_DUNGEON(20, "The Infernal Abyss", new String[]{
                "Explorer: The Infernal Abyss",
        }, 100, "achievement.the_infernal_abyss"),

        TUTORAL_ISLAND(21, "Tutorial Island", new String[]{
                "Explorer: Tutorial Island",
        }, 100, "achievement.tutorial_island"),

        NOVICE(22, "Novice", new String[]{
                "Dungeon Realms Novice",
        }, 100, "achievement.novice"),
        APPRENTICE(23, "Apprentice", new String[]{
                "Dungeon Realms Apprentice",
        }, 100, "achievement.apprentice"),
        ADEPT(24, "Adept", new String[]{
                "Dungeon Realms Adept",
        }, 100, "achievement.adept"),
        EXPERT(25, "Expert", new String[]{
                "Dungeon Realms Expert",
        }, 100, "achievement.expert"),
        MASTER(22, "Master", new String[]{
                "Dungeon Realms Master",
        }, 100, "achievement.master"),;

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
