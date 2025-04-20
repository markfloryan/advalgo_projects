class segment_tree_addition_and_get:
    def __init__(self, input_array):
        tree = [0]*(len(input_array)*4)
        self.build(input_array, 1, 0, len(input_array)-1)
    
    def build(self, input_array, curr_vertex, segment_left, segment_right):
        if segment_left == segment_right:
            self.tree[curr_vertex] = input_array[segment_left]
        else:
            segment_middle = (segment_left + segment_right) // 2
            self.build(input_array, curr_vertex*2, segment_left, segment_middle)
            self.build(input_array, curr_vertex*2+1, segment_middle + 1, segment_right)
            self.tree[curr_vertex] = 0
    
    def add_to_range(self, curr_vertex, segment_left, segment_right, left, right, num_to_add):
        if left > right:
            return
        if left == segment_left and right == segment_right:
            self.tree[curr_vertex] += num_to_add
        else:
            segment_middle = (segment_left + segment_right) // 2
            self.add_to_range(curr_vertex*2, segment_left, segment_middle, left, min(right, segment_middle), num_to_add)
            self.add_to_range(curr_vertex*2+1, segment_middle + 1, segment_right, max(left, segment_middle+1), right, num_to_add)

    def get(self, curr_vertex, segment_left, segment_right, position):
        if segment_left == segment_right:
            return self.tree[curr_vertex]
        segment_middle = (segment_left + segment_right) // 2
        if position <= segment_middle:
            return self.tree[curr_vertex] + self.get(curr_vertex * 2, segment_left, segment_right, position)
        else:
            return self.tree[curr_vertex] + self.get(curr_vertex * 2 + 1, segment_middle + 1, segment_right, position)

class segment_tree_assignment_and_get:
    def __init__(self, input_array):
        tree = [0]*(len(input_array)*4)
        marked = [False]*(len(input_array)*4)
        self.build(input_array, 1, 0, len(input_array)-1)
    
    def build(self, input_array, curr_vertex, segment_left, segment_right):
        if segment_left == segment_right:
            self.tree[curr_vertex] = input_array[segment_left]
        else:
            segment_middle = (segment_left + segment_right) // 2
            self.build(input_array, curr_vertex*2, segment_left, segment_middle)
            self.build(input_array, curr_vertex*2+1, segment_middle + 1, segment_right)
            self.tree[curr_vertex] = 0
    
    def push(self, vertex):
        if self.marked[vertex]:
            self.tree[vertex*2] = self.tree[vertex*2+1] = self.tree[vertex]
            self.marked[vertex*2] = self.marked[vertex*2+1] = True
            self.marked[vertex] = False

    def assign_to_range(self, curr_vertex, segment_left, segment_right, left, right, num_to_assign):
        if left > right:
            return
        if left == segment_left and right == segment_right:
            self.tree[curr_vertex] = num_to_assign
            self.marked[curr_vertex] = True
        else:
            segment_middle = (segment_left + segment_right) // 2
            self.assign_to_range(curr_vertex * 2, segment_left, segment_middle, left, min(right, segment_middle), num_to_assign)
            self.assign_to_range(curr_vertex * 2 + 1, segment_middle + 1, segment_right, max(left, segment_middle+1), right, num_to_assign)

    def get(self, curr_vertex, segment_left, segment_right, position):
        if segment_left == segment_right:
            return self.tree[curr_vertex]
        self.push(curr_vertex)
        segment_middle = (segment_left + segment_right) // 2
        if position <= segment_middle:
            return self.get(curr_vertex * 2, segment_left, segment_right, position)
        else:
            return self.get(curr_vertex * 2 + 1, segment_middle + 1, segment_right, position)

class segment_tree_addition_and_max:
    def __init__(self, input_array):
        tree = [0]*(len(input_array)*4)
        lazy = [0]*(len(input_array)*4)
        self.build(input_array, 1, 0, len(input_array)-1)

    def push(self, vertex):
        self.tree[vertex*2] += self.lazy[vertex]
        self.lazy[vertex*2] += self.lazy[vertex]
        self.tree[vertex*2+1] += self.lazy[vertex]
        self.lazy[vertex*2+1] += self.lazy[vertex]
        self.lazy[vertex] = 0
    
    def build(self, input_array, curr_vertex, segment_left, segment_right):
        if segment_left == segment_right:
            self.tree[curr_vertex] = input_array[segment_left]
        else:
            segment_middle = (segment_left + segment_right) // 2
            self.build(input_array, curr_vertex*2, segment_left, segment_middle)
            self.build(input_array, curr_vertex*2+1, segment_middle + 1, segment_right)
            self.tree[curr_vertex] = 0
    
    def add_to_range(self, curr_vertex, segment_left, segment_right, left, right, num_to_add):
        if left > right:
            return
        if left == segment_left and right == segment_right:
            self.tree[curr_vertex] += num_to_add
            self.lazy[curr_vertex] += num_to_add
        else:
            self.push(curr_vertex)
            segment_middle = (segment_left + segment_right) // 2
            self.add_to_range(curr_vertex*2, segment_left, segment_middle, left, min(right, segment_middle), num_to_add)
            self.add_to_range(curr_vertex*2+1, segment_middle + 1, segment_right, max(left, segment_middle+1), right, num_to_add)
            self.tree = max(self.tree[curr_vertex*2], self.tree(curr_vertex*2+1))

    def query(self, curr_vertex, segment_left, segment_right, left, right):
        if segment_left == segment_right:
            return float('-inf')
        
        if left == segment_left and right == segment_right:
            return self.tree[curr_vertex]
        
        self.push(curr_vertex)
        segment_middle = (segment_left + segment_right) // 2
        return max(self.query(curr_vertex*2, segment_left, segment_middle, left, min(right, segment_middle)),
                   self.query(curr_vertex*2+1, segment_middle+1, segment_right, max(left, segment_middle+1), right))

