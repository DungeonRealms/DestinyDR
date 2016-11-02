package net.dungeonrealms.vgame.anticheat.movement.watch;

import net.dungeonrealms.old.DungeonRealms;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.vgame.anticheat.flag.FlagHandler;
import net.dungeonrealms.vgame.anticheat.flag.Hack;
import net.dungeonrealms.vgame.anticheat.utils.AntiCheatUtils;
import net.dungeonrealms.vgame.anticheat.movement.MovementBase;
import net.dungeonrealms.vgame.anticheat.movement.MovementWatch;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

/**
 * Copyright © 2016 Matthew E Development - All Rights Reserved
 * You may NOT use, distribute and modify this code.
 * <p>
 * Created by Matthew E on 10/30/2016 at 11:59 AM.
 */
public class SpeedWatch extends MovementWatch {

    private MovementBase movementBase;

    private double sprinting = 0.83;
    private double sneaking = 0.215;
    private double cobweb = 0.2;
    private double ice = 1.7;
    private double jump = 1.8;
    private double velocity = 2;
    private double speedPotion = 1.45;
    private double slowPotion = 0.8;
    private double stairs = 1.5;

    private AntiCheatUtils utils = AntiCheatUtils.getInstance();

    public SpeedWatch(MovementBase movementBase) {
        super(movementBase);
        Bukkit.getServer().getPluginManager().registerEvents(this, DungeonRealms.getInstance());
    }

    @Override
    public void check(Player player) {

    }

    @Override
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player p = event.getPlayer();
        UUID pUUID = p.getUniqueId();
        if (Rank.isGM(p) || p.getAllowFlight() || p.isInsideVehicle())
            return;

        if (utils.isUnderBlock(p))
            return;
        if (utils.isGlidingWithElytra(p))
            return;

        Location from = event.getFrom();
        Location to = event.getTo();
        double dX = to.getX() - from.getX();
        double dY = to.getY() - from.getY();
        double dZ = to.getZ() - from.getZ();
        double distSq = dX * dX + dZ * dZ;

        if (movementBase.isTeleporting(pUUID))
            return;

        double speed = sprinting;
        if (p.isSneaking() && !movementBase.hasVelocityTimePassed(pUUID, 1000))
            speed = sneaking;
        if (utils.isOnIce(p, false))
            speed = ice;
        if (utils.isInWeb(p.getLocation()))
            speed = cobweb;
        if (utils.isOnStair(p))
            speed *= stairs;
        if (isJumping(p, from, to))
            speed *= jump;
        if (!movementBase.hasVelocityTimePassed(pUUID, 1000)) {
            speed *= velocity;
        }
        if (p.hasPotionEffect(PotionEffectType.SPEED))
            speed *= (utils.getPotionEffect(p, PotionEffectType.SPEED).getAmplifier() + 1) * speedPotion;
        if (p.hasPotionEffect(PotionEffectType.SLOW))
            speed *= (utils.getPotionEffect(p, PotionEffectType.SLOW).getAmplifier() + 1) * slowPotion;
        speed *= 0.1;

        if (distSq > speed) {
            p.teleport(event.getFrom());
            FlagHandler.getInstance().flag(p, Hack.SPEED);
            return;
        }
    }

    private boolean isJumping(Player player, Location from, Location to) {
        boolean stepping = false;
        if (to.getY() > from.getY()) {
            stepping = AntiCheatUtils.getInstance().isOnSteps(player);
        }
        return ((!movementBase.isPlayerOnGround(player)) || (!AntiCheatUtils.getInstance().isLocationOnGround(to)) || (movementBase.getPlayerOnGroundMoves(player.getUniqueId()) <= 3) || (stepping));
    }
}
