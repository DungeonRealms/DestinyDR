package net.dungeonrealms.network.discord;

import com.google.gson.JsonObject;

/**
 * A list of DiscordChannels that have webhooks setup for.
 * 
 * Created March 11th, 2017.
 * @author Kneesnap
 */
public enum DiscordChannel {
	DEVELOPMENT("Dungeon Realms", "http://files.enjin.com/631183/portal/drlogo.png", "Wh3y8iKeY33M-2kWHdQx00I6HPaCqOB7VC3Pt5qy8wODIFTswIOZrTWnkbauQP57mWwT", "287277718482059264"),
	NOTIFICATIONS("Officer Johnson", "http://i.imgur.com/8MW7apB.png", "pdOLstfvB43aimMizGQfFjTXgmEkqNdy-IeaF2jmAWIlgwxT0M1a_h8ShIizM3GHeqjP", "287302942392844288"),
	STAFF_REPORTS("Ban Management", "http://files.enjin.com/631183/portal/drlogo.png", "L3_C9yNHuyOWWSNT1d_qokXt53ymftforgMwabLwK75dmiu8S0bYwZjnfQsYEuWJvfly", "290444027344584704");

	private final String username;
	private final String avatarURL;
	private final String tokenId;
	private final String channelId;
	
	DiscordChannel(String username, String avatarURL, String tokenId, String channelId) {
		this.tokenId = tokenId;
		this.channelId = channelId;
		this.username = username;
		this.avatarURL = avatarURL;
	}
	
	public String getURL() {
		return "https://discordapp.com/api/webhooks/" + this.channelId + "/" + this.tokenId;
	}
	
	public String getUsername() {
		return this.username;
	}
	
	public String getAvatarURL() {
		return this.avatarURL;
	}
	
	public JsonObject getPostData() {
		JsonObject obj = new JsonObject();
		if(getAvatarURL() != null)
			obj.addProperty("avatar_url", getAvatarURL());
		
		if(getUsername() != null)
			obj.addProperty("username", getUsername());
		return obj;
	}
}
