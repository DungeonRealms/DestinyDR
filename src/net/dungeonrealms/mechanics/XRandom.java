package net.dungeonrealms.mechanics;

/**
 * Created by Nick on 9/19/2015.
 * <p>
 * Java’s Random:  45.7 million longs per second
 * XOR: 320.6 million longs per second, or 7 times faster!
 */
public class XRandom {

    private long last;

    public XRandom() {
        this(System.currentTimeMillis());
    }

    public XRandom(long seed) {
        this.last = seed;
    }

    public int nextInt(int max) {
        last ^= (last << 21);
        last ^= (last >>> 35);
        last ^= (last << 4);
        int out = (int) last % max;
        return (out < 0) ? -out : out;
    }

}