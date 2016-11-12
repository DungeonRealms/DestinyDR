package net.dungeonrealms.network;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.minlog.Log;
import lombok.Getter;
import net.dungeonrealms.common.Constants;
import net.dungeonrealms.common.game.database.player.PlayerToken;
import net.dungeonrealms.common.network.ServerAddress;
import net.dungeonrealms.common.network.ShardInfo;
import net.dungeonrealms.network.packet.Packet;
import net.dungeonrealms.network.packet.type.BasicMessagePacket;
import net.dungeonrealms.network.packet.type.MonoPacket;
import net.dungeonrealms.network.packet.type.ServerListPacket;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by Giovanni on 12-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class Boostrap
{
    @Getter
    private static Kryo kryonet;

    @Getter
    private static boolean canRelay = false;

    public static void main(String[] par)
    {
        for (String string : new String[]{
                "",
                "------ MASTER SERVER ------",
                "Attempting to connect to " + Constants.MASTER_SERVER_IP + ":" + Constants.MASTER_SERVER_PORT + ".."})
        {
            Log.info(string);
        }

        try
        {
            MasterServer masterServer = new MasterServer();
            kryonet = masterServer.getKryo();
            register();
            for (String string : new String[]{
                    "Connection established with " + Constants.MASTER_SERVER_IP + ":" + Constants.MASTER_SERVER_PORT,
                    "", "Developed by Vawke", "www.dungeonrealms.net"})
            {
                Log.info(string);
            }
            canRelay = true;
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private static void register()
    {
        kryonet.register(Packet.class);
        kryonet.register(byte.class);
        kryonet.register(byte[].class);
        kryonet.register(BasicMessagePacket.class);
        kryonet.register(MonoPacket.class);
        kryonet.register(ServerListPacket.class);
        kryonet.register(ShardInfo.class);
        kryonet.register(ServerAddress.class);
        kryonet.register(PlayerToken.class);
        kryonet.register(PlayerToken[].class);
        kryonet.register(UUID.class);
        kryonet.register(String.class);
        kryonet.register(int.class);
    }
}
