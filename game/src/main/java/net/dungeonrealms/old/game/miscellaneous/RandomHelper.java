package net.dungeonrealms.old.game.miscellaneous;

import java.util.List;
import java.util.Random;

/**
 * Created by Kieran on 11/6/2015.
 */
public class RandomHelper {
    /**
     * The random number stream used by this helper.
     */
    public final static Random rand = new Random();

    /**
     * Returns a random int between min and max, inclusive.
     *
     * @param min The minimum number that the random number can be.
     * @param max The maximum number that the random number can be.
     * @return A random int between min and max, inclusive.
     */
    public static int getRandomNumberBetween(int min, int max) {
        return useRandomForNumberBetween(rand, min, max);
    }

    /**
     * Returns a random float between min and max, inclusive.
     *
     * @param min The minimum number that the random number can be.
     * @param max The maximum number that the random number can be.
     * @return A random float between min and max, inclusive.
     */
    public static float getRandomNumberBetween(float min, float max) {
        return useRandomForNumberBetween(rand, min, max);
    }

    /**
     * Using a specified random stream, returns a random int between min and max, inclusive.
     *
     * @param random The random stream of numbers to get a random number from.
     * @param min    The minimum number that the random number can be.
     * @param max    The maximum number that the random number can be.
     * @return A random int between min and max, inclusive.
     */
    public static int useRandomForNumberBetween(Random random, int min, int max) {
        return random.nextInt(max - min + 1) + min;
    }

    /**
     * Using a specified random stream, returns a random float between min and max, inclusive.
     *
     * @param random The random stream of numbers to get a random number from.
     * @param min    The minimum number that the random number can be.
     * @param max    The maximum number that the random number can be.
     * @return A random float between min and max, inclusive.
     */
    public static float useRandomForNumberBetween(Random random, float min, float max) {
        return random.nextFloat() * (max - min) + min;
    }

    /**
     * Gets a random element from a list.
     *
     * @param list The list to get a random element from.
     * @return A randomly selected element in the list, or null if the list is empty.
     */
    public static <T> T getRandomElementFromList(List<T> list) {
        if (!list.isEmpty()) {
            return list.get(getRandomNumberBetween(0, list.size() - 1));
        } else {
            return null;
        }
    }

    /**
     * Randomly returns true or false, using a specific chance (0 to 1) of being true.
     *
     * @param chance The chance (0 to 1) of returning true.
     * @return A randomly selected boolean value according to the chance of being true.
     */
    public static boolean getRandomChance(float chance) {
        return rand.nextFloat() < chance;
    }
}
