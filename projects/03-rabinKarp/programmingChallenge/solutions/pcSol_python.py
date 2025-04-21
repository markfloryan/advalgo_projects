import sys
import os

def count_palindromic_substrings(s):
    # Reverse the string for comparison later
    r = s[::-1]

    base, q, ans = 26, 10**9 + 7, 0

    # Iterate over all possible starting indices of substrings
    for i in range(len(s)):
        # Initialize hash values for original and reversed substrings
        original_hash = 0
        reversed_hash = 0

        # Iterate over all possible ending indices of substrings
        for j in range(i, len(s)):
            # Convert current character to a number based on unicode normalized by 'a'
            current_char_value = ord(s[j]) + 1

            # Update rolling hash for original string, we are adding a character to the end so left shift and make space for lower order bit to be added
            original_hash = (original_hash * base + current_char_value) % q

            # Compute position of corresponding character in the reversed string
            reverse_char_index = len(r) - j - 1
            reverse_char_value = ord(r[reverse_char_index]) + 1

            # For reversed hash the new character added is at the beginning, so we need to set/add higher order bit to hash
            reversed_hash = (reversed_hash + reverse_char_value * pow(base, j - i, q)) % q

            # If hashes match, we might have a palindrome.
            if original_hash == reversed_hash:
                # Confirm it is actually a palindrome by manually checking, avoid false positives and spurious hits
                if s[i:j+1] == s[i:j+1][::-1]:
                    ans += 1

    # Final count of palindromic substrings
    return ans

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python3 pcSol_python.py <input_file_path>")
        sys.exit(1)

    input_file = sys.argv[1]

    base_name = os.path.basename(input_file) 
    test_number = base_name.split('.')[-1]     
    expected_output_file = f"../io/test.out.{test_number}"

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
