package net.dungeonrealms.mastery;

import com.sk89q.worldedit.internal.gson.JsonObject;
import com.sk89q.worldedit.internal.gson.JsonParser;
import org.apache.commons.io.Charsets;
import org.bukkit.craftbukkit.libs.joptsimple.internal.Strings;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

/**
 * Created by Nick on 11/30/2015.
 */
public class UUIDHelper {

    public static UUID getOfflineUUID(String name) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(Charsets.UTF_8));
    }

    public static UUID getOnlineUUID(String name) {
        if (!Strings.isNullOrEmpty(name)) {
            try {
                URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + name);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setUseCaches(false);
                connection.setDoInput(true);
                connection.setDoOutput(true);
                JsonObject profile = (JsonObject) new JsonParser().parse(new InputStreamReader(connection.getInputStream()));
                return UUID.fromString(fullUUID(profile.get("id").toString()));
            } catch (Exception e) {
            }
        }
        return null;
    }
    public static String uuidToName(String uuid) {
        if (!uuid.isEmpty()) {
            try {
                URL url = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + cleanUUID(uuid));
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setUseCaches(false);
                connection.setDoInput(true);
                connection.setDoOutput(true);
                InputStreamReader isr = new InputStreamReader(connection.getInputStream());
                JsonObject profile = (JsonObject) new JsonParser().parse(isr);
                return profile.get("name").toString().replace('"', '\000').trim();
            } catch (Exception e) {
            }
        }
        return "";
    }

    public static UUID stringToUUID(String uuid) {
        try {
            return UUID.fromString(uuid);
        } catch (Exception e) {
        }
        return null;
    }

    public static String fullUUID(String uuid) {
        uuid = cleanUUID(uuid);
        uuid = uuid.substring(0, 8) + "-" + uuid.substring(8, 12) + "-" + uuid.substring(12, 16) + "-" + uuid.substring(16, 20) + "-" + uuid.substring(20, 32);
        return uuid;
    }

    public static String cleanUUID(String uuid) {
        return uuid.replaceAll("-", "");
    }
}