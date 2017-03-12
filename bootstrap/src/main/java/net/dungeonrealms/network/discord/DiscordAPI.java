package net.dungeonrealms.network.discord;

import java.io.OutputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import com.google.gson.JsonObject;

/**
 * Discord API - Perform actions on the DR discord server.
 * @author Kneesnap
 */
public class DiscordAPI {
	
	/**
	 * Sends a message to the specified discord channel.
	 * @param channel
	 * @param message
	 */
	public static void sendMessage(DiscordChannel channel, String message) {
		JsonObject postData = channel.getPostData();
		postData.addProperty("content", message);
		sendRequest(channel.getURL(), postData.toString());
	}
	
	private static void sendRequest(String url, String postData) {
		new Thread(() -> {
			try {
        		HttpsURLConnection con = (HttpsURLConnection) new URL(url).openConnection();
        		con.setReadTimeout(5000);
        		con.setRequestMethod("POST");
        		con.setRequestProperty("Content-Type", "application/json");
        		con.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; U; Linux i686) Gecko/20071127 Firefox/2.0.0.11");
        		con.setDoOutput(true);
        		OutputStream out = con.getOutputStream();
        		out.write(postData.getBytes());
        		out.flush();
        		out.close();
        	} catch (Exception e) {
            	e.printStackTrace();
        	}
		}).start();
	}
}
