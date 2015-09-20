package net.dungeonrealms.teleportation;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.combat.CombatLog;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Kieran on 9/18/2015.
 */
public class Teleportation {

    static Teleportation instance = null;
    public static Teleportation getInstance() {
        if (instance == null) {
            return new Teleportation();
        }
        return instance;
    }

    public static HashMap<UUID, Integer> PLAYER_TELEPORT_COOLDOWNS = new HashMap<>();
    public static HashMap<UUID, Location> PLAYERS_TELEPORTING = new HashMap<>();

    public static Location Harrison_Field;
    public static Location Dark_Oak_Tavern;
    public static Location Deadpeaks_Mountain_Camp;
    public static Location Trollsbane_tavern;
    public static Location Tripoli;
    public static Location Gloomy_Hollows;
    public static Location Crestguard_Keep;
    public static Location Cyrennica;
    public static Location Tutorial;

    public enum EnumTeleportType {
        HEARTHSTONE(0, "Hearthstone"),
        TELEPORT_BOOK(1, "Teleport Book");

        private int id;
        private String name;

        EnumTeleportType(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    public void startInitialization() {
        Cyrennica = new Location(Bukkit.getWorlds().get(0), -367, 83, 390);
        Harrison_Field = new Location(Bukkit.getWorlds().get(0), -594, 58, 687, 92.0F, 1F);
        Dark_Oak_Tavern = new Location(Bukkit.getWorlds().get(0), 280, 58, 1132, 2.0F, 1F);
        Deadpeaks_Mountain_Camp = new Location(Bukkit.getWorlds().get(0), -1173, 105, 1030, -88.0F, 1F);
        Trollsbane_tavern = new Location(Bukkit.getWorlds().get(0), 962, 94, 1069, -153.0F, 1F);
        Tripoli = new Location(Bukkit.getWorlds().get(0), -1320, 90, 370, 153F, 1F);
        Gloomy_Hollows = new Location(Bukkit.getWorlds().get(0), -590, 43, 0, 144F, 1F);
        Crestguard_Keep = new Location(Bukkit.getWorlds().get(0), -1428, 115, -489, 95F, 1F);
        Tutorial = new Location(Bukkit.getWorlds().get(0), 824, 48, -103, 124F, 1F);

        Bukkit.getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), () -> {
            for (Map.Entry<UUID, Integer> e : PLAYER_TELEPORT_COOLDOWNS.entrySet()) {
                TeleportAPI.addPlayerHearthstoneCD(e.getKey(), (e.getValue() - 1));
            }
        }, 0, 20L);
    }

    public static void teleportPlayer(UUID uuid, EnumTeleportType teleportType, NBTTagCompound nbt) {
        Player player = Bukkit.getPlayer(uuid);
        TeleportAPI.addPlayerCurrentlyTeleporting(uuid, player.getLocation());
        String locationName;
        if (teleportType == EnumTeleportType.HEARTHSTONE) {
            locationName = TeleportAPI.getLocationFromDatabase(uuid);
        } else {
            if (nbt != null) {
                locationName = nbt.getString("usage").toLowerCase();
            } else {
                locationName = "cyrennica";
            }
        }
        Location location = TeleportAPI.getLocationFromString(locationName);

        if (location.equals(Cyrennica)) {
            player.sendMessage("Teleporting to Cyrennica.");
        } else if (location.equals(Harrison_Field)) {
            player.sendMessage("Teleporting to Harrison Field.");
        } else if (location.equals(Dark_Oak_Tavern)) {
            player.sendMessage("Teleporting to Dark Oak Tavern.");
        } else if (location.equals(Deadpeaks_Mountain_Camp)) {
            player.sendMessage("Teleporting to return to DeadPeaks Camp.");
        } else if (location.equals(Trollsbane_tavern)) {
            player.sendMessage("Teleporting to Trollsbane Tavern.");
        } else if (location.equals(Tripoli)) {
            player.sendMessage("Teleporting to return to Tripoli.");
        } else if (location.equals(Gloomy_Hollows)) {
            player.sendMessage("Teleporting to Gloomy Hollows.");
        } else if (location.equals(Crestguard_Keep)) {
            player.sendMessage("Teleporting to Crestgaurd Keep.");
        } else if (location.equals(Tutorial)) {
            player.sendMessage("Teleporting to the Tutorial Island");
        }

        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 220, 2));
        player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 220, 1));
        player.playSound(player.getLocation(), Sound.AMBIENCE_CAVE, 1F, 1F);

        final int[] taskTimer = {6};
        int taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), () -> {

            if (TeleportAPI.isPlayerCurrentlyTeleporting(player.getUniqueId())) {
                if (player.getLocation().getX() == PLAYERS_TELEPORTING.get(player.getUniqueId()).getX() && player.getLocation().getZ() == PLAYERS_TELEPORTING.get(player.getUniqueId()).getZ()) {
                    if (taskTimer[0] <= 0) {
                        if (CombatLog.isInCombat(uuid)) {
                            player.sendMessage("Your teleport has been interrupted by combat!");
                            if (teleportType == EnumTeleportType.HEARTHSTONE) {
                                TeleportAPI.addPlayerHearthstoneCD(uuid, 300);
                            }
                        } else {
                            player.teleport(location);
                            if (teleportType == EnumTeleportType.HEARTHSTONE) {
                                TeleportAPI.addPlayerHearthstoneCD(uuid, 300);
                            }
                        }
                        TeleportAPI.removePlayerCurrentlyTeleporting(uuid);
                    }
                    taskTimer[0]--;
                } else {
                    TeleportAPI.removePlayerCurrentlyTeleporting(uuid);
                    player.removePotionEffect(PotionEffectType.BLINDNESS);
                    player.removePotionEffect(PotionEffectType.CONFUSION);
                    player.sendMessage("Your teleport was canceled due to moving!");
                    if (teleportType == EnumTeleportType.HEARTHSTONE) {
                        TeleportAPI.addPlayerHearthstoneCD(uuid, 500);
                    }
                }
            }
        },0 ,20L);
        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> Bukkit.getScheduler().cancelTask(taskID), 160L);
    }
}
