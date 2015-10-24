package net.dungeonrealms.mastery;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.handlers.HealthHandler;
import net.dungeonrealms.handlers.KarmaHandler;
import net.dungeonrealms.handlers.ScoreboardHandler;
import net.dungeonrealms.mongo.DatabaseAPI;
import net.dungeonrealms.mongo.EnumData;
import net.dungeonrealms.mongo.EnumOperators;
import net.dungeonrealms.party.Party;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

/**
 * Created by Nick on 10/19/2015.
 */
public class GamePlayer {

    private Player T;

    public GamePlayer(Player player) {
        T = player;
    }

    /**
     * Get the players tier.
     *
     * @return The players Tier
     * @since 1.0
     */
    public Tier getPlayerTier() {
        int level = getLevel();
        if (level >= 1 || level <= 10) {
            return Tier.TIER1;
        } else if (level >= 11 || level <= 20) {
            return Tier.TIER2;
        } else if (level >= 21 || level <= 30) {
            return Tier.TIER3;
        } else if (level >= 31 || level <= 40) {
            return Tier.TIER4;
        } else if (level >= 41 || level <= 64) {
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
        return Party.getInstance().isInParty(T);
    }

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
        return (int) DatabaseAPI.getInstance().getData(EnumData.LEVEL, T.getUniqueId());
    }

    /**
     * Gets the players experience.
     *
     * @return the experience
     * @since 1.0
     */
    public double getExperience() {
        return Double.valueOf(String.valueOf(DatabaseAPI.getInstance().getData(EnumData.EXPERIENCE, T.getUniqueId())));
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

    /**
     * Gets the players maximum health.
     *
     * @return int (maxhealth)
     * @since 1.0
     */
    public int getPlayerMaxHP() {
        if (T.hasMetadata("maxHP")) {
            return T.getMetadata("maxHP").get(0).asInt();
        } else {
            return HealthHandler.getInstance().calculateMaxHPFromItems(T);
        }
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

    /**
     * Add experience to the player
     *
     * @param experienceToAdd the amount of experience to add.
     * @apiNote Will automagically level the player up if the experience is enough.
     * @since 1.0
     */
    public void addExperience(double experienceToAdd) {
        int level = getLevel();
        double experience = getExperience();

        if (level >= 64) return;

        double futureExperience = experience + experienceToAdd;

        /**
         * Will only happen if the players should level up!
         */
        //TODO: Fix this formula for levels 1-9
        if (futureExperience > (level * 1000) + Math.round(level % (64 * 2))) {
            DatabaseAPI.getInstance().update(T.getUniqueId(), EnumOperators.$SET, "info.level", level + 1, false);
            DatabaseAPI.getInstance().update(T.getUniqueId(), EnumOperators.$SET, "info.experience", experienceToAdd - experience, false);
            Utils.log.info("[LEVEL] Leveling " + T.getName() + " to level " + getLevel() + 1 + " with new experience" + String.valueOf(experience - experience));
            DatabaseAPI.getInstance().update(T.getUniqueId(), EnumOperators.$INC, "info.attributes.bufferPoints", 6, false);
            T.sendMessage(ChatColor.GREEN + "You have reached level " + ChatColor.AQUA + level + 1 + ChatColor.GREEN + " and have gained 6 Attribute Points!");
            ScoreboardHandler.getInstance().setPlayerHeadScoreboard(T, getPlayerAlignment().getAlignmentColor(), level + 1);
        } else {
            DatabaseAPI.getInstance().update(T.getUniqueId(), EnumOperators.$SET, "info.experience", experienceToAdd, true);
            T.sendMessage(ChatColor.GREEN.toString() + ChatColor.BOLD + "+ " + ChatColor.GREEN + experienceToAdd + " experience!");
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

}
