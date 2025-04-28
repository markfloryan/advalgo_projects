import sys
import os

def rabin_karp(text, pattern):
    indices, base, q, target_hash, curr_hash, l = [], 26, 10**9 + 7, 0, 0, 0

    # Compute the hash of the pattern
    for i in range(len(pattern)):
        # Go in reverse of pattern to represent leftmost character as highest order
        target_hash = (target_hash * base + (ord(pattern[i]) + 1)) % q

    # Sliding window template
    for r in range(len(text)):
        # Left shift hash to make room for new character in base 26, then add new character's unicode normalized by 'a'
        curr_hash = (curr_hash * base + (ord(text[r]) + 1)) % q

        # Update hash value with rolling hash technique when window becomes oversized
        if r - l + 1 > len(pattern):
            # Remove leftmost highest order character at position l
            curr_hash = (curr_hash - (ord(text[l]) + 1) * pow(base, len(pattern), q)) % q
            l += 1

        # Check if the current window matches the pattern
        if r - l + 1 == len(pattern) and curr_hash == target_hash:
            if text[l:r+1] == pattern:      # Manual check to avoid false positives and spurious hits
                indices.append(l)

    # Final list of starting indices where the pattern is found in the text
    return indices

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python3 rabinKarp.py <input_file_path>")
        sys.exit(1)

    input_file = sys.argv[1]

    base_name = os.path.basename(input_file)
    test_number = base_name.split('.')[-1]
    expected_output_file = f"io/sample.out.{test_number}"

    # Read input (first line: pattern, second line: text)
    with open(input_file, "r") as f:
        lines = f.read().strip().split('\n')
        pattern = lines[0]
        text = lines[1]

    # Read expected output
    if os.path.exists(expected_output_file):
        with open(expected_output_file, "r") as f:
            expected_output = eval(f.read().strip())  
    else:
        expected_output = None

    # Run solution
    actual_output = rabin_karp(text, pattern)

    # Print results
    if expected_output is not None:
        print("Expected Output:", expected_output)
    print("Actual Output:  ", actual_output)
    if expected_output is not None:
        print("Test Passed:    ", actual_output == expected_output)
