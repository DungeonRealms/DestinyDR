package net.dungeonrealms.mastery;

import net.dungeonrealms.mongo.DatabaseAPI;
import net.dungeonrealms.mongo.EnumData;
import net.dungeonrealms.mongo.EnumOperators;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

/**
 * Created by Nick on 10/19/2015.
 */
public class GamePlayer<T extends HumanEntity> {

    private Player T;

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
        } else if (level >= 41 || level <= 50) {
            return Tier.TIER5;
        } else
            return Tier.TIER1;
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
        return (double) DatabaseAPI.getInstance().getData(EnumData.EXPERIENCE, T.getUniqueId());
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
        if (futureExperience > (level * 1000) + factorial(Math.round(8 % level))) {
            DatabaseAPI.getInstance().update(T.getUniqueId(), EnumOperators.$SET, "info.level", level + 1, true);
            DatabaseAPI.getInstance().update(T.getUniqueId(), EnumOperators.$SET, "info.experience", experienceToAdd - experience, true);
            Utils.log.info("[LEVEL] Leveling " + T.getName() + " to level " + getLevel() + 1 + " with new experience" + String.valueOf(experience - experience));
        } else {
            DatabaseAPI.getInstance().update(T.getUniqueId(), EnumOperators.$SET, "info.experience", experienceToAdd, true);
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
        if (n == 1) {
            return 1;
        }
        output = factorial(n - 1) * n;
        return output;
    }


}
