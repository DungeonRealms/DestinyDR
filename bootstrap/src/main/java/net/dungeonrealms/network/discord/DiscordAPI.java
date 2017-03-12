package net.dungeonrealms.network.discord;

import java.io.OutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
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
		System.out.println(url);
		System.out.println(postData);
		new Thread(() -> {
			try {
        		HttpsURLConnection con = (HttpsURLConnection) new URL(url).openConnection();
        		con.setReadTimeout(5000);
        		con.setRequestMethod("POST");
        		con.setRequestProperty("Content-Type", "application/json");
        		con.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; U; Linux i686) Gecko/20071127 Firefox/2.0.0.11");
        		con.setDoOutput(true);
        		con.setDoInput(true);
        		OutputStream out = con.getOutputStream();
        		out.write(postData.getBytes());
        		out.flush();
        		out.close();
        		System.out.println("Status Code = " + con.getResponseCode());
        		
        		BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
        		StringBuilder result = new StringBuilder();
        		String line;
        		while((line = reader.readLine()) != null)
        		    result.append(line);
        		
        		System.out.println(result.toString());
        	} catch (Exception e) {
            	e.printStackTrace();
        	}
		}).start();
	}
}
