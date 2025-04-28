import functools

def sortWithBlockSize(q1, q2, blockSize):
    q1lb = q1[0] // blockSize # get the block of the left index of query one
    q2lb = q2[0] // blockSize # get the block of the left index of query two
    return q1lb - q2lb if q1lb != q2lb else q1[1] - q2[1] # if the blocks differ, sort by them, otherwise break a tie with the right indices

class Mo:
    def __init__(self, blockSize, data):
        self.blockSize = blockSize
        self.data = data # data is an object that has init(), add(idx), remove(idx), and answer() functions
    
    def query(self, queries):
        
        queriesWithIdx = [(queries[i][0], queries[i][1], i) for i in range(len(queries))] # create tuples that bundle each query with its index

        sortedQueries = sorted(queriesWithIdx, key = functools.cmp_to_key(lambda r1, r2 : sortWithBlockSize(r1, r2, self.blockSize)))
        
        results = list(range(len(queries))) # create an array to store query results in their original order

        self.data.init() # initialize the data class

        l = 0
        r = -1
        for q in sortedQueries:
            while q[0] < l: # while the current query's left index is less than the current query range, decrement the query range left index and add to the range data structure
                l -= 1
                self.data.add(l)
            while r < q[1]: # while the current query's right index is greater than the current query range, increment the query range right index and add to the range data structure
                r += 1
                self.data.add(r)
            while l < q[0]: # while the current query's left index is greater than the current query range, increment the query range left index and remove from the range data structure
                self.data.remove(l)
                l += 1
            while q[1] < r: # while the current query's right index is less than the current query range, decrement the query range right index and remove from the range data structure
                self.data.remove(r)
                r -= 1
            results[q[2]] = self.data.answer() # get the current answer and write it to the index of the query's original position
        
        return results



class ModeData:
    def __init__(self, array):
        self.array = array

    def init(self):
        self.frequencies = {} # maps numbers to current frequencies
        self.buckets = [None for i in range(len(self.array))] # array of frequency buckets, index 0 represents frequency 1 (so numbers with "zero" frequency are not stored in any buckets)
        self.modeFreq = 0 # stores the frequency of the current mode

    # convenience functions for handling frequencies being offset by 1 from bucket indicies
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
        if not val in self.frequencies: # add to frequencies map if not yet seen
            self.frequencies[val] = 0
        # remove from current bucket and insert into next bucket (next 3 lines)
        self.removeFromBucket(self.frequencies[val], val) 
        self.frequencies[val] += 1
        self.addToBucket(self.frequencies[val], val)
        if self.modeFreq < self.frequencies[val]: # if this is the new mode, increment the mode
            self.modeFreq = self.frequencies[val]

    def remove(self, idx):
        val = self.array[idx]
        self.removeFromBucket(self.frequencies[val], val)
        if self.frequencies[val] == self.modeFreq and not self.buckets[self.frequencies[val] - 1]: # if val's frequency is the mode and its the last in its bucket, decrement the mode
            self.modeFreq -= 1
        self.frequencies[val] -= 1
        self.addToBucket(self.frequencies[val], val) # insert val into the previous bucket

    def answer(self):
        # this loop only runs once because it returns on the first iteration, this is just an easy way to grab an arbitrary element of a set
        for val in self.buckets[self.modeFreq - 1]:
            return (val, self.modeFreq)

# qs = [(0, 4), (0, 6), (0, 3), (1, 4), (1, 6), (4, 9), (3, 10)]
# arr = [8, 3, 4, 5, 3, 2, 3, 1, 3, 2, 8, 10, 11, 3, 2]
# data = ModeData(arr)
# mo = Mo(3, data)
# results = mo.query(qs)

# for i in range(len(qs)):
#     print(qs[i], results[i])