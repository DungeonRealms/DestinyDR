package net.dungeonrealms.handlers;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.combat.CombatLog;
import net.dungeonrealms.mastery.Utils;
import net.dungeonrealms.mongo.DatabaseAPI;
import net.dungeonrealms.mongo.EnumData;
import net.dungeonrealms.mongo.EnumOperators;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.*;

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
            if (CombatLog.isInCombat(player)) {
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
                    setPlayerAlignment(player, EnumPlayerAlignments.NEUTRAL.name);
                } else if (currentAlignment.equals(EnumPlayerAlignments.NEUTRAL)) {
                    setPlayerAlignment(player, EnumPlayerAlignments.LAWFUL.name);
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
                    player.sendMessage("");
                    player.sendMessage(ChatColor.GREEN + "              " + "* YOU ARE NOW " + ChatColor.BOLD + " LAWFUL " + ChatColor.GREEN + "ALIGNMENT *");
                    player.sendMessage(ChatColor.GRAY + "While lawful, you will not lose any equipped armor on death, instead, all armor will lose 30% of its durability when you die.");
                    player.sendMessage("");
                    break;
                case NEUTRAL:
                    playerAlignmentTime.put(player, 120);
                    playerAlignments.put(player, alignment);
                    player.sendMessage("");
                    player.sendMessage(ChatColor.YELLOW + "              " + "* YOU ARE NOW " + ChatColor.BOLD + " NEUTRAL " + ChatColor.YELLOW + "ALIGNMENT *");
                    player.sendMessage(ChatColor.GRAY + "While neutral, you have a 50% chance of dropping your weapon, and a 25% chance of dropping each piece of equipped armor on death.");
                    player.sendMessage("");
                    break;
                case CHAOTIC:
                    playerAlignmentTime.put(player, 1200);
                    playerAlignments.put(player, alignment);
                    player.sendMessage("");
                    player.sendMessage(ChatColor.RED + "              " + "* YOU ARE NOW " + ChatColor.BOLD + " CHAOTIC " + ChatColor.RED + "ALIGNMENT *");
                    player.sendMessage(ChatColor.GRAY + "While chaotic, you cannot enter any major cities or safe zones. If you are killed while chaotic, you will lose everything in your inventory.");
                    player.sendMessage("");
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

    public static void handlePlayerPsuedoDeath(Player player, Entity killer) {
        LivingEntity leKiller = null;
        switch (killer.getType()) {
            case ARROW:
                Arrow attackingArrow = (Arrow) killer;
                if (!(attackingArrow.getShooter() instanceof LivingEntity)) break;
                leKiller = (LivingEntity) attackingArrow.getShooter();
                break;
            case WITHER_SKULL:
                WitherSkull witherSkull = (WitherSkull) killer;
                if (!(witherSkull.getShooter() instanceof LivingEntity)) break;
                leKiller = (LivingEntity) witherSkull.getShooter();
                break;
            case PLAYER:
                leKiller = (LivingEntity) killer;
                break;
            default:
                Utils.log.info("[KARMA] Could not find deathcause for player " + player.getName());
                break;
        }
        Player killerPlayer;
        if (leKiller instanceof Player) {
            killerPlayer = (Player) leKiller;
            String alignmentPlayer = getPlayerRawAlignment(player);
            String alignmentKiller = getPlayerRawAlignment(killerPlayer);
            if (alignmentPlayer.equalsIgnoreCase(EnumPlayerAlignments.LAWFUL.name)) {
                setPlayerAlignment(killerPlayer, EnumPlayerAlignments.CHAOTIC.name);
            } else if (alignmentPlayer.equalsIgnoreCase(EnumPlayerAlignments.NEUTRAL.name)) {
                setPlayerAlignment(killerPlayer, alignmentKiller);
            }
        }
    }

    public static void handleAlignmentChanges(Player player) {
        String alignmentPlayer = getPlayerRawAlignment(player);
        if (alignmentPlayer.equalsIgnoreCase(EnumPlayerAlignments.LAWFUL.name)) {
            setPlayerAlignment(player, EnumPlayerAlignments.NEUTRAL.name);
        } else if (alignmentPlayer.equalsIgnoreCase(EnumPlayerAlignments.NEUTRAL.name)) {
            setPlayerAlignment(player, EnumPlayerAlignments.NEUTRAL.name);
        }
    }
}
