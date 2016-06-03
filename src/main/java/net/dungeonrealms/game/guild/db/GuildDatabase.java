package net.dungeonrealms.game.guild.db;

import com.mongodb.client.model.Filters;
import net.dungeonrealms.game.guild.GuildDatabaseAPI;
import net.dungeonrealms.game.mongo.Database;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.mongo.EnumOperators;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Class written by APOLLOSOFTWARE.IO on 6/2/2016
 */

public class GuildDatabase implements GuildDatabaseAPI {

    private static GuildDatabase instance = null;

    public static GuildDatabase getInstance() {
        if (instance == null) {
            instance = new GuildDatabase();
        }
        return instance;
    }

    public volatile ConcurrentHashMap<String, Document> GUILDS = new ConcurrentHashMap<>();

    @Override
    public void startInitialization() {

    }

    public void createGuild(String guildName, String clanTag, UUID owner, Consumer<Boolean> action) {
        Document doc = Database.guilds.find(Filters.eq("info.name", guildName)).first();

        if (doc == null) {
            doc = new Document("info",
                    new Document("uuid", owner)
                            .append("name", guildName)
                            .append("clanTag", clanTag))
                    .append("motd", "Default MOTDO :(")
                    .append("officers", new ArrayList<String>())
                    .append("members", new ArrayList<String>())
                    .append("netLevel", 1)
                    .append("experience", 0);
            Database.guilds.insertOne(doc);
            GUILDS.put(guildName, doc);
            action.accept(true);

        } else action.accept(false);
    }


    public boolean isGuildNull(UUID uuid) {
        return false;
    }


    public String getGuildOf(UUID uuid) {
        return null;
    }


    public void demotePlayer(String guildName, UUID uuid) {

    }


    public void promotePlayer(String guildName, UUID uuid) {

    }


    public void kickFrom(UUID executor, String guildName, UUID uuid) {

    }


    public boolean isOwner(UUID uuid, String guildName) {
        return false;
    }


    public boolean isMember(UUID uuid, String guildName) {
        return false;
    }


    public boolean isOfficer(UUID uuid, String guildName) {
        return false;
    }


    public void saveAllGuilds() {

    }


    public void setMotdOf(String guildName, String motd) {

    }


    public boolean isOwnerOfGuild(UUID player) {
        return false;
    }


    public List<String> getEnemiesOf(String guildName) {
        return null;
    }


    public List<String> getAlliesOf(String guildName) {
        return null;
    }


    public void sendAlert(String guildName, String message) {

    }


    public void saveGuild(String guildName) {

    }


    public String getClanTagOf(String guildName) {
        return null;
    }


    public List<String> getAllOfflineOf(String guildName) {
        return null;
    }


    public List<String> getAllOnlineNamesOf(String guildName) {
        return null;
    }


    public List<UUID> getAllOnlineOf(String guildName) {
        return null;
    }


    public void setGuild(UUID uuid, String guildName) {
        DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.GUILD, guildName, false);

        saveGuild(guildName);
    }


    public boolean doesClanTagExist(String clanTag, Consumer<Boolean> action) {
        return Database.guilds.find(Filters.eq("info.clantag", clanTag)).iterator().hasNext();
    }


    public boolean doesGuildNameExist(String guildName, Consumer<Boolean> action) {
        return Database.guilds.find(Filters.eq("info.name", guildName)).iterator().hasNext();
    }
}
