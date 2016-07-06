package net.dungeonrealms.game.listeners;


import net.dungeonrealms.DungeonRealmsProxy;
import net.dungeonrealms.game.handlers.FriendHandler;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.Arrays;
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


            // RELAY PACKET //
            if (subChannel.equals("Update")
                //|| subChannel.equals("Ping") || subChannel.equals("Pinged")
                    ) {
                plugin.relayPacket("DungeonRealms", event.getData());
            }

            if (subChannel.equals("Alert")) {
                // READ MESSAGE IN STREAM //
                String message = in.readUTF();

                plugin.getProxy().getPlayers().stream().forEach(player -> player.sendMessage(message));
            }

            if (subChannel.equals("Guilds")) {
                String command = in.readUTF();

                if (command.contains("message:")) {
                    String[] commandArray = command.split(":");
                    String[] filter = Arrays.copyOfRange(commandArray, 1, commandArray.length);

                    String guildName = in.readUTF();
                    String message = in.readUTF();

                    plugin.sendMessageToGuild(guildName, message, filter);
                    return;
                }

                switch (command) {
                    case "message": {
                        String guildName = in.readUTF();
                        String message = in.readUTF();

                        plugin.sendMessageToGuild(guildName, message);
                        break;
                    }
                }
            }
            if (subChannel.equals("Friends")) {
                Bukkit.broadcastMessage("FRIENDS PACKET");
                String msg = in.readUTF();
                if (msg.contains("join:")) {
                    String[] content = msg.split(",");
                    String uuid = content[1];
                    String name = content[2];
                    String shard = content[3];
                    plugin.notifyFriends(UUID.fromString(uuid), name, shard);
                }

                return;
            }


        } catch (EOFException e) {
            // Do nothing.
        } catch (IOException e) {
            // This should never happen.
            e.printStackTrace();
        }
    }

}