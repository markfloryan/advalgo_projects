import java.io.*;
import java.util.*;

public class Solution {
    class Node {}

    class SegTreeAdditionAndGet {}

    public int gcdHelper(int a, int b) {
        if (a == 0) return b;
        if (b == 0) return a;

        a = Math.abs(a);
        b = Math.abs(b);

        if (a > b) {
            return gcdHelper(b, a);
        } else {
            return gcdHelper(b % a, a);
        }
    }

    class SegTreeGCD {}


    public static void main(String[] args) throws IOException {
        int testCases = 20;

        for (int testCase = 1; testCase <= testCases; testCase++) {
            System.out.println("test case " + testCase);

            String testInPath = "../io/test.in." + testCase;
            String testOutPath = "../io/test.out." + testCase;

            BufferedReader testIn = new BufferedReader(new FileReader(testInPath));
            BufferedReader testOut = new BufferedReader(new FileReader(testOutPath));

            StringTokenizer tokenizer = new StringTokenizer(testIn.readLine());
            int N = Integer.parseInt(tokenizer.nextToken());
            int Q = Integer.parseInt(tokenizer.nextToken());

            int[] a = new int[N];
            tokenizer = new StringTokenizer(testIn.readLine());
            for (int i = 0; i < N; i++) {
                a[i] = Integer.parseInt(tokenizer.nextToken());
            }

            int[] diffA = new int[N - 1];
            for (int i = 1; i < N; i++) {
                diffA[i - 1] = a[i] - a[i - 1];
            }

            SegTreeAdditionAndGet addSegTree = new SegTreeAdditionAndGet(a);
            SegTreeGCD gcdSegTree = new SegTreeGCD(diffA);

            for (int i = 0; i < Q; i++) {
                String line = testIn.readLine();
                tokenizer = new StringTokenizer(line);
                String queryType = tokenizer.nextToken();

                if (queryType.equals("GCD")) {
                    int l = Integer.parseInt(tokenizer.nextToken());
                    int r = Integer.parseInt(tokenizer.nextToken());

                    // TODO: update seg tree queries
                    int aL = addSegTree.get( l);
                    int gcd = gcdSegTree.query(l, r - 1);
                    int res = gcd_helper(aL, gcd);

                    // UNCOMMENT FOR NORMAL SOLUTION
                    // System.out.println(res.toString());

                    int expected = Integer.parseInt(testOut.readLine().trim());
                    assert res == expected : "Test case " + testCase + " failed on GCD query: expected " + expected + ", got " + res;

                } else if (queryType.equals("ADD")) {
                    int l = Integer.parseInt(tokenizer.nextToken());
                    int r = Integer.parseInt(tokenizer.nextToken());
                    int x = Integer.parseInt(tokenizer.nextToken());

                    // TODO: update seg tree queries
                    addSegTree.update(l, r, x);
                    gcdSegTree.update(l - 1, x);
                    gcdSegTree.update(r, -x);
                }
            }

            testIn.close();
            testOut.close();
        }
} 