import functools
from math import floor, sqrt


def sort_with_block_size(r1, r2, block_size):
    r1lb = r1[0] // block_size
    r2lb = r2[0] // block_size
    return r1lb - r2lb if r1lb != r2lb else r1[1] - r2[1]

class Mo:
    def __init__(self, block_size, data):
        self.block_size = block_size
        self.data = data # data is an object that has init(), add(idx), remove(idx), and answer() functions

    def query(self, queries):
        queriesWithIdx = [(queries[i][0], queries[i][1], i) for i in range(len(queries))]
        sortedQueries = sorted(queriesWithIdx, key = functools.cmp_to_key(lambda r1, r2 : sort_with_block_size(r1, r2, self.block_size)))

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



class UniqueUsersData:
    def __init__(self, messages):
        self.messages = messages

    def init(self):
        self.uniqueUsers = {}

    def add(self, idx):
        if idx in messages:
            name = messages[idx]
            if name not in self.uniqueUsers:
                self.uniqueUsers[name] = 1
            else:
                self.uniqueUsers[name] += 1



    def remove(self, idx):
        if idx in messages:
            name = messages[idx]
            self.uniqueUsers[name] -= 1
            if self.uniqueUsers[name] == 0:
                del self.uniqueUsers[name]


    def answer(self):
        return len(self.uniqueUsers)

first_line = input()
first_line = first_line.split()
num_messages = int(first_line[0])
num_queries = int(first_line[1])

messages = {}

for i in range(num_messages):
    new_message = input()
    new_message = new_message.split()
    messages[int(new_message[0])] = new_message[1]

queries = []

for i in range(num_queries):
    new_query = input()
    new_query = new_query.split()
    queries.append((int(new_query[0]), int(new_query[1])))

data = UniqueUsersData(messages)
block_size = floor(sqrt(num_messages))
mo = Mo(block_size, data)
results = mo.query(queries)

for i in range(len(results)):
    print(queries[i], ":", results[i])