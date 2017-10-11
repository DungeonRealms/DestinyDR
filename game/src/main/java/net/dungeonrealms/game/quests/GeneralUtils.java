package net.dungeonrealms.game.quests;

import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class GeneralUtils {

    public static Location jsonToLoc(JsonObject obj) {
        Location l = new Location(obj.has("world") ? Bukkit.getWorld(obj.get("world").getAsString()) : Bukkit.getWorlds().get(0), obj.get("x").getAsDouble(), obj.get("y").getAsDouble(), obj.get("z").getAsDouble());
        l.setPitch(obj.get("pitch").getAsFloat());
        l.setYaw(obj.get("yaw").getAsFloat());
        return l;
    }

    public static JsonObject locToJson(Location loc) {
        JsonObject obj = new JsonObject();
        if (loc.getWorld() != null)
            obj.addProperty("world", loc.getWorld().getName());

        obj.addProperty("x", loc.getX());
        obj.addProperty("y", loc.getY());
        obj.addProperty("z", loc.getZ());
        obj.addProperty("yaw", loc.getYaw());
        obj.addProperty("pitch", loc.getPitch());
        return obj;
    }
}
