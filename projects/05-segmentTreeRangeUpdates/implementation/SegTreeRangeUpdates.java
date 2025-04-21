package implementation;

import java.io.*;

class Node {
    int l, r, val;
    Node lChild, rChild;
    boolean marked; // For SegTreeAssignAndGet
    int lazy; // For SegTreeAdditionAndMax

    Node(int left, int right) {
        this.l = left;
        this.r = right;
        this.val = 0;
        this.marked = false;
        this.lazy = 0;
    }
}


// SegTreeStandard: Basic segment tree for range sum queries
class SegTreeStandard {
    Node root;

    public SegTreeStandard(int[] nums) {
        this.root = build(nums, 0, nums.length - 1);
    }

    private Node build(int[] nums, int l, int r) {
        Node node = new Node(l, r);

        if (l == r) {
            node.val = nums[l];
        } else {
            int mid = (l + r) / 2;
            node.lChild = build(nums, l, mid);
            node.rChild = build(nums, mid + 1, r);
            node.val = node.lChild.val + node.rChild.val;
        }
        return node;
    }

    public int query(int l, int r) {
        return query(root, l, r);
    }

    private int query(Node node, int l, int r) {
        if (node.r < l || node.l > r)
            return 0;
        if (l <= node.l && node.r <= r)
            return node.val;
        return query(node.lChild, l, r) + query(node.rChild, l, r);
    }

    public void update(int index, int newVal) {
        update(root, index, newVal);
    }

    public void update(Node node, int index, int newVal) {
        if (node.l == node.r) {
            node.val = newVal;
            return;
        }
        if (index <= node.lChild.r) {
            update(node.lChild, index, newVal);
        } else {
            update(node.rChild, index, newVal);
        }
        node.val = node.lChild.val + node.rChild.val;
    }
}


// SegTreeAdditionAndGet: Supports range addition and point queries
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


// SegTreeAssignAndGet: Supports range assignment and point queries
class SegTreeAssignAndGet {
    Node root;

    SegTreeAssignAndGet(int[] arr) {
        root = new Node(0, arr.length - 1);
        build(arr, root);
    }

    private void build(int[] arr, Node node) {
        if (node.l == node.r) {
            node.val = arr[node.l];
            node.marked = true;
            return;
        }
        int mid = (node.l + node.r) / 2;
        node.lChild = new Node(node.l, mid);
        node.rChild = new Node(mid + 1, node.r);
        build(arr, node.lChild);
        build(arr, node.rChild);
    }

    private void push(Node node) {
        if (node.marked && node.lChild != null && node.rChild != null) {
            // Push the assignment to children
            node.lChild.val = node.val;
            node.lChild.marked = true;

            node.rChild.val = node.val;
            node.rChild.marked = true;

            node.marked = false;
        }
    }

    void update(int l, int r, int val) {
        update(root, l, r, val);
    }

    private void update(Node node, int l, int r, int val) {
        if (r < node.l || l > node.r)
            return;

        if (l <= node.l && node.r <= r) {
            node.val = val;
            node.marked = true;
            return;
        }

        push(node);
        update(node.lChild, l, r, val);
        update(node.rChild, l, r, val);
    }

    int get(int index) {
        return get(root, index);
    }

    private int get(Node node, int index) {
        if (node.l == node.r)
            return node.val;

        push(node);
        if (index <= node.lChild.r)
            return get(node.lChild, index);
        else
            return get(node.rChild, index);
    }
}



class SegTreeAdditionAndMax {
    Node root;

    SegTreeAdditionAndMax(int[] arr) {
        root = new Node(0, arr.length - 1);
        build(arr, root);
    }

    private void build(int[] arr, Node node) {
        if (node.l == node.r) {
            node.val = arr[node.l];
            return;
        }
        int mid = (node.l + node.r) / 2;
        node.lChild = new Node(node.l, mid);
        node.rChild = new Node(mid + 1, node.r);
        build(arr, node.lChild);
        build(arr, node.rChild);
        node.val = Math.max(node.lChild.val, node.rChild.val);
    }

    private void pushDown(Node node) {
        if (node.lazy != 0) {
            // Push the lazy value to children
            if (node.lChild != null) {
                node.lChild.lazy += node.lazy;
                node.lChild.val += node.lazy;
            }
            if (node.rChild != null) {
                node.rChild.lazy += node.lazy;
                node.rChild.val += node.lazy;
            }
            node.lazy = 0;
        }
    }

    void update(int l, int r, int add) {
        update(root, l, r, add);
    }

    private void update(Node node, int l, int r, int add) {
        if (r < node.l || l > node.r)
            return;

        if (l <= node.l && node.r <= r) {
            node.val += add;
            node.lazy += add;
            return;
        }

        pushDown(node);
        update(node.lChild, l, r, add);
        update(node.rChild, l, r, add);
        node.val = Math.max(node.lChild.val, node.rChild.val);
    }

    int get(int index) {
        return get(root, index);
    }

    private int get(Node node, int index) {
        if (node.l == node.r)
            return node.val;

        pushDown(node);
        if (index <= node.lChild.r)
            return get(node.lChild, index);
        else
            return get(node.rChild, index);
    }

    int queryMax(int l, int r) {
        return queryMax(root, l, r);
    }

    private int queryMax(Node node, int l, int r) {
        if (r < node.l || l > node.r)
            return Integer.MIN_VALUE;

        if (l <= node.l && node.r <= r)
            return node.val;

        pushDown(node);
        return Math.max(queryMax(node.lChild, l, r), queryMax(node.rChild, l, r));
    }
}


// Main class for testing
public class SegTreeRangeUpdates {
    public static void main(String[] args) {
        for (int testCase = 1; testCase <= 3; testCase++) {
            String testInFp = "implementation/io/sample.in." + testCase;
            String testOutFp = "implementation/io/sample.out." + testCase;

            File inFile = new File(testInFp);
            File outFile = new File(testOutFp);

            // Check if files exist
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

                // Read the first line to determine test type
                int testType = Integer.parseInt(reader.readLine().trim());
                System.out
                        .println("Test type: " + testType + " ("
                                + (testType == 1 ? "Addition and Get"
                                        : testType == 2 ? "Assignment and Get"
                                                : testType == 3 ? "Addition and Max" : "Unknown")
                                + ")");

                // Read array size and number of operations
                String[] params = reader.readLine().split(" ");
                int n = Integer.parseInt(params[0]);
                int q = Integer.parseInt(params[1]);

                System.out.println("Array size: " + n + ", Operations: " + q);

                // Read the array values
                String[] values = reader.readLine().split(" ");
                int[] array = new int[n];
                for (int i = 0; i < n; i++) {
                    array[i] = Integer.parseInt(values[i]);
                }

                System.out.print("Initial array: ");
                for (int val : array) {
                    System.out.print(val + " ");
                }
                System.out.println();

                // Initialize the appropriate segment tree
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

                // Process operations
                for (int i = 0; i < q; i++) {
                    String line = reader.readLine();
                    if (line == null) {
                        System.err.println("Unexpected end of input file at operation " + i);
                        break;
                    }

                    String[] operation = line.split(" ");
                    String opType = operation[0];

                    if (opType.equals("GET")) {
                        int index = Integer.parseInt(operation[1]);
                        int result = 0;

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

                        // Check result against expected output
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
                    } else if (opType.equals("MAX")) {
                        if (testType != 3) {
                            System.err.println(
                                    "MAX operation found in non-MAX test type: " + testType);
                            continue;
                        }

                        int l = Integer.parseInt(operation[1]);
                        int r = Integer.parseInt(operation[2]);
                        int result = ((SegTreeAdditionAndMax) tree).queryMax(l, r);

                        // Check result against expected output
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
                    } else if (opType.equals("UPDATE")) {
                        int l = Integer.parseInt(operation[1]);
                        int r = Integer.parseInt(operation[2]);
                        int val = Integer.parseInt(operation[3]);

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
                System.err.println("Error in test case " + testCase + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}

