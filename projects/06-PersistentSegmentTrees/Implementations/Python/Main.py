#!/usr/bin/env python3
import sys
from PST import PST

def main():
    if len(sys.argv) != 3:
        print(f"Usage: {sys.argv[0]} <input.in> <expected.out>", file=sys.stderr)
        sys.exit(1)

    input_path, expected_path = sys.argv[1], sys.argv[2]

    try:
        with open(input_path, 'r') as f:
            n, m = map(int, f.readline().split())
            arr = list(map(int, f.readline().split()))
            pst = PST(n)
            pst.construct(arr)

            results = []
            for _ in range(m):
                parts = f.readline().split()
                cmd = parts[0]
                if cmd == "update":
                    idx, x = int(parts[1]), int(parts[2])
                    pst.update(idx, x)
                elif cmd == "query":
                    ver, l, r = map(int, parts[1:])
                    results.append(pst.query(ver, l, r))
    except FileNotFoundError:
        print(f"Error: could not open input file '{input_path}'", file=sys.stderr)
        sys.exit(1)

    try:
        with open(expected_path, 'r') as f:
            expected = [int(line.strip()) for line in f if line.strip()]
    except FileNotFoundError:
        print(f"Error: could not open expected file '{expected_path}'", file=sys.stderr)
        sys.exit(1)

    all_pass = (len(results) == len(expected))
    if not all_pass:
        print(f"Mismatch in number of results: got {len(results)}, expected {len(expected)}", file=sys.stderr)

    tests = min(len(results), len(expected))
    for i in range(tests):
        act = results[i]
        exp = expected[i]
        match = (act == exp)
        print(f"Test {i}:")
        print(f"  Actual:   {act}")
        print(f"  Expected: {exp}")
        print(f"  Match:    {'YES' if match else 'NO'}\n")
        if not match:
            all_pass = False

    if all_pass:
        print("All tests passed!")
        sys.exit(0)
    else:
        print("Some tests failed.")
        sys.exit(1)

if __name__ == "__main__":
    main()
