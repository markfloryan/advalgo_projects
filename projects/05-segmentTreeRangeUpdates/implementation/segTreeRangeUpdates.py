# a standard segment tree for addition for reference against modified versions
# this uses an array-based implementation of the segment tree instead of a node based one
class segment_tree:
    def __init__(self, input_array):
        self.tree = [0]*(len(input_array)*4)
        self.build(input_array, 1, 0, len(input_array)-1)
    
    def build(self, input_array, curr_vertex, segment_left, segment_right):
        if segment_left == segment_right:
            self.tree[curr_vertex] = input_array[segment_left]
        else:
            segment_middle = (segment_left + segment_right) // 2
            self.build(input_array, curr_vertex*2, segment_left, segment_middle)
            self.build(input_array, curr_vertex*2+1, segment_middle + 1, segment_right)
            self.tree[curr_vertex] = self.tree[curr_vertex*2] + self.tree[curr_vertex*2+1]
       
    def update(self, curr_vertex, segment_left, segment_right, position, new_value):
        if segment_left == segment_right:
            self.tree[curr_vertex] = new_value
        else:
            segment_middle = (segment_left + segment_right) // 2
            if position <= segment_middle:
                self.update(curr_vertex*2, segment_left, segment_middle, position, new_value)
            else:
                self.update(curr_vertex*2+1, segment_middle+1, segment_right, position, new_value)
            self.tree[curr_vertex] = self.tree[curr_vertex*2] + self.tree[curr_vertex*2+1] 

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

# lazily change the numbers in a range to another - changes are not written to leaf nodes until needed
# this is done by "marking" a vertex, indicating that every value underneath is equal to the one in the vertex
class segment_tree_assignment_and_get:
    def __init__(self, input_array):
        self.tree = [0]*(len(input_array)*4)
        # no vertices are marked
        self.marked = [False]*(len(input_array)*4)
        self.build(input_array, 1, 0, len(input_array)-1)
    
    def build(self, input_array, curr_vertex, segment_left, segment_right):
        if segment_left == segment_right:
            self.tree[curr_vertex] = input_array[segment_left]
        else:
            segment_middle = (segment_left + segment_right) // 2
            self.build(input_array, curr_vertex*2, segment_left, segment_middle)
            self.build(input_array, curr_vertex*2+1, segment_middle + 1, segment_right)
            self.tree[curr_vertex] = 0

    def update(self, curr_vertex, segment_left, segment_right, left, right, num_to_assign):
        if left > right:
            return
        # Stop as soon as you reach a segment that is entirely encompassed by the update boundaries
        # Set the value, and mark that all values below are now the same as this one
        if left == segment_left and right == segment_right:
            self.tree[curr_vertex] = num_to_assign
            self.marked[curr_vertex] = True
        # continue down the tree, some of this segment is outside the update range
        else:
            segment_middle = (segment_left + segment_right) // 2
            self.update(curr_vertex * 2, segment_left, segment_middle, left, min(right, segment_middle), num_to_assign)
            self.update(curr_vertex * 2 + 1, segment_middle + 1, segment_right, max(left, segment_middle+1), right, num_to_assign)

    # if a vertex is marked, unmark it and mark the two below it, assigning them the same value as the current vertex
    # push is used as get progresses down the tree
    def push(self, vertex):
        if self.marked[vertex]:
            self.tree[vertex*2] = self.tree[vertex*2+1] = self.tree[vertex]
            self.marked[vertex*2] = self.marked[vertex*2+1] = True
            self.marked[vertex] = False

    # go down the tree updating segments as necessary
    def get(self, curr_vertex, segment_left, segment_right, position):
        # leaf node
        if segment_left == segment_right:
            return self.tree[curr_vertex]
        # propagate the assignment (if there is one) to the two immediate children of the segment (and no more)
        self.push(curr_vertex)
        segment_middle = (segment_left + segment_right) // 2
        # continue going down the tree
        if position <= segment_middle:
            return self.get(curr_vertex * 2, segment_left, segment_middle, position)
        else:
            return self.get(curr_vertex * 2 + 1, segment_middle + 1, segment_right, position)

# we want to be able to add a value to a range, and query for the max in a range
# we simply store the max value at each vertex, but this value is recomputed during each update!
class segment_tree_addition_and_max:
    def __init__(self, input_array):
        self.tree = [0]*(len(input_array)*4)
        # we need to keep an additional value at each vertex - the value to be added that hasn't been propagated
        self.lazy = [0]*(len(input_array)*4)
        self.build(input_array, 1, 0, len(input_array)-1)
    
    # recursively build, as you return back up, store the max value
    def build(self, input_array, curr_vertex, segment_left, segment_right):
        if segment_left == segment_right:
            self.tree[curr_vertex] = input_array[segment_left]
        else:
            segment_middle = (segment_left + segment_right) // 2
            self.build(input_array, curr_vertex*2, segment_left, segment_middle)
            self.build(input_array, curr_vertex*2+1, segment_middle + 1, segment_right)
            # store max value in vertex
            self.tree[curr_vertex] = max(self.tree[curr_vertex*2], self.tree[curr_vertex*2+1])
    
    # the value to be added, kept in lazy needs to be pushed down before traversing to a child node
    def push(self, vertex):
        # value is added to the actual values of the children and to the recordkeeping
        self.tree[vertex*2] += self.lazy[vertex]
        self.lazy[vertex*2] += self.lazy[vertex]
        self.tree[vertex*2+1] += self.lazy[vertex]
        self.lazy[vertex*2+1] += self.lazy[vertex]
        # after propagating value, reset
        self.lazy[vertex] = 0

    def update(self, curr_vertex, segment_left, segment_right, left, right, num_to_add):
        if left > right:
            return
        # first segment that is fully encompassed by update range
        if left == segment_left and right == segment_right:
            # the max is increased by this amount
            self.tree[curr_vertex] += num_to_add
            # lazily store the value to be added to all values inside this segment
            self.lazy[curr_vertex] += num_to_add
        # as you traverse down the tree, changes need to propagated
        else:
            self.push(curr_vertex)
            segment_middle = (segment_left + segment_right) // 2
            self.update(curr_vertex*2, segment_left, segment_middle, left, min(right, segment_middle), num_to_add)
            self.update(curr_vertex*2+1, segment_middle + 1, segment_right, max(left, segment_middle+1), right, num_to_add)
             # store the new maximum as you return back up   
            self.tree[curr_vertex] = max(self.tree[curr_vertex*2], self.tree[curr_vertex*2+1])

    # update values as you traverse the tree
    def query(self, curr_vertex, segment_left, segment_right, left, right):
        if left > right:
            return float('-inf')
        # for segments fully encompassed by query range, return the max
        if left == segment_left and right == segment_right:
            return self.tree[curr_vertex]
        # propagate changes
        self.push(curr_vertex)
        segment_middle = (segment_left + segment_right) // 2
        # the maximum of the segments below is the maximum of this segment
        return max(self.query(curr_vertex*2, segment_left, segment_middle, left, min(right, segment_middle)),
                   self.query(curr_vertex*2+1, segment_middle+1, segment_right, max(left, segment_middle+1), right))
    
 
# run tests for the modified segment tree implementations
for test_num in range(1,4):
    print(f"TESTING VARIANT {test_num}:")
    test_fp = f"io/sample.in.{test_num}"
    test_expected_fp = f"io/sample.out.{test_num}"

    # open test files
    with open(test_fp, 'r') as input, open(test_expected_fp, 'r') as expected_output:
        V = int(input.readline().strip())
        N, Q = map(int, input.readline().strip().split())

        input_list = list(map(int, input.readline().strip().split()))

        # create segment tree based on V, given by input file
        match V:
            case 1:
                seg_tree = segment_tree_addition_and_get(input_list)
            case 2: 
                seg_tree = segment_tree_assignment_and_get(input_list)
            case 3:
                seg_tree = segment_tree_addition_and_max(input_list)


        for q in range(Q):
            line = input.readline().strip().split()
            # based on command, perform get, update, or max
            match(line[0]):
                # get
                case "GET":
                    res = seg_tree.get(1, 0, N-1, int(line[1]))
                    expected = expected_output.readline().strip()
                    print(f"Got value {res} (Expected: {expected}))")
                    assert(res == int(expected))
                # update
                case "UPDATE":
                    print(f"Updating range {line[1]}-{line[2]} with value {line[2]}")
                    seg_tree.update(1, 0, N-1, int(line[1]), int(line[2]), int(line[3]))
                # max
                case "MAX":
                    res = seg_tree.query(1, 0, N-1, int(line[1]), int(line[2]))
                    expected = expected_output.readline().strip()
                    print(f"Got value {res} (Expected: {expected}))")
                    assert(res == int(expected))
