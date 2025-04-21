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


    public static void main(String[] args) throws IOException {
        int testCases = 20;

        for (int testCase = 1; testCase <= testCases; testCase++) {
            System.out.println("Running test case " + testCase);

            String testInPath = "programmingChallenge/io/test.in." + testCase;
            String testOutPath = "programmingChallenge/io/test.out." + testCase;

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

            Solution solutionInstance = new Solution();
            SegTreeAdditionAndGet addSegTree = solutionInstance.new SegTreeAdditionAndGet(a);
            SegTreeGCD gcdSegTree = solutionInstance.new SegTreeGCD(diffA);

            boolean passed = true;

            for (int i = 0; i < Q; i++) {
                String line = testIn.readLine();
                tokenizer = new StringTokenizer(line);
                String queryType = tokenizer.nextToken();

                if (queryType.equals("GCD")) {
                    int l = Integer.parseInt(tokenizer.nextToken());
                    int r = Integer.parseInt(tokenizer.nextToken());

                    int aL = addSegTree.get(l);
                    int gcd = gcdSegTree.query(l, r - 1);
                    int res = solutionInstance.gcdHelper(aL, gcd);

                    int expected = Integer.parseInt(testOut.readLine().trim());
                    if (res != expected) {
                        System.out.println("Test case " + testCase
                                + " failed on GCD query: expected " + expected + ", got " + res);
                        passed = false;
                    }

                } else if (queryType.equals("ADD")) {
                    int l = Integer.parseInt(tokenizer.nextToken());
                    int r = Integer.parseInt(tokenizer.nextToken());
                    int x = Integer.parseInt(tokenizer.nextToken());

                    addSegTree.update(l, r, x);
                    gcdSegTree.update(l - 1, x);
                    gcdSegTree.update(r, -x);
                }
            }

            testIn.close();
            testOut.close();

            if (passed) {
                System.out.println("Test case " + testCase + " PASSED");
            } else {
                System.out.println("Test case " + testCase + " FAILED");
            }
        }
    }
}
