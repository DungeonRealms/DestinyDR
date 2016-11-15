package net.dungeonrealms.backend.bungee;

import net.dungeonrealms.common.awt.SuperHandler;
import net.dungeonrealms.common.old.game.database.DatabaseAPI;
import net.dungeonrealms.common.old.game.database.data.EnumData;
import net.dungeonrealms.common.old.game.database.data.EnumOperators;
import net.dungeonrealms.common.old.game.punishment.PunishAPI;
import net.dungeonrealms.common.old.network.bungeecord.BungeeServerInfo;
import net.dungeonrealms.common.old.network.bungeecord.BungeeServerTracker;
import net.dungeonrealms.vgame.Game;
import org.bson.Document;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by Giovanni on 13-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class BungeeHandler implements PluginMessageListener, SuperHandler.Handler
{
    @Override
    public void prepare()
    {
        Game.getGame().getServer().getMessenger().registerOutgoingPluginChannel(Game.getGame(), "BungeeCord");
        Game.getGame().getServer().getMessenger().registerIncomingPluginChannel(Game.getGame(), "BungeeCord", this);
        BungeeServerTracker.startTask(3L);
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message)
    {
        if (!channel.equalsIgnoreCase("BungeeCord")) return;

        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(message)))
        {
            String subChannel = in.readUTF();
            try
            {
                if (subChannel.equals("IP"))
                {
                    String address = in.readUTF();

                    DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.IP_ADDRESS, address, true);
                    DatabaseAPI.getInstance().retrieveDocumentFromAddress(address, existingDoc ->
                    {
                        if (existingDoc != null)
                        {
                            UUID uuid = UUID.fromString(((Document) existingDoc.get("info")).get("uuid", String.class));

                            if (PunishAPI.getInstance().isBanned(uuid))
                                PunishAPI.getInstance().ban(player.getUniqueId(), player.getName(), Game.getGame().getGameShard().getShardInfo().getShardID(), -1, "Ban evading", null);
                        }
                    });
                    return;
                }

                if (subChannel.equals("PlayerCount"))
                {
                    String server = in.readUTF();

                    if (in.available() > 0)
                    {
                        int online = in.readInt();

                        BungeeServerInfo serverInfo = BungeeServerTracker.getOrCreateServerInfo(server);
                        serverInfo.setOnlinePlayers(online);
                    }
                }

            } catch (Exception e)
            {
                // This should never happen.
                e.printStackTrace();
            }
        } catch (IOException e)

        {
            e.printStackTrace();
        }
    }
}
