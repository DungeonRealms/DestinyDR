package net.dungeonrealms.network;

import com.esotericsoftware.minlog.Log;

import java.io.IOException;

/**
 * This master server network is a cross communication
 * platform for the DungeonRealm servers.
 * <p>
 * The platform is using the Kryonet library
 * check out https://github.com/EsotericSoftware/kryonet for
 * documentation.
 * <p>
 * Class written by APOLLOSOFTWARE.IO on 7/7/2016
 */

public class MasterServerApplication {

    private static KryonetServer server;


    public static void main(String[] args) {
        Log.info(" ");
        Log.info("[Launching master server application thread] ...");
        Log.info(" ");

        try {
            server = new KryonetServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
