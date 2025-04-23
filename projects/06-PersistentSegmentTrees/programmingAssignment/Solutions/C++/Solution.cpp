#include <iostream>
#include <vector>

using namespace std;
using ll = long long;

constexpr int MAX = 100001;


struct Node {
    ll value;
    ll maximum;
    Node *leftChild;
    Node *rightChild;
    Node() {
        leftChild = rightChild = nullptr;
        value = maximum = 0;
    }
    Node(Node *l, Node *r, ll v) {
        leftChild = l, rightChild = r;
        value = maximum = v;
    }
};

Node *root[MAX];

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
            newNode->maximum = (val==nullptr ? 0 : val[l]);
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
    Node* update(const int l, const int r, const Node *prevVerPtr, const int idx, const ll newVal, const ll newMax) {
        Node *newNode = makeNode();
        // base case: if l == r we've found the node to update in the previous version tree, now set the new version's node to the new value
        if (l == r) {
            newNode->value = newVal;
            newNode->maximum = newMax;
        } else {
            // otherwise recurse down the tree to find the node to update
            if (const int middle = (l + r) / 2; idx <= middle) {
                // if the index is in the left half, update the left child and set the right child to the previous version's right child
                newNode->leftChild = update(l, middle, prevVerPtr->leftChild, idx, newVal, newMax);
                newNode->rightChild = prevVerPtr->rightChild;
            } else {
                // if the index is in the right half, update the right child and set the left child to the previous version's left child
                newNode->rightChild = update(middle + 1, r, prevVerPtr->rightChild, idx, newVal, newMax);
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

    ll query_node_maximum(const Node* node, const int idx, const int l, const int r) {
        // indices are oob
        if (idx > r || idx < l) {
            return -1;
        }
        // if the current node is completely within the query range, return its value
        if (l == idx && r == idx) {
            return node->maximum;
        } else {
            const int middle = (l + r) / 2;
            const ll left = query_node_maximum(node->leftChild, idx, l, middle);
            const ll right = query_node_maximum(node->rightChild, idx, middle + 1, r);
            return max(left,right);
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

        ll old_max = query_node_maximum(idx);
        ll newMax = val > old_max ? val : old_max;
        this->version = version + 1;
        root[version] = update(0, n-1, root[version-1], idx, val, newMax);
    }

    // Public query function; takes a version, left index, and right index
    // calls the private query function to get the sum of the values in the range
    ll query(int queryVersion, const int l, const int r) {
        return query(root[queryVersion], 0, n-1, l, r);
    }

    ll query_node_maximum(int idx){
        return query_node_maximum(root[version], idx, 0, n-1);
    }

    void swap(const int idx_1, const int idx_2){
        ll idx_1_val, idx_2_val;
        idx_1_val = query(version,idx_1,idx_1);
        ll idx_1_oldMax = query_node_maximum(idx_1);
        idx_2_val = query(version, idx_2, idx_2);
        ll idx_2_oldMax = query_node_maximum(idx_2);
        this->version = version + 1;
        Node* Temp = update(0,n-1, root[version - 1], idx_1, idx_2_val, idx_2_val > idx_1_oldMax ? idx_2_val : idx_1_oldMax);
        root[version] = update(0,n-1, Temp, idx_2, idx_1_val, idx_1_val > idx_2_oldMax ? idx_1_val : idx_2_oldMax);
    }

    // helper function to get a pointer to the root node of a specific version
    // can be used to look at the state of the tree at that version
    static Node *getVersion(const int version) {
        return root[version];
    }

    void printTree(Node* node, int l, int r){
        if(l == r){
            cout << node->value << " " << node->maximum << endl;
        }
        else{
            int middle = (l + r) / 2;
            printTree(node->leftChild, l, middle);
            printTree(node->rightChild, middle + 1, r);
        }
    }

    void printTree(){
        cout << "VERSION: " << version << endl;
        printTree(root[version], 0, n-1);
    }
};




int main(){
    int n,c; cin >> n >> c;
    ll scores[n];
    for(int i = 0; i < n; i++){
        int score; cin >> score;
        scores[i] = score;
    }
    PST* Tree = new PST(n);
    Tree->construct(scores);
    for(int i = 0; i < c; i++){
        char command; cin >> command;
        if(command == 'S'){
            int x, y; cin >> x >> y;
            Tree->swap(x - 1,y - 1);
        }
        else if (command == 'R'){
            int idx; ll val; cin >> idx >> val;
            Tree->update(idx - 1, val);
        }
        else if(command == 'P'){
            int idx; cin >> idx;
            ll maximum = Tree->query_node_maximum(idx - 1);
            Tree->update(idx - 1,maximum);
        }
        else if(command == 'Q'){
            int l,r,v1,v2; cin >> l >> r >> v1 >> v2;
            ll v1_total = Tree->query(v1 - 1, l - 1, r - 1);
            ll v2_total = Tree->query(v2 - 1, l - 1, r - 1);
            cout << abs(v2_total - v1_total) << endl;
        }
        else{
            cout << "ERROR BAD INPUT PARSING" << endl;
            exit(1);
        }
    }
}