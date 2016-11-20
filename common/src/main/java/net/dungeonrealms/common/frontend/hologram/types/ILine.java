package net.dungeonrealms.common.frontend.hologram.types;

import net.minecraft.server.v1_9_R2.Packet;
import org.bukkit.Location;

/**
 * Created by Evoltr on 11/20/2016.
 */
public interface ILine {

    Packet[] getSpawnPackets(Location location);

    Packet[] getDespawnPackets();
}
