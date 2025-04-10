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

text = "ababcabcabababd"
pattern = "aba"
matches = rabin_karp(text, pattern)
print("Pattern found at indices:", matches)