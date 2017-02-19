package net.dungeonrealms.game.quests;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import com.google.gson.JsonObject;

public class GeneralUtils {
	
	public static Location jsonToLoc(JsonObject obj){
		Location l = new Location(Bukkit.getWorlds().get(0), obj.get("x").getAsDouble(), obj.get("y").getAsDouble(), obj.get("z").getAsDouble());
		l.setPitch(obj.get("pitch").getAsFloat());
		l.setYaw(obj.get("yaw").getAsFloat());
		return l;
	}
	
	public static JsonObject locToJson(Location loc){
		JsonObject obj = new JsonObject();
		obj.addProperty("x", loc.getX());
		obj.addProperty("y", loc.getY());
		obj.addProperty("z", loc.getZ());
		obj.addProperty("yaw", loc.getYaw());
		obj.addProperty("pitch", loc.getPitch());
		return obj;
	}
}
