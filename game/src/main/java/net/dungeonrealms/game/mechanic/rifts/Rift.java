package net.dungeonrealms.game.mechanic.rifts;

import io.netty.util.internal.ConcurrentSet;
import lombok.Getter;
import net.dungeonrealms.game.mechanic.ReflectionAPI;
import net.dungeonrealms.game.world.item.Item;
import net.minecraft.server.v1_9_R2.EntitySheep;
import net.minecraft.server.v1_9_R2.EnumColor;
import net.minecraft.server.v1_9_R2.TileEntityBeacon;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Beacon;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_9_R2.block.CraftBeacon;
import org.bukkit.entity.Entity;

import java.util.List;
import java.util.Set;

public abstract class Rift {

    @Getter
    protected Location beaconLocation;

    @Getter
    protected int tier;

    protected Item.ElementalAttribute attribute;

    private String nearbyCity;

    private transient RiftState riftState;
    private transient Set<Entity> spawnedEntities = new ConcurrentSet<>();

    public Rift(Location spawn, int tier, Item.ElementalAttribute elementalType, String nearbyCity) {
        this.beaconLocation = spawn;
        this.tier = tier;
        this.attribute = elementalType;
        this.nearbyCity = nearbyCity;
    }

    public void onRiftEnd() {
        this.destroy();

    }

    public void onRiftStart() {
    }

    public abstract int getMaxMobLimit();

    /**
     * Seconds in between each spawn?
     *
     * @return
     */
    public abstract int getSpawnDelay();

    /**
     * Called around every 1 second? Seems suffice?
     */
    public void onRiftTick() {
        Set<Entity> spawned = getSpawnedEntities();
//        if(spawned.size() <= getMaxMobLimit() && )
    }

    public void createRift() {
        Block beaconBlock = beaconLocation.getBlock();
        if (!beaconBlock.getChunk().isLoaded())
            beaconBlock.getChunk().load();

        beaconBlock.setType(Material.BEACON);

        Beacon beacon = (Beacon) beaconBlock.getState();
        CraftBeacon beac = (CraftBeacon) beacon;
        List<TileEntityBeacon.BeaconColorTracker> colorTracker =
                (List<TileEntityBeacon.BeaconColorTracker>) ReflectionAPI.getObjectFromField("g", TileEntityBeacon.class, beac);
        if (colorTracker != null) {
            colorTracker.clear();
            //Add beacon color?
            colorTracker.add(new TileEntityBeacon.BeaconColorTracker(EntitySheep.a(EnumColor.BLUE)));
            Bukkit.getLogger().info("adding blue color!");
        }
    }

    public void destroy() {
        if (!spawnedEntities.isEmpty()) {
            for (Entity ent : spawnedEntities) {
                ent.remove();
            }
            spawnedEntities.clear();
        }
    }

    public Set<Entity> getSpawnedEntities() {
        for (Entity ent : this.spawnedEntities) {
            if (ent.isDead()) this.spawnedEntities.remove(ent);
        }
        return this.spawnedEntities;
    }

}
