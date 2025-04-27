package implementation;

import java.io.*;

/**
 * Node class used by all segment tree implementations. Represents a range of the array with
 * associated data and children nodes.
 */
class Node {
    int l, r, val; // Left index, right index, and value stored at this node
    Node lChild, rChild; // References to left and right child nodes
    boolean marked; // Flag for lazy propagation in assignment operations
    int lazy; // Stores pending updates for lazy propagation in addition operations

    /**
     * Creates a new node covering the specified range [left, right]. Initializes with default
     * values for the different segment tree operations.
     */
    Node(int left, int right) {
        this.l = left;
        this.r = right;
        this.val = 0; // Default value (will be overridden during tree construction)
        this.marked = false; // No pending assignments
        this.lazy = 0; // No pending additions
    }
}


/**
 * Basic segment tree implementation for range sum queries. Allows querying the sum of any subarray
 * and updating individual elements.
 */
class SegTreeStandard {
    Node root; // Root node of the segment tree

    /**
     * Constructs a segment tree from the input array. Creates a complete binary tree where each
     * node represents a range of the array.
     */
    public SegTreeStandard(int[] nums) {
        this.root = build(nums, 0, nums.length - 1);
    }

    /**
     * Recursively builds the segment tree structure. Each node covers a range from l to r in the
     * original array. Leaf nodes contain individual array values, while internal nodes aggregate
     * their children.
     */
    private Node build(int[] nums, int l, int r) {
        Node node = new Node(l, r);

        if (l == r) {
            // Base case: leaf node represents a single array element
            node.val = nums[l];
        } else {
            // Recursively build left and right subtrees
            int mid = (l + r) / 2; // Divide the current range into two halves
            node.lChild = build(nums, l, mid);
            node.rChild = build(nums, mid + 1, r);

            // Internal node value is the sum of its children's values
            node.val = node.lChild.val + node.rChild.val;
        }
        return node;
    }

    /**
     * Public interface for querying the sum of elements in range [l, r].
     */
    public int query(int l, int r) {
        return query(root, l, r);
    }

    /**
     * Recursively computes the sum of elements in range [l, r]. Uses the tree structure to
     * efficiently skip irrelevant segments.
     */
    private int query(Node node, int l, int r) {
        // Case 1: Current node's range is completely outside the query range
        if (node.r < l || node.l > r)
            return 0; // This segment doesn't contribute to the result

        // Case 2: Current node's range is completely inside the query range
        if (l <= node.l && node.r <= r)
            return node.val; // Use the pre-computed sum for this segment

        // Case 3: Partial overlap - need to check both children
        return query(node.lChild, l, r) + query(node.rChild, l, r);
    }

    /**
     * Updates a single array element and recalculates affected sums in the tree.
     */
    public void update(int index, int newVal) {
        update(root, index, newVal);
    }

    /**
     * Recursively updates the tree after changing a single array element. Traverses down to the
     * leaf node and updates ancestors on the way back up.
     */
    public void update(Node node, int index, int newVal) {
        if (node.l == node.r) {
            // Base case: found the leaf node for this index
            node.val = newVal;
            return;
        }

        // Navigate to the correct child based on the index
        if (index <= node.lChild.r) {
            update(node.lChild, index, newVal);
        } else {
            update(node.rChild, index, newVal);
        }

        // Update this node's value by recalculating from children's updated values
        node.val = node.lChild.val + node.rChild.val;
    }
}


/**
 * Enhanced segment tree that supports range addition operations and point queries. Uses a "sum up
 * the path" approach for queries and direct range updates.
 */
class SegTreeAdditionAndGet {
    Node root;

    /**
     * Builds a segment tree for range addition operations. Each node initially holds the
     * corresponding value from the input array.
     */
    SegTreeAdditionAndGet(int[] a) {
        root = new Node(0, a.length - 1);
        build(a, root);
    }

    /**
     * Recursively constructs the segment tree from the input array. Unlike the standard segment
     * tree, internal nodes start with value 0 since additions will be propagated up the tree.
     */
    private void build(int[] a, Node curNode) {
        if (curNode.l == curNode.r) {
            // Base case: leaf node stores the original array value
            curNode.val = a[curNode.l];
            return;
        }

        // Split the range and build children recursively
        int mid = (curNode.l + curNode.r) / 2;
        curNode.lChild = new Node(curNode.l, mid);
        curNode.rChild = new Node(mid + 1, curNode.r);
        build(a, curNode.lChild);
        build(a, curNode.rChild);

        // Internal nodes start with 0 - values accumulate through updates
        curNode.val = 0;
    }

    /**
     * Retrieves the current value at a specific array position. Accumulates all modifications along
     * the path from root to the leaf.
     */
    int get(int pos) {
        return get(root, pos);
    }

    /**
     * Recursively traverses the tree to find the value at the specified position. The actual value
     * is the sum of all nodes along the path from root to leaf.
     */
    private int get(Node curNode, int pos) {
        if (curNode.l == curNode.r)
            return curNode.val; // Base case: reached the leaf node

        // Traverse down the tree while accumulating values
        if (pos <= curNode.lChild.r) {
            return curNode.val + get(curNode.lChild, pos); // Go left
        } else {
            return curNode.val + get(curNode.rChild, pos); // Go right
        }
    }

    /**
     * Adds a value to all elements in the range [l, r]. Uses a distributed storage approach rather
     * than lazy propagation.
     */
    void update(int l, int r, int add) {
        update(root, l, r, add);
    }

    /**
     * Recursively applies the addition to nodes covering the specified range. Stores the addition
     * at the highest possible nodes without propagating down.
     */
    private void update(Node curNode, int l, int r, int add) {
        if (l > r)
            return; // Invalid range check

        if (curNode.l == l && curNode.r == r) {
            // Case: Current node's range exactly matches the update range
            curNode.val += add; // Store the addition at this node
            return;
        }

        // Case: Partial match - recursively update appropriate children
        // Update left child if the range overlaps
        update(curNode.lChild, l, Math.min(r, curNode.lChild.r), add);

        // Update right child if the range overlaps
        update(curNode.rChild, Math.max(l, curNode.rChild.l), r, add);
    }
}


/**
 * Segment tree variant that supports range assignment operations and point queries. Uses lazy
 * propagation with a marking mechanism to delay propagating assignments.
 */
class SegTreeAssignAndGet {
    Node root;

    /**
     * Constructs the segment tree from an input array. Each leaf node stores its corresponding
     * array value.
     */
    SegTreeAssignAndGet(int[] arr) {
        root = new Node(0, arr.length - 1);
        build(arr, root);
    }

    /**
     * Recursively builds the tree structure from the input array. Leaf nodes are marked as
     * initialized with specific values.
     */
    private void build(int[] arr, Node node) {
        if (node.l == node.r) {
            // Base case: leaf node represents a single array element
            node.val = arr[node.l];
            node.marked = true; // Mark as having a definite value
            return;
        }

        // Build the left and right subtrees
        int mid = (node.l + node.r) / 2;
        node.lChild = new Node(node.l, mid);
        node.rChild = new Node(mid + 1, node.r);
        build(arr, node.lChild);
        build(arr, node.rChild);
    }

    /**
     * Pushes a pending assignment from a parent node to its children. This implements lazy
     * propagation to delay updates until necessary.
     */
    private void push(Node node) {
        if (node.marked && node.lChild != null && node.rChild != null) {
            // If this node has a pending assignment, propagate it to children
            node.lChild.val = node.val;
            node.lChild.marked = true; // Mark child as having a new assignment

            node.rChild.val = node.val;
            node.rChild.marked = true; // Mark child as having a new assignment

            node.marked = false; // Clear the mark after propagation
        }
    }

    /**
     * Assigns a value to all elements in range [l, r].
     */
    void update(int l, int r, int val) {
        update(root, l, r, val);
    }

    /**
     * Recursively applies the assignment to nodes covering the target range. Uses lazy propagation
     * to efficiently handle large ranges.
     */
    private void update(Node node, int l, int r, int val) {
        // Case: No overlap between node range and update range
        if (r < node.l || l > node.r)
            return;

        // Case: Node range is fully contained in update range
        if (l <= node.l && node.r <= r) {
            node.val = val; // Set the new value
            node.marked = true; // Mark for lazy propagation
            return;
        }

        // Case: Partial overlap - propagate pending updates before proceeding
        push(node);

        // Recursively update children
        update(node.lChild, l, r, val);
        update(node.rChild, l, r, val);
    }

    /**
     * Retrieves the current value at a specific position.
     */
    int get(int index) {
        return get(root, index);
    }

    /**
     * Recursively finds the value at the specified index, applying any pending assignments during
     * traversal.
     */
    private int get(Node node, int index) {
        if (node.l == node.r)
            return node.val; // Base case: reached the target leaf node

        // Ensure any pending assignments are propagated
        push(node);

        // Navigate to the appropriate child
        if (index <= node.lChild.r)
            return get(node.lChild, index);
        else
            return get(node.rChild, index);
    }
}


/**
 * Segment tree optimized for range addition operations and range maximum queries. Uses lazy
 * propagation to efficiently handle both operations.
 */
class SegTreeAdditionAndMax {
    Node root;

    /**
     * Constructs a segment tree optimized for maximum value queries. Each internal node stores the
     * maximum value in its range.
     */
    SegTreeAdditionAndMax(int[] arr) {
        root = new Node(0, arr.length - 1);
        build(arr, root);
    }

    /**
     * Recursively builds the segment tree from the input array. Each internal node stores the
     * maximum value of its subtree.
     */
    private void build(int[] arr, Node node) {
        if (node.l == node.r) {
            // Base case: leaf node represents a single array element
            node.val = arr[node.l];
            return;
        }

        // Build left and right subtrees
        int mid = (node.l + node.r) / 2;
        node.lChild = new Node(node.l, mid);
        node.rChild = new Node(mid + 1, node.r);
        build(arr, node.lChild);
        build(arr, node.rChild);

        // Internal node stores the maximum value from its children
        node.val = Math.max(node.lChild.val, node.rChild.val);
    }

    /**
     * Propagates pending addition operations from a node to its children. This is the core of lazy
     * propagation optimization.
     */
    private void pushDown(Node node) {
        if (node.lazy != 0) {
            // If there's a pending addition, propagate it to children
            if (node.lChild != null) {
                node.lChild.lazy += node.lazy; // Accumulate lazy value
                node.lChild.val += node.lazy; // Update actual value
            }
            if (node.rChild != null) {
                node.rChild.lazy += node.lazy; // Accumulate lazy value
                node.rChild.val += node.lazy; // Update actual value
            }
            node.lazy = 0; // Clear the lazy value after propagation
        }
    }

    /**
     * Adds a value to all elements in range [l, r].
     */
    void update(int l, int r, int add) {
        update(root, l, r, add);
    }

    /**
     * Recursively applies the addition to nodes covering the target range. Uses lazy propagation
     * for efficiency with large ranges.
     */
    private void update(Node node, int l, int r, int add) {
        // Case: No overlap between node range and update range
        if (r < node.l || l > node.r)
            return;

        // Case: Node range is fully contained in update range
        if (l <= node.l && node.r <= r) {
            node.val += add; // Update the maximum value
            node.lazy += add; // Store for lazy propagation
            return;
        }

        // Case: Partial overlap - propagate pending updates before proceeding
        pushDown(node);

        // Recursively update children
        update(node.lChild, l, r, add);
        update(node.rChild, l, r, add);

        // Recalculate this node's maximum from its children
        node.val = Math.max(node.lChild.val, node.rChild.val);
    }

    /**
     * Retrieves the current value at a specific position.
     */
    int get(int index) {
        return get(root, index);
    }

    /**
     * Recursively finds the value at the specified index, applying any pending additions during
     * traversal.
     */
    private int get(Node node, int index) {
        if (node.l == node.r)
            return node.val; // Base case: reached the target leaf node

        // Ensure any pending additions are propagated
        pushDown(node);

        // Navigate to the appropriate child
        if (index <= node.lChild.r)
            return get(node.lChild, index);
        else
            return get(node.rChild, index);
    }

    /**
     * Finds the maximum value in the range [l, r].
     */
    int queryMax(int l, int r) {
        return queryMax(root, l, r);
    }

    /**
     * Recursively finds the maximum value in the specified range, applying any pending lazy updates
     * during traversal.
     */
    private int queryMax(Node node, int l, int r) {
        // Case: No overlap between node range and query range
        if (r < node.l || l > node.r)
            return Integer.MIN_VALUE; // Identity element for max operation

        // Case: Node range is fully contained in query range
        if (l <= node.l && node.r <= r)
            return node.val; // Use the pre-computed maximum

        // Case: Partial overlap - propagate pending updates first
        pushDown(node);

        // Find maximum across both children
        return Math.max(queryMax(node.lChild, l, r), queryMax(node.rChild, l, r));
    }
}


/**
 * Main class to test the different segment tree implementations against test cases. Reads input
 * files, executes operations, and compares results with expected outputs.
 */
public class SegTreeRangeUpdates {
    public static void main(String[] args) {
        // Run each of the three test cases
        for (int testCase = 1; testCase <= 3; testCase++) {
            // Construct file paths for test case input and expected output
            String testInFp = "implementation/io/sample.in." + testCase;
            String testOutFp = "implementation/io/sample.out." + testCase;

            File inFile = new File(testInFp);
            File outFile = new File(testOutFp);

            // Validate that test files exist before proceeding
            if (!inFile.exists()) {
                System.err.println("Input file not found: " + testInFp);
                continue;
            }

            if (!outFile.exists()) {
                System.err.println("Output file not found: " + testOutFp);
                continue;
            }

            System.out.println("\nRunning test case " + testCase);

            try (BufferedReader reader = new BufferedReader(new FileReader(inFile));
                    BufferedReader outReader = new BufferedReader(new FileReader(outFile))) {

                // Read the test type to determine which segment tree implementation to use
                int testType = Integer.parseInt(reader.readLine().trim());
                System.out
                        .println("Test type: " + testType + " ("
                                + (testType == 1 ? "Addition and Get"
                                        : testType == 2 ? "Assignment and Get"
                                                : testType == 3 ? "Addition and Max" : "Unknown")
                                + ")");

                // Read problem parameters: array size and number of operations
                String[] params = reader.readLine().split(" ");
                int n = Integer.parseInt(params[0]); // Array size
                int q = Integer.parseInt(params[1]); // Number of operations

                System.out.println("Array size: " + n + ", Operations: " + q);

                // Parse the initial array values
                String[] values = reader.readLine().split(" ");
                int[] array = new int[n];
                for (int i = 0; i < n; i++) {
                    array[i] = Integer.parseInt(values[i]);
                }

                // Display the initial array for debugging
                System.out.print("Initial array: ");
                for (int val : array) {
                    System.out.print(val + " ");
                }
                System.out.println();

                // Create the appropriate segment tree variant based on test type
                Object tree = null;
                switch (testType) {
                    case 1: // Addition and Get
                        tree = new SegTreeAdditionAndGet(array);
                        break;
                    case 2: // Assignment and Get
                        tree = new SegTreeAssignAndGet(array);
                        break;
                    case 3: // Addition and Max
                        tree = new SegTreeAdditionAndMax(array);
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown test type: " + testType);
                }

                // Process each operation from the input file
                for (int i = 0; i < q; i++) {
                    String line = reader.readLine();
                    if (line == null) {
                        System.err.println("Unexpected end of input file at operation " + i);
                        break;
                    }

                    // Parse the operation details
                    String[] operation = line.split(" ");
                    String opType = operation[0];

                    // Handle GET operations (retrieve a single value)
                    if (opType.equals("GET")) {
                        int index = Integer.parseInt(operation[1]);
                        int result = 0;

                        // Execute GET on the appropriate tree type
                        switch (testType) {
                            case 1:
                                result = ((SegTreeAdditionAndGet) tree).get(index);
                                break;
                            case 2:
                                result = ((SegTreeAssignAndGet) tree).get(index);
                                break;
                            case 3:
                                result = ((SegTreeAdditionAndMax) tree).get(index);
                                break;
                        }

                        // Verify result against expected output
                        String expectedLine = outReader.readLine();
                        if (expectedLine == null) {
                            System.err.println("Unexpected end of output file at operation " + i);
                            break;
                        }

                        int expected = Integer.parseInt(expectedLine.trim());
                        if (result == expected) {
                            System.out.println("GET " + index + ": " + result + " ✓");
                        } else {
                            System.out.println("GET " + index + ": " + result + " ✗ (Expected: "
                                    + expected + ")");
                        }
                    }
                    // Handle MAX operations (find maximum in a range)
                    else if (opType.equals("MAX")) {
                        // Validate that MAX is only used with the Addition and Max tree
                        if (testType != 3) {
                            System.err.println(
                                    "MAX operation found in non-MAX test type: " + testType);
                            continue;
                        }

                        int l = Integer.parseInt(operation[1]);
                        int r = Integer.parseInt(operation[2]);
                        int result = ((SegTreeAdditionAndMax) tree).queryMax(l, r);

                        // Verify result against expected output
                        String expectedLine = outReader.readLine();
                        if (expectedLine == null) {
                            System.err.println("Unexpected end of output file at operation " + i);
                            break;
                        }

                        int expected = Integer.parseInt(expectedLine.trim());
                        if (result == expected) {
                            System.out.println("MAX " + l + " " + r + ": " + result + " ✓");
                        } else {
                            System.out.println("MAX " + l + " " + r + ": " + result
                                    + " ✗ (Expected: " + expected + ")");
                        }
                    }
                    // Handle UPDATE operations (modify values in a range)
                    else if (opType.equals("UPDATE")) {
                        int l = Integer.parseInt(operation[1]);
                        int r = Integer.parseInt(operation[2]);
                        int val = Integer.parseInt(operation[3]);

                        // Execute UPDATE on the appropriate tree type
                        switch (testType) {
                            case 1:
                                ((SegTreeAdditionAndGet) tree).update(l, r, val);
                                break;
                            case 2:
                                ((SegTreeAssignAndGet) tree).update(l, r, val);
                                break;
                            case 3:
                                ((SegTreeAdditionAndMax) tree).update(l, r, val);
                                break;
                        }

                        System.out.println("UPDATE " + l + " " + r + " " + val);
                    }
                }

                System.out.println("Test case " + testCase + " completed");

            } catch (Exception e) {
                // Handle any errors that occur during test execution
                System.err.println("Error in test case " + testCase + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
