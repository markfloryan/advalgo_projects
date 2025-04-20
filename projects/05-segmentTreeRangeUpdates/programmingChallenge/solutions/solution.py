class SegTreeAdditionAndGet:
    pass

def gcd_helper(a, b):
    if a == 0: return b
    if b == 0: return a

    a = abs(a)
    b = abs(b)

    if (a > b):
        return gcd_helper(b, a)
    else:
        return gcd_helper(b % a, a)
    

class SegTreeGCD:
    pass



if __name__ == "__main__":
    test_cases = 20

    for test_case in range(20):
        print(f"test case {test_case}")
        test_in_fp = f"../io/test.in.{test_case}"
        test_out_fp = f"../io/test.out.{test_case}"

        with open(test_in_fp, 'r') as test_in, open(test_out_fp, 'r') as test_out:
            N_Q_line = test_in.readline()
            N, Q = map(int, N_Q_line.strip().split())

            a = list(map(int, test_in.readline().strip().split()))
            diff_a = [];
            for i in range(1,N):
                diff_a = a[i] - a[i-1]

            # construct seg trees
            add_segtree = SegTreeAdditionAndGet()
            gcd_segtree = SegTreeGCD()

            for _ in range(Q):
                query = test_in.readline().strip()

                if query.startswith("GCD"):
                    _, l, r = query.split()
                    l, r = int(l), int(r)

                    # TODO: come back when you get seg tree implementations
                    res = gcd_helper(add_segtree.get(l), gcd_segtree.query(l, r - 1))

                    # UNCOMMENT FOR NORMAL SOLUTION
                    # print(res)

                    # ---- FOR TESTING ----
                    expected = int(test_out.readline())
                    assert res == expected
                    # ---------------------

                elif query.startswith("ADD"):
                    _, l, r, x = query.split()
                    l, r, x = int(l), int(r), int(x)

                    # range update
                    add_segtree.update(l, r, x)
                    # only boundary points change in difference array
                    gcd_segtree.update(l - 1, x)
                    gcd_segtree.update(r, -x)
