import java.io.*;
import java.util.*;

public class Solution {
    class Node {
        int l, r;
        int val;
        Node lChild, rChild;

        Node(int left, int right) {
            this.l = left;
            this.r = right;
        }
    }
    class SegTreeAdditionAndGet {
        Node root;

        SegTreeAdditionAndGet(int[] a) {
            root = new Node(0, a.length - 1);
            build(a, root);
        }

        private void build(int[] a, Node curNode) {
            if (curNode.l == curNode.r) {
                curNode.val = a[curNode.l];
                return;
            }
            int mid = (curNode.l + curNode.r) / 2;
            curNode.lChild = new Node(curNode.l, mid);
            curNode.rChild = new Node(mid + 1, curNode.r);
            build(a, curNode.lChild);
            build(a, curNode.rChild);
            curNode.val = 0;
        }

        int get(int pos) {
            return get(root, pos);
        }

        private int get(Node curNode, int pos) {
            if (curNode.l == curNode.r)
                return curNode.val;
            if (pos <= curNode.lChild.r) {
                return curNode.val + get(curNode.lChild, pos);
            } else {
                return curNode.val + get(curNode.rChild, pos);
            }
        }

        void update(int l, int r, int add) {
            update(root, l, r, add);
        }

        private void update(Node curNode, int l, int r, int add) {
            if (l > r)
                return;
            if (curNode.l == l && curNode.r == r) {
                curNode.val += add;
                return;
            }
            update(curNode.lChild, l, Math.min(r, curNode.lChild.r), add);
            update(curNode.rChild, Math.max(l, curNode.rChild.l), r, add);
        }
    }

    public int gcdHelper(int a, int b) {
        if (a == 0)
            return b;
        if (b == 0)
            return a;

        a = Math.abs(a);
        b = Math.abs(b);

        if (a > b) {
            return gcdHelper(b, a);
        } else {
            return gcdHelper(b % a, a);
        }
    }

    class SegTreeGCD {
        Node root;
        int length;

        SegTreeGCD(int[] a) {
            if (a.length == 0)
                return;
            root = new Node(0, a.length - 1);
            length = a.length;
            build(a, root);
        }

        private void build(int[] a, Node curNode) {
            if (curNode.l == curNode.r) {
                curNode.val = a[curNode.l];
                return;
            }
            int mid = (curNode.l + curNode.r) / 2;
            curNode.lChild = new Node(curNode.l, mid);
            curNode.rChild = new Node(mid + 1, curNode.r);
            build(a, curNode.lChild);
            build(a, curNode.rChild);
            curNode.val = gcdHelper(curNode.lChild.val, curNode.rChild.val);
        }

        int query(int l, int r) {
            return query(root, l, r);
        }

        private int query(Node curNode, int l, int r) {
            if (l > r)
                return 0;
            if (curNode.l == l && curNode.r == r)
                return curNode.val;
            return gcdHelper(query(curNode.lChild, l, Math.min(r, curNode.lChild.r)),
                    query(curNode.rChild, Math.max(l, curNode.rChild.l), r));
        }

        void update(int pos, int add) {
            update(root, pos, add);
        }

        private void update(Node curNode, int pos, int add) {
            if (pos < 0 || pos >= length)
                return;
            if (curNode.l == curNode.r) {
                curNode.val += add;
                return;
            }
            if (pos <= curNode.lChild.r) {
                update(curNode.lChild, pos, add);
            } else {
                update(curNode.rChild, pos, add);
            }
            curNode.val = gcdHelper(curNode.lChild.val, curNode.rChild.val);
        }
    }

    public void runTestCases() throws IOException {
        int testCases = 20;
        int totalGcdQueries = 0;
        int passedGcdQueries = 0;

        for (int testCase = 1; testCase <= testCases; testCase++) {
            System.out.println("\n----- Test Case " + testCase + " -----");

            String testInFp = "programmingChallenge/io/test.in." + testCase;
            String testOutFp = "programmingChallenge/io/test.out." + testCase;

            try {
                BufferedReader testIn = new BufferedReader(new FileReader(testInFp));
                BufferedReader testOut = new BufferedReader(new FileReader(testOutFp));

                StringTokenizer st = new StringTokenizer(testIn.readLine());
                int N = Integer.parseInt(st.nextToken());
                int Q = Integer.parseInt(st.nextToken());

                System.out.println("Array size: " + N + ", Queries: " + Q);

                int[] a = new int[N];
                st = new StringTokenizer(testIn.readLine());
                for (int i = 0; i < N; i++) {
                    a[i] = Integer.parseInt(st.nextToken());
                }

                // Output first few elements of the array for verification
                System.out.print("Array sample: [");
                for (int i = 0; i < Math.min(5, N); i++) {
                    System.out.print(a[i] + (i < Math.min(5, N) - 1 ? ", " : ""));
                }
                System.out.println(N > 5 ? ", ...]" : "]");

                // Create the difference array
                int[] diffA = new int[N - 1];
                for (int i = 1; i < N; i++) {
                    diffA[i - 1] = a[i] - a[i - 1];
                }

                // Construct segment trees
                /* the problem with using one segment tree is that naively updating the values of the GCD segment tree is log-linear time 
                   we take advantage of the property that GCD(a, b) == GCD(a, a-b) to store the GCDs of the differences
                   this is advantageous because to update the GCDs of a range, we only need to update the boundaries, as the rest of the differences are unchanged
                   also, we already have a segment tree that can do range updates in logarithmic time if it's just addition
                   so: one segment tree handles a and the other one handles a-b in GCD(a, a-b), which is equivalent to GCD(a, b) */
                SegTreeAdditionAndGet addSegTree = new SegTreeAdditionAndGet(a);
                SegTreeGCD gcdSegTree = new SegTreeGCD(diffA);

                int gcdQueriesInTestCase = 0;
                int passedGcdQueriesInTestCase = 0;

                // Perform queries
                for (int i = 0; i < Q; i++) {
                    st = new StringTokenizer(testIn.readLine());
                    String queryType = st.nextToken();

                    if (queryType.equals("GCD")) {
                        int l = Integer.parseInt(st.nextToken());
                        int r = Integer.parseInt(st.nextToken());

                        int res = gcdHelper(addSegTree.get(l), gcdSegTree.query(l, r - 1));
                        gcdQueriesInTestCase++;
                        totalGcdQueries++;

                        // For testing
                        int expected = Integer.parseInt(testOut.readLine());

                        if (res == expected) {
                            passedGcdQueriesInTestCase++;
                            passedGcdQueries++;
                            System.out.println("GCD Query " + gcdQueriesInTestCase
                                    + ": PASSED - GCD(" + l + ", " + r + ") = " + res);
                        } else {
                            System.out.println("GCD Query " + gcdQueriesInTestCase
                                    + ": FAILED - GCD(" + l + ", " + r + ") Expected: " + expected
                                    + ", Got: " + res);
                        }
                    } else if (queryType.equals("ADD")) {
                        int l = Integer.parseInt(st.nextToken());
                        int r = Integer.parseInt(st.nextToken());
                        int x = Integer.parseInt(st.nextToken());

                        System.out.println(
                                "ADD Query: Adding " + x + " to range [" + l + ", " + r + "]");

                        // Normal range update
                        addSegTree.update(l, r, x);

                        // ADD x to the left boundary
                        if (l > 0) {
                            gcdSegTree.update(l - 1, x);
                        }

                        // SUBTRACT x from the right boundary
                        if (r < N - 1) {
                            gcdSegTree.update(r, -x);
                        }
                    }
                }

                System.out
                        .println("Test case " + testCase + " results: " + passedGcdQueriesInTestCase
                                + "/" + gcdQueriesInTestCase + " GCD queries passed");

                testIn.close();
                testOut.close();
            } catch (FileNotFoundException e) {
                System.out.println("Test case " + testCase + " files not found: " + e.getMessage());
            }
        }

        System.out.println("\n----- Overall Results -----");
        System.out.println("Total GCD queries across all test cases: " + passedGcdQueries + "/"
                + totalGcdQueries + " passed");
        if (passedGcdQueries == totalGcdQueries) {
            System.out.println("ALL TESTS PASSED! ✅");
        } else {
            System.out.println("SOME TESTS FAILED ❌");
        }
    }

    public static void main(String[] args) throws IOException {
        Solution solution = new Solution();
        solution.runTestCases();
    }
}
