import java.io.*;
import java.util.*;

/**
 * Solution class that demonstrates using segment trees for calculating GCD over ranges by using a
 * clever combination of a regular segment tree and a difference array. This approach allows for
 * efficient range updates and GCD queries.
 */
public class Solution {
    /**
     * Node class representing a segment tree node. Each node covers a range of the array and stores
     * aggregated information.
     */
    class Node {
        int l, r; // Left and right boundaries of the segment
        int val; // Value stored at this node (sum, GCD, etc.)
        Node lChild, rChild; // References to left and right children

        /**
         * Creates a node representing the segment [left, right] of the array.
         */
        Node(int left, int right) {
            this.l = left;
            this.r = right;
            // Value will be set during tree construction
        }
    }

    /**
     * Segment tree implementation for range addition operations and point queries. Similar to the
     * previous example, but included here as part of this solution.
     */
    class SegTreeAdditionAndGet {
        Node root; // Root node of the segment tree

        /**
         * Constructs a segment tree from the input array. The tree is designed to support range
         * additions efficiently.
         */
        SegTreeAdditionAndGet(int[] a) {
            root = new Node(0, a.length - 1);
            build(a, root);
        }

        /**
         * Recursively builds the segment tree structure. Leaf nodes store original array values,
         * while internal nodes are initialized to 0 since they'll accumulate additions.
         */
        private void build(int[] a, Node curNode) {
            if (curNode.l == curNode.r) {
                // Base case: leaf node corresponds to a single array element
                curNode.val = a[curNode.l];
                return;
            }

            // Split the current range into two halves and build subtrees
            int mid = (curNode.l + curNode.r) / 2;
            curNode.lChild = new Node(curNode.l, mid);
            curNode.rChild = new Node(mid + 1, curNode.r);
            build(a, curNode.lChild);
            build(a, curNode.rChild);

            // Internal nodes start with 0 value - additions will be stored here
            curNode.val = 0;
        }

        /**
         * Retrieves the current value at a specific position. Accumulates all modifications along
         * the path from root to leaf.
         */
        int get(int pos) {
            return get(root, pos);
        }

        /**
         * Recursively traverses the tree to find the value at position pos. The actual value is the
         * sum of all nodes along the path from root to leaf.
         */
        private int get(Node curNode, int pos) {
            if (curNode.l == curNode.r)
                return curNode.val; // Reached leaf node with the actual value

            // Continue traversing while accumulating values
            if (pos <= curNode.lChild.r) {
                // Position is in left subtree
                return curNode.val + get(curNode.lChild, pos);
            } else {
                // Position is in right subtree
                return curNode.val + get(curNode.rChild, pos);
            }
        }

        /**
         * Adds a value to all elements in range [l, r]. Uses efficient range update strategy
         * without propagating to leaves immediately.
         */
        void update(int l, int r, int add) {
            update(root, l, r, add);
        }

        /**
         * Recursively applies the addition to all nodes covering parts of the target range. Uses a
         * "responsibility chain" approach where each node stores only what it's responsible for.
         */
        private void update(Node curNode, int l, int r, int add) {
            if (l > r)
                return; // Invalid range check

            if (curNode.l == l && curNode.r == r) {
                // Perfect match: current node's range exactly matches update range
                curNode.val += add; // Store the addition at this higher-level node
                return;
            }

            // Partial match: split the update across children
            // For left child, the range is [l, min(r, curNode.lChild.r)]
            update(curNode.lChild, l, Math.min(r, curNode.lChild.r), add);

            // For right child, the range is [max(l, curNode.rChild.l), r]
            update(curNode.rChild, Math.max(l, curNode.rChild.l), r, add);
        }
    }

    /**
     * Helper method to calculate GCD (Greatest Common Divisor) of two numbers. Uses the Euclidean
     * algorithm for efficient GCD calculation.
     */
    public int gcdHelper(int a, int b) {
        if (a == 0)
            return b; // GCD(0, b) = b
        if (b == 0)
            return a; // GCD(a, 0) = a

        // Ensure positive values for GCD calculation
        a = Math.abs(a);
        b = Math.abs(b);

        if (a > b) {
            // Ensure a is smaller than b for the algorithm to work efficiently
            return gcdHelper(b, a);
        } else {
            // The core of Euclidean algorithm: gcd(a, b) = gcd(b % a, a)
            return gcdHelper(b % a, a);
        }
    }

    /**
     * Segment tree specialized for GCD range queries. Efficiently finds the GCD of any range in the
     * array.
     */
    class SegTreeGCD {
        Node root; // Root node of the tree
        int length; // Length of the original array

        /**
         * Constructs a segment tree for GCD queries from the input array. Each internal node stores
         * the GCD of its children's values.
         */
        SegTreeGCD(int[] a) {
            if (a.length == 0)
                return; // Handle empty array case

            root = new Node(0, a.length - 1);
            length = a.length;
            build(a, root);
        }

        /**
         * Recursively builds the GCD segment tree. Each node stores the GCD of all elements in its
         * range.
         */
        private void build(int[] a, Node curNode) {
            if (curNode.l == curNode.r) {
                // Base case: leaf node corresponds to a single array element
                curNode.val = a[curNode.l];
                return;
            }

            // Split current range and build subtrees
            int mid = (curNode.l + curNode.r) / 2;
            curNode.lChild = new Node(curNode.l, mid);
            curNode.rChild = new Node(mid + 1, curNode.r);
            build(a, curNode.lChild);
            build(a, curNode.rChild);

            // Internal node stores GCD of its children's values
            curNode.val = gcdHelper(curNode.lChild.val, curNode.rChild.val);
        }

        /**
         * Query the GCD of elements in range [l, r].
         */
        int query(int l, int r) {
            return query(root, l, r);
        }

        /**
         * Recursively find the GCD of all elements in range [l, r]. Utilizes the tree structure to
         * efficiently compute GCD of large ranges.
         */
        private int query(Node curNode, int l, int r) {
            if (l > r)
                return 0; // Invalid range or empty range, GCD would be undefined

            if (curNode.l == l && curNode.r == r)
                return curNode.val; // Perfect range match, use pre-computed GCD

            // Split the query into two parts and combine with GCD
            return gcdHelper(
                    // Query left child for range [l, min(r, curNode.lChild.r)]
                    query(curNode.lChild, l, Math.min(r, curNode.lChild.r)),
                    // Query right child for range [max(l, curNode.rChild.l), r]
                    query(curNode.rChild, Math.max(l, curNode.rChild.l), r));
        }

        /**
         * Update a single position in the array and recalculate affected GCDs. Adds a value to the
         * element at position pos.
         */
        void update(int pos, int add) {
            update(root, pos, add);
        }

        /**
         * Recursively update the tree after changing an element. Traverses down to the leaf and
         * updates GCDs on the way back up.
         */
        private void update(Node curNode, int pos, int add) {
            // Check for valid position
            if (pos < 0 || pos >= length)
                return;

            if (curNode.l == curNode.r) {
                // Base case: found the leaf node for this position
                curNode.val += add; // Apply the update to this element
                return;
            }

            // Navigate to the correct child based on position
            if (pos <= curNode.lChild.r) {
                update(curNode.lChild, pos, add); // Position in left subtree
            } else {
                update(curNode.rChild, pos, add); // Position in right subtree
            }

            // Recalculate this node's GCD based on updated children
            curNode.val = gcdHelper(curNode.lChild.val, curNode.rChild.val);
        }
    }

    /**
     * Runs a series of test cases to verify the correctness of the implementation. Tests both range
     * addition operations and GCD queries.
     */
    public void runTestCases() throws IOException {
        int testCases = 20; // Total number of test cases to run
        int totalGcdQueries = 0; // Count of all GCD queries across all tests
        int passedGcdQueries = 0; // Count of correctly answered GCD queries

        for (int testCase = 1; testCase <= testCases; testCase++) {
            System.out.println("\n----- Test Case " + testCase + " -----");

            // Construct paths to input and output files for this test case
            String testInFp = "programmingChallenge/io/test.in." + testCase;
            String testOutFp = "programmingChallenge/io/test.out." + testCase;

            try {
                // Open the input and expected output files
                BufferedReader testIn = new BufferedReader(new FileReader(testInFp));
                BufferedReader testOut = new BufferedReader(new FileReader(testOutFp));

                // Parse problem parameters: array size and number of queries
                StringTokenizer st = new StringTokenizer(testIn.readLine());
                int N = Integer.parseInt(st.nextToken()); // Array size
                int Q = Integer.parseInt(st.nextToken()); // Number of queries

                System.out.println("Array size: " + N + ", Queries: " + Q);

                // Read the initial array
                int[] a = new int[N];
                st = new StringTokenizer(testIn.readLine());
                for (int i = 0; i < N; i++) {
                    a[i] = Integer.parseInt(st.nextToken());
                }

                // Display a sample of the array for verification
                System.out.print("Array sample: [");
                for (int i = 0; i < Math.min(5, N); i++) {
                    System.out.print(a[i] + (i < Math.min(5, N) - 1 ? ", " : ""));
                }
                System.out.println(N > 5 ? ", ...]" : "]");

                // Create a difference array: each element is the difference between
                // adjacent elements in the original array
                // This is key to the algorithm for handling both range updates and GCD queries
                int[] diffA = new int[N - 1];
                for (int i = 1; i < N; i++) {
                    diffA[i - 1] = a[i] - a[i - 1];
                }

                // Construct two segment trees:
                // 1. A regular segment tree for handling range additions and point queries
                SegTreeAdditionAndGet addSegTree = new SegTreeAdditionAndGet(a);
                // 2. A GCD segment tree for the difference array
                SegTreeGCD gcdSegTree = new SegTreeGCD(diffA);

                int gcdQueriesInTestCase = 0;
                int passedGcdQueriesInTestCase = 0;

                // Process each query from the input file
                for (int i = 0; i < Q; i++) {
                    st = new StringTokenizer(testIn.readLine());
                    String queryType = st.nextToken();

                    if (queryType.equals("GCD")) {
                        // Handle GCD query - find GCD of all elements in range [l, r]
                        int l = Integer.parseInt(st.nextToken());
                        int r = Integer.parseInt(st.nextToken());

                        // Key insight: GCD of range [l, r] can be computed from:
                        // 1. The value at position l
                        // 2. The GCD of differences in range [l, r-1]
                        int res = gcdHelper(addSegTree.get(l), gcdSegTree.query(l, r - 1));
                        gcdQueriesInTestCase++;
                        totalGcdQueries++;

                        // Verify against expected result
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
                        // Handle ADD query - add a value to all elements in range [l, r]
                        int l = Integer.parseInt(st.nextToken());
                        int r = Integer.parseInt(st.nextToken());
                        int x = Integer.parseInt(st.nextToken());

                        System.out.println(
                                "ADD Query: Adding " + x + " to range [" + l + ", " + r + "]");

                        // Update the regular segment tree for range addition
                        addSegTree.update(l, r, x);

                        // Key insight: When adding x to range [l,r], the differences change at:
                        // 1. Position l-1: difference increases by x (if l > 0)
                        // 2. Position r: difference decreases by x (if r < N-1)

                        // Increase the difference at the left boundary
                        if (l > 0) {
                            gcdSegTree.update(l - 1, x);
                        }

                        // Decrease the difference at the right boundary
                        if (r < N - 1) {
                            gcdSegTree.update(r, -x);
                        }
                    }
                }

                // Report results for this test case
                System.out
                        .println("Test case " + testCase + " results: " + passedGcdQueriesInTestCase
                                + "/" + gcdQueriesInTestCase + " GCD queries passed");

                testIn.close();
                testOut.close();
            } catch (FileNotFoundException e) {
                System.out.println("Test case " + testCase + " files not found: " + e.getMessage());
            }
        }

        // Report overall results across all test cases
        System.out.println("\n----- Overall Results -----");
        System.out.println("Total GCD queries across all test cases: " + passedGcdQueries + "/"
                + totalGcdQueries + " passed");
        if (passedGcdQueries == totalGcdQueries) {
            System.out.println("ALL TESTS PASSED! ✅");
        } else {
            System.out.println("SOME TESTS FAILED ❌");
        }
    }

    /**
     * Entry point of the program. Creates a Solution instance and runs the test cases.
     */
    public static void main(String[] args) throws IOException {
        Solution solution = new Solution();
        solution.runTestCases();
    }
}
