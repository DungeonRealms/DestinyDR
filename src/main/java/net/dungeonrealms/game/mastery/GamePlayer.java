package net.dungeonrealms.game.mastery;

import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.achievements.Achievements;
import net.dungeonrealms.game.handlers.HealthHandler;
import net.dungeonrealms.game.handlers.KarmaHandler;
import net.dungeonrealms.game.handlers.ProtectionHandler;
import net.dungeonrealms.game.handlers.ScoreboardHandler;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.mongo.EnumOperators;
import net.dungeonrealms.game.player.stats.PlayerStats;
import net.dungeonrealms.game.world.items.DamageAPI;
import net.dungeonrealms.game.world.items.Item;
import net.dungeonrealms.game.world.items.Item.AttributeType;
import net.dungeonrealms.game.world.party.Affair;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Map;

/**
 * Created by Nick on 10/19/2015.
 */
public class GamePlayer {

    private Player T;
    private PlayerStats stats;

    /**
     * Attribute values and their values
     */
    @Setter
    @Getter
    private Map<String, Integer[]> attributes;
    @Setter
    @Getter
    private boolean attributesLoaded;

    public GamePlayer(Player player) {
        T = player;
        stats = new PlayerStats(player.getUniqueId());
        API.GAMEPLAYERS.put(player.getName(), this);
    }

    /**
     * Get the players tier.
     *
     * @return The players Tier
     * @since 1.0
     */
    public Tier getPlayerTier() {
        int level = getLevel();
        if (level >= 1 || level <= 9) {
            return Tier.TIER1;
        } else if (level >= 10 || level <= 19) {
            return Tier.TIER2;
        } else if (level >= 20 || level <= 29) {
            return Tier.TIER3;
        } else if (level >= 30 || level <= 39) {
            return Tier.TIER4;
        } else if (level >= 40 || level <= 100) {
            return Tier.TIER5;
        } else
            return Tier.TIER1;
    }

    /**
     * Checks if player is in party.
     *
     * @return
     * @since 1.0
     */
    public boolean isInParty() {
        return Affair.getInstance().isInParty(T);
    }

    /**
     * Set ECash for player. Database updates as well.
     *
     * @param ECash
     */
    public void setECash(int ECash) {DatabaseAPI.getInstance().update(T.getUniqueId(), EnumOperators.$SET, EnumData.ECASH, ECash, true); }

    /**
     * Simple Tiers
     *
     * @since 1.0
     */
    enum Tier {
        TIER1,
        TIER2,
        TIER3,
        TIER4,
        TIER5,
    }

    /**
     * Gets the players level
     *
     * @return the level
     * @since 1.0
     */
    public int getLevel() {
        return stats.getLevel();
    }


    /**
     * Gets the players experience.
     *
     * @return the experience
     * @since 1.0
     */
    public int getExperience() {
        return (int) DatabaseAPI.getInstance().getData(EnumData.EXPERIENCE, T.getUniqueId());
    }

    /**
     * Checks if the player is in a Dungeon
     *
     * @return Is player in Dungeon?
     */
    public boolean isInDungeon() {
        return T.getWorld().getName().contains("DUNGEON");
    }

    /**
     * Checks if the player is in a Players Realm
     *
     * @return Is player in Realm?
     */
    public boolean isInRealm() {
        return !T.getWorld().getName().contains("DUNGEON") && !T.getWorld().getName().contains("DUEL") && !T.getWorld().equals(Bukkit.getWorlds().get(0));
    }

    /**
     * Gets the players current alignment.
     *
     * @return the alignment
     * @since 1.0
     */
    public KarmaHandler.EnumPlayerAlignments getPlayerAlignment() {
        return KarmaHandler.EnumPlayerAlignments.getByName(String.valueOf(DatabaseAPI.getInstance().getData(EnumData.ALIGNMENT, T.getUniqueId())));
    }

    /**
     * Gets the players current health.
     *
     * @return int (health)
     * @since 1.0
     */
    public int getPlayerCurrentHP() {
        if (T.hasMetadata("currentHP")) {
            return T.getMetadata("currentHP").get(0).asInt();
        } else {
            return 50;
        }
    }

    public int getEcashBalance() {
        return (int) DatabaseAPI.getInstance().getData(EnumData.ECASH, T.getUniqueId());
    }

    /**
     * Gets the players maximum health.
     *
     * @return int (maxhealth)
     * @since 1.0
     */
    public int getPlayerMaxHP() {
        int temp = HealthHandler.getInstance().calculateMaxHPFromItems(T);
        return temp + ((int) (temp * getStats().getVitHP()));
    }

    /**
     * Sets the players MaximumHP
     * to the given value.
     *
     * @param maxHP
     * @since 1.0
     */
    public void setPlayerMaxHPLive(int maxHP) {
        T.setMetadata("maxHP", new FixedMetadataValue(DungeonRealms.getInstance(), maxHP));
    }

    /**
     * Sets the players HP
     * to the given value.
     *
     * @param hp
     * @since 1.0
     */
    public void setPlayerHPLive(int hp) {
        T.setMetadata("currentHP", new FixedMetadataValue(DungeonRealms.getInstance(), hp));
    }


    public int getEXPNeeded(int level) {
        if (level >= 101) {
            return 0;
        }
        double difficulty = 1;
        if (level >= 1 && level < 40) {
            difficulty = 1.3;
        }
        else if (level >= 40 && level < 60) {
            difficulty = 1.6;
        }
        else if (level >= 60 && level < 80) {
            difficulty = 2.2;
        }
        else if (level >= 80) {
            difficulty = 2.6;
        }
//        return (int) (100 * Math.pow(level, 2.24)); old level exp formula
        return (int) ((100 * Math.pow(level, 2)) * difficulty + 500); // patch 1.9 exp formula
    }


    /**
     * Add experience to the player
     *
     * @param experienceToAdd the amount of experience to add.
     * @apiNote Will automagically level the player up if the experience is enough.
     * @since 1.0
     */
    public void addExperience(int experienceToAdd, boolean isParty) {
        int level = getLevel();
        if (level >= 100) return;
        int experience = getExperience();
        String expPrefix = ChatColor.YELLOW.toString() + ChatColor.BOLD + "        + ";
        if (isParty) {
            expPrefix = ChatColor.YELLOW.toString() + ChatColor.BOLD + "            " + ChatColor.AQUA.toString() + ChatColor.BOLD + "P " + ChatColor.RESET + ChatColor.GRAY + " >> " + ChatColor.YELLOW.toString() + ChatColor.BOLD + "+";
        }
        int subBonus = 0;
        int subPlusBonus = 0;
        int futureExperience = experience + experienceToAdd + subBonus + subPlusBonus;
        int xpNeeded = getEXPNeeded(level);
        if (futureExperience >= xpNeeded) {
            updateLevel(level + 1, true, false);
        } else {
            DatabaseAPI.getInstance().update(T.getUniqueId(), EnumOperators.$INC, EnumData.EXPERIENCE, experienceToAdd + subBonus + subPlusBonus, true);
            if ((boolean) DatabaseAPI.getInstance().getData(EnumData.TOGGLE_DEBUG, T.getUniqueId())) {
                    T.sendMessage(expPrefix + ChatColor.YELLOW + Math.round(experienceToAdd) + ChatColor.BOLD + " EXP " + ChatColor.GRAY + "[" + Math.round(futureExperience) + ChatColor.BOLD + "/" + ChatColor.GRAY + Math.round(getEXPNeeded(level)) + " EXP]");
            }
        }
    }

    /**
     * Updates a player's level. Can be called for a natural level up or for
     * an artificial change of a player's level via /set level or other means.
     * @param newLevel - the new level
     * @param levelUp - if the level change is natural
     * @param levelSet - if the level change is set artificially
     */
    public void updateLevel(int newLevel, boolean levelUp, boolean levelSet) {
        DatabaseAPI.getInstance().update(T.getUniqueId(), EnumOperators.$SET, EnumData.EXPERIENCE, 0, false);
        DatabaseAPI.getInstance().update(T.getUniqueId(), EnumOperators.$INC, EnumData.LEVEL, 1, true);

        if (levelUp) { // natural level up
            getStats().lvlUp();

            if (newLevel != getLevel()) return; // not a natural level up

            T.getWorld().playSound(T.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, .4F);
            T.playSound(T.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5F, 1F);

            T.sendMessage("");
            Utils.sendCenteredMessage(T, ChatColor.AQUA.toString() + ChatColor.BOLD + "******************************");
            Utils.sendCenteredMessage(T, ChatColor.GREEN.toString() + ChatColor.BOLD + "LEVEL UP");
            T.sendMessage("");
            Utils.sendCenteredMessage(T, ChatColor.GRAY + "You are now level: " + ChatColor.GREEN + newLevel);
            Utils.sendCenteredMessage(T, ChatColor.GRAY + "EXP to next level: " + ChatColor.GREEN + getEXPNeeded(newLevel));
            Utils.sendCenteredMessage(T, ChatColor.GRAY + "Free stat points: " + ChatColor.GREEN + this.getStats().freePoints);
            Utils.sendCenteredMessage(T, ChatColor.AQUA.toString() + ChatColor.BOLD + "******************************");
            T.sendMessage("");
        }
        else if (levelSet) { // level was set
            getStats().setPlayerLevel(newLevel);

            Utils.sendCenteredMessage(T, ChatColor.YELLOW + "Your level has been set to: " + ChatColor.LIGHT_PURPLE + newLevel);
            T.playSound(T.getLocation(), Sound.BLOCK_NOTE_PLING, 1f, 63f);
        }

        // update scoreboard
        ScoreboardHandler.getInstance().setPlayerHeadScoreboard(T, getPlayerAlignment().getAlignmentColor(), newLevel);
        switch (newLevel) {
            case 10:
                Achievements.getInstance().giveAchievement(T.getUniqueId(), Achievements.EnumAchievements.LEVEL_10);
                break;
            case 25:
                Achievements.getInstance().giveAchievement(T.getUniqueId(), Achievements.EnumAchievements.LEVEL_25);
                break;
            case 50:
                Achievements.getInstance().giveAchievement(T.getUniqueId(), Achievements.EnumAchievements.LEVEL_50);
                break;
            case 100:
                Achievements.getInstance().giveAchievement(T.getUniqueId(), Achievements.EnumAchievements.LEVEL_100);
                break;
            default:
                break;
        }
    }

    /**
     * Get the factorial of a number.
     *
     * @param n
     * @return
     * @since 1.0
     */
    public int factorial(int n) {
        int output;
        if (n == 0) return 0;
        output = factorial(n - 1) * n;
        return output;
    }

    /**
     * @return Player
     */
    public Player getPlayer() {
        return T;
    }

    /**
     * Checks Document for boolean value
     *
     * @return boolean
     */
    public boolean hasShopOpen() {
        return (boolean) DatabaseAPI.getInstance().getData(EnumData.HASSHOP, T.getUniqueId());
    }

    /**
     * @return Player Stats
     */
    public PlayerStats getStats() {
        return stats;
    }

    public int getPlayerGemFind() {
        return DamageAPI.calculatePlayerStat(T, Item.ArmorAttributeType.GEM_FIND);
    }

    public int getPlayerItemFind() {
        return DamageAPI.calculatePlayerStat(T, Item.ArmorAttributeType.ITEM_FIND);
    }

    public boolean hasNewbieProtection() {
        return ProtectionHandler.getInstance().getProtected_Players().contains(T.getName());
    }

    public void setAttributeVal(AttributeType type, Integer[] val) {
        attributes.put(type.getNBTName(), val);
    }

    public Integer[] getRangedAttributeVal(AttributeType type) {
        if (type == null) return new Integer[] { 0, 0 };
        return attributes.get(type.getNBTName());
    }

    /**
     * Gets the value of a player's specified attribute. Must be a non-range
     * attribute.
     *
     * @param type
     * @return if a ranged attribute, throws an error message and returns -1.
     */
    public int getStaticAttributeVal(AttributeType type) {
        if (type == null || type.isRange()) {
            Utils.log.warning("Invalid type or type is a ranged attribute.");
            return -1;
        }
        if (attributes.get(type.getNBTName()) == null) return -1;
        return attributes.get(type.getNBTName())[1];
    }

    /**
     * Changes the value of an attribute by the given difference. This difference
     * may be positive or negative.
     *
     * @param type
     * @param difference
     *
     * @return the new value of the attribute
     */
    public Integer[] changeAttributeVal(AttributeType type, Integer[] difference) {
        Integer[] oldVal = getRangedAttributeVal(type);
        Integer[] newTotalVal = new Integer[] { oldVal[0] + difference[0], oldVal[1] + difference[1] };
        attributes.put(type.getNBTName(), newTotalVal);
        return newTotalVal;
    }

    /**
     * Changes the value of an attribute by a percent difference. This difference
     * may be positive or negative.
     *
     * @param type
     * @param percentDifference
     * @return the new value of the attribute
     */
    public Integer[] changeAttributeValPercentage(AttributeType type, float percentDifference) {
        int newLow = (int) (attributes.get(type.getNBTName())[0] * ((percentDifference + 100.) / 100.));
        int newHigh = (int) (attributes.get(type.getNBTName())[1] * ((percentDifference + 100.) / 100.));
        return attributes.put(type.getNBTName(), new Integer[] { newLow, newHigh });
    }
}
