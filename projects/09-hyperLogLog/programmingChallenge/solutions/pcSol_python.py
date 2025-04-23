import sys
import math

class HyperLogLog:
    # p -> predefined by user (default 16)
    # m -> based on p (num reg)
    # alpha -> constant
    def __init__(self, p=16):
        self.p = p
        # m is number of registers -> 2^p
        # the more registers you have here the more "precision"
        self.m = 1 << p
        # register or "buckets"
        # use a simple bytearray to avoid NumPy overhead
        self.registers = bytearray(self.m)

        # contstant set based on num buckets
        if   self.m == 16:
            self.alpha = 0.673
        elif self.m == 32:
            self.alpha = 0.697
        elif self.m == 64:
            self.alpha = 0.709
        else:
            self.alpha = 0.7213 / (1 + 1.079 / self.m)

    def add(self, item):

        # hashed with Python built-in hash (64-bit truncated)
        h = hash(item) & ((1 << 64) - 1)

        # this determines the bucket (reg) that hash will go in
        idx = h >> (64 - self.p)

        # w is rest of bits from hash (not used in register) -> offset
        # this is the probabilistic part of the algorithm
        w = h & ((1 << (64 - self.p)) - 1)

        # all zeros
        if w == 0:
            rho = (64 - self.p) + 1
        # counts leading zeros
        else:
            rho = (64 - self.p) - w.bit_length() + 1

        # keep the MAXIMUM value only in the register or bucket
        if rho > self.registers[idx]:
            self.registers[idx] = rho

    def merge(self, other):
        # confirm the size p is same for both
        if self.p != other.p:
            raise ValueError("Cannot merge HLLs with different precision.")
        h = HyperLogLog(self.p)
        # take the max of each register
        for i in range(self.m):
            a = self.registers[i]
            b = other.registers[i]
            h.registers[i] = a if a >= b else b
        return h

    def cardinality(self, debug=False):
        if all(r == 0 for r in self.registers):
            return 0

        # harmonic mean: sum of 2^-registers
        Z = 0.0
        V = 0
        for r in self.registers:
            Z += 2.0 ** (-r)
            if r == 0:
                # Where V is the number of registers that are 0 which will be used later
                V += 1


        # E_raw: This actually computes the estimated unique values in the multiset
        # this is BEFORE edge case correction
        E_raw = self.alpha * self.m * self.m / Z


        #E_raw very SMALL (under or equal to 2.5 * num buckets) -> correction
        # we use a slightly different formula to predict the estimated distinct elements
        # this is only used for small e raw and when atleat one register is 0

        # Small case error correction - in small cases, the formula we normally use
        # fails to accurately estimate since most buckets are, so we switch to another metric
        # in this case.
        if E_raw <= 2.5 * self.m and V > 0:
            E = self.m * math.log(self.m / V)

        # If it isn't a small edge case, we proceed using the normal formula
        else:
            E = E_raw

        # Large case error correction
        # For very large values, we need to account for many hashing collision so we use
        # this formula
        threshold = (1 << 64) / 30.0
        if E > threshold:
            E = - (1 << 64) * math.log(1 - E / (1 << 64))

        # Round to nearest integer because we can't have "half" and element
        result = int(E + 0.5)

        return result


def stream_cards_to_hll(hll):
    line = sys.stdin.readline()
    for tok in line.strip().split():
        if tok == "$":
            continue
        hll.add(tok)
    return hll

def knapsack_01(prices, values, budget):
    N, W = len(prices), budget
    dp = [[0]*(W+1) for _ in range(N+1)]
    for i in range(1, N+1):
        wt, val = prices[i-1], values[i-1]
        for w in range(W+1):
            if w < wt:
                dp[i][w] = dp[i-1][w]
            else:
                dp[i][w] = max(dp[i-1][w], dp[i-1][w-wt] + val)
    sel, w = [], W
    for i in range(N, 0, -1):
        if dp[i][w] != dp[i-1][w]:
            sel.append(i-1)
            w -= prices[i-1]
    return list(reversed(sel))

def solve_pokemon_collection():
    budget, num = map(int, sys.stdin.readline().split())

    prices = []
    while len(prices) < num:
        prices.extend(map(int, sys.stdin.readline().split()))
    prices = prices[:num]

    your_hll = HyperLogLog(p=16)
    stream_cards_to_hll(your_hll)
    base = your_hll.cardinality()

    gains = []
    for _ in range(num):
        pack_hll = HyperLogLog(p=16)
        stream_cards_to_hll(pack_hll)
        union_hll = your_hll.merge(pack_hll)
        gains.append(union_hll.cardinality() - base)

    chosen = knapsack_01(prices, gains, budget)
    print(" ".join(str(i+1) for i in chosen))

if __name__ == "__main__":
    solve_pokemon_collection()