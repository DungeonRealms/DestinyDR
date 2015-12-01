package net.dungeonrealms.game.mechanics;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import net.dungeonrealms.API;
import net.minecraft.server.v1_8_R3.PacketPlayOutNamedSoundEffect;

/**
 * Created by Nick on 9/4/2015.
 */
public class SoundAPI {

    static SoundAPI instance = null;

    public static SoundAPI getInstance() {
        if (instance == null) {
            instance = new SoundAPI();
        }
        return instance;
    }

    /**
     * Sends a specific player reliable sound, w/o shit bukkit.
     *
     * @param player
     */
    public void playSound(String soundName, Player player) {
        Location TEMP_LOCATION = player.getLocation();
        PacketPlayOutNamedSoundEffect packet = new PacketPlayOutNamedSoundEffect(soundName, TEMP_LOCATION.getX(), TEMP_LOCATION.getY(), TEMP_LOCATION.getZ(), 1f, 64f);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }

    /**
     * Sends sound to players in a radius
     *
     * @param soundName
     * @param location
     * @param radius
     */
    public void playSoundAtLocation(String soundName, Location location, int radius) {
        for (Player player : API.getNearbyPlayers(location, radius)) {
            PacketPlayOutNamedSoundEffect packet = new PacketPlayOutNamedSoundEffect(soundName, location.getX(), location.getY(), location.getZ(), 1f, 64f);
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
        }
    }

}
