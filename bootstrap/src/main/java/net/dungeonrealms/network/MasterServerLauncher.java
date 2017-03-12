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
        Log.info("Master server initiated on Build " + Constants.BUILD_NUMBER);
        Log.info("Ready to sit back relax and relay packets");

        try {
            MasterServer server = new MasterServer();
            Log.info("Listening on " + Constants.MASTER_SERVER_IP + ":" + Constants.MASTER_SERVER_PORT);
            kryo = server.getKryo();
            
            server.registerListener(new MasterServerListener());

            Log.set(Log.LEVEL_INFO);
            registerClasses();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void registerClasses() {
        kryo.register(Packet.class, 0);
        kryo.register(byte.class, 1);
        kryo.register(byte[].class, 2);
        kryo.register(BasicMessagePacket.class, 3);
        kryo.register(ServerListPacket.class, 4);
        kryo.register(ShardInfo.class, 5);
        kryo.register(ServerAddress.class, 6);
        kryo.register(PlayerToken.class, 7);
        kryo.register(PlayerToken[].class, 8);
        kryo.register(UUID.class, 9);
        kryo.register(String.class, 10);
        kryo.register(int.class, 11);
    }

}
