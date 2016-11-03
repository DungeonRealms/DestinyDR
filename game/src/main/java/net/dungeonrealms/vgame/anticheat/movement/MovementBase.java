package net.dungeonrealms.vgame.anticheat.movement;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.vgame.anticheat.utils.AntiCheatUtils;
import net.dungeonrealms.vgame.anticheat.utils.MathUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerVelocityEvent;

import java.util.*;

/**
 * Copyright © 2016 Matthew E Development - All Rights Reserved
 * You may NOT use, distribute and modify this code.
 * <p>
 * Created by Matthew E on 10/30/2016 at 12:08 PM.
 */
public class MovementBase implements Listener {
    private Map<UUID, Long> playerStartMoveTimes;
    private Map<UUID, Location> playerStartMoveLocations;
    private Map<UUID, PlayerMoveEvent> playerLastMovement;
    private Map<UUID, Long> playerLastMoveTime;

    private Map<UUID, Long> playerLastTPTimes;
    private Map<UUID, Location> playerLastTPLocations;
    private List<UUID> teleportedPlayers;

    private Map<UUID, Boolean> playerOnGround;
    private Map<UUID, Integer> playerOnGroundMoves;
    private Map<UUID, Long> playerLastVelocityTime;
    private Map<UUID, Velocity> playerLastVelocitys;
    private Map<UUID, Boolean> playerUsingItem;

    private ArrayList<MovementWatch> movementChecks;

    public MovementBase() {
        playerStartMoveLocations = new HashMap<>();
        playerStartMoveTimes = new HashMap<>();
        playerLastMovement = new HashMap<>();
        playerLastMoveTime = new HashMap<>();

        playerLastTPTimes = new HashMap<>();
        playerLastTPLocations = new HashMap<>();
        teleportedPlayers = new ArrayList<>();

        playerOnGround = new HashMap<>();
        playerOnGroundMoves = new HashMap<>();
        playerLastVelocityTime = new HashMap<>();
        playerLastVelocitys = new HashMap<>();
        playerUsingItem = new HashMap<>();

        movementChecks = new ArrayList<>();
        Bukkit.getServer().getPluginManager().registerEvents(this, DungeonRealms.getInstance());
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Location origTo = event.getTo().clone();
        Location origFrom = event.getFrom().clone();

        updateCachePreMove(event);
        raiseChecks(event);
        updateCachePostMove(event, origTo, origFrom);
    }

    private void updateCachePreMove(PlayerMoveEvent event) {
        Player p = event.getPlayer();
        UUID pUUID = p.getUniqueId();

        if (!playerStartMoveTimes.containsKey(pUUID)) {
            playerStartMoveTimes.put(pUUID, System.currentTimeMillis());
            playerStartMoveLocations.put(pUUID, event.getFrom());
        } else {
            int newMoveTimeThreshold = 500;
            if (hasPlayerMoveTimePassed(pUUID, newMoveTimeThreshold)) {
                playerStartMoveTimes.put(pUUID, System.currentTimeMillis());
                playerStartMoveLocations.put(pUUID, event.getFrom());
            }
        }

        boolean onGround = AntiCheatUtils.getInstance().isPlayerOnGround(p);
        playerOnGround.put(pUUID, onGround);
        if (onGround) {
            if (!playerOnGroundMoves.containsKey(pUUID))
                playerOnGroundMoves.put(pUUID, 1);
            else
                playerOnGroundMoves.put(pUUID, playerOnGroundMoves.get(pUUID) + 1);
        } else
            playerOnGroundMoves.put(pUUID, 0);
    }

    private void raiseChecks(PlayerMoveEvent event) {
        for (MovementWatch check : movementChecks)
            check.onPlayerMove(event);
    }

    private void updateCachePostMove(PlayerMoveEvent event, Location origTo, Location origFrom) {
        Player p = event.getPlayer();
        UUID pUUID = p.getUniqueId();

        playerLastMovement.put(pUUID, event);
        playerLastMoveTime.put(pUUID, System.currentTimeMillis());

        if (teleportedPlayers.contains(pUUID))
            teleportedPlayers.remove(pUUID);

        if (playerLastVelocitys.containsKey(pUUID) && !isAffectedByVelocity(p))
            playerLastVelocitys.remove(pUUID);
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        UUID pUUID = event.getPlayer().getUniqueId();
        playerLastTPTimes.put(pUUID, System.currentTimeMillis());
        playerLastTPLocations.put(pUUID, event.getTo());
        playerLastMoveTime.put(pUUID, System.currentTimeMillis());
        teleportedPlayers.add(pUUID);
    }

    @EventHandler
    public void onVelocity(PlayerVelocityEvent event) {
        playerLastVelocityTime.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
        playerLastVelocitys.put(event.getPlayer().getUniqueId(), new Velocity(event));
    }

    //region Getters for the caching

    public PlayerMoveEvent getLastMovement(UUID pUUID) {
        if (!playerLastMovement.containsKey(pUUID))
            return null;
        return playerLastMovement.get(pUUID);
    }

    public long getLastVelocityTime(UUID pUUID) {
        if (!playerLastVelocityTime.containsKey(pUUID))
            return 0;
        return playerLastVelocityTime.get(pUUID);
    }

    public boolean hasVelocityTimePassed(UUID pUUID, int time) {
        return System.currentTimeMillis() - getLastVelocityTime(pUUID) > time;
    }

    public Velocity getLastVelocity(UUID pUUID) {
        return playerLastVelocitys.containsKey(pUUID) ? playerLastVelocitys.get(pUUID) : null;
    }

    public boolean isAffectedByVelocity(Player p) {
        Velocity velocity = getLastVelocity(p.getUniqueId());
        return velocity != null && velocity.isWithinVelocityRange(p.getLocation());
    }

    public boolean hasPlayerMoveTimePassed(Player p, int milliseconds) {
        return hasPlayerMoveTimePassed(p.getUniqueId(), milliseconds);
    }

    public boolean hasPlayerMoveTimePassed(UUID pUUID, int milliseconds) {
        return System.currentTimeMillis() >= playerStartMoveTimes.get(pUUID) + milliseconds;
    }

    public Location getPlayerMoveStartLocation(Player p) {
        if (!playerStartMoveLocations.containsKey(p.getUniqueId()))
            return null;

        return playerStartMoveLocations.get(p.getUniqueId());
    }

    public long getPlayerLastTPTime(UUID pUUID) {
        if (!playerLastTPTimes.containsKey(pUUID))
            return -1;
        return playerLastTPTimes.get(pUUID);
    }

    public Location getPlayerLastTPLocation(UUID pUUID) {
        if (!playerLastTPLocations.containsKey(pUUID))
            return null;
        return playerLastTPLocations.get(pUUID);
    }

    public boolean isTeleportingTo(UUID pUUID, Location to) {
        Location tpTo = getPlayerLastTPLocation(pUUID);
        return isTeleporting(pUUID) && tpTo != null && MathUtils.isPositionSame(to, tpTo, 0);
    }

    public boolean isTeleporting(UUID pUUID) {
        return teleportedPlayers.contains(pUUID);
    }

    public boolean isPlayerOnGround(UUID pUUID) {
        if (!playerOnGround.containsKey(pUUID)) {
            playerOnGround.put(pUUID, AntiCheatUtils.getInstance().isPlayerOnGround(Bukkit.getPlayer(pUUID)));
        }
        return playerOnGround.get(pUUID);
    }

    public boolean isPlayerOnGround(Player p) {
        UUID pUUID = p.getUniqueId();
        if (!playerOnGround.containsKey(pUUID)) {
            playerOnGround.put(pUUID, AntiCheatUtils.getInstance().isPlayerOnGround(p));
        }
        return playerOnGround.get(pUUID);
    }

    public int getPlayerOnGroundMoves(UUID pUUID) {
        if (!playerOnGroundMoves.containsKey(pUUID)) {
            if (isPlayerOnGround(pUUID))
                playerOnGroundMoves.put(pUUID, 1);
            else
                playerOnGroundMoves.put(pUUID, 0);
        }
        return playerOnGroundMoves.get(pUUID);
    }

    public long getLastMoveTime(UUID pUUID) {
        if (!playerLastMoveTime.containsKey(pUUID))
            return -1;
        return playerLastMoveTime.get(pUUID);
    }
    //endregion

    //region Observator functions
    public void registerMovementCheck(MovementWatch movementCheck) {
        movementChecks.add(movementCheck);
    }

    public void unregisterMovementCheck(MovementWatch movementCheck) {
        movementChecks.remove(movementCheck);
    }
    //endregion
}
