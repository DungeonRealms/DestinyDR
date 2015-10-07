package net.dungeonrealms.karma;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.combat.CombatLog;
import net.dungeonrealms.mastery.Utils;
import net.dungeonrealms.mongo.DatabaseAPI;
import net.dungeonrealms.mongo.EnumData;
import net.dungeonrealms.mongo.EnumOperators;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Kieran on 10/7/2015.
 */
public class KarmaHandler {

    private static KarmaHandler instance = null;

    public static KarmaHandler getInstance() {
        if (instance == null) {
            instance = new KarmaHandler();
        }
        return instance;
    }

    public static HashMap<Player, EnumPlayerAlignments> playerAlignments = new HashMap<>();
    public static ConcurrentHashMap<Player, Integer> playerAlignmentTime = new ConcurrentHashMap<>();

    public enum EnumPlayerAlignments {
        LAWFUL(0, "lawful"),
        NEUTRAL(1, "neutral"),
        CHAOTIC(2, "chaotic");

        private int id;
        private String name;

        EnumPlayerAlignments(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public static EnumPlayerAlignments getByName(String rawName) {
            for (EnumPlayerAlignments particleEffect : values()) {
                if (particleEffect.name.equalsIgnoreCase(rawName)) {
                    return particleEffect;
                }
            }
            return null;
        }
    }

    public void startInitialization() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(DungeonRealms.getInstance(), this::updateAllPlayerAlignments, 100L, 20L);
    }

    private void updateAllPlayerAlignments() {
        for (Map.Entry<Player, EnumPlayerAlignments> alignment : playerAlignments.entrySet()) {
            Player player = alignment.getKey();
            EnumPlayerAlignments currentAlignment = alignment.getValue();

            if (!(playerAlignmentTime.containsKey(player))) {
                continue;
            }
            if (CombatLog.isInCombat(player.getUniqueId())) {
                continue;
            }
            if (currentAlignment.equals(EnumPlayerAlignments.LAWFUL)) {
                if (playerAlignmentTime.containsKey(player)) {
                    playerAlignmentTime.remove(player);
                }
                continue;
            }
            if (!(player.getWorld().getName().equalsIgnoreCase(DungeonRealms.getInstance().getServer().getWorlds().get(0).getName()))) {
                continue;
            }

            int timeLeft = playerAlignmentTime.get(player);
            timeLeft--;

            if (timeLeft <= 0) {
                if (currentAlignment.equals(EnumPlayerAlignments.CHAOTIC)) {
                    playerAlignments.put(player, EnumPlayerAlignments.NEUTRAL);
                    playerAlignmentTime.put(player, 120);
                } else if (currentAlignment.equals(EnumPlayerAlignments.NEUTRAL)) {
                    playerAlignments.put(player, EnumPlayerAlignments.LAWFUL);
                    playerAlignmentTime.remove(player);
                }
            } else {
                playerAlignmentTime.put(player, timeLeft);
            }
        }
    }

    public static void handleLoginEvents(Player player) {
        setPlayerAlignment(player, getAlignmentOnLogin(player.getUniqueId()));
    }

    public static void handleLogoutEvents(Player player) {
        DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, "info.alignment", getPlayerRawAlignment(player), false);
    }

    public static String getPlayerRawAlignment(Player player) {
        if (playerAlignments.containsKey(player)) {
            return playerAlignments.get(player).name;
        }
        return "lawful"; //Should never happen, but safety checks.
    }

    public static void setPlayerAlignment(Player player, String alignmentRawName) {
        EnumPlayerAlignments alignment = EnumPlayerAlignments.getByName(alignmentRawName);
        if (alignment != null) {
            switch (alignment) {
                case LAWFUL:
                    playerAlignments.put(player, alignment);
                    break;
                case NEUTRAL:
                    playerAlignmentTime.put(player, 120);
                    playerAlignments.put(player, alignment);
                    break;
                case CHAOTIC:
                    playerAlignmentTime.put(player, 120);
                    playerAlignments.put(player, alignment);
                    break;
                default:
                    Utils.log.info("[KARMA] Could not set player " + player.getName() + "'s alignment! UH OH");
                    break;
            }
        }
    }

    public static String getAlignmentOnLogin(UUID uuid) {
        if (DatabaseAPI.getInstance().getData(EnumData.ALIGNMENT, uuid) != null) {
            return String.valueOf(DatabaseAPI.getInstance().getData(EnumData.ALIGNMENT, uuid));
        } else {
            return "lawful"; //Safety check, but should never return that
        }
    }

}
