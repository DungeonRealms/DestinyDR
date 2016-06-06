package net.dungeonrealms.game.guild.db;

import com.mongodb.client.model.Filters;
import net.dungeonrealms.game.guild.GuildDatabaseAPI;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mongo.Database;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.mongo.EnumOperators;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

/**
 * Class written by APOLLOSOFTWARE.IO on 6/2/2016
 */

@SuppressWarnings("unchecked")
public class GuildDatabase implements GuildDatabaseAPI {

    private static GuildDatabaseAPI instance = null;


    public static GuildDatabaseAPI getInstance() {
        if (instance == null) instance = new GuildDatabase();
        return instance;
    }

    public void createGuild(String guildName, String clanTag, UUID owner, Consumer<Boolean> callback) {
        Database.guilds.insertOne(GuildDatabaseAPI.getDocumentTemplate(owner.toString(), guildName, clanTag));
        Utils.log.warning("New guild created: " + guildName);
        doesGuildNameExist(guildName, callback);
    }


    public boolean doesClanTagExist(String clanTag, Consumer<Boolean> action) {
        return get("clanTag", clanTag, String.class) != null;
    }

    public boolean doesGuildNameExist(String guildName, Consumer<Boolean> action) {
        return get("name", guildName, String.class) != null;
    }

    private Object get(String guildName, String key, Class<?> clazz) {
        Bson query = and(eq("name", guildName));

        return ((Document) Database.guilds.find(query).first().get("info")).get(key, clazz);
    }


    private Object get(String guildName, String key, Object value, Class<?> clazz) {
        Bson query = and(eq("name", guildName), eq(key, value));

        return ((Document) Database.guilds.find(query).first().get("info")).get(key, clazz);
    }

    private Object get(String key, Object value, Class<?> clazz) {
        Bson query = eq(key, value);

        return ((Document) Database.guilds.find(query).first().get("info")).get(key, clazz);
    }

    private void update(String guildName, String key, EnumOperators EO, Object value) {

        // INSTANTLY UPDATES THE MONGODB SERVER //
        Database.guilds.updateOne(Filters.eq("info.guildName", guildName), new Document(EO.getUO(), new Document("info." + key, value)));
    }


    public boolean isOwner(UUID uuid, String guildName) {
        return get(guildName, "owner", uuid.toString(), String.class) != null;
    }

    public boolean isGuildNull(UUID uuid) {
        return getGuildOf(uuid) != null;
    }


    public String getGuildOf(UUID uuid) {
        return (String) DatabaseAPI.getInstance().getData(EnumData.GUILD, uuid);
    }


    public void promotePlayer(String guildName, UUID uuid) {
        if (getGuildOf(uuid) == null) return;

        modifyRank(guildName, uuid, true);
    }


    public void demotePlayer(String guildName, UUID uuid) {
        if (getGuildOf(uuid) == null) return;

        modifyRank(guildName, uuid, false);
    }


    private void modifyRank(String guildName, UUID uuid, boolean promote) {
        List<String> officers = (List<String>) get(guildName, "officers", ArrayList.class);


        if (!officers.contains(uuid.toString())) {
            if (promote) {
                //ADD TO OFFICERS
                update(guildName, "officers", EnumOperators.$PUSH, uuid.toString());
                // REMOVE FROM MEMBERS
                update(guildName, "members", EnumOperators.$PULL, uuid.toString());
            } else {
                //REMOVE FROM OFFICERS
                update(guildName, "officers", EnumOperators.$PULL, uuid.toString());
                // ADD TO MEMBERS
                update(guildName, "members", EnumOperators.$PUSH, uuid.toString());

            }
        }
    }

    public void setMotdOf(String guildName, String motd) {
        update(guildName, "motd", EnumOperators.$SET, motd);
    }


    public void kickFrom(UUID executor, String guildName, UUID uuid) {

    }


    public boolean isMember(UUID uuid, String guildName) {
        return false;
    }


    public boolean isOfficer(UUID uuid, String guildName) {
        return false;
    }


    public void saveAllGuilds() {

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


}
