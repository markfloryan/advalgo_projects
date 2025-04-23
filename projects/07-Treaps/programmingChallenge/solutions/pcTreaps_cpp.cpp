#include <iostream>
#include <random>
#include <stack>

using namespace std;

// Each node represents a banana: (position, ready time)
struct Node {
    int p, t; // p = position, t = time when banana becomes eatable
    int priority; // randomized heap priority
    Node *left, *right;
    Node(int _p, int _t) : p(_p), t(_t), left(nullptr), right(nullptr) {
        priority = rand();
    }
};

// Split treap into (< key) and (≥ key)
void split(Node* node, int key, Node*& left, Node*& right) {
    if (!node) return void(left = right = nullptr);
    if (key < node->p) {
        split(node->left, key, left, node->left);
        right = node;
    } else {
        split(node->right, key, node->right, right);
        left = node;
    }
}

// Merge two treaps assuming all keys in a < keys in b
Node* merge(Node* a, Node* b) {
    if (!a || !b) return a ? a : b;
    if (a->priority > b->priority) {
        a->right = merge(a->right, b);
        return a;
    } else {
        b->left = merge(a, b->left);
        return b;
    }
}

// Insert node into treap
Node* insert(Node* root, Node* n) {
    if (!root) return n;
    if (n->priority > root->priority) {
        split(root, n->p, n->left, n->right);
        return n;
    }
    if (n->p < root->p)
        root->left = insert(root->left, n);
    else
        root->right = insert(root->right, n);
    return root;
}

// Remove node with given key from treap
Node* remove(Node* root, int key) {
    if (!root) return nullptr;
    if (root->p == key) return merge(root->left, root->right);
    if (key < root->p) root->left = remove(root->left, key);
    else root->right = remove(root->right, key);
    return root;
}

// Simulate the monkey moving from position 0, eating as many bananas as possible within T seconds
int simulate(Node* root, int T) {
    // iterative in-order traversal
    stack<Node*> stk;
    Node* cur = root;
    int pos = 0, time = 0, cnt = 0;
    while (cur || !stk.empty()) {
        while (cur) {
            stk.push(cur);
            cur = cur->left;
        }
        cur = stk.top(); stk.pop();
        // Move from current position to next banana
        int move = cur->p - pos;
        time += move;
        pos = cur->p;
        if (time > T) break;
        // Can eat immediately
        if (time >= cur->t) cnt++;
        else if (cur->t <= T) { // Banana not ready yet, but we can wait
            time = cur->t;
            cnt++;
        } else { // Banana won't be ready in time
            cur = cur->right;
            continue;
        };
        cur = cur->right;
    }
    return cnt;
}

int main() {
    ios::sync_with_stdio(false);
    cin.tie(nullptr);

    int q;
    cin >> q;
    Node* root = nullptr;

    while (q--) {
        string cmd;
        cin >> cmd;
        if (cmd == "ADD") {
            int p, t;
            cin >> p >> t;
            root = insert(root, new Node(p, t));
        } else if (cmd == "REMOVE") {
            int p;
            cin >> p;
            root = remove(root, p);
        } else if (cmd == "QUERY") {
            int T;
            cin >> T;
            cout << simulate(root, T) << '\n';
        }
    }
}
