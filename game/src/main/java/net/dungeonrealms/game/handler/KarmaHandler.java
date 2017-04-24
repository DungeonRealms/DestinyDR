package net.dungeonrealms.game.handler;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Created by Kieran on 10/7/2015.
 */
public class KarmaHandler implements GenericMechanic {

    private static KarmaHandler instance = null;

    public static KarmaHandler getInstance() {
        if (instance == null) {
            instance = new KarmaHandler();
        }
        return instance;
    }

    //    public static HashMap<Player, EnumPlayerAlignments> PLAYER_ALIGNMENTS = new HashMap<>();
//    public static ConcurrentHashMap<Player, Integer> PLAYER_ALIGNMENT_TIMES = new ConcurrentHashMap<>();
    public static HashMap<Player, EnumPlayerAlignments> PLAYER_LOCATIONS = new HashMap<>();
    public static List<Location> CHAOTIC_RESPAWNS = new ArrayList<>();

    private static int CHAOTIC_COOLDOWN = 1200;
    private static int NEUTRAL_COOLDOWN = 120;

    public enum EnumPlayerAlignments {
        LAWFUL(0, "lawful", ChatColor.WHITE, "-30% Durability Keep Arm/Wep on Death"),
        NEUTRAL(1, "neutral", ChatColor.YELLOW, "25%/50% Lose Arm/Wep on Death"),
        CHAOTIC(2, "chaotic", ChatColor.RED, "Inventory LOST on Death"),
        NONE(3, "none", ChatColor.GRAY, "-30% Durability Keep Arm/Wep on Death");

        private int id;
        private String name;
        private ChatColor alignmentColor;
        public String description;

        EnumPlayerAlignments(int id, String name, ChatColor alignmentColor, String description) {
            this.id = id;
            this.name = name;
            this.alignmentColor = alignmentColor;
            this.description = description;
        }

        public static EnumPlayerAlignments getByName(String rawName) {
            for (EnumPlayerAlignments playerAlignments : values()) {
                if (playerAlignments.name.equalsIgnoreCase(rawName)) {
                    return playerAlignments;
                }
            }
            return null;
        }

        public ChatColor getAlignmentColor() {
            return alignmentColor;
        }
    }

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.POPE;
    }

    @Override
    public void startInitialization() {
        CHAOTIC_RESPAWNS.add(new Location(Bukkit.getWorlds().get(0), -382, 68, 867));
        CHAOTIC_RESPAWNS.add(new Location(Bukkit.getWorlds().get(0), -350, 67, 883));
        CHAOTIC_RESPAWNS.add(new Location(Bukkit.getWorlds().get(0), -330, 65, 898));
        CHAOTIC_RESPAWNS.add(new Location(Bukkit.getWorlds().get(0), -419, 61, 830));

        Bukkit.getScheduler().runTaskTimer(DungeonRealms.getInstance(), this::updateAllPlayerAlignments, 100L, 20L);
    }

    @Override
    public void stopInvocation() {

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
        PlayerWrapper.getPlayerWrappers().values().stream().filter(wrap -> wrap.getPlayer() != null && wrap.getPlayer().isOnline() && wrap.getAlignmentTime() != -1 && GameAPI.isMainWorld(wrap.getPlayer().getWorld()))
                .forEach(wrap -> {
                    int timeLeft = wrap.getAlignmentTime();
                    timeLeft--;
                    if (timeLeft <= 0) {
                        EnumPlayerAlignments currentAlignment = wrap.getPlayerAlignment();
                        if (currentAlignment == EnumPlayerAlignments.CHAOTIC) {
                            setPlayerAlignment(wrap.getPlayer(), EnumPlayerAlignments.NEUTRAL, currentAlignment, false);
//                            wrap.setAlignmentTime(NEUTRAL_COOLDOWN);
                        } else if (currentAlignment == EnumPlayerAlignments.NEUTRAL) {
                            setPlayerAlignment(wrap.getPlayer(), EnumPlayerAlignments.LAWFUL, currentAlignment, false);
                        }
                    } else {
                        wrap.setAlignmentTime(timeLeft);
                    }
                });
//        for (PlayerWrapper wrapper : PlayerWrapper.getPlayerWrappers().values()) {
//            if (wrapper.getAlignmentTime() == -1) continue;


//            Player player = alignment.getKey();
//            if (!(PLAYER_ALIGNMENT_TIMES.containsKey(player))) {
//                continue;
//            }
//            Player player = wrapper.getPlayer();
//            if (!player.getWorld().equals(Bukkit.getServer().getWorlds().get(0))) {
//                continue;
//            }
//
//            int timeLeft = PLAYER_ALIGNMENT_TIMES.get(player);
//            timeLeft--;
//
//            if (timeLeft <= 0) {
//                EnumPlayerAlignments currentAlignment = alignment.getValue();
//                if (currentAlignment == EnumPlayerAlignments.CHAOTIC) {
//                    setPlayerAlignment(player, EnumPlayerAlignments.NEUTRAL, currentAlignment, false);
//                    PLAYER_ALIGNMENT_TIMES.put(player, NEUTRAL_COOLDOWN);
//                } else if (currentAlignment == EnumPlayerAlignments.NEUTRAL) {
//                    setPlayerAlignment(player, EnumPlayerAlignments.LAWFUL, currentAlignment, false);
//                    PLAYER_ALIGNMENT_TIMES.remove(player);
//                }
//            } else if (timeLeft > 0) {
//                PLAYER_ALIGNMENT_TIMES.put(player, timeLeft);
//            }
//        }
    }

    /**
     * Handles players logging in,
     * sets their alignment based on
     * their database document.
     *
     * @param player
     * @since 1.0
     */
    public void handleLoginEvents(Player player) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> setPlayerAlignment(player, EnumPlayerAlignments.getByName(getAlignmentOnLogin(player.getUniqueId())), null, true), 20L);
    }

    /**
     * Handles players logging out,
     * updates database document with
     * their alignment.
     *
     * @param player
     * @since 1.0
     */
//    public void handleLogoutEvents(Player player) {
//        if (PLAYER_ALIGNMENT_TIMES.containsKey(player))
//            PLAYER_ALIGNMENT_TIMES.remove(player);
//    }

//    public void saveToMongo(Player player) {
//        int alignmentTime = 0;
//        if (PLAYER_ALIGNMENT_TIMES.containsKey(player))
//            alignmentTime = PLAYER_ALIGNMENT_TIMES.get(player);
//        if (alignmentTime > 0) {
//            DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.ALIGNMENT_TIME, alignmentTime, true);
//        } else {
//            DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.ALIGNMENT_TIME, 0, true);
//        }
//        DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.ALIGNMENT, getPlayerRawAlignment(player).name, true);
//    }

    /**
     * Sets the alignment of a specific player
     * adds them to hashmap with cooldown
     * if applicable and sends them a message
     * detailing what that alignment causes.
     *
     * @param player
     * @param alignmentTo
     * @since 1.0
     */
    public void setPlayerAlignment(Player player, EnumPlayerAlignments alignmentTo, EnumPlayerAlignments alignmentFrom, boolean login) {
        GamePlayer gamePlayer = GameAPI.getGamePlayer(player);
        if (gamePlayer == null) {
            return;
        }
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
        if (wrapper == null) return;
        EnumPlayerAlignments alignmentPlayer = alignmentFrom != null ? alignmentFrom : wrapper.getPlayerAlignment();
        int alignmentTime = wrapper.getAlignmentTime();

        if (alignmentTo == null || alignmentTo == EnumPlayerAlignments.NONE) {
            alignmentTo = EnumPlayerAlignments.LAWFUL;
        }

        switch (alignmentTo) {
            case LAWFUL:
                // Don't show alignment on player login.
                if (!login) {
                    player.sendMessage(new String[]{
                            "",
                            ChatColor.GREEN + "              " + "* YOU ARE NOW " + ChatColor.BOLD + ChatColor.UNDERLINE + "LAWFUL" + ChatColor.RESET + ChatColor.GREEN + " ALIGNMENT *",
                            ChatColor.GRAY + "While lawful, you will not lose any equipped armor on death, instead, all armor will lose 30% of its durability when you die.",
                            ""
                    });
                    alignmentTime = 0;
                }
                break;
            case NEUTRAL:
                if ((alignmentPlayer != EnumPlayerAlignments.NEUTRAL) && !login) {
                    player.sendMessage(new String[]{
                            "",
                            ChatColor.YELLOW + "              " + "* YOU ARE NOW " + ChatColor.BOLD + ChatColor.UNDERLINE + "NEUTRAL" + ChatColor.RESET + ChatColor.YELLOW + " ALIGNMENT *",
                            ChatColor.GRAY + "While neutral, you have a 50% chance of dropping your weapon, and a 25% chance of dropping each piece of equipped armor on death.",
                            ""
                    });
                }
                alignmentTime = NEUTRAL_COOLDOWN;
                break;
            case CHAOTIC:
                if ((alignmentPlayer != EnumPlayerAlignments.CHAOTIC) && !login) {
                    player.sendMessage(new String[]{
                            "",
                            ChatColor.RED + "              " + "* YOU ARE NOW " + ChatColor.BOLD + ChatColor.UNDERLINE + "CHAOTIC" + ChatColor.RESET + ChatColor.RED + " ALIGNMENT *",
                            ChatColor.GRAY + "While chaotic, you cannot enter any major cities or safe zones. If you are killed while chaotic, you will lose everything in your inventory.",
                            ""
                    });
                }
                alignmentTime += login ? 10 : CHAOTIC_COOLDOWN;
                break;
            default:
                Utils.log.info("[KARMA] Could not set player " + player.getName() + "'s alignment! UH OH");
                break;
        }

        wrapper.setAlignmentTime(alignmentTime);
        if (alignmentPlayer != alignmentTo) {
//            PLAYER_ALIGNMENTS.put(player, alignmentTo);
            ScoreboardHandler.getInstance().setPlayerHeadScoreboard(player, alignmentTo.alignmentColor, gamePlayer.getLevel());
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
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(uuid);
        if (wrapper == null) return null;
        return wrapper.getPlayerAlignment().name;
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
            case TIPPED_ARROW:
            case SNOWBALL:
            case SMALL_FIREBALL:
            case ENDER_PEARL:
            case FIREBALL:
            case WITHER_SKULL:
                Projectile projectile = (Projectile) killer;
                if (!(projectile.getShooter() instanceof LivingEntity)) break;
                leKiller = (LivingEntity) projectile.getShooter();
                break;
            case PLAYER:
                leKiller = (LivingEntity) killer;
                break;
            default:
                break;
        }
        Player killerPlayer;
        if (GameAPI.isPlayer(leKiller)) {
            killerPlayer = (Player) leKiller;
            GamePlayer deathGP = GameAPI.getGamePlayer(player);
            PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
            PlayerWrapper killerWrapper = PlayerWrapper.getPlayerWrapper(killerPlayer);
            if (deathGP != null) {
                wrapper.getPlayerGameStats().setDeaths(wrapper.getPlayerGameStats().getDeaths() + 1);
            }
            EnumPlayerAlignments alignmentPlayer = wrapper.getPlayerAlignment();
            GamePlayer killerGP = GameAPI.getGamePlayer(killerPlayer);
            if (killerGP != null) {
                if (killerGP.hasNewbieProtection()) {
                    ProtectionHandler.getInstance().removePlayerProtection(killerPlayer);
                }
                killerWrapper.getPlayerGameStats().setPlayerKills(killerWrapper.getPlayerGameStats().getPlayerKills() + 1);
                if (alignmentPlayer == EnumPlayerAlignments.LAWFUL) {
                    killerWrapper.getPlayerGameStats().setLawfulKills(killerWrapper.getPlayerGameStats().getLawfulKills() + 1);
                } else {
                    killerWrapper.getPlayerGameStats().setUnlawfulKills(killerWrapper.getPlayerGameStats().getUnlawfulKills() + 1);
                }
            }
            EnumPlayerAlignments alignmentKiller = killerWrapper.getPlayerAlignment();
            if (alignmentPlayer == EnumPlayerAlignments.LAWFUL) {
                setPlayerAlignment(killerPlayer, EnumPlayerAlignments.CHAOTIC, alignmentKiller, false);
            } else {
                setPlayerAlignment(killerPlayer, alignmentKiller, alignmentKiller, false);
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
    public void handleAlignmentChanges(Player player, PlayerWrapper wrapper) {
        EnumPlayerAlignments alignmentPlayer = wrapper.getPlayerAlignment();
        if (alignmentPlayer == EnumPlayerAlignments.LAWFUL) {
            setPlayerAlignment(player, EnumPlayerAlignments.NEUTRAL, alignmentPlayer, false);
        } else if (alignmentPlayer == EnumPlayerAlignments.NEUTRAL) {
            wrapper.setAlignmentTime(NEUTRAL_COOLDOWN);
        }
    }

    public void tellPlayerRegionInfo(Player player) {
        if (!PLAYER_LOCATIONS.containsKey(player)) {
            PLAYER_LOCATIONS.put(player, EnumPlayerAlignments.NONE);
            return;
        }
        if (GameAPI.isInSafeRegion(player.getLocation()) && !PLAYER_LOCATIONS.get(player).equals(EnumPlayerAlignments.LAWFUL)) {
            Utils.sendCenteredMessage(player, ChatColor.GREEN.toString() + ChatColor.BOLD + "*** SAFE ZONE (DMG-OFF) ***");
            player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SHOOT, 0.25F, 0.30F);
            PLAYER_LOCATIONS.put(player, EnumPlayerAlignments.LAWFUL);
            return;
        }
        if (!GameAPI.isInSafeRegion(player.getLocation()) && GameAPI.isNonPvPRegion(player.getLocation()) && !PLAYER_LOCATIONS.get(player).equals(EnumPlayerAlignments.NEUTRAL)) {
            Utils.sendCenteredMessage(player, ChatColor.YELLOW.toString() + ChatColor.BOLD + "*** WILDERNESS (MOBS-ON, PVP-OFF) ***");
            player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SHOOT, 0.25F, 0.30F);
            PLAYER_LOCATIONS.put(player, EnumPlayerAlignments.NEUTRAL);
            return;
        }
        if (!GameAPI.isInSafeRegion(player.getLocation()) && !GameAPI.isNonPvPRegion(player.getLocation()) && !PLAYER_LOCATIONS.get(player).equals(EnumPlayerAlignments.CHAOTIC)) {
            Utils.sendCenteredMessage(player, ChatColor.RED.toString() + ChatColor.BOLD + "*** CHAOTIC ZONE (PVP-ON) ***");
            player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SHOOT, 0.25F, 0.30F);
            PLAYER_LOCATIONS.put(player, EnumPlayerAlignments.CHAOTIC);
        }
    }

    public int getAlignmentTime(Player player, PlayerWrapper wrapper) {
        return wrapper.getAlignmentTime();
    }
}
