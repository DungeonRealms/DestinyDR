package net.dungeonrealms.game.mechanic.rifts;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.line.TextLine;
import com.google.common.collect.Lists;
import io.netty.util.internal.ConcurrentSet;
import lombok.Getter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.util.TimeUtil;
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
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
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

    public static final int MAX_PORTALS = 6, MAX_ALIVE_TIME = 30 * 60;
    //    MAX_ALIVE_TIME = 50;
    private long CLOSING_TIME = -1;
    private static final Material PORTAL = Material.END_GATEWAY;

    public RiftPortal(Player player, Block clicked, int tier) {
        this.portalOwner = player;
        this.middle = clicked.getRelative(BlockFace.UP);
        this.tier = tier;
        getPortalLocations();
    }

    public boolean isDoneGenerating() {
        return genned >= 6;
    }

    private List<Location> portalLocations = Lists.newArrayList();

    private void getPortalLocations() {
//        //First, -1x, -2z
        //second = +1x, -2z
        //third = +2x, 0z
        //fourth = +1x, 2z
        //fourth = -1x, 2z
        //fourth = -2x, 0z

        portalLocations.add(middle.getLocation().add(-1, 0, -3));
        portalLocations.add(middle.getLocation().add(1, 0, -3));
        portalLocations.add(middle.getLocation().add(3, 0, 0));
        portalLocations.add(middle.getLocation().add(1, 0, 3));
        portalLocations.add(middle.getLocation().add(-1, 0, 3));
        portalLocations.add(middle.getLocation().add(-3, 0, 0));
    }

    public boolean canPlacePortals() {
        for (Location loc : portalLocations) {
            Block lower = loc.getBlock();
            if (lower.getType() != Material.AIR || lower.getRelative(BlockFace.UP).getType().isSolid() || !lower.getRelative(BlockFace.DOWN).getType().isSolid()) {
                return false;
            }

            //All blocks MUST be in the safezone.
            if (!GameAPI.isInSafeRegion(loc)) return false;
        }

        if (this.middle.getLocation().getBlock().getType() != Material.AIR &&
                this.middle.getLocation().add(0, 1, 0).getBlock().getType() != Material.AIR) {
            return false;
        }

        if (GameAPI.isAnyMaterialNearby(middle, 4, Lists.newArrayList(Material.CHEST, Material.ENDER_CHEST, Material.TRAPPED_CHEST, Material.ANVIL, Material.ENDER_PORTAL_FRAME, Material.END_GATEWAY)))
            return false;

        RiftPortal portal = RiftPortal.getNearbyRiftPortal(this.middle.getLocation(), 15);
        if (portal != null)
            return false;


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

        changeBlock(this.getMiddle(), Material.ENDER_PORTAL_FRAME, (byte) 4, false);
        ParticleAPI.spawnBlockParticles(this.middle.getLocation().add(0.5, 0, .5), Material.ENDER_PORTAL_FRAME);
        this.getMiddle().getWorld().playSound(this.getMiddle().getLocation(), Sound.BLOCK_GLASS_BREAK, 1, 1.1F);

        hologram = HologramsAPI.createHologram(DungeonRealms.getInstance(), middle.getLocation().add(.5, 3.3, .5));
        updateHologram();

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

    public void updateHologram() {
        if (hologram == null) return;
        Item.ItemTier tier = Item.ItemTier.getByTier(getTier());

        String line1 = tier.getColor().toString() + ChatColor.BOLD + "Rift";
        String line2 = tier.getColor() + "Opened by " + getPortalOwner().getName();
        String line3 = ChatColor.LIGHT_PURPLE.toString() + (MAX_PORTALS - portalsUsed) + " Portals Remaining";
        String line4 = ChatColor.WHITE.toString() + "Closing in " + TimeUtil.formatDifference(CLOSING_TIME == -1 ? MAX_ALIVE_TIME : (CLOSING_TIME - System.currentTimeMillis()) / 1000);

        if (hologram.size() <= 0) {
            hologram.appendTextLine(line1);
            hologram.appendTextLine(line2);
            hologram.appendTextLine(line3);
            hologram.appendTextLine(line4);
            ItemStack item = new ItemStack(Material.CHORUS_FRUIT_POPPED);
            item.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
            hologram.appendItemLine(item);
        } else {
            updateHologramLine(line1, 0);
            updateHologramLine(line2, 1);
            updateHologramLine(line3, 2);
            updateHologramLine(line4, 3);
        }
    }

    private void updateHologramLine(String string, int line) {
        TextLine li = (TextLine) hologram.getLine(line);
        if (li.getText() != null && li.getText().equals(string)) return;
        li.setText(string);
    }

    public void onPortalsGenerated() {

        CLOSING_TIME = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(MAX_ALIVE_TIME);

        for (Block block : portalBlocks) {
            if (block.getType() == Material.END_GATEWAY) {
                TileEntityEndGateway gateway = ((CraftEndGateway) block.getState()).getTileEntity();
                gateway.exactTeleport = true;
                //Send end teleport location.
                gateway.exitPortal = new BlockPosition(block.getLocation().getX(), block.getLocation().getY(), block.getLocation().getZ());
                block.getWorld().playSound(block.getLocation(), Sound.ENTITY_WITHER_AMBIENT, .7F, 1.4F);
            }
        }
    }

    public int getAttemptsLeft() {
        return MAX_PORTALS - portalsUsed;
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

        if (portalsUsed == 0) {
            //First spawn?
            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {

                if (riftInstance.isFinished()) return;

                riftInstance.spawnBoss(BossType.RiftEliteBoss);
            }, 20 * 3);
        }
        portalsUsed++;
        Utils.sendCenteredMessage(player, ChatColor.GRAY + "You have " + ChatColor.BOLD + (MAX_PORTALS - portalsUsed) + ChatColor.GRAY + " attempts left to defeat this rift!");
        this.updateHologram();
    }

    public void onUpdate() {
        if (isDoneGenerating()) {
            for (Block portal : portalBlocks) {
                if (portal.getChunk().isLoaded()) {
                    if (portal.getType() != PORTAL)
                        portal.setTypeIdAndData(PORTAL.getId(), (byte) 2, false);
                }
            }

            if (this.hologram != null)
                this.updateHologram();


            if (CLOSING_TIME <= System.currentTimeMillis() && CLOSING_TIME != -1) {
                Utils.sendCenteredMessage(getPortalOwner(), ChatColor.RED + "Your Rift has been sealed shut!");
                removePortals(false);
            }

        } else {
            for (Location loc : portalLocations)
                ParticleAPI.spawnParticle(Particle.PORTAL, loc.clone().add(.5, 1, .5), .24F, 1F, .24F, 30, .01F);
        }

        ParticleAPI.spawnParticle(Particle.ENCHANTMENT_TABLE, this.getMiddle().getLocation().add(.5, 2, .5), 15, .1F, .01F);
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

//        b.getWorld().playEffect(b.getLocation().add(0.5, 1, .5), Effect.DRAGON_BREATH, 0);

        registerPortal(changeBlock(b.getRelative(BlockFace.UP), PORTAL, (byte) 2, true));
    }

    private void registerPortal(Block block) {
        portalBlocks.add(block);
    }

    public void removePortals(boolean outOfLives) {
        removeAllBlocks();

        if (this.hologram != null)
            this.hologram.delete();

        if (riftInstance != null && !riftInstance.isFinished())
            riftInstance.remove();

        riftPortalMap.remove(getPortalOwner().getUniqueId());
        if (outOfLives && getPortalOwner() != null && getPortalOwner().isOnline()) {
            getPortalOwner().sendMessage("");
            Utils.sendCenteredMessage(getPortalOwner(), ChatColor.RED + ChatColor.BOLD.toString() + "All of your Rift Portals have been sealed!");
            getPortalOwner().playSound(getPortalOwner().getLocation(), Sound.ENTITY_WITHER_DEATH, 1, 1.4F);
            getPortalOwner().sendMessage("");
        }
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
