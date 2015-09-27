package net.dungeonrealms.anticheat;

import net.dungeonrealms.mastery.AsyncUtils;
import net.dungeonrealms.mastery.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by Nick on 9/25/2015.
 */
public class AntiProxy {

    static AntiProxy instance = null;

    public static AntiProxy getInstance() {
        if (instance == null) {
            instance = new AntiProxy();
        }
        return instance;
    }


    /**
     * Because Kayaba and Red are going to be such bitches.
     * ANd we all know that negros like them will follow
     * the path of niggerish nigger. We'll build in a automagically
     * proxyifier detector to detect dem negro from 2 farm fields
     * away.
     *
     * @param uuid
     * @param ip
     * @return
     * @since 1.0
     */
    public boolean isProxying(UUID uuid, InetAddress ip) {

        Future<Boolean> isProxy = AsyncUtils.pool.submit(() -> {
            Utils.log.info("[ANTI-PROXY] [ASYNC] Checking player " + uuid.toString() + " w/ ip " + ip.toString().replace("/", ""));
            try {

                URL url = new URL("http://www.shroomery.org/ythan/proxycheck.php?ip=" + ip.toString().replace("/", ""));
                URLConnection connection = url.openConnection();
                connection.connect();

                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine = in.readLine();
                in.close();
                if (inputLine == null) return false;
                switch (inputLine) {
                    case "Y":
                        return true;
                    case "N":
                        return false;
                    case "X":
                        Utils.log.warning("Unable to check ifIs Proxy for user " + uuid);
                        return false;
                    default:
                        Utils.log.warning("DEFAULT FIRED SWITCH() " + uuid);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        });

        try {
            return isProxy.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return false;
    }

}
