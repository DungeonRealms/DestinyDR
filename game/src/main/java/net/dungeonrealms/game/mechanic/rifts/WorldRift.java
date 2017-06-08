package net.dungeonrealms.game.mechanic.rifts;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.util.PacketUtils;
import net.dungeonrealms.game.mechanic.ParticleAPI;
import net.dungeonrealms.game.mechanic.ReflectionAPI;
import net.dungeonrealms.game.world.item.Item;
import net.minecraft.server.v1_9_R2.PacketPlayOutTileEntityData;
import net.minecraft.server.v1_9_R2.TileEntityEndGateway;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_9_R2.block.CraftEndGateway;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class WorldRift extends Rift {

    private transient Block highestPortal;

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

        if (riftState != RiftState.SPAWNING) return;

        riftState = RiftState.WAITING;

        Location loc = getLocation().add(1.5, 5, 0);
        loc.getWorld().playSound(loc, Sound.ENTITY_ENDERDRAGON_DEATH, 10, .95F);

        new BukkitRunnable() {
            int phase = 1;

            public void run() {
                if (phase > 3) {
                    cancel();

                    changedBlocks.forEach((loc, mat) -> {
                        if (ThreadLocalRandom.current().nextInt(3) == 0) return;
                        ParticleAPI.spawnBlockParticles(loc, loc.getBlock().getType() == Material.END_GATEWAY ? Material.PORTAL : loc.getBlock().getType());
                    });

                    ParticleAPI.spawnParticle(Particle.PORTAL, loc, 100, 1.5F, 1F);
                    loc.getWorld().playSound(loc, Sound.ENTITY_WITHER_AMBIENT, 10, .8F);

//                    returnBlocks();
                    destroy();
                    return;
                }

                loc.getWorld().playSound(loc, Sound.ENTITY_WITHER_BREAK_BLOCK, 10, .8F);
                createRiftPortal(phase++, true);
            }
        }.runTaskTimer(DungeonRealms.getInstance(), 20, 20);

        sendSealedMessage();
    }

    private transient Map<Integer, List<Offset>> portalStageOffsets;

    public Map<Integer, List<Offset>> getPortalOffsets() {
        portalStageOffsets = new HashMap<>();

        portalStageOffsets.put(1, Lists.newArrayList(
                new Offset(0, 0, 0, 7, 2, 2),
                new Offset(-1, 2, 0, 3, -1, -1),
                new Offset(1, -1, 0, 10, 2, 1),
                new Offset(2, 0, 0, 10, 1, 1),
                new Offset(3, 1, 0, 10, 1, 1),
                new Offset(4, 2, 0, 8, 4, 1),
                new Offset(5, 3, 0, 3, -1, -1)
        ));

        portalStageOffsets.put(2, Lists.newArrayList(
                new Offset(0, 0, 0, 4, -1, -1),
                new Offset(1, -1, 0, 9, 4, 1),
                new Offset(2, 0, 0, 10, 2, 1),
                new Offset(3, 1, 0, 10, 1, 3),
                new Offset(4, 4, 0, 6, -1, -1)
        ));

        portalStageOffsets.put(3, Lists.newArrayList(
                new Offset(0, 0, 0, 3, -1, -1),
                new Offset(1, -1, 0, 8, 4, 1),
                new Offset(2, 0, 0, 10, 3, 3),
                new Offset(3, 3, 0, 8, 1, 4),
                new Offset(4, 7, 0, 3, -1, -1)
        ));

        return portalStageOffsets;
    }

    @Override
    public void createRift() {
        createRiftPortal(1, false);
        //Dont block?
        super.createRift();
    }

    @Override
    public void onRiftTick() {
        super.onRiftTick();
        if (highestPortal != null && highestPortal.getChunk().isLoaded() && highestPortal.getType() == Material.END_GATEWAY) {
            TileEntityEndGateway gateway = ((CraftEndGateway) highestPortal.getState()).getTileEntity();
            ReflectionAPI.setField("f", TileEntityEndGateway.class, gateway, 120);
            ReflectionAPI.setField("g", TileEntityEndGateway.class, gateway, 0);
            //Send that update packet so its always the right beam.
            PacketPlayOutTileEntityData action = gateway.getUpdatePacket();
            Bukkit.getScheduler().scheduleAsyncDelayedTask(DungeonRealms.getInstance(), () -> {
                for (Player pl : GameAPI.getNearbyPlayersAsync(getLocation(), 50)) {
                    PacketUtils.sendPacket(pl, action);
                }
            });
        }
    }

    public void createRiftPortal(int phase, boolean clearPrevious) {
        Block start = getLocation().clone().add(0, 7, 0).getBlock();
        List<Offset> offsets = getPortalOffsets().get(phase);
        if (offsets == null) return;

        List<Block> changes = Lists.newArrayList();
        Location location = start.getLocation();
        for (Offset off : offsets) {
            if (off.getTop() == -1 && off.getBottom() == -1) {
                changes.addAll(createObsidianLine(location.clone().add(off.getX(), off.getY(), off.getZ()), off.getLength()));
            } else {
                changes.addAll(createLine(location.clone().add(off.getX(), off.getY(), off.getZ()), off.getLength(), off.getTop(), off.getBottom()));
            }
        }

        //Remove all old blocks, but keep them if they are already changed to what we want.
        changedBlocks.forEach((loc, material) -> {
            Block block = loc.getBlock();
            if (changes.contains(block) || block.getType() != Material.OBSIDIAN && block.getType() != Material.END_GATEWAY)
                return;

            if (ThreadLocalRandom.current().nextBoolean())
                ParticleAPI.spawnBlockParticles(loc, block.getType() == Material.END_GATEWAY ? Material.PORTAL : block.getType());

            block.setTypeIdAndData(material.getItemTypeId(), material.getData(), false);
            changedBlocks.remove(loc);
        });
    }

    private List<Block> createObsidianLine(Location location, int length) {
        List<Block> changed = Lists.newArrayList();
        for (int i = 0; i < length; i++) {
            Block block = location.clone().add(0, i, 0).getBlock();
            changed.add(changeBlock(block, Material.OBSIDIAN));
        }
        return changed;
    }

    private List<Block> createLine(Location bottom, int length, int top, int bottomLength) {
        List<Block> blocks = Lists.newArrayList();
        for (int i = 0; i < length; i++) {
            boolean isTop = i >= length - top;
            boolean isBottom = i < bottomLength;
            Location l = bottom.clone().add(0, i, 0);
            if (isTop || isBottom) {
                blocks.add(changeBlock(l, Material.OBSIDIAN));
            } else {
                Block endGateway = changeBlock(l, Material.END_GATEWAY);
                blocks.add(endGateway);

                TileEntityEndGateway gateway = ((CraftEndGateway) endGateway.getState()).getTileEntity();
                gateway.i();
                if (highestPortal == null || highestPortal.getY() < endGateway.getY()) {
                    highestPortal = endGateway;
                }
            }
        }
        return blocks;
    }


    @Getter
    @AllArgsConstructor
    private class Offset {
        int x, y, z;
        int length, top, bottom;
    }
}
