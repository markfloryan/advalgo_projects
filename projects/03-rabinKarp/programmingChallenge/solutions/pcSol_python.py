import sys
import os

def count_palindromic_substrings(s):
    MOD = 10**9 + 7         # Large prime modulus to minimize hash collisions
    BASE = 131              # A reasonably large base to generate unique rolling hashes
    n = len(s)
    s_rev = s[::-1]         # Reversed version of the string, needed to detect palindromes by comparing hash values

    # Precompute hash values for all prefixes of s and reversed s
    # These allow us to compare substrings in constant time later using the Rabin-Karp method
    prefix_hash = [0] * (n + 1)      # prefix_hash[i] stores the hash of the first i characters of s
    rev_prefix_hash = [0] * (n + 1)  # rev_prefix_hash[i] stores the hash of the first i characters of reversed s
    power = [1] * (n + 1)            # power[i] stores BASE^i modulo MOD for fast multiplication rollback

    for i in range(n):
        # Building up and store the prefix hashes incrementally
        prefix_hash[i + 1] = (prefix_hash[i] * BASE + ord(s[i])) % MOD
        rev_prefix_hash[i + 1] = (rev_prefix_hash[i] * BASE + ord(s_rev[i])) % MOD
        power[i + 1] = (power[i] * BASE) % MOD

    # Rolling hash function to extract the hash of a substring [l, r)
    # by subtracting the hash of the prefix before l from the prefix ending at r,
    # and adjusting for position using the precomputed powers of BASE
    def get_hash(l, r, h, p):
        return (h[r] - h[l] * p[r - l]) % MOD

    count = 0  # Total number of palindromic substrings

    # Try all possible substring lengths from 1 to n
    for length in range(1, n + 1):
        # Slide a window of that length over the string
        for i in range(n - length + 1):
            j = i + length  # Exclusive end of the substring

            # Hash of s[i..j-1]
            h1 = get_hash(i, j, prefix_hash, power)
            # Hash of reversed substring: this corresponds to s[i..j-1] reversed,
            # which is located in s_rev from n-j to n-i
            h2 = get_hash(n - j, n - i, rev_prefix_hash, power)

            # If both hashes match, we assume it's a palindrome
            if h1 == h2:
                count += 1

    return count




if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python3 pcSol_python.py <input_file_path>")
        sys.exit(1)

    input_file = sys.argv[1]

    base_name = os.path.basename(input_file) 
    test_number = base_name.split('.')[-1]     
    expected_output_file = f"io/test.out.{test_number}"

    # Read input
    with open(input_file, "r") as f:
        input_str = f.read().strip()

    # Read expected output
    if os.path.exists(expected_output_file):
        with open(expected_output_file, "r") as f:
            expected_output = int(f.read().strip())
    else:
        expected_output = None

    # Run solution
    actual_output = count_palindromic_substrings(input_str)

    # Print results
    if expected_output is not None:
        print("Expected Output:", expected_output)
    print("Actual Output:  ", actual_output)
    if expected_output is not None:
        print("Test Passed:    ", actual_output == expected_output)
