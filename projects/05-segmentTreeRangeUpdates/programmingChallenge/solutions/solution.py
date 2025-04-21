import math

class SegTreeAdditionAndGet:
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
            self.tree[curr_vertex] = 0
    
    def update(self, curr_vertex, segment_left, segment_right, left, right, num_to_add):
        if left > right:
            return
        if left == segment_left and right == segment_right:
            self.tree[curr_vertex] += num_to_add
        else:
            segment_middle = (segment_left + segment_right) // 2
            self.update(curr_vertex*2, segment_left, segment_middle, left, min(right, segment_middle), num_to_add)
            self.update(curr_vertex*2+1, segment_middle + 1, segment_right, max(left, segment_middle+1), right, num_to_add)

    def get(self, curr_vertex, segment_left, segment_right, position):
        if segment_left == segment_right:
            return self.tree[curr_vertex]
        segment_middle = (segment_left + segment_right) // 2
        if position <= segment_middle:
            return self.tree[curr_vertex] + self.get(curr_vertex * 2, segment_left, segment_middle, position)
        else:
            return self.tree[curr_vertex] + self.get(curr_vertex * 2 + 1, segment_middle + 1, segment_right, position)


class SegTreeGCD:
    def __init__(self, input_array):
        self.tree = [0]*(len(input_array)*4)
        self.build(input_array, 1, 0, len(input_array)-1)
        self.length = len(input_array)
    
    def build(self, input_array, curr_vertex, segment_left, segment_right):
        if segment_left == segment_right:
            self.tree[curr_vertex] = input_array[segment_left]
        else:
            segment_middle = (segment_left + segment_right) // 2
            self.build(input_array, curr_vertex*2, segment_left, segment_middle)
            self.build(input_array, curr_vertex*2+1, segment_middle + 1, segment_right)
            self.tree[curr_vertex] = math.gcd(self.tree[curr_vertex*2], self.tree[curr_vertex*2+1])
       
    def update(self, curr_vertex, segment_left, segment_right, position, num_to_add):
        if position < 0 or position >= self.length:
            return
        if segment_left == segment_right:
            self.tree[curr_vertex] += num_to_add
            return
        
        segment_middle = (segment_left + segment_right) // 2
        if position <= segment_middle:
            self.update(curr_vertex*2, segment_left, segment_middle, position, num_to_add)
        else:
            self.update(curr_vertex*2+1, segment_middle+1, segment_right, position, num_to_add)
        self.tree[curr_vertex] = math.gcd(self.tree[curr_vertex*2], self.tree[curr_vertex*2+1])


    def query(self, curr_vertex, segment_left, segment_right, left, right):
        if left > right:
            return 0
        if left == segment_left and right == segment_right:
            return self.tree[curr_vertex]
        segment_middle = (segment_left + segment_right) // 2
        return math.gcd(self.query(curr_vertex*2, segment_left, segment_middle, left, min(right, segment_middle)),
                self.query(curr_vertex*2+1, segment_middle+1,segment_right, max(left, segment_middle+1), right))


if __name__ == "__main__":
    test_cases = 20

    for test_case in range(1, 21):
        print(f"test case {test_case}")
        test_in_fp = f"../io/test.in.{test_case}"
        test_out_fp = f"../io/test.out.{test_case}"

        with open(test_in_fp, 'r') as test_in, open(test_out_fp, 'r') as test_out:
            N_Q_line = test_in.readline()
            N, Q = map(int, N_Q_line.strip().split())

            a = list(map(int, test_in.readline().strip().split()))
            diff_a = [0]*(N-1)
            for i in range(1,N):
                diff_a[i-1] = a[i] - a[i-1]

            # construct seg trees
            add_segtree = SegTreeAdditionAndGet(a)
            gcd_segtree = SegTreeGCD(diff_a)
            # gcd = SegTreeGCD(a)

            for _ in range(Q):
                query = test_in.readline().strip()

                if query.startswith("GCD"):
                    _, l, r = query.split()
                    l, r = int(l), int(r)
                    res = math.gcd(add_segtree.get(1, 0, len(a)-1, l), gcd_segtree.query(1, 0, len(a)-1, l, r-1))
                    # res = gcd.query(1, 0, len(a)-1, l, r)
                    # print("----")
                    # print(res)

                    # ---- FOR TESTING ----
                    expected = int(test_out.readline())
                    # print(expected)
                    if res != expected:
                        print(add_segtree.get(1, 0, len(a)-1, l), gcd_segtree.query(1, 0, len(a)-1, l, r-1))
                        print(res, expected)
                    # assert res == expected
                    # ---------------------

                elif query.startswith("ADD"):
                    _, l, r, x = query.split()
                    l, r, x = int(l), int(r), int(x)

                    # range update
                    add_segtree.update(1, 0, len(a)-1, l, r, x)
                    # only boundary points change in difference array
                    gcd_segtree.update(1, 0, len(a)-1, l - 1, x)
                    gcd_segtree.update(1, 0, len(a)-1, r, -x)
