package net.dungeonrealms.mastery;

import com.google.common.collect.ImmutableList;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

/**
 * Created by Nick on 10/28/2015.
 */
public class NameFetcher implements Callable<String> {
    //private static final String PROFILE_URL = "https://sessionserver.mojang.com/session/minecraft/profile/";
    private static final String PROFILE_URL = "https://api.mojang.com/user/profiles/";
    private final JSONParser jsonParser = new JSONParser();
    private final List<UUID> uuids;

    public NameFetcher(List<UUID> uuids) {
        this.uuids = ImmutableList.copyOf(uuids);
    }

    @Override
    public String call() throws Exception {
        for (UUID uuid : uuids) {
            HttpURLConnection connection = (HttpURLConnection) new URL(PROFILE_URL + uuid.toString().replace("-", "") + "/names").openConnection();
            JSONArray response = (JSONArray) jsonParser.parse(new InputStreamReader(connection.getInputStream()));
            String name = response.get(0).toString().split("\"")[3];
            if (name == null) {
                continue;
            }
            return name;
        }
        return "BOB";
    }
}