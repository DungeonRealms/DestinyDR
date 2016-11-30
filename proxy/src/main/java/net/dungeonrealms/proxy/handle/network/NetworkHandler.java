package net.dungeonrealms.proxy.handle.network;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import net.dungeonrealms.common.awt.handler.BungeeHandler;
import net.dungeonrealms.network.packet.type.BasicMessagePacket;
import net.dungeonrealms.proxy.DungeonBungee;
import net.dungeonrealms.proxy.netty.shard.ShardBalancer;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by Giovanni on 1-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class NetworkHandler extends Listener implements BungeeHandler {
    @Override
    public void prepare() {
        DungeonBungee.getDungeonBungee().getNetworkProxy().getGameClient().registerListener(this);
    }

    @Override
    public void received(Connection connection, Object object) {
        if (object instanceof BasicMessagePacket) {
            BasicMessagePacket packet = (BasicMessagePacket) object;

            try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(packet.data))) {
                String task = in.readUTF();

                if (task.equals("MoveSessionToken")) // Move that lad
                {
                    // Call the shard balancer & move them to a new session
                    new ShardBalancer(UUID.fromString(in.readUTF()), false, Boolean.valueOf(in.readUTF()), true).handle();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
