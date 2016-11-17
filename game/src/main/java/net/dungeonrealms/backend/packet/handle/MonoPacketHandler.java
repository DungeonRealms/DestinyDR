package net.dungeonrealms.backend.packet.handle;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import net.dungeonrealms.common.awt.handler.SuperHandler;
import net.dungeonrealms.network.packet.type.MonoPacket;
import net.dungeonrealms.vgame.Game;
import org.bukkit.ChatColor;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by Giovanni on 12-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class MonoPacketHandler extends Listener implements SuperHandler.Handler
{
    @Override
    public void prepare()
    {
        Game.getGame().getGameShard().getGameClient().registerListener(this);
    }

    @Override
    public void received(Connection connection, Object object)
    {
        if (object instanceof MonoPacket)
        {
            MonoPacket monoPacket = (MonoPacket) object;
            try (DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(monoPacket.getData())))
            {
                if (dataInputStream.readUTF().equals("SEND_WEAPON"))
                {
                    UUID item = UUID.fromString(dataInputStream.readUTF());
                    Game.getGame().getRegistryHandler().getWeaponRegistry().receive(item);
                }
                if (dataInputStream.readUTF().equals("SEND_ARMOR"))
                {

                }
            } catch (IOException e)
            {
                Game.getGame().getInstanceLogger().sendMessage(ChatColor.RED + "Mono Packet received, failed to read");
            }
        }
    }
}
