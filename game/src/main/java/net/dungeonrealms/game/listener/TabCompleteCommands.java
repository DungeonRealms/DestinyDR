package net.dungeonrealms.game.listener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

/**
 * Created by Brad on 09/06/2016.
 */
public class TabCompleteCommands implements Listener {

    private static PacketListener listener;

    public void onEnable() {
        listener = new PacketAdapter(DungeonRealms.getInstance(), PacketType.Play.Client.TAB_COMPLETE) {
            public void onPacketReceiving(PacketEvent event) {
                if (event.getPacketType() == PacketType.Play.Client.TAB_COMPLETE) {
                    // Allow GMs/OPs/DEVs to access tab completion.
                    if (Rank.isGM(event.getPlayer())) return;

                    PacketContainer packet = event.getPacket();
                    String message = (packet.getSpecificModifier(String.class).read(0)).toLowerCase();
                    if ((message.startsWith("/") || message.startsWith("@"))) event.setCancelled(true);
                }
            }
        };

        ProtocolLibrary.getProtocolManager().addPacketListener(listener);
        Bukkit.getServer().getPluginManager().registerEvents(this, DungeonRealms.getInstance());
    }

    public void onDisable() {
        ProtocolLibrary.getProtocolManager().removePacketListener(listener);
        HandlerList.unregisterAll(this);
    }

}
