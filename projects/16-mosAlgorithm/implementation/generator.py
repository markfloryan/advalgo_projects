import random
import math

from mosAlgorithm import Mo, ModeData

def bruteForceMode(array, queries):
    results = []
    for query in queries:
        counts = {}
        mode = None
        modeCount = 0
        for i in range(query[0], query[1] + 1):
            val = array[i]
            if not val in counts:
                counts[val] = 0
            counts[val] += 1
            if modeCount < counts[val]:
                modeCount = counts[val]
                mode = val
        results.append((mode, modeCount))
    return results

def generateArray(length, maximum):
    return [random.randint(1, maximum) for i in range(length)]

def generateQueries(amount, length):
    results = []
    for i in range(amount):
        l = random.randint(0, length - 1)
        r = random.randint(l, length - 1)
        results.append((l, r))
    return results

def compareResults(array, queries):
    bruteResults = bruteForceMode(array, queries)
    data = ModeData(array)
    mo = Mo(int(math.sqrt(len(array))), data)
    moResults = mo.query(queries)
    for i in range(len(queries)):
        if bruteResults[i][1] != moResults[i][1]:
            print(f"conflict on query {i}: {queries[i]}; brute gave {bruteResults[i]} and mo gave  {moResults[i]}")
            # return
    # print(f"all match for arr {array} with queries {queries}")

# qs = [(0, 4), (0, 6), (0, 3), (1, 4), (1, 6), (4, 9), (3, 10)]
# arr = [8, 3, 4, 5, 3, 2, 3, 1, 3, 2, 8, 10, 11, 3, 2]
for i in range(1):
    length = 10000
    arr = generateArray(length, 20)
    qs = generateQueries(1000, length)
    compareResults(arr, qs)
