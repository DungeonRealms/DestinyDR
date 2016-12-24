package net.dungeonrealms.network;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.minlog.Log;
import net.dungeonrealms.common.Constants;
import net.dungeonrealms.common.game.database.player.PlayerToken;
import net.dungeonrealms.common.network.ServerAddress;
import net.dungeonrealms.common.network.ShardInfo;
import net.dungeonrealms.network.packet.Packet;
import net.dungeonrealms.network.packet.type.BasicMessagePacket;
import net.dungeonrealms.network.packet.type.ServerListPacket;

import java.io.IOException;
import java.util.UUID;

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

public class MasterServerLauncher {

    private static Kryo kryo;


    public static void main(String[] args) {

        Constants.build();

        Log.info("");
        Log.info("Master server initiated on " + Constants.BUILD_VERSION + " Build " + Constants.BUILD_NUMBER);
        Log.info("Alright. let's do this boys...");
        Log.info("Ready to sit back relax and relay packets");

        try {
            MasterServer server = new MasterServer();
            Log.info("Listening on " + Constants.MASTER_SERVER_IP + ":" + Constants.MASTER_SERVER_PORT);
            kryo = server.getKryo();

            Log.set(Log.LEVEL_INFO);
            registerClasses();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void registerClasses() {
        kryo.register(Packet.class);
        kryo.register(byte.class);
        kryo.register(byte[].class);
        kryo.register(BasicMessagePacket.class);
        kryo.register(ServerListPacket.class);
        kryo.register(ShardInfo.class);
        kryo.register(ServerAddress.class);
        kryo.register(PlayerToken.class);
        kryo.register(PlayerToken[].class);
        kryo.register(UUID.class);
        kryo.register(String.class);
        kryo.register(int.class);
    }

}
