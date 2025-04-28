#include <iostream>
#include <algorithm>
#include <vector>
#include <unordered_map>
#include <unordered_set>

int blockSize;

struct Query{
    int left;
    int right;
    int originalIndex;
};

bool compareDesc(Query q1, Query q2){
    int q1Block = q1.left / blockSize;
    int q2Block = q2.left / blockSize;
    if (q1Block == q2Block) {
        return q1.right < q2.right;
    }
    return q1Block < q2Block;
}

class ModeData {
private:
    std::vector<int> array;
    std::unordered_map<int, int> frequencies;
    std::vector<std::unordered_set<int>> buckets;
    int modeFreq;

public:
    ModeData(const std::vector<int>& arr) : array(arr) {}

    void init() {
        frequencies.clear();
        buckets.resize(array.size());
        for (auto& bucket : buckets) {
            bucket.clear();
        }
        modeFreq = 0;
    }

    // Convenience functions for handling frequencies being offset by 1 from bucket indices
    void addToBucket(int freq, int item) {
        if (freq <= 0) return;
        buckets[freq - 1].insert(item);
    }

    void removeFromBucket(int freq, int item) {
        if (freq <= 0) return;
        buckets[freq - 1].erase(item);
    }

    void add(int idx) {
        int val = array[idx];
        if (frequencies.find(val) == frequencies.end()) {
            frequencies[val] = 0;
        }
        // Remove from current bucket and insert into next bucket
        removeFromBucket(frequencies[val], val);
        frequencies[val]++;
        addToBucket(frequencies[val], val);
        if (modeFreq < frequencies[val]) {
            modeFreq = frequencies[val];
        }
    }

    void remove(int idx) {
        int val = array[idx];
        removeFromBucket(frequencies[val], val);
        if (frequencies[val] == modeFreq && buckets[frequencies[val] - 1].empty()) {
            modeFreq--;
        }
        frequencies[val]--;
        addToBucket(frequencies[val], val);
    }

    std::pair<int, int> answer() {
        if (modeFreq == 0) return {0, 0};
        return {*buckets[modeFreq - 1].begin(), modeFreq};
    }
};

class Mo {
private:
    int blockSize;
    ModeData& data;

public:
    Mo(int bs, ModeData& d) : blockSize(bs), data(d) {}

    std::vector<std::pair<int, int>> query(const std::vector<Query>& queries) {
        // Create a copy of queries with their original indices
        std::vector<Query> sortedQueries = queries;
        for (size_t i = 0; i < sortedQueries.size(); i++) {
            sortedQueries[i].originalIndex = i;
        }

        // Sort queries according to Mo's algorithm
        std::sort(sortedQueries.begin(), sortedQueries.end(), compareDesc);

        // Create result array
        std::vector<std::pair<int, int>> results(queries.size());

        // Initialize data structure
        data.init();

        int l = 0;
        int r = -1;
        
        for (const auto& q : sortedQueries) {
            // Extend or shrink current range to match query range
            while (q.left < l) {
                l--;
                data.add(l);
            }
            while (r < q.right) {
                r++;
                data.add(r);
            }
            while (l < q.left) {
                data.remove(l);
                l++;
            }
            while (q.right < r) {
                data.remove(r);
                r--;
            }
            
            // Store the answer at the original query index
            results[q.originalIndex] = data.answer();
        }
        
        return results;
    }
};

int main() {
    std::vector<int> arr = {8, 3, 4, 5, 3, 2, 3, 1, 3, 2, 8, 10, 11, 3, 2};
    std::vector<Query> queries = {
        {0, 4}, {0, 6}, {0, 3}, {1, 4}, {1, 6}, {4, 9}, {3, 10}
    };
    
    blockSize = std::sqrt(arr.size());
    ModeData data(arr);
    Mo mo(blockSize, data);
    
    auto results = mo.query(queries);
    
    for (size_t i = 0; i < queries.size(); i++) {
        std::cout << "Query (" << queries[i].left << ", " << queries[i].right 
                  << ") - Mode: " << results[i].first << ", Frequency: " << results[i].second << std::endl;
    }
    
    return 0;
}
