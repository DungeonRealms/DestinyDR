package net.dungeonrealms.mongo.achievements;

import com.mongodb.client.model.Filters;
import net.dungeonrealms.mastery.Utils;
import net.dungeonrealms.mongo.Database;
import net.dungeonrealms.mongo.DatabaseAPI;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by Nick on 8/29/2015.
 */
public class Achievements {

    private static Achievements instance = null;

    public static Achievements getInstance() {
        if (instance == null) {
            instance = new Achievements();
        }
        return instance;
    }

    private boolean hasAchievement(UUID uuid, EnumAchievements achievement) {
        Object info = DatabaseAPI.PLAYERS.get(uuid).get("collectibles");
        ArrayList<String> TEMP_LIST = (ArrayList<String>) ((Document) info).get("achievements");
        if (TEMP_LIST == null) {
            Utils.log.warning("Unable to process hasAchievement() method for " + uuid + " the list is NULL!?");
        }
        return TEMP_LIST.contains(achievement.getMongoName());
    }

    public void giveAchievement(UUID uuid, EnumAchievements achievement) {
        if (!(hasAchievement(uuid, achievement))) {
            Database.collection.updateOne(Filters.eq("info.uuid", uuid.toString()), new Document("$push", new Document("collectibles.achievements", achievement.getMongoName())),
                    (result, t) -> Bukkit.broadcastMessage(ChatColor.AQUA + Bukkit.getPlayer(uuid).getName() + ChatColor.YELLOW + " has earned " + ChatColor.GREEN + achievement.getName()));
        }
    }

    public enum EnumAchievements {

        FIRST_LOGIN(0, "First Login", new String[]{
                "",
                "Congratulations You've logged in!",
                "",
        }, 100, "achievement.first_login");

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
