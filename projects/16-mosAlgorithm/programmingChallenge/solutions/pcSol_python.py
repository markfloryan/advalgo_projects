# importing necessary libraries
import functools  # for higher-order functions like cmp_to_key
from math import floor, sqrt  # mathematical functions
import bisect  # for binary search operations on sorted lists

# function to compare two range queries based on their block number and right endpoint
def sort_with_block_size(r1, r2, block_size):
    # if either range is None, return 0 (equivalent)
    if r1 is None or r2 is None:
        return 0
    # calculate which block each range's left endpoint belongs to
    r1lb = r1[0] // block_size
    r2lb = r2[0] // block_size
    # if ranges are in different blocks, sort by block number
    # otherwise sort by right endpoint
    return r1lb - r2lb if r1lb != r2lb else r1[1] - r2[1]

# converts timestamp queries to message index queries
def translate_timestamp_queries_to_messages(queries, messages):
    # extract all timestamps from messages
    timestamps = [message[0] for message in messages]
    indexed_queries = []
    # process each query as a pair of timestamps
    for l_ts, r_ts in queries:
        # find message index for left timestamp using binary search
        l_idx = bisect.bisect_left(timestamps, l_ts)
        # find message index for right timestamp using binary search
        # subtract 1 to make it inclusive
        r_idx = bisect.bisect_right(timestamps, r_ts) - 1
        # only add valid ranges (left ≤ right)
        if l_idx <= r_idx:
            indexed_queries.append((l_idx, r_idx))
        else:
            # mark invalid ranges as None
            indexed_queries.append(None)
    return indexed_queries

# mo's algorithm implementation for range queries
class Mo:
    def __init__(self, block_size, data):
        # size of blocks for dividing the array
        self.block_size = block_size
        # data structure that handles add/remove operations
        self.data = data  # data is an object that has init(), add(idx), remove(idx), and answer() functions

    def query(self, queries):
        # add original query indices to track results
        queries_with_idx = [(query[0], query[1], index) if query is not None else (-1,-1, index) for index, query in enumerate(queries)]
        # sort queries according to mo's algorithm
        # first by left endpoint's block, then by right endpoint
        sortedQueries = sorted(queries_with_idx, key = functools.cmp_to_key(lambda r1, r2 : sort_with_block_size(r1, r2, self.block_size)))

        # pre-allocate results array
        results = list(range(len(queries)))

        # initialize data structure
        self.data.init()

        # current query window boundaries
        l = 0
        r = -1
        # process each query
        for index, q in enumerate(sortedQueries):
            # handle invalid queries
            if q[0] == -1 and q[1] == -1:
                results[q[2]] = 0
                continue
            # expand/contract current window's left boundary
            while q[0] < l:
                l -= 1
                self.data.add(l)
            # expand/contract current window's right boundary
            while r < q[1]:
                r += 1
                self.data.add(r)
            # handle case where window needs to move right
            while l < q[0]:
                self.data.remove(l)
                l += 1
            # handle case where window needs to contract from right
            while q[1] < r:
                self.data.remove(r)
                r -= 1
            # store the answer for this query at its original position
            results[q[2]] = self.data.answer()

        return results


# data structure to count unique users in a range of messages
class UniqueUsersData:
    def __init__(self, messages):
        self.messages = messages
        # ensure messages are sorted by timestamp
        self.messages.sort(key=lambda x: x[0])

    def init(self):
        # initialize empty dictionary to track user counts
        self.uniqueUsers = {}

    def add(self, idx):
        # extract username from message
        name = self.messages[idx][1]
        # add or increment user count
        if name not in self.uniqueUsers:
            self.uniqueUsers[name] = 1
        else:
            self.uniqueUsers[name] += 1

    def remove(self, idx):
        # extract username from message
        name = self.messages[idx][1]
        # decrement user count
        self.uniqueUsers[name] -= 1
        # remove user if count reaches zero
        if self.uniqueUsers[name] == 0:
            del self.uniqueUsers[name]

    def answer(self):
        # return number of unique users in current range
        return len(self.uniqueUsers)

# parse input for number of messages and queries
first_line = input()
first_line = first_line.split()
num_messages = int(first_line[0])
num_queries = int(first_line[1])

# read message data
messages = []
for i in range(num_messages):
    new_message = input()
    new_message = new_message.split()
    # store as (timestamp, username) tuples
    messages.append((int(new_message[0]),new_message[1]))

# read query data
queries = []
for i in range(num_queries):
    new_query = input()
    new_query = new_query.split()
    # store as (start_timestamp, end_timestamp) tuples
    queries.append((int(new_query[0]), int(new_query[1])))

# convert timestamp queries to message index queries
indexed_queries = translate_timestamp_queries_to_messages(queries, messages)

# initialize data structure for unique users
data = UniqueUsersData(messages)
# calculate optimal block size for mo's algorithm
block_size = floor(sqrt(num_messages))
# initialize mo's algorithm
mo = Mo(block_size, data)
# process all queries using mo's algorithm
results = mo.query(indexed_queries)

# output results
for i in range(len(results)):
    print(results[i])