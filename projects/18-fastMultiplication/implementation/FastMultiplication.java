// Toom-Cook multiplication algorithm
// TODO: comment (document) what's going on

public class FastMultiplication {
    public static long multiply(long m, long n, int km, int kn, int radix) {
        long base = selectBase(m, n, km, kn, radix);
        long[] mSplit = split(m, base, km);
        long[] nSplit = split(n, base, kn);
        int evalPoints = 2 * Math.max(km, kn) - 1;
        long[] mEval = eval(mSplit, evalPoints);
        long[] nEval = eval(nSplit, evalPoints);
        long[] pointWise = pointWiseMultiplication(mEval, nEval, km, kn, radix);
        long[] interpolated = interpolate(pointWise, evalPoints);
        long result = recompose(interpolated);
        return result;
    }
    private static long selectBase(long m, long n, int km, int kn, int radix) {
        int power = (int) Math.max(Math.floor(Math.ceil(Math.log(m) / Math.log(radix)) / km), Math.floor(Math.ceil(Math.log(n) / Math.log(radix)) / kn)) + 1;
        return (long) Math.pow(radix, power);
    }
    private static long[] split(long x, long base, int k) {
        long[] result = new long[k];
        for (int i = 0; i < k; i++) {
            result[i] = x % base;
            x /= base;
        }
        return result;
    }
    private static long[] eval(long[] x, int evalPoints) {
        int[] points = new int[evalPoints];
        points[0] = 0;
        for (int i = 1; i < evalPoints; i += 2) {
            int n = (i / 2) + 1;
            points[i] = n;
            points[i + 1] = -n;
        }
        long[] result = new long[evalPoints];
        for (int i = 0; i < evalPoints; i++) {
            result[i] = 0;
            for (int j = 0; j < x.length; j++) {
                result[i] += x[j] * Math.pow(points[i], j);
            }
        }
        return result;
    }
    private static long[] pointWiseMultiplication(long[] mEval, long[] nEval, int km, int kn, int radix) {
        long[] result = new long[mEval.length];
        for (int i = 0; i < mEval.length; i++) {
            result[i] = multiply(mEval[i], nEval[i], km, kn, radix);
        }
        return result;
    }
    private static long[] interpolate(long[] pointWise, int evalPoints) {
        int[] points = new int[evalPoints];
        points[0] = 0;
        for (int i = 1; i < evalPoints; i += 2) {
            int n = (i / 2) + 1;
            points[i] = n;
            points[i + 1] = -n;
        }
        long[] result = new long[evalPoints];
        // TODO: complete
        return result;
    }
    private static long recompose(long[] interpolated) {
        return 1; // TODO: complete
    }
}
