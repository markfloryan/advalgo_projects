#include <vector>
#include <random>
#include <utility>   // std::pair
#include <algorithm> // std::move
#include <iostream>
#include <fstream>
#include <sstream>
#include <string>

class Treap {

private:
    // Node stores key, priority and pointers to left and right children
    struct Node {
        int key;
        double priority;
        Node* left = nullptr;
        Node* right = nullptr;
        Node(int k, double p) : key(k), priority(p) {}
    };

    // Used to hold the result of a split operation
    struct SplitNodes { Node* left; Node* right; };

    Node* root = nullptr;  // Root node of the treap
    std::mt19937_64 rng{ std::random_device{}() };  // Random number generator
    std::uniform_real_distribution<double> dist{0.0, 1.0}; // Uniform distribution for priorities

    // Recursively delete all nodes in a subtree
    static void clear(Node* n) {
        if (!n) return;
        clear(n->left);
        clear(n->right);
        delete n;
    }


    // Performs a right rotation to maintain heap property
    // Right rotate to fix heap violation
    //      y              x
    //      / \            / \
    //     x   a   =>     c   y
    //    / \                / \
    //   c   b              b   a
    static Node* rotateRight(Node* y) {
        Node* x = y->left;
        y->left = x->right;
        x->right = y;
        return x;
    }

    // Performs a left rotation to maintain heap property
    //     x                  y
    //    / \                / \
    //   c   y    =>       x   a
    //      / \           / \
    //     b   a         c   b
    static Node* rotateLeft(Node* x) {
        Node* y = x->right;
        x->right = y->left;
        y->left = x;
        return y;
    }


    // Insert key with optional priority into treap rooted at n
    Node* insert(Node* n, int key, double* priPtr) {
        if (!n) {
            double p = (priPtr) ? *priPtr : dist(rng); // Use given or random priority
            return new Node(key, p);
        }
        if (key < n->key) {
            n->left = insert(n->left, key, priPtr);  // Recurse left
            if (n->left->priority < n->priority)     // Fix heap if needed
                n = rotateRight(n);
        } else if (key > n->key) {
            n->right = insert(n->right, key, priPtr); // Recurse right
            if (n->right->priority < n->priority)
                n = rotateLeft(n);
        }
        // Duplicate keys are ignored
        return n;
    }


    // Delete key from treap rooted at n
    Node* erase(Node* n, int key) {
        if (!n) return nullptr;
        if (key < n->key) {
            n->left = erase(n->left, key);  // Recurse left
        } else if (key > n->key) {
            n->right = erase(n->right, key); // Recurse right
        } else {
            // Found node to delete
            // to safely remove the node, need to rotate it down until it becomes a leaf
            if (!n->left || !n->right) {
                Node* child = (n->left) ? n->left : n->right;
                delete n;
                return child;
            }
            // Two children: rotate the one with smaller priority up
            if (n->left->priority < n->right->priority) {
                n = rotateRight(n);
                n->right = erase(n->right, key);
            } else {
                n = rotateLeft(n);
                n->left = erase(n->left, key);
            }
        }
        return n;
    }


    // Check if key exists in treap rooted at n
    static bool search(Node* n, int key) {
        while (n) {
            if (key < n->key) n = n->left;
            else if (key > n->key) n = n->right;
            else return true;
        }
        return false;
    }


    // Split treap rooted at n into two treaps based on key
    static SplitNodes split(Node* n, int key) {
        if (!n) return {nullptr, nullptr};
        if (key <= n->key) {
            //recurse to the left subtree to find the correct split point
            auto sub = split(n->left, key);
            n->left = sub.right;  //Attach leftover right split to node
            return { sub.left, n };
        } else {
            auto sub = split(n->right, key);
            n->right = sub.left;  //Attach leftover left split to node
            return { n, sub.right };
        }
    }

// Merge two treaps assuming all keys in 'left' are strictly less than all keys in 'right'.
// This ensures the result maintains both BST and heap properties.
static Node* mergeNodes(Node* left, Node* right) {
    // Base case: if either subtree is null, return the non-null one
    if (!left || !right) return left ? left : right;
    if (left->priority < right->priority) {
        // 'left' has higher priority (smaller value), so it becomes the root
        // Merge 'right' into the right subtree of 'left'
        left->right = mergeNodes(left->right, right);
        return left;
    } else {
        // 'right' has higher priority, so it becomes the root
        // Merge 'left' into the left subtree of 'right'
        right->left = mergeNodes(left, right->left);
        return right;
    }
}
public:
    // ========== CTOR/DTOR & MOVE ONLY ==========
    Treap() = default;
    ~Treap() { clear(root); }  // Recursively delete all nodes

    Treap(const Treap&) = delete;
    Treap& operator=(const Treap&) = delete;  // Prevent copy

    Treap(Treap&& other) noexcept : root(other.root), rng(std::move(other.rng)) {
        other.root = nullptr;
    }
    Treap& operator=(Treap&& other) noexcept {
        if (this != &other) {
            clear(root);
            root = other.root;
            other.root = nullptr;
        }
        return *this;
    }

    // ===================== PUBLIC API =====================
    // Insert key with random priority
    void insert(int key)                    { root = insert(root, key, nullptr); }
    // Insert key with given priority
    void insert(int key, double priority)   { root = insert(root, key, &priority); }

    // Erase key from treap
    void erase(int key)                     { root = erase(root, key); }
    // Check if key exists in treap
    bool contains(int key) const            { return search(root, key); }

    // Split treap into two based on key threshold
    std::pair<Treap, Treap> split(int key) {
        auto parts = split(root, key);
        Treap left, right;
        left.root = parts.left;
        right.root = parts.right;
        root = nullptr;  // Invalidate this treap
        return { std::move(left), std::move(right) };
    }

    // Merge two treaps into one
    static Treap merge(Treap&& left, Treap&& right) {
        Treap t;
        t.root = mergeNodes(left.root, right.root);
        left.root = nullptr;
        right.root = nullptr;
        return t;
    }

    // Return sorted list of keys using inorder traversal
    std::vector<int> inorder() const {
        std::vector<int> out;
        out.reserve(128);
        inorderRec(root, out);
        return out;
    }


private:
    // Helper for recursive inorder traversal
    static void inorderRec(Node* n, std::vector<int>& out) {
        if (!n) return;
        inorderRec(n->left, out);
        out.push_back(n->key);
        inorderRec(n->right, out);
    }
};



void print_vector(const std::string& label, const std::vector<int>& vec) {
    std::cout << label << ": [";
    for (size_t i = 0; i < vec.size(); ++i) {
        std::cout << vec[i];
        if (i + 1 < vec.size()) std::cout << ", ";
    }
    std::cout << "]\n";
}

// Read and execute treap operations from input stream
Treap run_treap_ops(std::istream& input, int n) {
    Treap t;
    std::string line;

    for (int i = 0; i < n; ++i) {
        std::getline(input, line);
        std::istringstream iss(line);
        std::string cmd;
        int val;
        iss >> cmd >> val;

        if (cmd == "Insert") {
            t.insert(val);
        } else if (cmd == "Delete") {
            t.erase(val);
        } else if (cmd == "Search") {
            std::cout << "Search " << val << ": " << (t.contains(val) ? "Found" : "Not Found") << "\n";
        } else if (cmd == "Inorder") {
            auto res = t.inorder();
            print_vector("Inorder", res);
        }
    }

    return t;
}

int main(int argc, char* argv[]) {
    if (argc < 2) {
        std::cerr << "Usage: ./treap <input_file>\n";
        return 1;
    }

    std::ifstream infile(argv[1]);
    if (!infile) {
        std::cerr << "Error: Could not open file.\n";
        return 1;
    }

    std::string mode;
    std::getline(infile, mode);

    std::string args;
    std::getline(infile, args);
    std::istringstream arg_stream(args);

    if (mode == "Merge") {
        int n1, n2;
        arg_stream >> n1 >> n2;
        Treap t1 = run_treap_ops(infile, n1);
        Treap t2 = run_treap_ops(infile, n2);
        Treap merged = Treap::merge(std::move(t1), std::move(t2));
        print_vector("Merged inorder", merged.inorder());

    } else if (mode == "Split") {
        int n;
        arg_stream >> n;
        Treap t = run_treap_ops(infile, n);
        std::string split_key_line;
        std::getline(infile, split_key_line);
        int split_key = std::stoi(split_key_line);
        auto [left, right] = t.split(split_key);
        print_vector("Left treap inorder", left.inorder());
        print_vector("Right treap inorder", right.inorder());

    } else if (mode == "Basic") {
        int n;
        arg_stream >> n;
        run_treap_ops(infile, n);
    } else {
        std::cerr << "Unknown mode: " << mode << "\n";
        return 1;
    }

    return 0;
}
