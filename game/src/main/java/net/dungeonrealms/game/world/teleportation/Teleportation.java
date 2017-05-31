package net.dungeonrealms.game.world.teleportation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.ParticleAPI;
import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;
import net.dungeonrealms.game.player.combat.CombatLog;
import net.dungeonrealms.game.quests.Quests;
import net.dungeonrealms.game.quests.objectives.ObjectiveUseHearthStone;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by Kieran on 9/18/2015.
 */
public class Teleportation implements GenericMechanic {

	@Getter
    private static Teleportation instance = new Teleportation();

    public static HashMap<UUID, Integer> PLAYER_TELEPORT_COOLDOWNS = new HashMap<>();
    public static HashMap<UUID, Location> PLAYERS_TELEPORTING = new HashMap<>();

    //Avalon enter / exit
    public static Location Underworld;
    public static Location Overworld;

    @AllArgsConstructor @Getter
    public enum EnumTeleportType {
        HEARTHSTONE(Particle.SPELL, Particle.SPELL, 10),
        TELEPORT_BOOK(Particle.SPELL_WITCH, Particle.PORTAL, 5);
        
        private Particle particleA;
        private Particle particleB;
        private int tickDelay;
    }

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.CATHOLICS;
    }

    @Override
	public void startInitialization() {
        Underworld = new Location(Bukkit.getWorlds().get(0), -362, 172, -3440, -90F, 1F);
        Overworld = new Location(Bukkit.getWorlds().get(0), -1158, 96, -515, 91F, 1F);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), () -> {
            for (Map.Entry<UUID, Integer> e : PLAYER_TELEPORT_COOLDOWNS.entrySet())
                TeleportAPI.addPlayerHearthstoneCD(e.getKey(), (e.getValue() - 1));
        }, 20L, 20L);
    }

    @Override
    public void stopInvocation() {

    }

    /**
     * Teleports a player to a location.
     *
     * @param uuid
     * @param teleportType
     * @param nbt
     * @since 1.0
     */
    public void teleportPlayer(UUID uuid, EnumTeleportType teleportType, TeleportLocation location) {
        Player player = Bukkit.getPlayer(uuid);
        if (!GameAPI.isMainWorld(player.getWorld()))
            return;
        
        TeleportAPI.addPlayerCurrentlyTeleporting(uuid, player.getLocation());
        
        if (teleportType == EnumTeleportType.HEARTHSTONE) {
        	location = TeleportLocation.valueOf(TeleportAPI.getLocationFromDatabase(uuid).toUpperCase());
        } else {
        	player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 220, 2));
            player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 220, 1));
            player.playSound(player.getLocation(), Sound.AMBIENT_CAVE, 1F, 1.5F);
        }

        String message = ChatColor.WHITE.toString() + ChatColor.BOLD + "TELEPORTING" +  " - " + ChatColor.AQUA + location.getDisplayName();

        player.sendMessage(message);

        final int[] taskTimer = {teleportType.getTickDelay()}; 

        Location startingLocation = player.getLocation();
        final boolean[] hasCancelled = {false};
        final TeleportLocation teleportTo = location;
        int taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), () -> {
            if (TeleportAPI.isPlayerCurrentlyTeleporting(player.getUniqueId()) && !hasCancelled[0]) {
                if (player.getWorld().equals(Bukkit.getWorlds().get(0))) {
                    if (player.getLocation().distanceSquared(startingLocation) <= 4 && !CombatLog.isInCombat(player)) {
                        player.sendMessage(ChatColor.WHITE.toString() + ChatColor.BOLD + "TELEPORTING " + ChatColor.RESET + "... " + taskTimer[0] + "s");
                        ParticleAPI.spawnParticle(teleportType.getParticleA(), player.getLocation(), 250, 1F);
                        ParticleAPI.spawnParticle(teleportType.getParticleB(), player.getLocation(), 400, 4F);
                        
                        if (taskTimer[0] <= 0) {
                        	if (teleportType == EnumTeleportType.HEARTHSTONE)
                                TeleportAPI.addPlayerHearthstoneCD(uuid, 280);
                        	
                            if (CombatLog.isInCombat(player)) {
                                player.sendMessage(ChatColor.RED + "Your teleport has been interrupted by combat!");
                            } else {
                                GameAPI.teleport(player, teleportTo.getLocation());
                            }
                            
                            TeleportAPI.removePlayerCurrentlyTeleporting(uuid);
                        }
                        
                        taskTimer[0]--;
                    } else {
                        hasCancelled[0] = true;
                        if (teleportType == EnumTeleportType.TELEPORT_BOOK) {
                            player.removePotionEffect(PotionEffectType.BLINDNESS);
                            player.removePotionEffect(PotionEffectType.CONFUSION);
                        }
                        player.sendMessage(ChatColor.RED + "Your teleport was cancelled!");
                        if (teleportType == EnumTeleportType.HEARTHSTONE) {
                            TeleportAPI.addPlayerHearthstoneCD(uuid, 300);
                        }
                    }
                }
            }
        }, 0, 20L);
        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
            Bukkit.getScheduler().cancelTask(taskID);
            TeleportAPI.removePlayerCurrentlyTeleporting(uuid);
            Quests.getInstance().triggerObjective(player, ObjectiveUseHearthStone.class);
        }, (taskTimer[0] * 20L) + 10L);
    }
}
