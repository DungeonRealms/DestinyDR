package net.dungeonrealms.proxy.handle.channel;

import net.dungeonrealms.common.awt.handler.old.BungeeHandler;
import net.dungeonrealms.proxy.DungeonBungee;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.event.EventHandler;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

/**
 * Created by Giovanni on 1-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class ChannelHandler implements BungeeHandler.ListeningHandler {
    @Override
    public void prepare() {
        DungeonBungee.getDungeonBungee().getProxy().getPluginManager().registerListener(DungeonBungee.getDungeonBungee(), this);
    }

    @EventHandler
    public void onPluginMessageReceived(PluginMessageEvent event) {
        if (event.getTag().equals("BungeeCord")) {
            try (DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(event.getData()))) {
                if (dataInputStream.readUTF().equals("Alert")) {
                    String message = dataInputStream.readUTF();

                    DungeonBungee.getDungeonBungee().getProxy().getPlayers().stream().forEach(player -> {
                        player.sendMessage(message);
                    });
                }
            } catch (Exception e) {
                // Well damn, how did this happen?
            }
        }
    }
}
