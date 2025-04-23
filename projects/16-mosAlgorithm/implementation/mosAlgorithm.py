import functools

def sortWithBlockSize(r1, r2, blockSize):
    r1lb = r1[0] // blockSize
    r2lb = r2[0] // blockSize
    return r1lb - r2lb if r1lb != r2lb else r1[1] - r2[1]

class Mo:
    def __init__(self, blockSize, data):
        self.blockSize = blockSize
        self.data = data # data is an object that has init(), add(idx), remove(idx), and answer() functions
    
    def query(self, queries):
        queriesWithIdx = [(queries[i][0], queries[i][1], i) for i in range(len(queries))]
        sortedQueries = sorted(queriesWithIdx, key = functools.cmp_to_key(lambda r1, r2 : sortWithBlockSize(r1, r2, self.blockSize)))
        
        results = list(range(len(queries)))

        self.data.init()

        l = 0
        r = -1
        for q in sortedQueries:
            while q[0] < l:
                l -= 1
                self.data.add(l)
            while r < q[1]:
                r += 1
                self.data.add(r)
            while l < q[0]:
                self.data.remove(l)
                l += 1
            while q[1] < r:
                self.data.remove(r)
                r -= 1
            results[q[2]] = self.data.answer()
        
        return results



class ModeData:
    def __init__(self, array):
        self.array = array
    def init(self):
        self.frequencies = {}
        self.buckets = [None for i in range(len(self.array))]
        self.modeFreq = 0
    def addToBucket(self, freq, item):
        if not self.buckets[freq - 1]:
            self.buckets[freq - 1] = set()
        self.buckets[freq - 1].add(item)
    def removeFromBucket(self, freq, item):
        if freq - 1 < 0:
            return
        self.buckets[freq - 1].remove(item)
    def add(self, idx):
        val = self.array[idx]
        if not val in self.frequencies:
            self.frequencies[val] = 0
        self.removeFromBucket(self.frequencies[val], val)
        self.frequencies[val] += 1
        self.addToBucket(self.frequencies[val], val)
        if self.modeFreq < self.frequencies[val]:
            self.modeFreq = self.frequencies[val]
    def remove(self, idx):
        val = self.array[idx]
        self.removeFromBucket(self.frequencies[val], val)
        if self.frequencies[val] == self.modeFreq and not self.buckets[self.frequencies[val] - 1]:
            self.modeFreq -= 1
        self.frequencies[val] -= 1
        self.addToBucket(self.frequencies[val], val)
    def answer(self):
        # print(self.frequencies, self.buckets)
        for val in self.buckets[self.modeFreq - 1]:
            return (val, self.modeFreq)

# qs = [(0, 4), (0, 6), (0, 3), (1, 4), (1, 6), (4, 9), (3, 10)]
# arr = [8, 3, 4, 5, 3, 2, 3, 1, 3, 2, 8, 10, 11, 3, 2]
# data = ModeData(arr)
# mo = Mo(3, data)
# results = mo.query(qs)

# for i in range(len(qs)):
#     print(qs[i], results[i])