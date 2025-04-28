import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java Main <input.in> <expected.out>");
            System.exit(1);
        }
        String inputFile = args[0];
        String expectedFile = args[1];

        List<Long> results = new ArrayList<>();
        try (Scanner fin = new Scanner(new File(inputFile))) {
            int n = fin.nextInt();
            int m = fin.nextInt();
            long[] arr = new long[n];
            for (int i = 0; i < n; i++) {
                arr[i] = fin.nextLong();
            }

            PST pst = new PST(n);
            pst.construct(arr);

            for (int i = 0; i < m; i++) {
                String cmd = fin.next();
                if ("update".equals(cmd)) {
                    int idx = fin.nextInt();
                    long x = fin.nextLong();
                    pst.update(idx, x);
                } else if ("query".equals(cmd)) {
                    int ver = fin.nextInt();
                    int l = fin.nextInt();
                    int r = fin.nextInt();
                    results.add(pst.query(ver, l, r));
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("Error: could not open input file");
            System.exit(1);
        }

        List<Long> expected = new ArrayList<>();
        try (Scanner fout = new Scanner(new File(expectedFile))) {
            while (fout.hasNextLong()) {
                expected.add(fout.nextLong());
            }
        } catch (FileNotFoundException e) {
            System.err.println("Error: could not open expected file");
            System.exit(1);
        }

        boolean allPass = (results.size() == expected.size());
        if (!allPass) {
            System.err.printf("Mismatch in number of results: got %d, expected %d%n",
                              results.size(), expected.size());
        }

        int tests = Math.min(results.size(), expected.size());
        for (int i = 0; i < tests; i++) {
            long act = results.get(i);
            long exp = expected.get(i);
            boolean match = (act == exp);

            System.out.printf("Test %d:%n", i);
            System.out.printf("  Actual:   %d%n", act);
            System.out.printf("  Expected: %d%n", exp);
            System.out.printf("  Match:    %s%n%n", match ? "YES" : "NO");

            if (!match) {
                allPass = false;
            }
        }

        if (allPass) {
            System.out.println("All tests passed!");
            System.exit(0);
        } else {
            System.out.println("Some tests failed.");
            System.exit(1);
        }
    }
}
