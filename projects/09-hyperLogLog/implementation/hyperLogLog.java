import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

class HyperLogLog {
    
    private final int p;
    private final int m;
    private final byte[] registers;
    private final double alpha;
    private final MessageDigest md;

    // p -> predefined by user (default 16)
    // m -> based on p (num reg)
    // alpha -> constant
    public HyperLogLog(int p) {
        this.p = p;
        // m is number of registers -> 2^p
        // the more registers you have here the more "precision"
        this.m = 1 << p;
        // register or "buckets"
        this.registers = new byte[m];
        // contstant set based on num buckets
        switch (m) {
            case 16:
                alpha = 0.673;
                break;
            case 32:
                alpha = 0.697;
                break;
            case 64:
                alpha = 0.709;
                break;
            default:
                alpha = 0.7213 / (1 + 1.079 / m);
                break;
        }
        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-1 not supported", e);
        }
    }

    public void add(Object item) {

        byte[] data = item.toString().getBytes(StandardCharsets.UTF_8);
        // hashed with SHA1 (160 bit hash)
        byte[] digest = md.digest(data);
        long h = 0;
        // cut this hash to 8 bytes or 64 bits in this implementation
        for (int i = 0; i < 8; i++) {
            h = (h << 8) | (digest[i] & 0xFFL);
        }

        // shift by 64 (total bits) - p to get the top p bits from the hash
        // example: 0x12345678
        // top bits are on left

        // this determines the bucket (reg) that hash will go in
        int idx = (int)(h >>> (64 - p));

        // w is rest of bits from hash (not used in register) -> offset
        // this is the probabalistic part of the algorithm
        long w  = h & ((1L << (64 - p)) - 1);

        int rho;
        // all zeros
        if (w == 0) {
            rho = (64 - p) + 1;
        // counts leading zeros
        } 
        else {
            rho = Long.numberOfLeadingZeros(w) - p + 1;
        }
        
        // The & 0xFF essentially masks off the sign extension
        int current = registers[idx] & 0xFF;
        // keep the MAXIMUM value only in the register or bucket
        // for example if the number of leading zeros in the bucket was 4 already
        // and we calculated a rho of 2, this register's value would stay as 4
        if (rho > current) {
            registers[idx] = (byte) rho;
        }
    }

    public HyperLogLog merge(HyperLogLog other) {
        // confirm the size p is same for both
        if (this.p != other.p) {
            throw new IllegalArgumentException("Cannot merge HLL with different precision");
        }
        HyperLogLog hll = new HyperLogLog(this.p);
        for (int i = 0; i < m; i++) {
            //This gave me trouble you need this because any value in the hll register > 127 would be treated negative without.
            //The & 0xFF essentially masks off the sign extension
            // just take the max of all registers for each one
            // for example take the maximum for register 0 in both HyperLogLogs and do this for all following registers
            hll.registers[i] = (byte) Math.max(this.registers[i] & 0xFF, other.registers[i] & 0xFF);
        }
        return hll;
    }

    public long cardinality(boolean debug) {
        // if all registers are zero, return 0
        boolean allZero = true;
        // if all 0 just break
        for (byte b : registers) {
            if (b != 0) { allZero = false; break; }
        }
        if (allZero) {
            return 0L;
        }

        // this is harmonic mean
        // way of averaging
        double Z = 0.0;
        int zeros = 0;
        for (int i = 0; i < m; i++) {
            int r = registers[i] & 0xFF;
            Z += Math.pow(2.0, -r);
            if (r == 0) zeros++;
        }
        // eraw: this actually computes the estimated distinct values
        double Eraw = alpha * m * m / Z;

        // E_raw very SMALL (under or equal to 2.5) -> correction
        // we use a slightly different formula to predict the estimated distinct elements
        // this is only used for small e raw and when at least one register is 0
        // else just set E to the E_raw calculated earlier
        double E = Eraw;
        if (Eraw <= 2.5 * m && zeros > 0) {
            E = m * Math.log(m / (double) zeros);
        }

        // this is edge case for LARGE e_raw
        // use a different edge formula for calculating e_raw
        double two64 = Math.pow(2.0, 64);
        double threshold = two64 / 30.0;
        if (E > threshold) {
            E = - two64 * Math.log(1.0 - E / two64);
        }

        // round up to nearest interger if small correction or large correction
        long result = (long) (E + 0.5);
        return result;
    }

    public static void main(String[] args) throws Exception {
        // Create a scanner to read input
        java.util.Scanner scanner = new java.util.Scanner(System.in);
        String filename = scanner.nextLine();
        scanner.close();
        
        // Read the file content
        java.nio.file.Path path = java.nio.file.Paths.get(filename);
        String content = new String(java.nio.file.Files.readAllBytes(path));
        
        // Split by $ to get separate HLL datasets
        String[] hllDatasets = content.split("\\$");
        
        // Initialize list to store all HLLs
        java.util.List<HyperLogLog> hlls = new java.util.ArrayList<>();
        
        // Process each dataset
        for (String dataset : hllDatasets) {
            if (dataset.trim().isEmpty()) continue; // Skip empty datasets
            
            // Create a new HLL for this dataset (using default precision 16)
            HyperLogLog hll = new HyperLogLog(16);
            
            // Process all space-separated items
            String[] items = dataset.trim().split("\\s+");
            for (String item : items) {
                hll.add(item);
            }
            
            // Add to our collection
            hlls.add(hll);
        }
        
        // If we have multiple HLLs, merge them
        if (hlls.isEmpty()) {
            System.out.println(0); // No data
        } else if (hlls.size() == 1) {
            System.out.println(hlls.get(0).cardinality(false));
        } else {
            // Start with the first HLL
            HyperLogLog mergedHll = hlls.get(0);
            
            // Merge with each subsequent HLL
            for (int i = 1; i < hlls.size(); i++) {
                mergedHll = mergedHll.merge(hlls.get(i));
            }
            
            // Print the cardinality of the merged HLL
            System.out.println(mergedHll.cardinality(false));
        }
    }
}