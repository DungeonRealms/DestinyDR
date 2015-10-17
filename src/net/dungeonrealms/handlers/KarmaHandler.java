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
            for (EnumPlayerAlignments playerAlignments : values()) {
                if (playerAlignments.name.equalsIgnoreCase(rawName)) {
                    return playerAlignments;
                }
            }
            return null;
        }
    }

    public void startInitialization() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(DungeonRealms.getInstance(), this::updateAllPlayerAlignments, 100L, 20L);
    }

    /**
     * Updates all player alignments
     * from Chaotic->Neutral or Neutral->Lawful
     * if they are not in combat and in the
     * main world
     *
     * @since 1.0
     */
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

    /**
     * Handles players logging in,
     * sets their alignment based on
     * their mongo document.
     *
     * @param player
     * @since 1.0
     */
    public void handleLoginEvents(Player player) {
        setPlayerAlignment(player, getAlignmentOnLogin(player.getUniqueId()));
    }

    /**
     * Handles players logging out,
     * updates mongo document with
     * their alignment.
     *
     * @param player
     * @since 1.0
     */
    public void handleLogoutEvents(Player player) {
        DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, "info.alignment", getPlayerRawAlignment(player), false);
    }

    /**
     * Returns the players current alignment
     * as a string.
     *
     * @param player
     * @return String
     * @since 1.0
     */
    public String getPlayerRawAlignment(Player player) {
        if (playerAlignments.containsKey(player)) {
            return playerAlignments.get(player).name;
        }
        return "lawful"; //Should never happen, but safety checks.
    }

    /**
     * Sets the alignment of a specific player
     * adds them to hashmap with cooldown
     * if applicable and sends them a message
     * detailing what that alignment causes.
     *
     * @param player
     * @param alignmentRawName
     * @since 1.0
     */
    public void setPlayerAlignment(Player player, String alignmentRawName) {
        EnumPlayerAlignments alignment = EnumPlayerAlignments.getByName(alignmentRawName);
        String playerAlignment = getPlayerRawAlignment(player);
        if (alignment != null) {
            switch (alignment) {
                case LAWFUL:
                    if (!(playerAlignment.equalsIgnoreCase(EnumPlayerAlignments.LAWFUL.name))) {
                        player.sendMessage(new String[]{
                                "",
                                ChatColor.GREEN + "              " + "* YOU ARE NOW " + ChatColor.BOLD + ChatColor.UNDERLINE + "LAWFUL" + ChatColor.RESET + ChatColor.GREEN + " ALIGNMENT *",
                                ChatColor.GRAY + "While lawful, you will not lose any equipped armor on death, instead, all armor will lose 30% of its durability when you die.",
                                ""
                        });
                    }
                    playerAlignments.put(player, alignment);
                    break;
                case NEUTRAL:
                    if (!(playerAlignment.equalsIgnoreCase(EnumPlayerAlignments.NEUTRAL.name))) {
                        player.sendMessage(new String[]{
                                "",
                                ChatColor.YELLOW + "              " + "* YOU ARE NOW " + ChatColor.BOLD + ChatColor.UNDERLINE + "NEUTRAL" + ChatColor.RESET + ChatColor.YELLOW + " ALIGNMENT *",
                                ChatColor.GRAY + "While neutral, you have a 50% chance of dropping your weapon, and a 25% chance of dropping each piece of equipped armor on death.",
                                ""
                        });
                    }
                    playerAlignmentTime.put(player, 120);
                    playerAlignments.put(player, alignment);
                    break;
                case CHAOTIC:
                    if (!(playerAlignment.equalsIgnoreCase(EnumPlayerAlignments.CHAOTIC.name))) {
                        player.sendMessage(new String[]{
                                "",
                                ChatColor.RED + "              " + "* YOU ARE NOW " + ChatColor.BOLD + ChatColor.UNDERLINE + "CHAOTIC" + ChatColor.RESET + ChatColor.RED + " ALIGNMENT *",
                                ChatColor.GRAY + "While chaotic, you cannot enter any major cities or safe zones. If you are killed while chaotic, you will lose everything in your inventory.",
                                ""
                        });
                    }
                    playerAlignmentTime.put(player, 1200);
                    playerAlignments.put(player, alignment);
                    break;
                default:
                    Utils.log.info("[KARMA] Could not set player " + player.getName() + "'s alignment! UH OH");
                    break;
            }
        }
    }

    /**
     * Returns the players current alignment
     * from Mongo Doc as a string.
     *
     * @param uuid
     * @return String
     * @since 1.0
     */
    public static String getAlignmentOnLogin(UUID uuid) {
        if (DatabaseAPI.getInstance().getData(EnumData.ALIGNMENT, uuid) != null && !String.valueOf(DatabaseAPI.getInstance().getData(EnumData.ALIGNMENT, uuid)).equalsIgnoreCase("")) {
            return String.valueOf(DatabaseAPI.getInstance().getData(EnumData.ALIGNMENT, uuid));
        } else {
            return "lawful"; //Safety check, but should never return that
        }
    }

    /**
     * Handles when the player "dies" in combat
     * Checks to see if their killer should change alignment
     * and changes it if they should.
     *
     * @param player
     * @param killer
     * @since 1.0
     */
    public void handlePlayerPsuedoDeath(Player player, Entity killer) {
        LivingEntity leKiller = null;
        switch (killer.getType()) {
            case ARROW:
                Arrow attackingArrow = (Arrow) killer;
                if (!(attackingArrow.getShooter() instanceof LivingEntity)) break;
                leKiller = (LivingEntity) attackingArrow.getShooter();
                break;
            case SNOWBALL:
                Snowball snowball = (Snowball) killer;
                if (!(snowball.getShooter() instanceof LivingEntity)) break;
                leKiller = (LivingEntity) snowball.getShooter();
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

    /**
     * Handles when the player attacks another player
     * Checks to see if they should change alignment
     * and changes it if they should.
     *
     * @param player
     * @since 1.0
     */
    public void handleAlignmentChanges(Player player) {
        String alignmentPlayer = getPlayerRawAlignment(player);
        if (alignmentPlayer.equalsIgnoreCase(EnumPlayerAlignments.LAWFUL.name)) {
            setPlayerAlignment(player, EnumPlayerAlignments.NEUTRAL.name);
        } else if (alignmentPlayer.equalsIgnoreCase(EnumPlayerAlignments.NEUTRAL.name)) {
            setPlayerAlignment(player, EnumPlayerAlignments.NEUTRAL.name);
        }
    }
}
