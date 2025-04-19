#include <iostream>
#include <vector>

using namespace std;


class Node {
    public:
        int l, r;
        int val;
        Node* l_child;
        Node* r_child;

        Node(int left, int right) {
            l = left;
            r = right;
        }
};


class SegTree {
    public:
        SegTree(vector<int>& a) {
            Node* root = new Node(0, a.size() - 1);
            build(a, root);
        }

        // ----------- RANGE UPDATES -----------

        void range_update() {

        }

        // ----------- STANDARD SEGTREE IMPLEMENTATION -----------

        int merge(Node* node1, Node* node2) {
            return node1->val + node2->val;
        }

        void build(vector<int>& a, Node* cur_node) {
            if (cur_node->l == cur_node->r) {
                cur_node->val = a[cur_node->l];
                return;
            }

            int mid = (cur_node->l + cur_node->r) / 2;
            cur_node->l_child = new Node(cur_node->l, mid);
            cur_node->r_child = new Node(mid + 1, cur_node->r);

            build(a, cur_node->l_child);
            build(a, cur_node->r_child);

            cur_node->val = merge(cur_node->l_child, cur_node->r_child);
        }

        int sum(Node* cur_node, int l, int r) {
            if (l > r) {
                return 0;
            }
            if (l == cur_node->l && r == cur_node->r) {
                return cur_node->val;
            }

            return sum(cur_node->l_child, l, min(r, cur_node->l_child->r))
                    + sum(cur_node->r_child, max(r, cur_node->r_child->l), r);
        }

        void update(Node* cur_node, int pos, int new_val) {
            if (cur_node->l == cur_node->r) {
                cur_node->val = new_val;
                return;
            }
            if (pos <= cur_node->l_child->r) {
                update(cur_node->l_child, pos, new_val);
            } else {
                update(cur_node->r_child, pos, new_val);
            }

            cur_node->val = merge(cur_node->l_child, cur_node->r_child);
        }
};