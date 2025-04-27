import java.math.BigInteger;
import java.util.Random;
import java.util.Scanner;
import java.security.SecureRandom;
// see primalityTests.java under implementation for a more detailed explanation

public class pcSol_java {
    private SecureRandom random = new SecureRandom();
    
    // Binary exponentiation: calculates (a^b) % m
    private BigInteger binpow(BigInteger a, BigInteger b, BigInteger m) {
        BigInteger r = BigInteger.ONE;
        a = a.mod(m);
        while (b.compareTo(BigInteger.ZERO) > 0) {
            if (b.testBit(0)) { // b & 1 == 1
                r = r.multiply(a).mod(m);
            }
            b = b.shiftRight(1); // b >>= 1
            a = a.multiply(a).mod(m);
        }
        return r;
    }
    
    // Helper function for Miller-Rabin primality test
    private boolean checkComposite(BigInteger n, BigInteger a, BigInteger d, int s) {
        BigInteger r = binpow(a, d, n);
        if (r.equals(BigInteger.ONE) || r.equals(n.subtract(BigInteger.ONE))) {
            return false;
        }
        for (int i = 0; i < s - 1; i++) {
            r = r.multiply(r).mod(n);
            if (r.equals(n.subtract(BigInteger.ONE))) {
                return false;
            }
        }
        return true;
    }
    
    // Miller-Rabin primality test
    private boolean isPrime(BigInteger n, int k) {
        // Check small cases
        if (n.compareTo(BigInteger.valueOf(4)) < 0) {
            return n.equals(BigInteger.valueOf(2)) || n.equals(BigInteger.valueOf(3));
        }
        
        // Check if even
        if (n.mod(BigInteger.valueOf(2)).equals(BigInteger.ZERO)) {
            return false;
        }
        
        // Find s and d such that n-1 = 2^s * d where d is odd
        int s = 0;
        BigInteger d = n.subtract(BigInteger.ONE);
        while (d.mod(BigInteger.valueOf(2)).equals(BigInteger.ZERO)) {
            d = d.shiftRight(1);
            s++;
        }
        
        // Witness loop
        for (int i = 0; i < k; i++) {
            // Generate random a between 2 and n-2
            BigInteger a;
            do {
                a = new BigInteger(n.bitLength(), random);
            } while (a.compareTo(BigInteger.valueOf(2)) < 0 || a.compareTo(n.subtract(BigInteger.valueOf(2))) > 0);
            
            if (checkComposite(n, a, d, s)) {
                return false;
            }
        }
        return true;
    }
    
    // Overloaded method with default k=15
    private boolean isPrime(BigInteger n) {
        return isPrime(n, 15);
    }
    
    // Custom hash function
    private BigInteger fartcoinHash(String s) {
        BigInteger h = BigInteger.ZERO;
        for (int i = 0; i < s.length(); i++) {
            h = h.shiftLeft(1);
            h = h.xor(BigInteger.valueOf(s.charAt(i)));
        }
        return h;
    }
    
    // Main processing method
    public void process() {
        Scanner scanner = new Scanner(System.in);
        int n = Integer.parseInt(scanner.nextLine());
        String blockchain = "fartcoin";
        int acceptedCount = 0;
        
        for (int i = 0; i < n; i++) {
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) {
                continue;
            }
            
            String username;
            String nonce;
            
            String[] parts = line.split("\\s+", 2);
            username = parts[0];
            
            if (parts.length < 2) {
                // No nonce provided
                System.out.println(username + " rejected " + acceptedCount);
                continue;
            }
            
            nonce = parts[1];
            
            
            String newBlockchain = blockchain + nonce;
            BigInteger h = fartcoinHash(newBlockchain);
                
            
            if (isPrime(h)) {
                acceptedCount++;
                blockchain = newBlockchain;
                System.out.println(username + " accepted " + acceptedCount);
            } else {
                System.out.println(username + " rejected " + acceptedCount);
            }
        }
        
        scanner.close();
    }
    
    public static void main(String[] args) {
        pcSol_java solution = new pcSol_java();
        solution.process();
    }
}
