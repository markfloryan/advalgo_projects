import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class pcSol_java {
    public static void main(String[] args) throws Exception {
        // read input and create object for getting solution
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        List<Integer> chosen = solvePokemonCollection(reader, false);
        for (int i = 0; i < chosen.size(); i++) {
            if (i > 0) System.out.print(" ");
            System.out.print(chosen.get(i));
        }
        System.out.println();
    }

    public static List<Integer> solvePokemonCollection(BufferedReader reader, boolean debug) throws IOException, NoSuchAlgorithmException {

        // read first line for budget and number of packs
        String[] first = reader.readLine().trim().split("\\s+");
        int budget = Integer.parseInt(first[0]);
        int num    = Integer.parseInt(first[1]);

        // read second line for number of packs
        // read prices of packs
        List<Integer> prices = new ArrayList<>(num);
        for (int i = 0; i < num; i++) {
            String line = reader.readLine();
            if (line != null && !line.trim().isEmpty()) {
                prices.add(Integer.parseInt(line.trim()));
            } else {
                i--; 
            }
        }

        // implement HyperLogLog algorithm to estimate distinct elements
        // has p = 16 by default
        HyperLogLog yourHLL = new HyperLogLog(16);
        String yourLine = reader.readLine();
        // insert your own cards into the HLL
        if (yourLine != null) {
            for (String tok : yourLine.trim().split("\\s+")) {
                if (!tok.isEmpty()) yourHLL.add(tok);
            }
        }
        // count of user collection
        long baseCount = yourHLL.cardinality(debug);

    
        // compare the different collections of cards from users using the merge method
        // the gains is then the difference between the merged HLL and the base HLL
        // this effectively computes the cards you would gain from the collection
        List<Long> gains = new ArrayList<>(num);
        for (int i = 0; i < num; i++) {
            HyperLogLog packHLL = new HyperLogLog(16);
            String packLine = reader.readLine();
            if (packLine != null) {
                for (String tok : packLine.trim().split("\\s+")) {
                    if (!tok.isEmpty()) packHLL.add(tok);
                }
            }
            // calculate difference (gain) and add to list
            long gain = yourHLL.merge(packHLL).cardinality(false) - baseCount;
            gains.add(gain);
        }

        //do knapsack to find optimal combination of packs to buy
        List<Integer> sel0 = knapsack01(prices, gains, budget);
        List<Integer> result = new ArrayList<>(sel0.size());
        for (int idx : sel0) {
            result.add(idx + 1);
        }
        return result;
    }

    public static List<Integer> knapsack01(List<Integer> prices, List<Long> values, int budget) {

        
        int N = prices.size();
        int W = budget;
        long[][] dp = new long[N+1][W+1];

        // dp[i][w] = max value for first i items with weight limit w
        for (int i = 1; i <= N; i++) {
            int wt = prices.get(i-1);
            long val = values.get(i-1);
            for (int w = 0; w <= W; w++) {
                if (w < wt) {
                    dp[i][w] = dp[i-1][w];
                } else {
                    dp[i][w] = Math.max(dp[i-1][w], dp[i-1][w-wt] + val);
                }
            }
        }

        // reconstruct the solution
        // find the items that were included in the optimal solution
        List<Integer> selection = new ArrayList<>();
        int w = W;
        for (int i = N; i > 0; i--) {
            if (dp[i][w] != dp[i-1][w]) {
                selection.add(i-1);
                w -= prices.get(i-1);
            }
        }

        // ascending
        Collections.sort(selection);
        return selection;
    }
}

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
        // 1) Check empty
        // if all registers are zero, return 0
        boolean allZero = true;
        // if all 0 just break
        for (byte b : registers) {
            if (b != 0) { allZero = false; break; }
        }
        if (allZero) {
            return 0L;
        }

        // 2) Harmonic mean
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

        // 3) Small‑range correction
        // E_raw very SMALL (under or equal to 2.5) -> correction
        // we use a slightly different formula to predict the estimated distinct elements
        // this is only used for small e raw and when at least one register is 0
        // else just set E to the E_raw calculated earlier
        double E = Eraw;
        if (Eraw <= 2.5 * m && zeros > 0) {
            E = m * Math.log(m / (double) zeros);
        }

        // 4) Large‑range correction (64‑bit)
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
}