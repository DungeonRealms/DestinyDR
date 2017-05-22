package net.dungeonrealms.network.discord;

import net.dungeonrealms.common.game.database.player.PlayerRank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.Arrays;

import com.google.gson.JsonObject;

/**
 * A list of DiscordChannels that have webhooks setup for.
 * 
 * Created March 11th, 2017.
 * @author Kneesnap
 */
@AllArgsConstructor @Getter
public enum DiscordChannel {
	DR_DISCUSSION("Dungeon Realms", "http://files.enjin.com/631183/portal/drlogo.png", "rnPUhz2zttpfkX0gvM0kNzgcH7KDfOcOCA0KrQg78hB6Z4nOXmhnukx0bcwqAo49I4nL", "290594073587351554", null),
	DEVELOPMENT("Neo", "http://i.imgur.com/KyGcQRt.png", "Wh3y8iKeY33M-2kWHdQx00I6HPaCqOB7VC3Pt5qy8wODIFTswIOZrTWnkbauQP57mWwT", "287277718482059264", PlayerRank.DEV),
	NOTIFICATIONS("Officer Johnson", "http://i.imgur.com/8MW7apB.png", "pdOLstfvB43aimMizGQfFjTXgmEkqNdy-IeaF2jmAWIlgwxT0M1a_h8ShIizM3GHeqjP", "287302942392844288", PlayerRank.GM),
	STAFF_REPORTS("Agent 47", "http://i.imgur.com/x8I5fWc.png", "L3_C9yNHuyOWWSNT1d_qokXt53ymftforgMwabLwK75dmiu8S0bYwZjnfQsYEuWJvfly", "290444027344584704", null);

	private final String username;
	private final String avatarURL;
	private final String tokenId;
	private final String channelId;
	private final PlayerRank channel;
	
	public String getURL() {
		return "https://discordapp.com/api/webhooks/" + this.channelId + "/" + this.tokenId;
	}
	
	public JsonObject getPostData() {
		JsonObject obj = new JsonObject();
		if(getAvatarURL() != null)
			obj.addProperty("avatar_url", getAvatarURL());
		
		if(getUsername() != null)
			obj.addProperty("username", getUsername());
		return obj;
	}
	
	public static DiscordChannel getByChannel(PlayerRank rankChannel) {
		return Arrays.stream(values()).filter(c -> c.getChannel() == rankChannel).findAny().orElse(null);
	}
}
