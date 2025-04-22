def manacher(s):
    """
    Manacher's algorithm exploits the idea that there are mirrored palindromes are within a larger palindrome.

    We have three cases:

    Case 1. If the palindrome at the current center falls completely within the right and left boundaries
    when calculated with the mirrored index, we have found the palindrome for that particular point.

    Case 2. If the palindrome at the current center reaches exactly to the right boundary
    when calculated with the mirrored index, we have to manually expand the palindrome beyond the right boundary.

    Case 3. If the palindrome at the current center falls outside the right boundary
    when calculated with the mirrored index, we have to reduce the radius to fit within the right boundary.
    After that, manually expand the palindrome.

    Any time we have the current index past the right boundary, we manually expand a palindrome.
    """

    # Preprocess the string with '#' in between characters to find even-length palindromes
    t = '#' + "#".join(s) + '#'

    n = len(t)

    # Array to store palindrome radii
    p = [0] * n

    # Center of old rightmost palindrome
    center = 0

    # Right boundary of the rightmost palindrome
    right = 0

    # Length of the longest palindrome found so far
    max_len = 0

    # Center index of the longest palindrome found
    center_index = 0

    # Iterate through all indices
    for i in range(1, n - 1):
        # Find the mirrored index reflected across old center
        mirror = 2 * center - i

        # Case 1 and 3
        if i < right:
            p[i] = min(right - i, p[mirror])

        # Case 2 and 3
        while i + (1 + p[i]) < n and i - (1 + p[i]) >= 0 and t[i + (1 + p[i])] == t[i - (1 + p[i])]:
            p[i] += 1

        # In case 2 and 3, if we expand beyond the right boundary, update old center and right
        if i + p[i] > right:
            center = i
            right = i + p[i]

        # Track the longest palindrome
        if p[i] > max_len:
            max_len = p[i]
            center_index = i

    # Extract the longest palindrome from the original string
    start = (center_index - max_len) // 2
    return s[start: start + max_len]

if __name__ == "__main__":
    import sys

    print("Enter the string:")
    for line in sys.stdin:
        input_string = line.strip()
        if not input_string:
            break
        result = manacher(input_string)
        print(f"Longest palindromic substring: {result}")
