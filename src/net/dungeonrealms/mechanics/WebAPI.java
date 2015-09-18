package net.dungeonrealms.mechanics;

import net.dungeonrealms.mastery.AsyncUtils;
import net.dungeonrealms.mastery.Utils;
import net.md_5.bungee.api.ChatColor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

/**
 * Created by Nick on 9/17/2015.
 */
public class WebAPI {

    public static volatile HashMap<String, Integer> ANNOUNCEMENTS = new HashMap<>();

    public static void fetchPrerequisites() {
        Utils.log.info("DungeonRealms [WEB-API] Loading... Prerequisites...");
        if (ANNOUNCEMENTS.size() > 0) {
            ANNOUNCEMENTS.clear();
        }
        AsyncUtils.pool.submit(() -> {
            try {
                URL url = new URL("http://dungeonrealms.com/api/announcements.txt");
                BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                String line;
                while ((line = in.readLine()) != null) {
                    if (line.startsWith(">")) {
                        ANNOUNCEMENTS.put(ChatColor.translateAlternateColorCodes('&', line), Integer.valueOf(line.split(",")[1]));
                    }
                }
            } catch (MalformedURLException e) {
                Utils.log.warning("MalformedURL WebAPI(WebAPI.class)");
            } catch (IOException e) {
                Utils.log.warning("IO Exception");
            }
        });
    }

}
