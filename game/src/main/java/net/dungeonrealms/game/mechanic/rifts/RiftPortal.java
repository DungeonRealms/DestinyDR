package net.dungeonrealms.game.mechanic.rifts;

import com.google.common.collect.Lists;
import lombok.Getter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.mechanic.ParticleAPI;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

public class RiftPortal {

    //NEed to store a list of portals changed.. allow them to tp in.
    @Getter
    private static Map<UUID, RiftPortal> riftPortalMap = new HashMap<>();

    private Map<Location, MaterialData> changedPortals = new HashMap<>();

    @Getter
    private List<Block> portalBlocks = Lists.newArrayList();

    @Getter
    private Player portalOwner;

    private Block middle;

    @Getter
    int genned = 0;

    public RiftPortal(Player player, Block clicked) {
        this.portalOwner = player;
        this.middle = clicked.getRelative(BlockFace.UP);
        riftPortalMap.put(player.getUniqueId(), this);
        getPortalLocations();
    }

    public boolean isDoneGenerating() {
        return genned >= 6;
    }

    private List<Location> portalLocations = Lists.newArrayList();

    private void getPortalLocations() {
        Location currentPortal = middle.getLocation();
        for (int i = 0; i < 6; i++) {
            if (i == 0) {
                //Start over in the back left?
                //Decrease z by 2 each time.

                //Start at back corner.
                currentPortal.add(-2, 0, 2);
            } else if (i == 2) {
                currentPortal = middle.getLocation().add(-2, 0, -2);
            } else {
                currentPortal.add(0, 0, 2);
            }
            portalLocations.add(currentPortal.clone());
        }
    }

    public boolean canPlacePortals() {
        return portalLocations.stream().map(Location::getBlock).allMatch(lower -> lower.getType() == Material.AIR && !lower.getRelative(BlockFace.UP).getType().isSolid() && lower.getRelative(BlockFace.DOWN).getType().isSolid());
    }

    public void createPortals(Consumer<Player> callback) {

        new BukkitRunnable() {
            public void run() {
                if (genned >= 6 || genned >= portalLocations.size()) {
                    cancel();
                    if (callback != null) {
                        callback.accept(getPortalOwner());
                    }
                    return;
                }

                Location portalLoc = portalLocations.get(genned);
                createPortal(portalLoc);
                portalLoc.getWorld().playSound(portalLoc, Sound.ENTITY_ENDERMEN_TELEPORT, 3F, .8F + ThreadLocalRandom.current().nextFloat() * .20F);
                ParticleAPI.spawnParticle(Particle.PORTAL, portalLoc.clone().add(.5, 1, .5), .25F, .25F, .25D, 30, 1);
                genned++;
            }
        }.runTaskTimer(DungeonRealms.getInstance(), 20, 17);
    }

    public void handlePortalUse(Player player, Block block) {
        returnBlocks(block.getLocation());
        returnBlocks(block.getRelative(BlockFace.UP).getLocation());
    }

    public void onUpdate() {
        if (isDoneGenerating())
            for (Block portal : portalBlocks)
                if (portal.getType() != Material.PORTAL)
                    portal.setTypeIdAndData(Material.PORTAL.getId(), (byte) 2, false);
    }


    private void removeAllBlocks() {
        changedPortals.forEach((l, mat) -> l.getBlock().setTypeIdAndData(mat.getItemTypeId(), mat.getData(), false));
        changedPortals.clear();
    }

    private void returnBlocks(Location location) {
        MaterialData previous = changedPortals.remove(location);
        if (previous != null)
            location.getBlock().setTypeIdAndData(previous.getItemTypeId(), previous.getData(), false);
    }

    private void createPortal(Location l) {
        Block b = l.getBlock();
        registerPortal(changeBlock(b, Material.PORTAL, (byte) 2, true));
        registerPortal(changeBlock(b.getRelative(BlockFace.UP), Material.PORTAL, (byte) 2, true));
    }

    private void registerPortal(Block block) {
        portalBlocks.add(block);
        block.setMetadata("riftPortal", new FixedMetadataValue(DungeonRealms.getInstance(), this.portalOwner.toString()));
    }

    public void removePortals() {
        removeAllBlocks();
        riftPortalMap.remove(getPortalOwner().getUniqueId());
    }

    public Block changeBlock(Block current, Material material, byte data, boolean playParticles) {
        this.changedPortals.put(current.getLocation(), new MaterialData(current.getType(), current.getData()));

        if (current.getType() != Material.AIR && playParticles)
            ParticleAPI.spawnBlockParticles(current.getLocation(), current.getType());

        current.setTypeIdAndData(material.getId(), data, false);
        return current;
    }

    public static RiftPortal getRiftPortal(Player player) {
        return riftPortalMap.get(player.getUniqueId());
    }

    public static RiftPortal getRiftPortalFromBlock(Block block) {
        return riftPortalMap.values().stream().filter(portal -> portal.getPortalBlocks().contains(block)).findFirst().orElse(null);
    }
}
