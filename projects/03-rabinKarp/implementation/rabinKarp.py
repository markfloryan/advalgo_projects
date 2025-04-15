import sys
import os

def rabin_karp(text, pattern, prime=101):
    n = len(text)
    m = len(pattern)
    d = 256  # number of characters in the input alphabet
    h = pow(d, m-1, prime)
    p = 0  # hash value for pattern
    t = 0  # hash value for text window
    result = []

    # Preprocessing: calculate the hash value of pattern and first text window
    for i in range(m):
        p = (d * p + ord(pattern[i])) % prime
        t = (d * t + ord(text[i])) % prime

    # Slide the pattern over text
    for i in range(n - m + 1):
        # Check the hash values
        if p == t:
            # If hash values match, check characters one by one
            if text[i:i + m] == pattern:
                result.append(i)

        # Calculate hash value for next window of text
        if i < n - m:
            t = (d * (t - ord(text[i]) * h) + ord(text[i + m])) % prime
            if t < 0:
                t += prime

    return result


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
