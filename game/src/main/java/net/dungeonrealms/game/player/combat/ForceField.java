package net.dungeonrealms.game.player.combat;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.RequiredArgsConstructor;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.handler.KarmaHandler;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.server.PluginDisableEvent;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class ForceField implements Listener, GenericMechanic {
    private final ExecutorService executorService = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("PvP ForceField Thread").build());
    private final Map<UUID, Set<Location>> previousUpdates = new HashMap<>();
    private static final List<BlockFace> ALL_DIRECTIONS = ImmutableList.of(BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST);

    @EventHandler
    public void shutdown(PluginDisableEvent event) {
        // Do nothing if plugin being disabled isn't CombatTagPlus
        if (event.getPlugin() != DungeonRealms.getInstance()) return;

        // Shutdown executor service and clean up threads
        executorService.shutdown();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException ignore) {
        }

        // Go through all previous updates and revert spoofed blocks
        for (UUID uuid : previousUpdates.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) continue;

            for (Location location : previousUpdates.get(uuid)) {
                Block block = location.getBlock();
                player.sendBlockChange(location, block.getType(), block.getData());
            }
        }
    }


    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void updateViewedBlocks(PlayerMoveEvent event) {
        final Player player = event.getPlayer();

        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
        GamePlayer gp = GameAPI.getGamePlayer(player);
        if (gp == null) return;

        // check if we have to send blocks or remove them
        if (!gp.isPvPTagged() && wrapper.getAlignment() != KarmaHandler.EnumPlayerAlignments.CHAOTIC &&
                !previousUpdates.containsKey(player.getUniqueId()))
            return;

        // Do nothing if player hasn't moved over a whole block
        Location t = event.getTo();
        Location f = event.getFrom();
        if (t.getBlockX() == f.getBlockX() && t.getBlockY() == f.getBlockY() &&
                t.getBlockZ() == f.getBlockZ()) {
            return;
        }

        // Asynchronously send block changes around player
        executorService.submit(() -> {
            // Stop processing if player has logged off
            UUID uuid = player.getUniqueId();
            if (!player.isOnline()) {
                previousUpdates.remove(uuid);
                return;
            }

            // Update the players force field perspective and find all blocks to stop spoofing
            Set<Location> changedBlocks = getChangedBlocks(player);
            Material forceFieldMaterial = Material.STAINED_GLASS;
            byte forceFieldMaterialDamage = 14; // 14 = red for stained glass

            Set<Location> removeBlocks;
            if (previousUpdates.containsKey(uuid)) {
                removeBlocks = previousUpdates.remove(uuid);
            } else {
                removeBlocks = new HashSet<>();
            }

            for (Location location : changedBlocks) {
            	if(location.getWorld() != player.getWorld())
            		return;
                player.sendBlockChange(location, forceFieldMaterial, forceFieldMaterialDamage);
                removeBlocks.remove(location);
            }

            // Remove no longer used spoofed blocks
            for (Location location : removeBlocks) {
            	if(location.getWorld() != player.getWorld())
            		return;
                Block block = location.getBlock();
                player.sendBlockChange(location, block.getType(), block.getData());
            }

            previousUpdates.put(uuid, changedBlocks);
        });
    }

    private Set<Location> getChangedBlocks(Player player) {
        Set<Location> locations = new HashSet<>();

        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
        GamePlayer gp = GameAPI.getGamePlayer(player);
        if (gp == null) return locations;

        // Do nothing if player is not tagged or chaotic
        if (!gp.isPvPTagged() && wrapper.getAlignment() != KarmaHandler.EnumPlayerAlignments.CHAOTIC) return locations;

        // Find the radius around the player
        int r = 10;
        Location l = player.getLocation();
        Location loc1 = l.clone().add(r, 0, r);
        Location loc2 = l.clone().subtract(r, 0, r);
        int topBlockX = loc1.getBlockX() < loc2.getBlockX() ? loc2.getBlockX() : loc1.getBlockX();
        int bottomBlockX = loc1.getBlockX() > loc2.getBlockX() ? loc2.getBlockX() : loc1.getBlockX();
        int topBlockZ = loc1.getBlockZ() < loc2.getBlockZ() ? loc2.getBlockZ() : loc1.getBlockZ();
        int bottomBlockZ = loc1.getBlockZ() > loc2.getBlockZ() ? loc2.getBlockZ() : loc1.getBlockZ();

        // Iterate through all blocks surrounding the player
        for (int x = bottomBlockX; x <= topBlockX; x++) {
            for (int z = bottomBlockZ; z <= topBlockZ; z++) {
                // Location corresponding to current loop
                Location location = new Location(l.getWorld(), (double) x, l.getY(), (double) z);

                // PvP is enabled here, no need to do anything else
                if (!GameAPI.isNonPvPRegion(location)) continue;

                // Check if PvP is enabled in a location surrounding this
                if (!isPvpSurrounding(location)) continue;

                // Add circular locations
                for (int i = -r; i < r; i++) {
                    Location loc = new Location(location.getWorld(), location.getX(), location.getY(), location.getZ());
                    loc.setY(loc.getY() + i);

                    if (l.distanceSquared(loc) > 80) continue;

                    // Do nothing if the block at the location is not air
                    if (!loc.getBlock().getType().equals(Material.AIR)) continue;

                    // Add this location to locations
                    locations.add(new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
                }
            }
        }

        return locations;
    }

    private boolean isPvpSurrounding(Location loc) {
        for (BlockFace direction : ALL_DIRECTIONS) {
            if (!GameAPI.isNonPvPRegion(loc.getBlock().getRelative(direction).getLocation())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.CATHOLICS;
    }

    @Override
    public void startInitialization() {
        Bukkit.getPluginManager().registerEvents(this, DungeonRealms.getInstance());
    }

    @Override
    public void stopInvocation() {
    }
}
