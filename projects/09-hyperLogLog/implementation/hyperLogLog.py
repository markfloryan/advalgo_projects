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
        if self.m == 16:
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

    def cardinality(self):
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

if __name__ == "__main__":
    import sys
    filename = sys.argv[1] if len(sys.argv) > 1 else input()
    
    # Open text file
    with open(filename, 'r') as file:
        content = file.read().strip()
    
    # Split by $ to get separate HLL datasets
    hll_datasets = content.split('$')
    
    # Initialize list to store all HLLs
    hlls = []
    
    for dataset in hll_datasets:
        if dataset.strip():
            hll = HyperLogLog()
            
            items = dataset.strip().split()
            for item in items:
                hll.add(item)
            
            hlls.append(hll)
    
    if not hlls:
        print(0)
    elif len(hlls) == 1:
        print(hlls[0].cardinality())
    else:
        merged_hll = hlls[0]
        
        for i in range(1, len(hlls)):
            merged_hll = merged_hll.merge(hlls[i])
        
        # Print the cardinality of the merged HLL
        print(merged_hll.cardinality())
