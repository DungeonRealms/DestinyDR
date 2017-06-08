package net.dungeonrealms.game.mechanic.rifts;

import net.dungeonrealms.game.mechanic.ParticleAPI;
import net.dungeonrealms.game.world.entity.util.EntityAPI;
import net.dungeonrealms.game.world.item.Item;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.material.MaterialData;

import java.util.concurrent.ThreadLocalRandom;

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
        this.changedBlocks.forEach((loc, mat) -> {
            if (ThreadLocalRandom.current().nextInt(3) == 0) return;
            ParticleAPI.spawnBlockParticles(loc, loc.getBlock().getType());
        });
        super.onRiftEnd();
    }

    @Override
    public void onRiftTick() {
        super.onRiftTick();
    }


    @Override
    public Entity spawnMob() {
        Entity spawned = super.spawnMob();

        if (EntityAPI.isElite(spawned))
            setGlassColor(DyeColor.RED);

        return spawned;
    }

    public void setGlassColor(DyeColor color) {
        Location loc = getLocation();
        Block block = loc.clone().subtract(0, 1, 0).getBlock();
        if (block.getType() != Material.STAINED_GLASS)
            changeBlock(block.getLocation(), Material.STAINED_GLASS);
        block.setData(color.getWoolData());


        Block above = loc.add(0, 2, 0).getBlock();
        if (above.getType() != Material.STAINED_GLASS)
            changeBlock(above.getLocation(), Material.STAINED_GLASS);

        above.setData(color.getWoolData());
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

        changeBlock(beaconBlock.getLocation(), Material.BEACON);

        int radius = 1;
        for (int x = -radius; x < radius + 1; x++) {
            for (int z = -radius; z < radius + 1; z++) {
                //Roof of obby.
                changeBlock(beaconBlock.getRelative(x, 4, z), new MaterialData(Material.OBSIDIAN));
                changeBlock(beaconBlock.getRelative(x, -1, z).getLocation(), Material.IRON_BLOCK);
            }
        }

        Item.ItemTier tier = Item.ItemTier.getByTier(getTier());
        setGlassColor(tier.getDyeColor());
        //Dont block?
        super.createRift();
    }

    private void createPortal(Location l, byte data) {
        boolean firstFace = data == (byte) 1 || data == (byte) 3;

        for (int i = 0; i < 3; i++)
            changeBlock(l.clone().add(firstFace ? -1 : 0, i, firstFace ? 0 : -1), Material.OBSIDIAN);
        for (int i = 0; i < 3; i++)
            changeBlock(l.clone().add(firstFace ? 1 : 0, i, firstFace ? 0 : 1), Material.OBSIDIAN);

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
