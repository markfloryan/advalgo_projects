import functools
from math import floor, sqrt
import bisect

def sort_with_block_size(r1, r2, block_size):
    if r1 is None or r2 is None:
        return 0
    r1lb = r1[0] // block_size
    r2lb = r2[0] // block_size
    return r1lb - r2lb if r1lb != r2lb else r1[1] - r2[1]

def translate_timestamp_queries_to_messages(queries, messages):
    timestamps = [message[0] for message in messages]
    indexed_queries = []
    for l_ts, r_ts in queries:
        l_idx = bisect.bisect_left(timestamps, l_ts)
        r_idx = bisect.bisect_right(timestamps, r_ts) - 1
        # subtract bisect right by 1 so we have the correct inclusive right index instead of the one after
        if l_idx <= r_idx:
            indexed_queries.append((l_idx, r_idx))
        else:
            indexed_queries.append(None)
    return indexed_queries

class Mo:
    def __init__(self, block_size, data):
        self.block_size = block_size
        self.data = data # data is an object that has init(), add(idx), remove(idx), and answer() functions

    def query(self, queries):
        queries_with_idx = [(query[0], query[1], index) if query is not None else (-1,-1, index) for index, query in enumerate(queries)]
        sortedQueries = sorted(queries_with_idx, key = functools.cmp_to_key(lambda r1, r2 : sort_with_block_size(r1, r2, self.block_size)))

        results = list(range(len(queries)))

        self.data.init()

        l = 0
        r = -1
        for index, q in enumerate(sortedQueries):
            if q[0] == -1 and q[1] == -1:
                results[q[2]] = 0
                continue
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
        self.messages.sort(key=lambda x: x[0])

    def init(self):
        self.uniqueUsers = {}


    def add(self, idx):
        name = self.messages[idx][1]
        if name not in self.uniqueUsers:
            self.uniqueUsers[name] = 1
        else:
            self.uniqueUsers[name] += 1


    def remove(self, idx):
        name = self.messages[idx][1]
        self.uniqueUsers[name] -= 1
        if self.uniqueUsers[name] == 0:
            del self.uniqueUsers[name]


    def answer(self):
        return len(self.uniqueUsers)

first_line = input()
first_line = first_line.split()
num_messages = int(first_line[0])
num_queries = int(first_line[1])

messages = []

for i in range(num_messages):
    new_message = input()
    new_message = new_message.split()
    messages.append((int(new_message[0]),new_message[1]))

queries = []

for i in range(num_queries):
    new_query = input()
    new_query = new_query.split()
    queries.append((int(new_query[0]), int(new_query[1])))

indexed_queries = translate_timestamp_queries_to_messages(queries, messages)


data = UniqueUsersData(messages)
block_size = floor(sqrt(num_messages))
mo = Mo(block_size, data)
results = mo.query(indexed_queries)

for i in range(len(results)):
    print(queries[i], ":", results[i])