import java.util.*;

public class pcTreaps_java {
    // Each node represents a banana: (position, ready time)
    static class Node {
        int p, t; // p = position, t = time when banana becomes eatable
        int size; // number of nodes in subtree (not needed here, but standard in treaps)
        long priority; // randomized heap priority
        Node left, right;

        Node(int p, int t) {
            this.p = p; this.t = t;
            this.size = 1;
            this.priority = RNG.nextLong();
        }
    }

    static Random RNG = new Random(42);
    static Node root = null;

    // Helper: return size of subtree
    static int size(Node x) {
        return x == null ? 0 : x.size;
    }

    // Update size field (useful if we ever want to extend this)
    static void update(Node x) {
        if (x != null) {
            x.size = 1 + size(x.left) + size(x.right);
        }
    }

    // Split treap into (< key) and (≥ key)
    static Node[] split(Node x, int key) {
        if (x == null) return new Node[]{null, null};
        if (key < x.p) {
            Node[] left = split(x.left, key);
            x.left = left[1];
            update(x);
            return new Node[]{left[0], x};
        } else {
            Node[] right = split(x.right, key);
            x.right = right[0];
            update(x);
            return new Node[]{x, right[1]};
        }
    }

    // Merge two treaps assuming all keys in a < keys in b
    static Node merge(Node a, Node b) {
        if (a == null || b == null) return a != null ? a : b;
        if (a.priority > b.priority) {
            a.right = merge(a.right, b);
            update(a);
            return a;
        } else {
            b.left = merge(a, b.left);
            update(b);
            return b;
        }
    }

    // Insert node into treap
    static Node insert(Node x, Node n) {
        if (x == null) return n;
        if (n.priority > x.priority) {
            Node[] sp = split(x, n.p);
            n.left = sp[0]; n.right = sp[1];
            update(n);
            return n;
        }
        if (n.p < x.p) x.left = insert(x.left, n);
        else x.right = insert(x.right, n);
        update(x);
        return x;
    }

    // Remove node with given key from treap
    static Node remove(Node x, int key) {
        if (x == null) return null;
        if (x.p == key) return merge(x.left, x.right);
        if (key < x.p) x.left = remove(x.left, key);
        else x.right = remove(x.right, key);
        update(x);
        return x;
    }

    // Simulate the monkey moving from position 0, eating as many bananas as possible within T seconds
    static int simulate(Node x, int T) {
        int pos = 0, time = 0, cnt = 0;
        // iterative in-order traversal
        Deque<Node> stack = new ArrayDeque<>();
        Node cur = x;

        while (cur != null || !stack.isEmpty()) {
            while (cur != null) {
                stack.push(cur);
                cur = cur.left;
            }
            cur = stack.pop();

            // Move from current position to next banana
            int move = cur.p - pos;
            time += move;
            pos = cur.p;

            if (time > T) break;

            // Can eat immediately
            if (time >= cur.t) {
                cnt++;
            }
            // Banana not ready yet, but we can wait
            else if (cur.t <= T) {
                time = cur.t;
                cnt++;
            }
            // Banana won't be ready in time
            else {
                cur = cur.right;
                continue;
            }
            cur = cur.right;
        }
        return cnt;
    }

    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);
        int q = Integer.parseInt(sc.nextLine());
        while (q-- > 0) {
            String[] tok = sc.nextLine().split(" ");
            switch (tok[0]) {
                case "ADD" -> {
                    int p = Integer.parseInt(tok[1]);
                    int t = Integer.parseInt(tok[2]);
                    root = insert(root, new Node(p, t));
                }
                case "REMOVE" -> {
                    int p = Integer.parseInt(tok[1]);
                    root = remove(root, p);
                }
                case "QUERY" -> {
                    int T = Integer.parseInt(tok[1]);
                    System.out.println(simulate(root, T));
                }
            }
        }
    }
}
