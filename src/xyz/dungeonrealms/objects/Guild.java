package xyz.dungeonrealms.objects;

import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by Nick on 12/10/2015.
 */
public final class Guild {

    HashMap<String, JSONObject> guilds = new HashMap<>();

    /**
     * Returns the JSONObject of guild.
     *
     * @param owner the owners uuid
     * @param name  the name of the guild
     * @return
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
        return temp;
    }

}
