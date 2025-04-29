#include <iostream>
#include <vector>
#include <string>
#include <format>
#include <fstream>
#include <cassert>

using namespace std;


class Node {
    public:
        int l, r;
        int val;
        Node* l_child;
        Node* r_child;

        /*
        For SegTreeAssignAndGet:

        `marked` indicates whether or not every array value within 
        this node's range is set to a common value.
        */
        bool marked;  

        /*
        For SegTreeAdditionAndMax:
        
        `lazy` stores the running sum of all of the addition queries that have 
        been applied to the full range of the node but that have not been 
        propogated down to its children yet
        */
        int lazy;

        Node(int left, int right) {
            l = left;
            r = right;
        }
};


// we provide a normal segtree implementation to compare against the modified versions
class SegTreeStandard {
    public:
        Node* root;

        SegTreeStandard(vector<int>& a) {
            root = new Node(0, a.size() - 1);
            build(a, root);
        }

        int sum(Node* cur_node, int l, int r) {
            if (l > r) {
                return 0;
            }
            if (l == cur_node->l && r == cur_node->r) {
                return cur_node->val;
            }

            return sum(cur_node->l_child, l, min(r, cur_node->l_child->r))
                    + sum(cur_node->r_child, max(l, cur_node->r_child->l), r);
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

            cur_node->val = cur_node->l_child->val + cur_node->r_child->val;
        }

    private:
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

            cur_node->val = cur_node->l_child->val + cur_node->r_child->val;
        }
};


class SegTreeAdditionAndGet {
    public:
        Node* root;

        SegTreeAdditionAndGet(vector<int>& a) {
            root = new Node(0, a.size() - 1);
            build(a, root);
        }

        // get simply returns `a[pos]`
        int get(Node* cur_node, int pos) {
            // if we're at a leaf node, just return it's value
            if (cur_node->l == cur_node->r) {
                return cur_node->val;
            }
            /*
            else, find out which child `pos` lies in, and query on that
            child -- BUT make sure to add the value from our current node
            to the answer. in this way, we keep a running tally of the 
            additions as we go down to get the final value
            */
            if (pos <= cur_node->l_child->r) {
                // add cur_node.val to the tally, then query the child
                return cur_node->val + get(cur_node->l_child, pos);
            } else {
                return cur_node->val + get(cur_node->r_child, pos);
            }
        }


        // update adds `add` to all numbers in the segment `a[l...r]`
        void update(Node* cur_node, int l, int r, int add) {
            // usual segtree base cases
            if (l > r) {
                return;
            }
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
        Node* root;

        SegTreeAssignAndGet(vector<int>& a) {
            root = new Node(0, a.size() - 1);
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
            now the node value stores whatever the contiguous assignment
            value is on that segment, if it has one. This will be set 
            IF AND ONLY IF the node is marked. So, to start, we just 
            set it to -1 (nil)
            */
            cur_node->val = -1;
        }
};


class SegTreeAdditionAndMax {
    public:
        Node* root;

        SegTreeAdditionAndMax(vector<int>& a) {
            root = new Node(0, a.size() - 1);
            build(a, root);
        }

        // returns the maximal value in `a[l...r]`
        int query(Node* cur_node, int l, int r) {
            // normal seg tree base cases
            if (l > r) {
                return -42069;
            }
            if (l == cur_node->l && r == cur_node->r) {
                return cur_node->val;
            }
            // we have to make calls to our children, so we need to push our info down
            push(cur_node);
            // now, query our children and take the max
            return max(query(cur_node->l_child, l, min(r, cur_node->l_child->r)), 
                        query(cur_node->r_child, max(l, cur_node->r_child->l), r));
        }

        // adds `add` to all numbers in the segment `a[l...r]`
        void update(Node* cur_node, int l, int r, int add) {
            // usual base case conditions
            if (l > r) {
                return;
            }
            if (l == cur_node->l && r == cur_node->r) {
                // update our value, like normal
                cur_node->val += add;
                // but! we also need to add to lazy to store for later propogation
                cur_node->lazy += add;
                return;
            }
            // since we're calling our children, we need to push down info if we haven't already
            push(cur_node);
            // update the children, as usual
            update(cur_node->l_child, l, min(r, cur_node->l_child->r), add);
            update(cur_node->r_child, max(l, cur_node->r_child->l), r, add);
            // to update our own value, just take the max of the children value
            cur_node->val = max(cur_node->l_child->val, cur_node->r_child->val);
        }

    private:
        void push(Node* node){
            // push down our lazy value to our children's values
            node->l_child->val += node->lazy;
            node->r_child->val += node->lazy;
            // AND push it down to the children's lazy values as well!
            node->l_child->lazy += node->lazy;
            node->r_child->lazy += node->lazy;
            // now just reset our own lazy value
            node->lazy = 0;
        }

        void build(vector<int>& a, Node* cur_node) {
            if (cur_node->l == cur_node->r) {
                // the maximum in a leaf node is just the array value
                cur_node->val = a[cur_node->l];
                return;
            }

            int mid = (cur_node->l + cur_node->r) / 2;
            cur_node->l_child = new Node(cur_node->l, mid);
            cur_node->r_child = new Node(mid + 1, cur_node->r);

            build(a, cur_node->l_child);
            build(a, cur_node->r_child);

            // the maximum of this node's range is just the max of the children's maximums
            cur_node->val = max(cur_node->l_child->val, cur_node->r_child->val);
        }
};


// ------------- FOR TESTING -------------

/*
to test the implementations, cd into the implementation directory and run the following commands:

g++ -std=c++11 segTreeRangeUpdates.cpp -o testcpp
./testcpp

there are three test cases that test each of the above modified segment trees, respectively.
*/

int main() {
    // perform all 3 test cases
    for (int test_num = 1; test_num <= 3; test_num++) {
        cout << "TESTING VARIANT " << test_num << ":" << endl;
        string test_fp = "io/sample.in." + to_string(test_num);
        string test_expected_fp = "io/sample.out." + to_string(test_num);

        // cout << "got here 1";

        // open file streams
        ifstream read(test_fp);
        ifstream expected_out(test_expected_fp);

        // cout << "got here 2";

        // read config vals
        int V, N, Q;
        read >> V >> N >> Q;

        // read in input array
        vector<int> a(N);
        for (int i = 0; i < N; i++) {
            read >> a[i];
        }

        // initialize trees
        SegTreeAdditionAndGet segTree1(a);
        SegTreeAssignAndGet segTree2(a);
        SegTreeAdditionAndMax segTree3(a);

        // cout << "got here 3";

        // read in queries
        for (int q = 0; q < Q; q++) {
            // get the query type
            string query_type;
            read >> query_type;

            if (query_type == "GET") {
                int pos;
                read >> pos;

                // perform query
                int res;
                if (test_num == 1) {
                    res = segTree1.get(segTree1.root, pos);
                } else if (test_num == 2) {
                    res = segTree2.get(segTree2.root, pos);
                }

                // get the expected val
                int expected;
                expected_out >> expected;

                // compare
                cout << "Got value " << res << " (Expected: " << expected << ")" << endl;
                assert(res == expected);
            } else if (query_type == "UPDATE") {
                int L, R, X;
                read >> L >> R >> X;

                cout << "Updating range " << L << "-" << R << " with value " << X << endl;

                // perform update queries
                if (test_num == 1) {
                    segTree1.update(segTree1.root, L, R, X);
                } else if (test_num == 2) {
                    segTree2.update(segTree2.root, L, R, X);
                } else if (test_num == 3) {
                    segTree3.update(segTree3.root, L, R, X);
                }
            } else if (query_type == "MAX") {
                // we're guaranteed to be testing variant 3
                int L, R;
                read >> L >> R;

                int res = segTree3.query(segTree3.root, L, R);
                int expected;
                expected_out >> expected;

                // compare
                cout << "Got max value " << res << " (Expected: " << expected << ")" << endl;
                assert(res == expected);
            }
        }

        cout << endl;

        // close file streams
        read.close();
        expected_out.close();
    }

    cout << "ALL TESTS PASSED" << endl;

    return 0;
}