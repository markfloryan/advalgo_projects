public class PST {
    private static final int MAX = 100001;
    private static final Node[] root = new Node[MAX];

    private final int n;
    private int version;

    public PST(int n) {
        this.n = n;
        this.version = 0;
    }

    private static Node makeNode() {
        return new Node();
    }

    private static long merge(Node l, Node r) {
        return l.value + r.value;
    }

    private Node construct(long[] val, int l, int r) {
        Node newNode = makeNode();
        if (l == r) {
            newNode.value = (val == null) ? 0 : val[l];
            newNode.maximum = (val == null) ? 0 : val[l];
            return newNode;
        }
        int middle = (l + r) / 2;
        newNode.leftChild = construct(val, l, middle);
        newNode.rightChild = construct(val, middle + 1, r);
        newNode.value = merge(newNode.leftChild, newNode.rightChild);
        return newNode;
    }

    private Node update(int l, int r, Node prev, int idx, long newVal, long newMax) {
        Node newNode = makeNode();
        if (l == r) {
            newNode.value = newVal;
            newNode.maximum = newMax;
        } else {
            int middle = (l + r) / 2;
            if (idx <= middle) {
                newNode.leftChild = update(l, middle, prev.leftChild, idx, newVal, newMax);
                newNode.rightChild = prev.rightChild;
            } else {
                newNode.rightChild = update(middle + 1, r, prev.rightChild, idx, newVal, newMax);
                newNode.leftChild = prev.leftChild;
            }
            newNode.value = merge(newNode.leftChild, newNode.rightChild);
        }
        return newNode;
    }

    private long query(Node node, int l, int r, int ql, int qr) {
        if (l > qr || r < ql) return 0;
        if (ql <= l && qr >= r) return node.value;
        int middle = (l + r) / 2;
        long left = query(node.leftChild, l, middle, ql, qr);
        long right = query(node.rightChild, middle + 1, r, ql, qr);
        return left + right;
    }

    private long queryNodeMaximum(Node node, int idx, int l, int r) {
        if (idx > r || idx < l) return -1;
        if (l == idx && r == idx) return node.maximum;
        int middle = (l + r) / 2;
        long left = queryNodeMaximum(node.leftChild, idx, l, middle);
        long right = queryNodeMaximum(node.rightChild, idx, middle + 1, r);
        return Math.max(left, right);
    }

    public void construct(long[] val) {
        root[0] = construct(val, 0, n - 1);
    }

    public void update(int idx, long val) {
        long oldMax = queryNodeMaximum(idx);
        long newMax = Math.max(val, oldMax);
        version++;
        root[version] = update(0, n - 1, root[version - 1], idx, val, newMax);
    }

    public long query(int queryVersion, int l, int r) {
        return query(root[queryVersion], 0, n - 1, l, r);
    }

    public long queryNodeMaximum(int idx) {
        return queryNodeMaximum(root[version], idx, 0, n - 1);
    }

    public void swap(int idx1, int idx2) {
        long val1 = query(version, idx1, idx1);
        long val2 = query(version, idx2, idx2);
        long max1 = queryNodeMaximum(idx1);
        long max2 = queryNodeMaximum(idx2);
        version++;
        Node temp = update(0, n - 1, root[version - 1], idx1, val2, Math.max(val2, max1));
        root[version] = update(0, n - 1, temp, idx2, val1, Math.max(val1, max2));
    }

    public static Node getVersion(int version) {
        return root[version];
    }

    public void printTree(Node node, int l, int r) {
        if (l == r) {
            System.out.println(node.value + " " + node.maximum);
        } else {
            int middle = (l + r) / 2;
            printTree(node.leftChild, l, middle);
            printTree(node.rightChild, middle + 1, r);
        }
    }

    public void printTree() {
        System.out.println("VERSION: " + version);
        printTree(root[version], 0, n - 1);
    }
}