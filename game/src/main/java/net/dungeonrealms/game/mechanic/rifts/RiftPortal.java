package net.dungeonrealms.game.mechanic.rifts;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.google.common.collect.Lists;
import io.netty.util.internal.ConcurrentSet;
import lombok.Getter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.ParticleAPI;
import net.dungeonrealms.game.mechanic.dungeons.BossType;
import net.dungeonrealms.game.mechanic.dungeons.Dungeon;
import net.dungeonrealms.game.mechanic.dungeons.DungeonManager;
import net.dungeonrealms.game.mechanic.dungeons.DungeonType;
import net.dungeonrealms.game.mechanic.dungeons.rifts.EliteRift;
import net.dungeonrealms.game.world.item.Item;
import net.dungeonrealms.game.world.realms.Realm;
import net.dungeonrealms.game.world.realms.Realms;
import net.minecraft.server.v1_9_R2.BlockPosition;
import net.minecraft.server.v1_9_R2.TileEntityEndGateway;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_9_R2.block.CraftEndGateway;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

public class RiftPortal {

    //NEed to store a list of portals changed.. allow them to tp in.
    @Getter
    private static Map<UUID, RiftPortal> riftPortalMap = new HashMap<>();

    private Map<Location, MaterialData> changedPortals = new HashMap<>();

//    private Map<Location, Hologram> holograms = new HashMap<>();

    private Hologram hologram;

    @Getter
    private List<Block> portalBlocks = Lists.newArrayList();

    //Pretty much want this to be fast.. so rip me..
    public static Set<String> activeBlockPositions = new ConcurrentSet<>();

    @Getter
    private Player portalOwner;

    @Getter
    private Block middle;

    @Getter
    private EliteRift riftInstance;

    @Getter
    int genned = 0, tier, portalsUsed = 0;

    private static final int MAX_PORTALS = 6;

    private static final Material PORTAL = Material.END_GATEWAY;

    public RiftPortal(Player player, Block clicked, int tier) {
        this.portalOwner = player;
        this.middle = clicked.getRelative(BlockFace.UP);
        riftPortalMap.put(player.getUniqueId(), this);
        this.tier = tier;
        getPortalLocations();
    }

    public boolean isDoneGenerating() {
        return genned >= 6;
    }

    private List<Location> portalLocations = Lists.newArrayList();

    private void getPortalLocations() {
        Location currentPortal = middle.getLocation();
        for (int i = 1; i < MAX_PORTALS + 1; i++) {
            if (i == 1) {
                //Start over in the back left?
                //Decrease z by 2 each time.

                //Start at back corner.
                currentPortal.add(-2, 0, -2);
            } else if (i == 4) {
                currentPortal = middle.getLocation().add(2, 0, -2);
            } else {
                currentPortal.add(0, 0, 2);
            }
            portalLocations.add(currentPortal.clone());
        }
    }

    public boolean canPlacePortals() {
        for (Location loc : portalLocations) {
            Block lower = loc.getBlock();
            if (lower.getType() != Material.AIR || lower.getRelative(BlockFace.UP).getType().isSolid() || !lower.getRelative(BlockFace.DOWN).getType().isSolid()) {
                return false;
            }
        }

        Realm nearby = Realms.getInstance().getNearbyRealm(middle.getLocation(), 15);

        return nearby == null;
    }

    public void createPortals(Consumer<Player> callback) {
        riftInstance = (EliteRift) DungeonManager.createDungeon(DungeonType.ELITE_RIFT, Lists.newArrayList());
        if (riftInstance == null) {
            callback.accept(null);
            return;
        }
        riftInstance.setTier(getTier());
        riftInstance.setOurTier(getTier());

        hologram = HologramsAPI.createHologram(DungeonRealms.getInstance(), middle.getLocation().add(.5, 3, .5));
        Item.ItemTier tier = Item.ItemTier.getByTier(getTier());
        hologram.appendTextLine(tier.getColor().toString() + ChatColor.BOLD + "Rift");
        hologram.appendTextLine(tier.getColor() + getPortalOwner().getName());
        hologram.appendTextLine(ChatColor.LIGHT_PURPLE.toString() + (MAX_PORTALS - portalsUsed) + " Portals Remaining");

        new BukkitRunnable() {
            @Override
            public void run() {
                if (genned >= 6 || genned >= portalLocations.size() || !getPortalOwner().isOnline()) {
                    cancel();
                    if (callback != null && getPortalOwner().isOnline()) {
                        callback.accept(getPortalOwner());
                    }
                    onPortalsGenerated();
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

    public void onPortalsGenerated() {
        for (Block block : portalBlocks) {
            if (block.getType() == Material.END_GATEWAY) {
                TileEntityEndGateway gateway = ((CraftEndGateway) block.getState()).getTileEntity();
                gateway.exactTeleport = true;
                //Send end teleport location.
                gateway.exitPortal = new BlockPosition(block.getLocation().getX(), block.getLocation().getY(), block.getLocation().getZ());
            }
        }
    }

    public void handlePortalUse(Player player, Block block) {
        if (portalsUsed >= MAX_PORTALS) {
            player.sendMessage(ChatColor.RED + "The rift seems to be sealed shut!");
            return;
        }

//        this.portalLocations.remove(block.getLocation());
        returnBlocks(block.getLocation());
        returnBlocks(block.getRelative(BlockFace.UP).getLocation());
        riftInstance.addPlayer(player);

        player.playNote(player.getLocation(), Instrument.PIANO, new Note(2));
        Utils.sendCenteredMessage(player, ChatColor.GRAY + "You have " + ChatColor.BOLD + (MAX_PORTALS - portalsUsed) + ChatColor.GRAY + " attempts left to defeat this rift!");

        if (portalsUsed == 0) {
            //First spawn?
            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                riftInstance.spawnBoss(BossType.RiftEliteBoss);
            }, 20 * 3);
        }

        portalsUsed++;
    }

    public void onUpdate() {
        if (isDoneGenerating())
            for (Block portal : portalBlocks)
                if (portal.getType() != PORTAL)
                    portal.setTypeIdAndData(PORTAL.getId(), (byte) 2, false);
    }


    private void removeAllBlocks() {
        changedPortals.forEach((l, mat) -> l.getBlock().setTypeIdAndData(mat.getItemTypeId(), mat.getData(), false));
        changedPortals.clear();
    }

    private void returnBlocks(Location location) {
        boolean portal = this.portalBlocks.remove(location.getBlock());
        MaterialData previous = changedPortals.remove(location);

        if (previous != null) {
            location.getBlock().setTypeIdAndData(previous.getItemTypeId(), previous.getData(), false);
            if (portal) {
                activeBlockPositions.remove(location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ());
                ParticleAPI.spawnBlockParticles(location.add(.5, .5, .5), Material.PORTAL);
                location.getWorld().playSound(location, Sound.BLOCK_GLASS_BREAK, 1, 1.1F);
            }
        }
    }

    private void createPortal(Location l) {
        Block b = l.getBlock();
        activeBlockPositions.add(l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ());
        registerPortal(changeBlock(b, PORTAL, (byte) 2, true));
        registerPortal(changeBlock(b.getRelative(BlockFace.UP), PORTAL, (byte) 2, true));

    }

    private void registerPortal(Block block) {
        portalBlocks.add(block);
    }

    public void removePortals() {
        removeAllBlocks();

        if (this.hologram != null)
            this.hologram.delete();

        if (riftInstance != null && !riftInstance.isFinished())
            riftInstance.remove();

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

    public static RiftPortal getPortalFromDungeon(Dungeon dungeon) {
        return riftPortalMap.values().stream().filter(port -> port.getRiftInstance() != null && port.getRiftInstance().equals(dungeon)).findFirst().orElse(null);
    }

    public static RiftPortal getNearbyRiftPortal(Location location, int radius) {
        radius *= radius;
        for (RiftPortal portal : riftPortalMap.values()) {
            if (portal.getMiddle() != null && portal.getMiddle().getWorld().equals(location.getWorld())) {
                if (portal.getMiddle().getLocation().distanceSquared(location) <= radius) {
                    return portal;
                }
            }
        }
        return null;
    }
}
