package ufw;

/**
 * simplified version of "java.util.Random" that creates identical "nextInt" values for given seed
 * <p>
 * not thread safe
 */
public class RandomMinimal {

    private static final long multiplier = 0x5DEECE66DL;
    private static final long addend = 0xBL;
    private static final long mask = (1L << 48) - 1;

    private long seed;

    public RandomMinimal(long seed) {
        this.seed = initialScramble(seed);
    }

    private static long initialScramble(long seed) {
        return (seed ^ multiplier) & mask;
    }

    protected int next(int bits) {
        long oldseed = this.seed;
        long nextseed = (oldseed * multiplier + addend) & mask;
        this.seed = nextseed;
        return (int) (nextseed >>> (48 - bits));
    }

    public int nextInt() {
        return next(32);
    }
}
