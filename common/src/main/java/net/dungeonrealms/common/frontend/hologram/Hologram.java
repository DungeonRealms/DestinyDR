package net.dungeonrealms.common.frontend.hologram;

import net.dungeonrealms.common.frontend.hologram.types.ILine;
import net.dungeonrealms.common.frontend.hologram.types.TextLine;
import net.dungeonrealms.common.frontend.utils.UtilEntity;
import net.minecraft.server.v1_9_R2.Packet;
import net.minecraft.server.v1_9_R2.PlayerConnection;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Evoltr on 11/20/2016.
 */
public class Hologram {

    private Location location;
    private List<ILine> lines = new ArrayList<>();
    private List<Player> loadedPlayers = new ArrayList<>();

    public Hologram(Location location) {
        this.location = location;
    }

    public void addTextLine(String text) {
        lines.add(new TextLine(text, UtilEntity.getRandomEntityID()));
    }

    public void createHologramForPlayer(Player player) {
        HologramManager.getInstance().addPlayerToHologram(player, this);
        checkSending(player);
    }

    public void removeHologramForPlayer(Player player) {
        HologramManager.getInstance().removePlayerHologram(player, this);
        removeFromPlayer(player);
    }

    public void checkSending(Player player) {
        if (isInRange(player)) {
            sendToPlayer(player);
        } else {
            removeFromPlayer(player);
        }
    }

    private void sendToPlayer(Player player) {
        if (isPlayerLoaded(player)) return;
        loadedPlayers.add(player);
        double locX = location.getX();
        double locY = location.getY();
        double locZ = location.getZ();
        PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().playerConnection;
        for (ILine line : lines) {
            Location spawnLocation = new Location(location.getWorld(), locX, locY, locZ);
            for (Packet packet : line.getSpawnPackets(spawnLocation)) {
                playerConnection.sendPacket(packet);
            }
            locY -= 0.3D;

        }
    }

    private void removeFromPlayer(Player player) {
        if (!isPlayerLoaded(player)) return;
        for (ILine line : lines) {
            PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().playerConnection;
            for (Packet packet : line.getDespawnPackets()) {
                playerConnection.sendPacket(packet);
            }
        }
        loadedPlayers.remove(player);
    }

    public boolean isPlayerLoaded(Player player) {
        return loadedPlayers.contains(player);
    }

    public boolean isInRange(Player player) {
        return location.getWorld() == player.getLocation().getWorld() && (location.distance(player.getLocation()) <= 48D);
    }

    public Location getLocation() {
        return location;
    }

    public List<ILine> getLines() {
        return lines;
    }

    public List<Player> getLoadedPlayers() {
        return loadedPlayers;
    }
}
