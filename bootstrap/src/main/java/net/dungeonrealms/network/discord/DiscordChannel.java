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
	DR_DISCUSSION("Dungeon Realms", "http://files.enjin.com/631183/portal/drlogo.png", "gavZCzsbod42Y08vvhEd7LNiUFFSAHAXX92bzIoycnvVDK2LQqSFl_gE-z-2kwoQE0y9", "376849765205278732", null),
	DEVELOPMENT("Neo", "http://i.imgur.com/KyGcQRt.png", "vGxxMGAC_h7XvxU54sKoseJNxGVxgUBhrNCJ9B0mq-J-pD7s3dR2HWH7A8vCyuUUGG2i", "376849120461324298", PlayerRank.DEV),
	NOTIFICATIONS("Officer Johnson", "http://i.imgur.com/8MW7apB.png", "4eS4fHzYFqB7riHQeOmASDazQigrm9e_JB5p6Eg6Bt5EzMkaCZF4cnHCdo4l7H6v96pe", "376849631222562820", PlayerRank.GM),
	STAFF_REPORTS("Agent 47", "http://i.imgur.com/x8I5fWc.png", "4ZUlK7P1w-pUnbOIftR4yZW1vgRvY4QRtX3vfK-WVR8CITxvpOfwehrz2QCZQtCZl8cw", "376849708032720897", null);

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
