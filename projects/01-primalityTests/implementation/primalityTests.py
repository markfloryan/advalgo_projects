import numpy as np

# the line below can be uncommented to force deterministic behavior for the Fermat or Miller-Rabin tests
# np.random.seed(42)

# Trial Division
def division_prime(n):
    # the largest number we have to try is the square root of n (truncated to an integer)
    r = int(np.sqrt(n))
    # if we cannot find a factor of n then n is prime and return true, else return false
    for i in range(2, r + 1):
        if n % i == 0:
            return False
    return True

# Binary Exponentiation With Modulo
# a helper function used in the Fermat and Miller-Rabin tests to efficiently compute large exponents modulo a number
def binary_power(base, exp, mod):
    result = 1
    # the modulus operator does not interfere with multiplication, so we can perform a modulus at each step
    base %= mod
    while exp:
        # if the binary representation of the exponent has a 1 at the end then we multiply the current base to the result and perform a modulus
        if exp & 1:
            result = result * base % mod
        # bit shift the exponent right
        exp >>= 1
        # now the last digit of the exponent represents the next power of 2, so we square the current base and perform a modulus
        base = base * base % mod
    return result

# Fermat Test
def fermat_prime(n, iters = 5):
    # some simple checks for small primes
    if n < 4:
        return n == 2 or n == 3

    # if we repeat the test for different random values for the base and we cannot find a witness, then n is probably prime and return true, else return false
    for i in range(iters):
        # picks an integer between 2 and n - 2 inclusive, which are the bounds for bases we should test
        a = np.random.randint(2, n - 2)
        # Fermat's little theorem states that a prime number satisfies a^(n - 1) == 1 mod n, else it is a composite number and the base is called a witness
        if binary_power(a, n - 1, n) != 1:
            return False
    return True

# Composite Checker
# a Miller-Rabin helper function that returns true if n is composite, else returns false
def check_composite(n, a, d, s):
    # Miller-Rabin states that a prime number satisfies a^d == 1 mod n OR a^(2^r * d) == -1 mod n for some integer r where 0 <= r <= s - 1, else it is a composite number and the base is called a witness
    result = binary_power(a, d, n)
    # check for the r == 0 case
    if result == 1 or result == n - 1:
        return False
    
    # check for the 1 <= r <= s - 1 case
    for i in range (1, s):
        result = result * result % n
        if result == n - 1:
            return False
    return True

# Miller-Rabin Test
def miller_rabin_prime(n, iters = 5):
    # some simple checks for small primes
    if n < 4:
        return n == 2 or n == 3
    
    # because we know that only odd numbers, except for 2, are candidates for being prime, we also know that n - 1 should be even
    # below, we introduce two integer variables s and d to replace n - 1 = 2^s * d, where d is odd
    s = 0
    d = n - 1
    # the number of trailing zeros is equal to the highest power of 2 that is a factor of n - 1, so we bit shift right until there are no more zeros
    while (d & 1) == 0:
        d >>= 1
        s += 1

    # if we repeat the test for different random values for the base and we cannot find a witness, then n is probably prime and return true, else return false
    for i in range(iters):
        # picks an integer between 2 and n - 2 inclusive, which are the bounds for bases we should test
        a = np.random.randint(2, n - 2)
        if check_composite(n, a, d, s):
            return False
    return True

# Deterministic Miller-Rabin without using np.random.seed()
def miller_rabin_deterministic_prime(n, iters = 5):
    # smallest prime check
    if n < 2:
        return False

    # deriving s and d
    s = 0
    d = n - 1
    while (d & 1) == 0:
        d >>= 1
        s += 1

    # it is good enough to simply just check the first 12 prime bases for testing a 64 bit integer
    # for a 32 bit integer we only need the first four prime bases
    # for a 81 bit integer we just add 41 to the list
    bases = [2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37]
    # if n is equal to a base in the list then it is prime, else check if it is composite with the helper function
    for a in bases:
        if n == a:
            return True
        if check_composite(n, a, d, s):
            return False
    return True