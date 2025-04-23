#include <iostream>
#include <fstream>
using namespace std;

// max number of versions, can be made larger or smaller. each update creates a new version
constexpr int MAX = 100;
using ll = long long;

// node struct that stores a long long as well as pointers to left and right children
// can be updated to store other data types; would potentially require updating the merge function
struct Node {
    ll value;
    Node *leftChild;
    Node *rightChild;
    // default constructor sets values to null
    Node() {
        leftChild = rightChild = nullptr;
        value = 0;
    }
    Node(Node *l, Node *r, ll v) {
        leftChild = l, rightChild = r;
        value = v;
    }
};

// global array of pointers to nodes where each Node is a root node for that version; each index represents a version
// index 0 is the original tree and each subsequent index is a new version created by an update
Node *root[MAX];

// Persistent Segment Tree class
class PST {
    const int n;
    int version;
    int size;

    static Node* makeNode() {
        return new Node;
    }

    // merge is simply the addition of the two values as this tree stores integers
    static ll merge(const Node *l, const Node *r) {
        return l->value + r->value;
    }

    // Private recursive constructor function: takes array of values to store and creates the tree
    Node* construct(ll val[], int l, int r) {
        Node *newNode = makeNode();
        // base case: if l == r, set the value of the node to the value at that index to be stored
        if (l == r) {
            newNode->value = (val==nullptr ? 0 : val[l]);
            return newNode;
        }
        // otherwise recursively create the left and right children
        const int middle = (l + r) / 2;
        newNode->leftChild = construct(val, l, middle);
        newNode->rightChild = construct(val, middle + 1, r);
        // once the children finish their construction, merge their values to set the value of the parent (this) node
        newNode->value = merge(newNode->leftChild, newNode->rightChild);
        return newNode;
    }

    // Private recursive update function
    Node* update(const int l, const int r, const Node *prevVerPtr, const int idx, const ll newVal) {
        Node *newNode = makeNode();
        // base case: if l == r we've found the node to update in the previous version tree, now set the new version's node to the new value
        if (l == r) {
            newNode->value = newVal;
        } else {
            // otherwise recurse down the tree to find the node to update
            if (const int middle = (l + r) / 2; idx <= middle) {
                // if the index is in the left half, update the left child and set the right child to the previous version's right child
                newNode->leftChild = update(l, middle, prevVerPtr->leftChild, idx, newVal);
                newNode->rightChild = prevVerPtr->rightChild;
            } else {
                // if the index is in the right half, update the right child and set the left child to the previous version's left child
                newNode->rightChild = update(middle + 1, r, prevVerPtr->rightChild, idx, newVal);
                newNode->leftChild = prevVerPtr->leftChild;
            }
            // once the children finish their updates, merge their values to set the value of the parent (this) node for this new version
            newNode->value = merge(newNode->leftChild, newNode->rightChild);
        }
        return newNode;
    }

    // Private query function
    ll query(const Node* node, const int l, const int r, const int ql, const int qr) {
        // indices are oob
        if (l > qr || r < ql) {
            return 0;
        }
        // if the current node is completely within the query range, return its value
        if (ql <= l && qr >= r) {
            return node->value;
        } else {
            // otherwise, recurse down the tree to find the sum of the values in the range
            // by looking at the left and right children
            const int middle = (l + r) / 2;
            const ll left = query(node->leftChild, l, middle, ql, qr);
            const ll right = query(node->rightChild, middle + 1, r, ql, qr);
            return left + right;
        }
    }

public:

    explicit PST(const int n): n(n) {
        version = 0;
        size = 0;
    }

    // Public constructor function; takes an array of values to store
    // calls private constructor function
    void construct(ll val[]) {
        root[0] = construct(val, 0, n-1);
    }

    // Public update function; takes an index to update and the new value
    // create a new version of the tree and call the private update function
    void update(const int idx, const ll val) {
        this->version = version + 1;
        root[version] = update(0, n-1, root[version-1], idx, val);
    }

    // Public query function; takes a version, left index, and right index
    // calls the private query function to get the sum of the values in the range
    ll query(int queryVersion, const int l, const int r) {
        return query(root[queryVersion], 0, n-1, l, r);
    }

    // helper function to get a pointer to the root node of a specific version
    // can be used to look at the state of the tree at that version
    static Node *getVersion(const int version) {
        return root[version];
    }
};

int main(int argc, char* argv[]) {
    if (argc != 3) {
        cerr << "Usage: " << argv[0] << " <input.in> <expected.out>\n";
        return 1;
    }

    ifstream fin(argv[1]);
    ifstream fout(argv[2]);
    if (!fin.is_open() || !fout.is_open()) {
        cerr << "Error: could not open input or expected file\n";
        return 1;
    }

    int n, m;
    fin >> n >> m;
    vector<ll> arr(n);
    for (int i = 0; i < n; i++) {
        fin >> arr[i];
    }

    PST pst(n);
    pst.construct(arr.data());

    vector<ll> results;
    for (int i = 0; i < m; i++) {
        string cmd;
        fin >> cmd;
        if (cmd == "update") {
            int idx; ll x;
            fin >> idx >> x;
            pst.update(idx, x);
        } else if (cmd == "query") {
            int ver, l, r;
            fin >> ver >> l >> r;
            results.push_back(pst.query(ver, l, r));
        }
    }

    vector<ll> expected;
    ll v;
    while (fout >> v) {
        expected.push_back(v);
    }

    bool all_pass = (results.size() == expected.size());
    if (!all_pass) {
        cerr << "Mismatch in number of results: got "
             << results.size() << ", expected "
             << expected.size() << "\n";
    }

    size_t tests = min(results.size(), expected.size());
    for (size_t i = 0; i < tests; i++) {
        bool match = (results[i] == expected[i]);
        cout << "Test " << i << ":\n"
             << "  Actual:   " << results[i] << "\n"
             << "  Expected: " << expected[i] << "\n"
             << "  Match:    " << (match ? "YES" : "NO") << "\n\n";
        if (!match) all_pass = false;
    }

    if (all_pass) {
        cout << "All tests passed!\n";
        return 0;
    } else {
        cout << "Some tests failed.\n";
        return 1;
    }
}
