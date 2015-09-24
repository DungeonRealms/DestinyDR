package net.dungeonrealms.mechanics;

/**
 * Created by Nick on 9/19/2015.
 * <p>
 * Java’s Random:  45.7 million longs per second
 * XOR: 320.6 million longs per second, or 7 times faster!
 */
public class XRandom {

    private long u;
    private long v = 4101842887655102017L;
    private long w = 1;

    public XRandom() {
        this(System.nanoTime());
    }

    private XRandom(long seed) {
        u = seed ^ v;
        nextLong();
        v = u;
        nextLong();
        w = v;
        nextLong();
    }

    private long nextLong() {
        try {
            u = u * 2862933555777941757L + 7046029254386353087L;
            v ^= v >>> 17;
            v ^= v << 31;
            v ^= v >>> 8;
            w = 4294957665L * (w) + (w >>> 32);
            long x = u ^ (u << 21);
            x ^= x >>> 35;
            x ^= x << 4;
            return (x + v) ^ w;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0l;
    }

    public int nextInt(int bits) {
        return (int) (nextLong() >>> (64 - bits));
    }
}