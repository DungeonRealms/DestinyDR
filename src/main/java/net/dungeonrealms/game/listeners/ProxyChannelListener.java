package net.dungeonrealms.game.listeners;


import net.dungeonrealms.DungeonRealmsProxy;
import net.dungeonrealms.game.guild.GuildDatabaseAPI;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.UUID;

public class ProxyChannelListener implements Listener {

    private DungeonRealmsProxy plugin;

    private static ProxyChannelListener instance;

    public static ProxyChannelListener getInstance() {
        if (instance == null) {
            instance = new ProxyChannelListener(DungeonRealmsProxy.getInstance());
        }
        return instance;
    }

    public ProxyChannelListener(DungeonRealmsProxy plugin) {
        this.plugin = plugin;
        BungeeCord.getInstance().registerChannel("DungeonRealms");
    }

    @EventHandler
    public void onPluginMessageReceived(PluginMessageEvent event) {
        if (!event.getTag().equals("DungeonRealms"))
            return;

        DataInputStream in = new DataInputStream(new ByteArrayInputStream(event.getData()));

        try {
            String subChannel = in.readUTF();

            if (subChannel.equals("Guilds")) {
                String command = in.readUTF();

                switch (command) {
                    case "alert": {

                        String guildName = in.readUTF();
                        String message = in.readUTF();


                        for (UUID uuid : GuildDatabaseAPI.get().getAllOfGuild(guildName)) {
                            ProxiedPlayer player = plugin.getProxy().getPlayer(uuid);

                            if (player != null)
                                player.sendMessage(message);
                        }
                    }
                }
            }

        } catch (EOFException e) {
            // Do nothing.
        } catch (IOException e) {
            // This should never happen.
            e.printStackTrace();
        }
    }

}