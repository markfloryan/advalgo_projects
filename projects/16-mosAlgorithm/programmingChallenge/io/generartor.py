import random
import sys
import math

M = 10
Q = 4

def generateArray(length, minimum, maximum):
    return [random.randint(minimum, maximum) for i in range(length)]

def generateQueries(amount, minimum, maximum):
    results = []
    delta = int(math.sqrt(maximum - minimum))
    for i in range(amount):
        l = 0
        r = 0
        if i % 3 == 0:
            l = random.randint(minimum, maximum)
            r = random.randint(l, maximum)
        elif i % 3 == 1:
            r = random.randint(minimum, maximum)
            l = random.randint(minimum, r)
        elif i % 3 == 2:
            l = random.randint(minimum, maximum - delta)
            r = random.randint(l, l + delta)
        results.append((l, r))
    return results



U = 5

if (1< len(sys.argv)):
    M = int(sys.argv[1])
    Q = int(sys.argv[2])
    U = int(sys.argv[3])



stamps = generateArray(M, 1, 10**8)
users = generateArray(M, 1, U)

stampMin = min(stamps)
stampMax = max(stamps)

queries = generateQueries(Q, stampMin, stampMax)


print(M, Q)

for i in range(M):
    print(stamps[i], users[i])

for i in range(Q):
    print(queries[i][0], queries[i][1])
