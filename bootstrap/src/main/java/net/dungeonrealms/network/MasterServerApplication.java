package net.dungeonrealms.network;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.minlog.Log;
import net.dungeonrealms.Constants;
import net.dungeonrealms.network.packet.Packet;
import net.dungeonrealms.network.packet.type.BasicMessagePacket;

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

    private static Kryo kryo;


    public static void main(String[] args) {


        Log.info("");
        Log.info("Master server initiated on " + Constants.BUILD_VERSION + " Build " + Constants.BUILD_NUMBER);
        Log.info("Alright. let's do this boys...");
        Log.info("Ready to sit back relax and relay packets");

        try {
            MasterServer server = new MasterServer();
            Log.info("Listening on " + Constants.MASTER_SERVER_IP + ":" + Constants.MASTER_SERVER_PORT);
            kryo = server.getKryo();

            Log.set(Log.LEVEL_TRACE);
            registerClasses();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void registerClasses() {
        kryo.register(Packet.class);
        kryo.register(BasicMessagePacket.class);
    }

}
