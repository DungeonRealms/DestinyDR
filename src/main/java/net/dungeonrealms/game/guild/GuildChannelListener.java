package net.dungeonrealms.game.guild;

import net.dungeonrealms.DungeonRealms;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;

public class GuildChannelListener implements PluginMessageListener {

    private static GuildChannelListener instance;

    public static GuildChannelListener getInstance() {
        if (instance == null) {
            instance = new GuildChannelListener(DungeonRealms.getInstance());
        }
        return instance;
    }

    public GuildChannelListener(DungeonRealms plugin) {
        Bukkit.getMessenger().registerOutgoingPluginChannel(plugin, "DungeonRealms");
        Bukkit.getMessenger().registerIncomingPluginChannel(plugin, "DungeonRealms", this);
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {

        if (!channel.equals("DungeonRealms")) return;

        DataInputStream in = new DataInputStream(new ByteArrayInputStream(message));

        try {
            String subChannel = in.readUTF();

            if (subChannel.equals("Guilds")) {

                String command = in.readUTF();


                switch (command) {
                    //TODO
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