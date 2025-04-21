import math

# lazily add a number to all numbers in a range, this is done by storing a value at the vertex to be added to all children
class segment_tree_addition_and_get:
    def __init__(self, input_array):
        self.tree = [0]*(len(input_array)*4)
        # recursively build the segment tree - start by calling build on the root vertex (1) and using the root segment boundaries (just the whole array)
        self.build(input_array, 1, 0, len(input_array)-1)
    
    def build(self, input_array, curr_vertex, segment_left, segment_right):
        # reached leaf node so insert value
        if segment_left == segment_right:
            self.tree[curr_vertex] = input_array[segment_left]
        else:
            # work towards leaf nodes
            segment_middle = (segment_left + segment_right) // 2
            self.build(input_array, curr_vertex*2, segment_left, segment_middle)
            self.build(input_array, curr_vertex*2+1, segment_middle + 1, segment_right)
            # no value is stored here due to lazy propagation - this value will be added to any query that is below it
            self.tree[curr_vertex] = 0
    
    # add a number to a range, done by adding this to the highest query segment(s) that are fully encompassed
    # when a query is made, these values will be added to the sum
    def update(self, curr_vertex, segment_left, segment_right, left, right, num_to_add):
        if left > right:
            return
        # this segment is fully encompased by the query range (store the addition here)
        if left == segment_left and right == segment_right:
            self.tree[curr_vertex] += num_to_add
        # continue down the tree, some of this segment is outside the update range
        else:
            segment_middle = (segment_left + segment_right) // 2
            self.update(curr_vertex*2, segment_left, segment_middle, left, min(right, segment_middle), num_to_add)
            self.update(curr_vertex*2+1, segment_middle + 1, segment_right, max(left, segment_middle+1), right, num_to_add)

    def get(self, curr_vertex, segment_left, segment_right, position):
        # reached a leaf node
        if segment_left == segment_right:
            return self.tree[curr_vertex]
        segment_middle = (segment_left + segment_right) // 2
        # go down the tree, adding the recorded changes as you go down
        if position <= segment_middle:
            return self.tree[curr_vertex] + self.get(curr_vertex * 2, segment_left, segment_middle, position)
        else:
            return self.tree[curr_vertex] + self.get(curr_vertex * 2 + 1, segment_middle + 1, segment_right, position)


# a modified segment tree, where the operation is greatest common denominator instead of addition, and update adds a value to a range
class segment_tree_gcd:
    def __init__(self, input_array):
        self.tree = [0]*(len(input_array)*4)
        self.build(input_array, 1, 0, len(input_array)-1)
        # store the length of the original array for use in update
        self.length = len(input_array)
    
    def build(self, input_array, curr_vertex, segment_left, segment_right):
        # leaf
        if segment_left == segment_right:
            self.tree[curr_vertex] = input_array[segment_left]
        else:
            segment_middle = (segment_left + segment_right) // 2
            self.build(input_array, curr_vertex*2, segment_left, segment_middle)
            self.build(input_array, curr_vertex*2+1, segment_middle + 1, segment_right)
            # store the greatest common denominator of this segment in the vertex
            self.tree[curr_vertex] = math.gcd(self.tree[curr_vertex*2], self.tree[curr_vertex*2+1])
       
    def update(self, curr_vertex, segment_left, segment_right, position, num_to_add):
        if position < 0 or position >= self.length:
            return
        # leaf node, add here
        if segment_left == segment_right:
            self.tree[curr_vertex] += num_to_add
            return
        
        # continue to traverse down the tree
        segment_middle = (segment_left + segment_right) // 2
        if position <= segment_middle:
            self.update(curr_vertex*2, segment_left, segment_middle, position, num_to_add)
        else:
            self.update(curr_vertex*2+1, segment_middle+1, segment_right, position, num_to_add)
        # store the gcd of the gcds of the vertices below, which is the gcd of the whole segment
        self.tree[curr_vertex] = math.gcd(self.tree[curr_vertex*2], self.tree[curr_vertex*2+1])


    def query(self, curr_vertex, segment_left, segment_right, left, right):
        if left > right:
            return 0
        # this segment fits entirely within the constraints, so return the gcd stored here
        if left == segment_left and right == segment_right:
            return self.tree[curr_vertex]
        segment_middle = (segment_left + segment_right) // 2
        # continue to traverse until reaching a segment that is entirely encompassed by the query range
        return math.gcd(self.query(curr_vertex*2, segment_left, segment_middle, left, min(right, segment_middle)),
                self.query(curr_vertex*2+1, segment_middle+1,segment_right, max(left, segment_middle+1), right))


if __name__ == "__main__":
    test_cases = 20

    # go through all test cases
    for test_case in range(1, test_cases+1):
        print(f"test case {test_case}")
        test_in_fp = f"../io/test.in.{test_case}"
        test_out_fp = f"../io/test.out.{test_case}"

        # open each test case
        with open(test_in_fp, 'r') as test_in, open(test_out_fp, 'r') as test_out:
            N_Q_line = test_in.readline()
            N, Q = map(int, N_Q_line.strip().split())

            # read in input array
            a = list(map(int, test_in.readline().strip().split()))
            diff_a = [0]*(N-1)
            for i in range(1,N):
                diff_a[i-1] = a[i] - a[i-1]

            # the problem with using one segment tree is that naively updating the values of the GCD segment tree is log-linear time 
            # we take advantage of the property that GCD(a, b) == GCD(a, a-b) to store the GCDs of the differences
            # this is advantageous because to update the GCDs of a range, we only need to update the boundaries, as the rest of the differences are unchanged
            # also, we already have a segment tree that can do range updates in logarithmic time if it's just addition
            # so: one segment tree handles a and the other one handles a-b in GCD(a, a-b), which is equivalent to GCD(a, b) 
            add_segtree = segment_tree_addition_and_get(a)
            gcd_segtree = segment_tree_gcd(diff_a)

            for _ in range(Q):
                query = test_in.readline().strip()

                if query.startswith("GCD"):
                    _, l, r = query.split()
                    l, r = int(l), int(r)
                    # GCD(a, a-b) is the same as GCD(a, b)
                    res = math.gcd(add_segtree.get(1, 0, len(a)-1, l), gcd_segtree.query(1, 0, len(diff_a)-1, l, r-1))
                    print(res)

                    # ---- FOR TESTING ----
                    # expected = int(test_out.readline())
                    # assert res == expected
                    # ---------------------

                elif query.startswith("ADD"):
                    _, l, r, x = query.split()p
                    l, r, x = int(l), int(r), int(x)

                    # range update
                    add_segtree.update(1, 0, len(a)-1, l, r, x)
                    # only boundary points change in difference array
                    gcd_segtree.update(1, 0, len(diff_a)-1, l - 1, x)
                    gcd_segtree.update(1, 0, len(diff_a)-1, r, -x)
             
