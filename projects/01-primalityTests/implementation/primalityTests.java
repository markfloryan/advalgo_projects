import java.util.Random;

public class PrimeChecker {
    private static final long RNG_SEED = 12345L;
    
    // Naive primality test
    public static boolean naive(long n) {
        if (n < 2) return false;
        // Check all numbers if they are factors of n
        for (long i = 2; i < n; ++i) {
            if (n % i == 0) {
                return false;
            }
        }
        // If no factors were found, n is prime
        return true;
    }
    
    // Optimized naive primality test
    public static boolean naiveOptimized(long n) {
        // Simple cases
        if (n < 4) return n == 2 || n == 3;
        // Skip all even numbers
        if ((n & 1) == 0) return false;
        // Check all numbers up to the square root of n
        for (long i = 3; i * i <= n; i += 2) {
            if (n % i == 0) {
                return false;
            }
        }
        // If no factors were found, n is prime
        return true;
    }
    
    // Helper function for fast binary exponentiation
    public static long binaryPower(long base, long exp, long mod) {
        long result = 1;
        base = base % mod;
        while (exp > 0) {
            if ((exp & 1) == 1) {    // If exp is odd
                result = (result * base) % mod;
            }
            exp = exp >> 1;                // Divide exp by 2
            base = (base * base) % mod;    // Square the base
        }
        return result;
    }
    
    // Fermat primality test
    public static boolean fermatPrime(long n, int iters) {
        // Simple cases
        if (n < 4) return n == 2 || n == 3;
        // Using Random with a seed for reproducibility
        Random random = new Random(RNG_SEED);
        for (int i = 0; i < iters; ++i) {
            // Generate a random number in the range [2, n-2]
            long a = 2 + (long)(random.nextDouble() * (n - 3));
            // Check if a^(n-1) is equiv to 1 mod n
            if (binaryPower(a, n - 1, n) != 1) {
                return false;  // n is composite
            }
        }
        return true;
    }
    
    // Check if n is composite using Miller-Rabin criteria
    private static boolean checkComposite(long n, long a, long d, int s) {
        long x = binaryPower(a, d, n);
        if (x == 1 || x == n - 1) return false;
        
        // Check the other cases for r
        for (int j = 1; j < s; ++j) {
            x = binaryPower(x, 2, n);
            // Checking a^(2^r * d), accumulate x
            if (x == n - 1) return false;
        }
        return true;
    }
    
    // Miller-Rabin primality test
    public static boolean millerRabin(long n, int iters) {
        if (n < 4) return n == 2 || n == 3;
        // Want to write n-1 as d*2^s, find suitable d and s
        int s = 0;
        long d = n - 1;
        while ((d & 1) == 0) {
            d = d >> 1;    // Divide d by 2
            s++;           // Increment s
        }
        
        // Using Random with a seed for reproducibility
        Random random = new Random(RNG_SEED);
        for (int i = 0; i < iters; ++i) {
            // Generate a random number in the range [2, n-2]
            long a = 2 + (long)(random.nextDouble() * (n - 3));
            if (checkComposite(n, a, d, s)) {
                return false;
            }
        }
        return true;  // n is *probably* prime
    }
    
    // Deterministic Miller-Rabin primality test for 64-bit integers
    public static boolean millerRabinDeterministic(long n, int iters) {
        if (n < 4) return n == 2 || n == 3;
        // Want to write n-1 as d*2^s, find suitable d and s
        int s = 0;
        long d = n - 1;
        while ((d & 1) == 0) {
            d = d >> 1;    // Divide d by 2
            s++;           // Increment s
        }
        
        // Only need to check first 12 primes for 64-bit ints
        long[] primes = {2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37};
        for (int i = 0; i < Math.min(iters, primes.length); ++i) {
            // Is a prime
            if (primes[i] == n) return true;
            if (checkComposite(n, primes[i], d, s)) {
                return false;
            }
        }
        return true;  // n is *probably* prime
    }
    
    public static void main(String[] args) {
        System.out.println("Hello, World!");
        
        // You can add test cases here
        // For example:
        // long testNumber = 997;
        // System.out.println("Is " + testNumber + " prime? " + millerRabinDeterministic(testNumber, 12));
    }
}
