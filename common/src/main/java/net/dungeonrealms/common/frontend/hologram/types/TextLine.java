package net.dungeonrealms.common.frontend.hologram.types;

import net.dungeonrealms.common.frontend.utils.UtilEntity;
import net.minecraft.server.v1_9_R2.Packet;
import org.bukkit.Location;

/**
 * Created by Evoltr on 11/20/2016.
 */
public class TextLine implements ILine {
    private String text;
    private int entityID;

    public TextLine(String text, int entityID) {
        this.text = text;
        this.entityID = entityID;
    }

    @Override
    public Packet[] getSpawnPackets(Location location) {
        Packet spawnPacket = UtilEntity.spawnArmorStand(location, text, entityID);
        return new Packet[] {
                spawnPacket
        };
    }

    @Override
    public Packet[] getDespawnPackets() {
        return new Packet[] {
                UtilEntity.destroyEntity(entityID)
        };
    }
}
