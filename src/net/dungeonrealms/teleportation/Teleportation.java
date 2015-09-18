package net.dungeonrealms.teleportation;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.combat.CombatLog;
import net.dungeonrealms.mongo.DatabaseAPI;
import net.dungeonrealms.mongo.EnumData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
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

    public static HashMap<UUID, Integer> PLAYER_TELEPORTS = new HashMap<>();

    public static Location Harrison_Field;
    public static Location Dark_Oak_Tavern;
    public static Location Deadpeaks_Mountain_Camp;
    public static Location Trollsbane_tavern;
    public static Location Tripoli;
    public static Location Gloomy_Hollows;
    public static Location Crestguard_Keep;
    public static Location Cyrennica;
    public static Location Tutorial;

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
            for (Map.Entry<UUID, Integer> e : PLAYER_TELEPORTS.entrySet()) {
                Player player = Bukkit.getPlayer(e.getKey());
                if (e.getValue() <= 0 && !(player.hasMetadata("Hearthstone"))) {
                    player.setMetadata("Hearthstone", new FixedMetadataValue(DungeonRealms.getInstance(), player.getUniqueId()));
                    player.sendMessage("Your Hearthstone is ready to be used!");
                }
                PLAYER_TELEPORTS.put(e.getKey(), (e.getValue() - 1));
            }
        }, 0, 20L);
    }

    public static void teleportPlayer(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        String locationName = getLocationFromDatabase(uuid);
        Location location = getLocationFromString(locationName);

        if (location.equals(Cyrennica)) {
            player.sendMessage("Using your Hearthstone to return to Cyrennica.");
        } else if (location.equals(Harrison_Field)) {
            player.sendMessage("Using your Hearthstone to return to Harrison Field.");
        } else if (location.equals(Dark_Oak_Tavern)) {
            player.sendMessage("Using your Hearthstone to return to Dark Oak Tavern.");
        } else if (location.equals(Deadpeaks_Mountain_Camp)) {
            player.sendMessage("Using your Hearthstone to return to DeadPeaks Camp.");
        } else if (location.equals(Trollsbane_tavern)) {
            player.sendMessage("Using your Hearthstone to return to Trollsbane Tavern.");
        } else if (location.equals(Tripoli)) {
            player.sendMessage("Using your Hearthstone to return to Tripoli.");
        } else if (location.equals(Gloomy_Hollows)) {
            player.sendMessage("Using your Hearthstone to return to Gloomy Hollows.");
        } else if (location.equals(Crestguard_Keep)) {
            player.sendMessage("Using your Hearthstone to return to Crestgaurd Keep.");
        } else if (location.equals(Tutorial)) {
            player.sendMessage("Using your Hearthstone to return to the Tutorial Island");
        }

        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 2));
        player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 140, 1));
        player.playSound(player.getLocation(), Sound.AMBIENCE_CAVE, 1F, 1F);

        final int[] taskTimer = {5};
        int taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), () -> {

            if (taskTimer[0] <= 0) {
                if (CombatLog.isInCombat(uuid)) {
                    player.sendMessage("Your teleport has been interrupted by combat!");
                    player.removeMetadata("Hearthstone", DungeonRealms.getInstance());
                } else {
                    player.teleport(location);
                    player.removeMetadata("Hearthstone", DungeonRealms.getInstance());
                }
            }
            taskTimer[0]--;
        },0 ,20L);
        Teleportation.PLAYER_TELEPORTS.put(uuid, 300);
        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> Bukkit.getScheduler().cancelTask(taskID), 140L);
    }

    public static String getLocationFromDatabase(UUID uuid) {
        if (DatabaseAPI.getInstance().getData(EnumData.HEARTHSTONE, uuid) != null) {
            return DatabaseAPI.getInstance().getData(EnumData.HEARTHSTONE, uuid).toString();
        } else {
            return "cyrennica";
        }
    }

    public static Location getLocationFromString(String location) {
        switch (location) {
            case "starter": {
                return Tutorial;
            }
            case "cyrennica": {
                return Cyrennica;
            }
            case "harrison": {
                return Harrison_Field;
            }
            case "dark_oak": {
                return Dark_Oak_Tavern;
            }
            case "trollsbane": {
                return Trollsbane_tavern;
            }
            case "tripoli": {
                return Tripoli;
            }
            case "gloomy_hollows": {
                return Gloomy_Hollows;
            }
            case "crestguard": {
                return Crestguard_Keep;
            }
            default: {
                return Cyrennica;
            }
        }
    }
}
