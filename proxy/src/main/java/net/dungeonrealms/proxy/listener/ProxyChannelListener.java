package net.dungeonrealms.proxy.listener;

import net.dungeonrealms.proxy.DungeonRealmsProxy;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;

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
    }

    @EventHandler
    public void onPluginMessageReceived(PluginMessageEvent event) {
        if (!event.getTag().equals("BungeeCord"))
            return;

        DataInputStream in = new DataInputStream(new ByteArrayInputStream(event.getData()));

        try {
            String subChannel = in.readUTF();

            if (subChannel.equals("Alert")) {
                // READ MESSAGE IN STREAM //
                String message = in.readUTF();

                plugin.getProxy().getPlayers().stream().forEach(player -> player.sendMessage(message));
            }
        } catch (EOFException e) {
            // Do nothing.
        } catch (IOException e) {
            // This should never happen.
            e.printStackTrace();
        }
    }

}