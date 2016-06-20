package net.dungeonrealms.game.network.bungeecord.serverpinger.response;


import net.dungeonrealms.game.network.bungeecord.serverpinger.PingResponse;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class SpigotPingResponse implements PingResponse {
    private boolean isOnline;
    private String motd;
    private int onlinePlayers;
    private int maxPlayers;

    public SpigotPingResponse(boolean isOnline, String motd, int onlinePlayers, int maxPlayers) {
        this.isOnline = isOnline;
        this.motd = motd;
        this.onlinePlayers = onlinePlayers;
        this.maxPlayers = maxPlayers;
    }

    public SpigotPingResponse(String jsonString) {

        if (jsonString == null || jsonString.isEmpty()) {
            motd = "Invalid ping response";
            return;
        }

        Object jsonObject = JSONValue.parse(jsonString);

        if (!(jsonObject instanceof JSONObject)) {
            motd = "Invalid ping response";

            return;
        }

        JSONObject json = (JSONObject) jsonObject;
        isOnline = true;

        Object descriptionObject = json.get("description");

        if (descriptionObject != null) {
            motd = descriptionObject.toString();
        } else {
            motd = "Invalid ping response";
        }

        Object playersObject = json.get("players");

        if (playersObject instanceof JSONObject) {
            JSONObject playersJson = (JSONObject) playersObject;

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
