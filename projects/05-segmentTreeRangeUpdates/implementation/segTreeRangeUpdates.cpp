#include <iostream>
#include <vector>

using namespace std;


class Node {
    public:
        int l, r;
        int val;
        bool marked;  // for range assignments
        Node* l_child;
        Node* r_child;

        Node(int left, int right) {
            l = left;
            r = right;
        }
};


class SegTreeStandard {
    public:
        SegTreeStandard(vector<int>& a) {
            Node* root = new Node(0, a.size() - 1);
            build(a, root);
        }

        // standard sum query
        int query(Node* cur_node, int l, int r) {
            if (l > r) {
                return 0;
            }
            if (l == cur_node->l && r == cur_node->r) {
                return cur_node->val;
            }

            return query(cur_node->l_child, l, min(r, cur_node->l_child->r))
                    + query(cur_node->r_child, max(l, cur_node->r_child->l), r);
        }

        // standard single value update
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

    private:
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
};


class SegTreeAdditionAndGet {
    public:
        SegTreeAdditionAndGet(vector<int>& a) {
            Node* root = new Node(0, a.size() - 1);
            build(a, root);
        }

        // get simply returns `a[pos]`
        int get(Node* cur_node, int pos) {
            // base case: if we're at a leaf node, just return it's value
            if (cur_node->l == cur_node->r) {
                return cur_node->val;
            }
            /*
            else, find out which child `pos` lies in, and query on that
            child -- BUT make sure to add the value from our current node
            to the answer. in this way, we keep a running tally of the 
            additions as we go down.
            */
            if (pos <= cur_node->l_child->r) {
                // add cur_node.val to the tally, query child
                return cur_node->val + get(cur_node->l_child, pos);
            } else {
                return cur_node->val + get(cur_node->r_child, pos);
            }
        }


        // update adds `add` to all numbers in the segment `a[l...r]`
        void update(Node* cur_node, int l, int r, int add) {
            // Degenerate base case
            if (l > r) {
                return;
            }
            /*
            Base case: If the update range perfectly matches the current 
            node's range, add to the node's value
            */
            if (l == cur_node->l && r == cur_node->r) {
                cur_node->val += add;
                return;
            }
            /*
            If the range doesn't match the node's range exactly, we have the 
            same cases to consider as in the normal seg tree range query:

            1. The update range falls completely within the left child,
            2. The update range falls completely within the right child,
            3. The range straddles both

            We handle this the same way we do in a normal segtree sum query,
            by taking the min/max of (r, mid)/(l, mid+1), respectively, to
            determine the range for the two update calls on our children. 
            Notice that there's no merge operation to consider, so we only have 
            to make the two calls, nothing else.
            */
           update(cur_node->l_child, l, min(r, cur_node->l_child->r), add);
           update(cur_node->r_child, max(l, cur_node->r_child->l), r, add);
        }

    private:
        void build(vector<int>& a, Node* cur_node) {
            if (cur_node->l == cur_node->r) {
                /*
                for leaf nodes, we still just store its corresponding 
                array value
                */
                cur_node->val = a[cur_node->l];
                return;
            }

            int mid = (cur_node->l + cur_node->r) / 2;
            cur_node->l_child = new Node(cur_node->l, mid);
            cur_node->r_child = new Node(mid + 1, cur_node->r);

            build(a, cur_node->l_child);
            build(a, cur_node->r_child);

            /*
            for non-leaf nodes, there is no value to add to the segment
            when we're first initializing the tree since we haven't 
            made any queries yet, so we just set to 0 for now.
            */
            cur_node->val = 0;
        }
};






class SegTreeAssignAndGet {
    public:
        SegTreeAssignAndGet(vector<int>& a) {
            Node* root = new Node(0, a.size() - 1);
            build(a, root);
        }

        // get simply returns `a[pos]`
        int get(Node* cur_node, int pos) {
            // base case: we're at a leaf node
            if (cur_node->l == cur_node->r) {
                return cur_node->val;
            }
            // push our info down to our children if necessary
            push(cur_node);
            // then simply query on the correct child
            if (pos <= cur_node->l_child->r) {
                return get(cur_node->l_child, pos);
            } else {
                return get(cur_node->r_child, pos);
            }
        }

        // 
        void update(Node* cur_node, int l, int r, int new_val) {
            // degenerate base case
            if (l > r) {
                return;
            }
            /*
            if the query range matches the node range exactly,
            set the node's value and mark it
            */
            if (l == cur_node->l && r == cur_node->r) {
                cur_node->val = new_val;
                cur_node->marked = true;
                return;
            }
            /*
            first, we need to push our info down to our children if necessary.
            this is our "lazy update" -- we only do this when we need to go
            deeper into the tree
            */
            push(cur_node);
            // now just call update on our children, as per usual
            update(cur_node->l_child, l, min(r, cur_node->l_child->r), new_val);
            update(cur_node->r_child, max(l, cur_node->r_child->l), r, new_val);
        }

    private:
        void push(Node* node){
            if (node->marked) {
                // push our value down to our children
                node->l_child->val = node->r_child->val = node->val;
                // mark the children to indicate they now have contiguous range values
                node->l_child->marked = node->r_child->marked = true;
                // unmark ourselves to indicate we don't have a contiguous range value anymore
                node->marked = false;
            }
        }

        void build(vector<int>& a, Node* cur_node) {
            if (cur_node->l == cur_node->r) {
                // we're still just storing the array value in the leaf nodes
                cur_node->val = a[cur_node->l];
                return;
            }

            int mid = (cur_node->l + cur_node->r) / 2;
            cur_node->l_child = new Node(cur_node->l, mid);
            cur_node->r_child = new Node(mid + 1, cur_node->r);

            build(a, cur_node->l_child);
            build(a, cur_node->r_child);

            /*
            now the node value store whatever the contiguous assignment
            value is on that segment, if it has one. This will be set 
            IF AND ONLY IF the node is marked. So, to start, we just 
            set it to -1 (nil)
            */
            cur_node->val = -1;
        }
};