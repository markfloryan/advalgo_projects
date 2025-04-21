#include <iostream>
#include <fstream>
#include <vector>
#include <algorithm>
#include <cassert>

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

// no comments included on this class since it's just copy paste from implementation
class SegTreeAdditionAndGet {
    public:
        Node* root;

        SegTreeAdditionAndGet(vector<int>& a) {
            root = new Node(0, a.size() - 1);
            build(a, root);
        }

        int get(Node* cur_node, int pos) {
            if (cur_node->l == cur_node->r) {
                return cur_node->val;
            }
            if (pos <= cur_node->l_child->r) {
                return cur_node->val + get(cur_node->l_child, pos);
            } else {
                return cur_node->val + get(cur_node->r_child, pos);
            }
        }

        void update(Node* cur_node, int l, int r, int add) {
            if (l > r) {
                return;
            }
            if (l == cur_node->l && r == cur_node->r) {
                cur_node->val += add;
                return;
            }
           update(cur_node->l_child, l, min(r, cur_node->l_child->r), add);
           update(cur_node->r_child, max(l, cur_node->r_child->l), r, add);
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

            cur_node->val = 0;
        }
};

// gcd helper since the c++ returns -1 sometimes
int gcd_helper(int A,int B) {
    if (A == 0) return B;
    if (B == 0) return A;

    A = abs(A);
    B = abs(B);

    if (A>B) {
        return gcd_helper(B,A);
    } else {
        return gcd_helper(B % A,A);
    }
}

// a normal range query, single point update seg-tree that supports GCD queries
class SegTreeGCD {
    public:
        Node* root;
        // the length of the array we store
        int length;

        SegTreeGCD(vector<int>& a) {
            root = new Node(0, a.size() - 1);
            length = a.size();
            build(a, root);
        }

        // this is a standard segtree range query -- nothing special to say
        int query(Node* cur_node, int l, int r) {
            if (l > r) {
                return 0;
            }
            if (l == cur_node->l && r == cur_node->r) {
                return cur_node->val;
            }

            // take gcd to merge
            return gcd_helper(query(cur_node->l_child, l, min(r, cur_node->l_child->r)),
                    query(cur_node->r_child, max(l, cur_node->r_child->l), r));
        }

        void update(Node* cur_node, int pos, int add) {
            // if the query falls outside our array range, just ignore it 
            if (pos < 0 || pos >= length) {
                return;
            }
            // base case, query matches node range
            if (cur_node->l == cur_node->r) {
                // add the value to our current
                cur_node->val += add;
                return;
            }
            // update the correct child based on which index we're searching
            if (pos <= cur_node->l_child->r) {
                update(cur_node->l_child, pos, add);
            } else {
                update(cur_node->r_child, pos, add);
            }
            // update our own value
            cur_node->val = gcd_helper(cur_node->l_child->val, cur_node->r_child->val);
        }

    private:
        // again, standard
        void build(vector<int>& a, Node* cur_node) {
            if (cur_node->l == cur_node->r) {
                // the gcd of a single number is just that number
                cur_node->val = a[cur_node->l];
                return;
            }

            int mid = (cur_node->l + cur_node->r) / 2;
            cur_node->l_child = new Node(cur_node->l, mid);
            cur_node->r_child = new Node(mid + 1, cur_node->r);

            build(a, cur_node->l_child);
            build(a, cur_node->r_child);

            // take gcd to merge
            cur_node->val = gcd_helper(cur_node->l_child->val, cur_node->r_child->val);
        }
};



/*
NOTES: 

do we need to have the numbers sorted as input? can we have negative values?
*/


int main() {
    int test_cases = 20;

    for (int test_case = 1; test_case <= test_cases; test_case++) {
        cout << "test case " << test_case << endl;
        // open file streams
        string test_in_fp = "../io/test.in." + to_string(test_case);
        string test_out_fp = "../io/test.out." + to_string(test_case);
        ifstream test_in(test_in_fp);
        ifstream test_out(test_out_fp);

        // read in parameters
        int N, Q;
        test_in >> N >> Q;

        // read in the file (list of integers)
        vector<int> a(N);
        for (int i = 0; i < N; i++) {
            test_in >> a[i];
        }

        /*
        construct the "difference" vector of size N - 1:
        [a_2 - a_1, a_3 - a_2, ..., a_n - a_{n-1}]
        */
        vector<int> diff_a(N-1);
        for (int i = 1; i < N; i++) {
            diff_a[i-1] = a[i] - a[i-1];
        }

        // construct an add + get seg tree on the original array
        SegTreeAdditionAndGet add_segtree(a);
        // construct a gcd seg tree ON THE DIFFERENCE ARRAY. this will allow for gcd computations that are minimally affected by range updates
        SegTreeGCD gcd_segtree(diff_a);

        // perform queries
        for (int i = 0; i < Q; i++) {
            string query_type;
            test_in >> query_type;

            if (query_type == "GCD") {
                int l, r;
                test_in >> l >> r;

                /*
                We want to find gcd(a_l,a_{l+1},...,a_r). But, note that

                gcd(a_l,a_{l+1},...,a_r) = gcd(a_l, a_{l+1} - a_l, ..., a_r - a_{r-1})
                                        = gcd(a_l, gcd(a_{l+1} - a_l, ..., a_r - a_{r-1}))
                                        = gcd(add_segtree.get(l), gcd_segtree.query(l, r-1))

                so, we'll use both trees to find the answer
                */

                int res = gcd_helper(add_segtree.get(add_segtree.root, l), gcd_segtree.query(gcd_segtree.root, l, r - 1));
                // comment out if you dont want computer to explode (it is just  too many prints for the big test cases)
                cout << res << endl;

                // ---- FOR TESTING ----
                int expected;
                test_out >> expected;
                assert(res == expected); // test against expected
                // ---------------------
            } else if (query_type == "ADD") {
                int l, r, x;
                test_in >> l >> r >> x;

                /*
                we can use the add segtree to update the entire range, as usual.
                we also note that, to update the gcd segtree, the only values 
                that will change in the difference array will be on the edge of the 
                query range. That is, 

                a_l - a_{l-1} --> a_l + x - a_{l-1}
                a_{r+1} - a_r --> a_{r+1} - (a_r + x)

                but, for any l < i <= r, we have

                a_i - a_{i-1} --> (a_i + x) - (a_{i-1} + x) = a_i - a_{i-1},

                so that the difference remains unchanged after the update. 
                so, we only need to call update on the two outer indeces.
                */

                // normal range update
                add_segtree.update(add_segtree.root, l, r, x);
                // ADD x to the left boundary
                gcd_segtree.update(gcd_segtree.root, l-1, x);
                // SUBTRACT x from the right boundary
                gcd_segtree.update(gcd_segtree.root, r, -x);
            }
        }

        test_in.close();
        test_out.close();
    }   

    return 0;
}
