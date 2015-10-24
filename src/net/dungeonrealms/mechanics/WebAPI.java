package net.dungeonrealms.mechanics;

import net.dungeonrealms.mastery.AsyncUtils;
import net.dungeonrealms.mastery.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Nick on 9/17/2015.
 */
public class WebAPI {

    public static volatile HashMap<String, Integer> ANNOUNCEMENTS = new HashMap<>();
    public static volatile ArrayList<String> JOIN_INFORMATION = new ArrayList<>();

    public static void fetchPrerequisites() {
        AsyncUtils.pool.submit(() -> {
            Utils.log.info("[WEB-API] [ASYNC] Loading... Prerequisites...");
            try {
                URL url = new URL("http://cherryio.com/backup/api/data.txt");
                BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                String line;
                while ((line = in.readLine()) != null) {
                    if (line.startsWith("A>")) {
                        ANNOUNCEMENTS.put(line.replace("A>", ""), Integer.valueOf(line.split(",")[1]));
                    } else if (line.startsWith("M>")) {
                        JOIN_INFORMATION.add(line.replace("M>", ""));
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
