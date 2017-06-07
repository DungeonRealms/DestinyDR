package net.dungeonrealms.game.mechanic.rifts;

import net.dungeonrealms.game.mechanic.ParticleAPI;
import net.dungeonrealms.game.mechanic.ReflectionAPI;
import net.dungeonrealms.game.world.entity.util.EntityAPI;
import net.dungeonrealms.game.world.item.Item;
import net.minecraft.server.v1_9_R2.EntitySheep;
import net.minecraft.server.v1_9_R2.EnumColor;
import net.minecraft.server.v1_9_R2.TileEntityBeacon;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_9_R2.block.CraftBeacon;
import org.bukkit.entity.Entity;
import org.bukkit.material.MaterialData;

import java.util.List;

public class WorldRift extends Rift {
    public WorldRift(Location spawn, int tier, Item.ElementalAttribute elementalType, String nearbyCity) {
        super(spawn, tier, elementalType, nearbyCity);
    }

    @Override
    public void onRiftStart() {
        super.onRiftStart();
    }

    @Override
    public int getMaxMobLimit() {
        return tier * 10;
    }

    @Override
    public int getSpawnDelay() {
        return 1;
    }

    @Override
    public void onRiftEnd() {
        super.onRiftEnd();
        Location beaconLocation = getLocation();
        if (beaconLocation.getBlock().getType() == Material.BEACON) {
            beaconLocation.getBlock().setType(Material.AIR);
            ParticleAPI.spawnBlockParticles(beaconLocation.clone().add(.5, .5, .5), Material.BEACON);
        }
    }

    @Override
    public void onRiftTick() {
        super.onRiftTick();
        Block beaconBlock = getLocation().getBlock();
        if (beaconBlock.getType() == Material.BEACON) {
            List<TileEntityBeacon.BeaconColorTracker> colorTracker =
                    (List<TileEntityBeacon.BeaconColorTracker>) ReflectionAPI.getObjectFromField("g", TileEntityBeacon.class, ((CraftBeacon) beaconBlock.getState()).getTileEntity());
            if (colorTracker != null) {
                colorTracker.clear();
                //Add beacon color?
                colorTracker.add(new TileEntityBeacon.BeaconColorTracker(EntitySheep.a(EnumColor.BLUE)));
            }
        }
    }


    @Override
    public Entity spawnMob() {
        Entity spawned = super.spawnMob();

        if (EntityAPI.isElite(spawned)) {
            Block block = getLocation().subtract(0, 1, 0).getBlock();
            if (block.getType() == Material.STAINED_GLASS)
                block.setData(DyeColor.RED.getWoolData());
        }
        return spawned;
    }

    @Override
    public void createRift() {
        Location beaconLocation = getLocation();

        Block beaconBlock = beaconLocation.clone().subtract(0, 2, 0).getBlock();
        if (!beaconBlock.getChunk().isLoaded())
            beaconBlock.getChunk().load();


        createPortal(beaconLocation.clone().add(2, 0, 0), (byte) 2);
        createPortal(beaconLocation.clone().add(-2, 0, 0), (byte) 6);
        createPortal(beaconLocation.clone().add(0, 0, 2), (byte) 1);
        createPortal(beaconLocation.clone().add(0, 0, -2), (byte) 3);

        changeBlock(beaconBlock.getRelative(BlockFace.UP), new MaterialData(Material.STAINED_GLASS.getId(), DyeColor.LIME.getWoolData()));
        changeBlock(beaconBlock.getLocation(), Material.BEACON);

        int radius = 1;
        for (int x = -radius; x < radius + 1; x++) {
            for (int z = -radius; z < radius + 1; z++) {
                Location iron = beaconBlock.getRelative(x, -1, z).getLocation();
                changeBlock(iron, Material.IRON_BLOCK);
            }
        }
        super.createRift();
    }

    private void createPortal(Location l, byte data) {
//        for (int side = 0; side < 2; side++) {
//            int xIncr = side == 0 ? data == (byte) 1 ? 1 : -1 : 0;
//            int yIncr = side == 0 ? data == (byte) 2 ? 1 : -1 : 0;
//            for (int i = 0; i < 3; i++)
//                changeBlock(l.clone().add(xIncr, i, yIncr), Material.OBSIDIAN);
//        }

        if (data == (byte) 1 || data == (byte) 3) {
            for (int i = 0; i < 3; i++)
                changeBlock(l.clone().add(1, i, 0), Material.OBSIDIAN);

            for (int i = 0; i < 3; i++)
                changeBlock(l.clone().add(-1, i, 0), Material.OBSIDIAN);
        } else {
            for (int i = 0; i < 3; i++)
                changeBlock(l.clone().add(0, i, 1), Material.OBSIDIAN);

            for (int i = 0; i < 3; i++)
                changeBlock(l.clone().add(0, i, -1), Material.OBSIDIAN);
        }

        if (data == (byte) 1 || data == (byte) 3) {
            changeBlock(l.clone().add(0, 0, data == 1 ? -1 : 1), Material.OBSIDIAN);
            changeBlock(l.clone().add(0, 1, data == 1 ? -1 : 1), Material.OBSIDIAN);
        } else {
            changeBlock(l.clone().add(data == 2 ? -1 : 1, 0, 0), Material.OBSIDIAN);
            changeBlock(l.clone().add(data == 2 ? -1 : 1, 1, 0), Material.OBSIDIAN);
        }

        changeBlock(l.clone().add(0, 2, 0), Material.OBSIDIAN);
        changeBlock(l, new MaterialData(Material.PORTAL, data));
        changeBlock(l.clone().add(0, 1, 0), new MaterialData(Material.PORTAL, data));
    }
}
