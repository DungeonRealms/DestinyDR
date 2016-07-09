package net.dungeonrealms.game.guild;

import net.dungeonrealms.DungeonRealms;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.Arrays;

public class GuildChannelListener implements PluginMessageListener {

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

                if (command.contains("message:")) {
                    String[] commandArray = command.split(":");
                    String[] filter = Arrays.copyOfRange(commandArray, 1, commandArray.length);

                    String guildName = in.readUTF();
                    String msg = in.readUTF();

                    GuildMechanics.getInstance().sendMessageToGuild(guildName, msg, filter);
                    return;
                }

                switch (command) {
                    case "message": {
                        String guildName = in.readUTF();
                        String msg = in.readUTF();

                        GuildMechanics.getInstance().sendMessageToGuild(guildName, msg);
                        break;
                    }

                    case "update": {
                        String guildName = in.readUTF();

                        if (GuildDatabaseAPI.get().isGuildCached(guildName))
                            GuildDatabaseAPI.get().updateCache(guildName);
                        break;
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