import random
import sys
# see primalityTests.py under implementation for a more detailed explanation

# binary exponentiation to efficiently calculate a^b % m
def binpow(a, b, m):
    r = 1
    # take a modulus at each step
    a %= m
    while b:
        # if b is odd then we multiply the current base to the result
        if b & 1:
            r = r * a % m
        # move on to the next power of 2
        b >>= 1
        a = a * a % m
    return r


# composite checker Miller-Rabin helper function
def check_composite(n, a, d, s):
    r = binpow(a, d, n)
    # check for the exponent r == 0 case
    if r == 1 or r == n - 1:
        return False

    # check for the exponent 1 <= r <= s - 1 case
    for _ in range(s - 1):
        r = r * r % n
        if r == n - 1:
            return False
    return True

# Miller-Rabin approach
def is_prime(n, k=15):
    # some simple checks for small primes
    if n < 4:
        return n == 2 or n == 3

    # representing n - 1 = 2^s * d
    s = 0
    d = n - 1
    while (d & 1) == 0:
        d >>= 1
        s += 1

    # witness loop
    for _ in range(k):
        a = random.randrange(2, n - 2)
        if check_composite(n, a, d, s):
            return False
    return True


# hashing function given in the problem statement
def fartcoin_hash(s):
    h = 0
    for char in s:
        # bitshift left then xor so that h can be odd
        h = h << 1
        h = h ^ ord(char)
    return h


# main processing function
if __name__ == "__main__":
    n = int(sys.stdin.readline())
    blockchain = "fartcoin"
    accepted_count = 0

    for _ in range(n):
        line = sys.stdin.readline().strip()
        if not line:
            continue
        username, nonce = line.split(maxsplit=1)

        new_blockchain = blockchain + nonce
        h = fartcoin_hash(new_blockchain)

        if is_prime(h):
            accepted_count += 1
            blockchain = new_blockchain
            print(f"{username} accepted {accepted_count}")
        else:
            print(f"{username} rejected {accepted_count}")
