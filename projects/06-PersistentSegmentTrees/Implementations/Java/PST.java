// Persistent Segment Tree class
public class PST {
    int n;
    int version;
    int size;

//   --------- Private Methods ---------

    // merge is simply the addition of the two values as this tree stores integers
    private static long merge(Node l, Node r){
        return l.value + r.value;
    }

    // Private recursive constructor function: takes array of values to store and creates the tree
    private Node construct(long[] val, int l, int r){
        Node newNode = new Node();
        // base case: if l == r, set the value of the node to the value at that index to be stored
        if(l == r){
            newNode.value = (val == null ? 0 : val[l]);
            return newNode;
        }
        // otherwise recursively create the left and right children
        int middle = (l + r)/2;
        newNode.leftChild = construct(val, l, middle);
        newNode.rightChild = construct(val, middle+1, r);
        // once the children finish their construction, merge their values to set the value of the parent (this) node
        newNode.value = merge(newNode.leftChild, newNode.rightChild);
        return newNode;
    }

    // Private recursive update function
    private Node update(int l, int r, Node prevVer, int idx, long newVal) {
        Node newNode = new Node();
        // base case: if l == r we've found the node to update in the previous version tree, now set the new version's node to the new value
        if(l == r){
            newNode.value = newVal;
        } else {
            // otherwise recurse down the tree to find the node to update
            int middle = (l + r) / 2;
            if (idx <= middle){
                // if the index is in the left half, update the left child and set the right child to the previous version's right child
                newNode.leftChild = update(l, middle, prevVer.leftChild, idx, newVal);
                newNode.rightChild = prevVer.rightChild;
            } else {
                // if the index is in the right half, update the right child and set the left child to the previous version's left child
                newNode.rightChild = update(middle + 1, r, prevVer.rightChild, idx, newVal);
                newNode.leftChild = prevVer.leftChild;
            }
            // once the children finish their updates, merge their values to set the value of the parent (this) node for this new version
            newNode.value = merge(newNode.leftChild, newNode.rightChild);
        }
        return newNode;
    }

    // Private query function
    private long query(Node node, int l, int r, int ql, int qr) {
        // indices are oob
        if (l > qr || r < ql) {
            return 0;
        }
        // if the current node is completely within the query range, return its value
        if (ql <= l && qr >= r) {
            return node.value;
        } else {
            // otherwise, recurse down the tree to find the sum of the values in the range
            // by looking at the left and right children
            int middle = (l + r) / 2;
            long left = query(node.leftChild, l, middle, ql, qr);
            long right = query(node.rightChild, middle + 1, r, ql, qr);
            return left + right;
        }
    }

//   --------- Public Methods ---------

    // max number of versions, can be made larger or smaller. each update creates a new version
    int MAX = 100;
    // global array of nodes where each Node is a root node for that version; each index represents a version
    // index 0 is the original tree and each subsequent index is a new version created by an update
    public Node[] root = new Node[MAX];


    public PST(int n) {
        this.n = n;
        this.version = 0;
        this.size = 0;
    }

    // Public constructor function; takes an array of values to store
    // calls private constructor function
    public void construct(long[] val){
        root[0] = construct(val, 0, n-1);
    }

    // Public update function; takes an index to update and the new value
    // create a new version of the tree and call the private update function
    public void update(int idx, long val) {
        this.version = version + 1;
        root[version] = update(0, n-1, root[version-1], idx, val);
    }

    // Public query function; takes a version, left index, and right index
    // calls the private query function to get the sum of the values in the range
    public long query(int queryVersion, int l, int r){
        return query(root[queryVersion], 0, n-1, l, r);
    }

    // helper function to get a pointer to the root node of a specific version
    // can be used to look at the state of the tree at that version
    public Node getVersion(int version){
        return root[version];
    }

}
