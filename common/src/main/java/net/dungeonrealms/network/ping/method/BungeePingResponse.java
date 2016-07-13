package net.dungeonrealms.network.ping.method;


import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.dungeonrealms.network.PingResponse;

public class BungeePingResponse implements PingResponse {
    private boolean isOnline;
    private String motd;
    private int onlinePlayers;
    private int maxPlayers;

    public BungeePingResponse(boolean isOnline, String motd, int onlinePlayers, int maxPlayers) {
        this.isOnline = isOnline;
        this.motd = motd;
        this.onlinePlayers = onlinePlayers;
        this.maxPlayers = maxPlayers;
    }

    public BungeePingResponse(String jsonString) {

        if (jsonString == null || jsonString.isEmpty()) {
            motd = "Invalid ping response";
            return;
        }

        Object jsonObject = new JsonParser().parse(jsonString);

        if (!(jsonObject instanceof JsonObject)) {
            motd = "Invalid ping response";

            return;
        }

        JsonObject json = (JsonObject) jsonObject;
        isOnline = true;

        Object descriptionObject = json.get("description");

        if (descriptionObject != null) {
            motd = descriptionObject.toString();
        } else {
            motd = "Invalid ping response";
        }

        Object playersObject = json.get("players");

        if (playersObject instanceof JsonObject) {
            JsonObject playersJson = (JsonObject) playersObject;

            Object onlineObject = playersJson.get("online");
            if (onlineObject instanceof Number) {
                onlinePlayers = ((Number) onlineObject).intValue();
            }

            Object maxObject = playersJson.get("max");
            if (maxObject instanceof Number) {
                maxPlayers = ((Number) maxObject).intValue();
            }
        }
    }

    public boolean isOnline() {
        return isOnline;
    }

    public String getMotd() {
        return motd;
    }

    public int getOnlinePlayers() {
        return onlinePlayers;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

}
