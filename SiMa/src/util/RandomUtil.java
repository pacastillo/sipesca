package util;
/** Returns randomly distributed numbers (white distribution)
  * over a specified interval for 4 specified types:
  * int, long, float & double
  */
public class RandomUtil {
    private static final long IA     = 16807;
    private static final long IM     = 2147483647;
    private static final long IQ     = 127773;
    private static final long IR     = 2836;
    private static final int NTAB    = 32;
    private static final long NDIV   = (long)(1+(IM-1)/NTAB);
    private static final double AM   = 1.0/IM;
    private static final double EPS  = 1.2e-7;
    private static final double RNMX = (1.0-EPS);
    private static long iy = 0;
    private static long[] iv = new long [NTAB];
    private static long seed    = System.currentTimeMillis();
    private static java.util.Random gaussRandom = new   java.util.Random(seed);

    /**
     *  The long implementation of the algorithm.
     *  @param <code>long</code> min: the value of the lower bound
     *  @param <code>long</code> max: the value of the upper bound
     *
     *  @return <code>long</code> the number randomized over the
     *  specified interval.
     */
    public static long getBoundedLongRandom(long min, long max) {
        double random = Math.random();
        double scale = (max-min);
        double shift = min;
        random *= scale;
        random += shift;
        return Math.round(random);
    }
    /**
     *  The long implementation of the algorithm.
     *  @param <code>long</code> min: the value of the lower bound
     *  @param <code>long</code> max: the value of the upper bound
     *
     *  @return <code>long</code> the number randomized over the
     *  specified interval.
     */
    public static double getBoundedDoubleRandom() {
        return(Math.random());
    }

    /**
     *  Same as above but double implementation of the algorithm.
     */
    public static double getBoundedDoubleRandom(double min, double max) {
        double random = Math.random();
        double scale = (max-min);
        double shift = min;
        random *= scale;
        random += shift;
        return random;
    }

    /**
     *  Same as above but float implementation of the algorithm.
     */
    public static float getBoundedFloatRandom(float min, float max) {
        float random = (float)Math.random(); // this is ok as value returned in [0-1]
        float scale = (max-min);
        float shift = min;
        random *= scale;
        random += shift;
        return random;
    }

    /**
     *  Same as above but int implementation of the algorithm.
     */
    public static int getBoundedIntRandom(int min, int max) {
        float random = (float)Math.random();
        float scale = (max-min);
        float shift = min;
        random *= scale;
        random += shift;
        return Math.round(random);
    }

    /** Adapted from Numerical Recipes in C
     *  'Minimal' random number generator
     *  Call with idum negative to initialize
     */
    public static double ran1(long idum){
        int j;
        long k;
        double temp;

        if (idum <= 0 || iy != 0) {
            if (-(idum) < 1) {
                idum = 1;
            } else {
                idum = -(idum);
            }
            for (j = NTAB+7 ; j >= 0 ; j--) {
                k    = (idum)/IQ;
                idum = IA*(idum-k*IQ)-IR*k;
                if (idum < 0)
                    idum += IM;
                if (j < NTAB)
                    iv[j] = idum;
            }
            iy = iv[0];
        }
        k    = (idum)/IQ;
        idum = IA*(idum-k*IQ)-IR*k;
        if (idum < 0)
            idum += IM;
        j       = (int)(iy/NDIV);
        iy      = iv[j];
        iv[j]   = idum;
        if ((temp = AM*iy) > RNMX)
            return RNMX;
        return temp;
    }
    /**
     * The number will be generated for a Gaussian generator with a mean and a standard deviation.
     */
    public static double gaussianRandom(double mean, double stdDev) {
        return (gaussRandom.nextGaussian() * stdDev + mean);
    }

}
