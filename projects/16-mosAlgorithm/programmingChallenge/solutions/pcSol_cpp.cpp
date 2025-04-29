#include <iostream>
#include <vector>
#include <unordered_map>
#include <algorithm>
#include <string>
#include <cmath>
#include <sstream>

// Simple pair to hold timestamp + username
struct Message {
    int timestamp;
    std::string user;
    
    Message(int t, std::string u) : timestamp(t), user(u) {}
};

// Holds a query in index space, plus its original position
struct Query {
    int l, r, idx;
    
    Query(int l, int r, int idx) : l(l), r(r), idx(idx) {}
};

class UniqueUsersData {
private:
    std::vector<Message> messages;
    std::unordered_map<std::string, int> counts;

public:
    // Constructor sorts messages by timestamp so we can binary search later
    UniqueUsersData(const std::vector<Message>& msgs) : messages(msgs) {
        std::sort(messages.begin(), messages.end(), 
                 [](const Message& a, const Message& b) {
                     return a.timestamp < b.timestamp;
                 });
    }

    // Reset frequency map before processing queries
    void init() {
        counts.clear();
    }

    // Add messages[idx] into the current window, incrementing that user's count
    void add(int idx) {
        std::string name = messages[idx].user;
        counts[name]++;
    }

    // Remove messages[idx] from the window, decrementing (and maybe deleting)
    // that user's count
    void remove(int idx) {
        std::string name = messages[idx].user;
        counts[name]--;
        if (counts[name] == 0) {
            counts.erase(name);
        }
    }

    // Answer is simply the number of distinct keys remaining
    int answer() {
        return counts.size();
    }
};

class Mo {
private:
    int blockSize;
    UniqueUsersData& data;

public:
    // blockSize = floor(sqrt(M)), data implements init/add/remove/answer
    Mo(int blockSize, UniqueUsersData& data) : blockSize(blockSize), data(data) {}

    std::vector<int> query(const std::vector<Query>& queries) {
        // Copy and sort queries by l/blockSize, with r as tie-breaker
        // to minimize pointer moves
        std::vector<Query> sorted = queries;
        std::sort(sorted.begin(), sorted.end(), [this](const Query& q1, const Query& q2) {
            int b1 = q1.l / blockSize; // get left block of q1
            int b2 = q2.l / blockSize; // get left block of q2
            if (b1 != b2) return b1 < b2;
            return q1.r < q2.r; // tie-break by right index if blocks are equal
        });

        std::vector<int> results(queries.size()); // to store answers in original order
        data.init(); // reset data structure
        int l = 0, r = -1; // current window is empty

        for (const Query& q : sorted) {
            // Handle "no messages in time window" case
            if (q.l == -1 && q.r == -1) {
                results[q.idx] = 0;
                continue;
            }
            
            // Adjust the current window to match the query range
            while (q.l < l) {
                l--;
                data.add(l);
            }
            while (r < q.r) {
                r++;
                data.add(r);
            }
            while (l < q.l) {
                data.remove(l);
                l++;
            }
            while (q.r < r) {
                data.remove(r);
                r--;
            }
            
            results[q.idx] = data.answer(); // Store answer in its original position
        }
        
        return results;
    }
};

// Binary-search lower_bound: first index where arr[idx] >= key
int lowerBound(const std::vector<int>& arr, int key) {
    int lo = 0, hi = arr.size();
    while (lo < hi) {
        int mid = (lo + hi) / 2;
        if (arr[mid] < key) lo = mid + 1;
        else hi = mid;
    }
    return lo;
}

// Binary-search upper_bound: first index where arr[idx] > key
int upperBound(const std::vector<int>& arr, int key) {
    int lo = 0, hi = arr.size();
    while (lo < hi) {
        int mid = (lo + hi) / 2;
        if (arr[mid] <= key) lo = mid + 1;
        else hi = mid;
    }
    return lo;
}

int main() {
    std::ios_base::sync_with_stdio(false);
    std::cin.tie(nullptr);
    
    int M, Q;
    std::cin >> M >> Q;
    
    // Read all messages (timestamp + username)
    std::vector<Message> messages;
    for (int i = 0; i < M; i++) {
        int ts;
        std::string user;
        std::cin >> ts >> user;
        messages.emplace_back(ts, user);
    }
    
    // Store the raw timestamp ranges
    std::vector<int> rawL(Q), rawR(Q);
    for (int i = 0; i < Q; i++) {
        std::cin >> rawL[i] >> rawR[i];
    }
    
    // Extract sorted timestamps array for binary-search
    std::vector<int> timestamps(M);
    for (int i = 0; i < M; i++) {
        timestamps[i] = messages[i].timestamp;
    }
    
    // Translate each timestamp query into an index-range [l_idx, r_idx]
    std::vector<Query> indexed;
    for (int i = 0; i < Q; i++) {
        int l_ts = rawL[i], r_ts = rawR[i];
        int l_idx = lowerBound(timestamps, l_ts); // first msg geq start time
        int r_idx = upperBound(timestamps, r_ts) - 1; // last msg leq end time
        
        if (l_idx <= r_idx) {
            indexed.emplace_back(l_idx, r_idx, i); // valid index range; add to list
        } else {
            // "no messages in this time window"
            indexed.emplace_back(-1, -1, i);
        }
    }
    
    // Run Mo's algorithm on the indexed queries
    UniqueUsersData dataObj(messages);
    int blockSize = static_cast<int>(std::floor(std::sqrt(M)));
    Mo mo(blockSize, dataObj);
    std::vector<int> answers = mo.query(indexed);
    
    // Print results
    for (int i = 0; i < Q; i++) {
        std::cout << answers[i] << "\n";
    }
    
    return 0;
}
