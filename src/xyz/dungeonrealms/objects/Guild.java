package xyz.dungeonrealms.objects;

import org.bukkit.entity.Player;
import org.json.simple.JSONObject;
import xyz.dungeonrealms.apis.Database;
import xyz.dungeonrealms.utilities.UUIDFetcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Created by Nick on 12/10/2015.
 */
@SuppressWarnings("unchecked")
public final class Guild {

    static HashMap<String, JSONObject> guilds = new HashMap<>();

    /**
     * Returns the JSONObject of guild.
     *
     * @param owner the owners uuid
     * @param name  the name of the guild
     * @return JsonObect of new Guild.
     */
    public JSONObject getNewGuildJson(UUID owner, String name) {
        JSONObject temp = new JSONObject();
        temp.put("name", name);
        temp.put("uuid", owner.toString());
        temp.put("co_owner", "");
        temp.put("officers", new ArrayList<String>());
        temp.put("members", new ArrayList<String>());
        temp.put("level", 1);
        temp.put("experience", 0);

        temp.put("allies", new ArrayList<String>());
        temp.put("enemies", new ArrayList<String>());

        temp.put("pendingAllies", new ArrayList<String>());
        return temp;
    }

    /**
     * @param guildName the guild name.
     * @return The name of owner.
     */
    public static String getOwner(String guildName) {
        JSONObject jsonObject = guilds.get(guildName);
        return UUIDFetcher.getName(UUID.fromString(jsonObject.get("owner").toString()));
    }

    /**
     * @param guildName the guild name.
     * @return The name of coOwner.
     */
    public static String getCoOwner(String guildName) {
        JSONObject jsonObject = guilds.get(guildName);
        return UUIDFetcher.getName(UUID.fromString(jsonObject.get("co_owner").toString()));
    }

    /**
     * Returns a boolean if a guild has a cowner.
     *
     * @param guildName the guild name.
     * @return returns boolean
     */
    public static boolean hasCoOwner(String guildName) {
        JSONObject jsonObject = guilds.get(guildName);
        return !jsonObject.get("co_owner").toString().isEmpty();
    }

    /**
     * Returns pendingAlly request List String
     *
     * @param guildName the guild name.
     * @return ListPendingAllies
     */
    public static List<String> getPendingAlliesOf(String guildName) {
        JSONObject jsonObject = guilds.get(guildName);
        return (List<String>) jsonObject.get("pendingAllies");
    }

    /**
     * Returns Officers List String
     *
     * @param guildName the guild name.
     * @return ListUUIDs
     */
    public static List<String> getOfficersOf(String guildName) {
        JSONObject jsonObject = guilds.get(guildName);
        return (List<String>) jsonObject.get("officers");
    }

    /**
     * Returns Members List String
     *
     * @param guildName the guild name.
     * @return ListUUIDs
     */
    public static List<String> getMembersOf(String guildName) {
        JSONObject jsonObject = guilds.get(guildName);
        return (List<String>) jsonObject.get("members");
    }

    /**
     * Checks if a player is in a guild.
     *
     * @param player player
     * @return boolean
     */
    public static boolean isInGuild(Player player) {
        JSONObject jsonObject = Database.getInstance().players.get(player.getUniqueId());
        return !jsonObject.get("guild").toString().isEmpty();
    }

    /**
     * Returns the players guild String name.
     *
     * @param player the player
     * @return GuildName
     */
    public static String getPlayerGuild(Player player) {
        JSONObject jsonObject = Database.getInstance().players.get(player.getUniqueId());
        return jsonObject.get("guild").toString();
    }

}
