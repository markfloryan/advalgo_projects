#include <cmath>
#include <iostream>
#include <algorithm>
#include <stdexcept>
#include <bits/stdc++.h>
#include <openssl/sha.h>
using namespace std;

class HyperLogLog {
// p -> predefined by user (default 16)
// m -> based on p (num reg)
// alpha -> constant
public:HyperLogLog(int precision = 16): p(precision), m(size_t(1) << p), registers(m, 0)
    {
        // m is number of registers -> 2^p
        // the more registers you have here the more "precision"
        // register or "buckets"
        // contstant set based on num buckets
        if (m == 16) 
            alpha = 0.673;
        else if (m == 32) 
            alpha = 0.697;
        else if (m == 64) 
            alpha = 0.709;
        else              
            alpha = 0.7213 / (1 + 1.079 / double(m));
    }

    // add functions for both string and int in c plus plus
    void add(int item) {
        // just to string
        string s = to_string(item);
        add(s);
    }

    void add(const string &item) {
        // hashed with SHA1 (160 bit hash)
        unsigned char digest[SHA_DIGEST_LENGTH];
        SHA_CTX ctx;
        SHA1_Init(&ctx);
        SHA1_Update(&ctx, item.data(), item.size());
        SHA1_Final(digest, &ctx);

        // cut this hash to 8 bytes or 64 bits in this implementation
        uint64_t h = 0;
        for (int i = 0; i < 8; ++i) {
            h = (h << 8) | digest[i];
        }
        processHash(h);
    }

    template<typename T>
    void add(const T& item) {
        add(to_string(item));
    }

    // merging two HyperLogLog objects
    HyperLogLog merge(const HyperLogLog &other) const {
        // confirm the size p is same for both - can't merge HyperLogLog's with different p's
        if (p != other.p)
            throw invalid_argument("Cannot merge HLLs with different precision");
        // just take the max of all registers for each one
        // for example take the maximum for register 0 in both HyperLogLogs and do this for all following registers
        HyperLogLog result(p);
        for (size_t i = 0; i < m; ++i)
            result.registers[i] = max(registers[i], other.registers[i]);
        return result;
    }

    uint64_t cardinality(bool debug = false) const {
        // if all registers are zero, return 0
        bool allZero = true;
        for (auto r : registers) {
            if (r != 0) { allZero = false; break; }
        }
        // if all registers are zero, return 0
        if (allZero) {
            return 0;
        }


        // this is harmonic mean
        // way of averaging that lower variance compared to arithmetic mean
        double Z = 0.0;
        int zeros = 0;
        for (auto r : registers) {
            Z += pow(2.0, -int(r));
            if (r == 0) ++zeros;
        }

        // eraw: this actually computes the estimated distinct values
        double Eraw = alpha * m * m / Z;
        double E = Eraw;
            
        // E_raw very SMALL (under or equal to 2.5) -> correction
        // we use a slightly different formula to predict the estimated distinct elements
        // this is only used for small e raw and when atleat one register is 0
        // else just set E to the E_raw calculated earlier
        if (Eraw <= 2.5 * m && zeros > 0) {
            E = m * log(double(m) / zeros);
        }

        // this is edge case for LARGE e_raw
        // use a different edge formula for calculating e_raw
        double two64 = pow(2.0, 64);
        double threshold = two64 / 30.0;
        if (E > threshold) {
            E = -two64 * log(1.0 - E / two64);
        }
        // round up to nearest interger
        return uint64_t(E + 0.5);
    }

// hashing function
private:void processHash(uint64_t h) {
        // hashed with SHA1 (160 bit hash) -> passed in as 64 bit
        // already cut to 8 bytes or 64 bits in this implementation

        // shift by 64 (total bits) - p to get the top p bits from the hash
        // example: 0x12345678
        // top bits are on left

        // this determines the bucket (reg) that hash will go in
        size_t idx = h >> (64 - p);

        // w is rest of bits from hash (not used in register) -> 64 bit offset
        // this is the probabalistic part of the algorithm
        uint64_t w = h & ((uint64_t(1) << (64 - p)) - 1);

        // all zeros and if not count leading zeros
        int rho = (w == 0) ? (64 - p + 1) : (__builtin_clzll(w) - p + 1);

        // keep the MAXIMUM value only in the register or bucket
        // for example if the number of leading zeros in the bucket was 4 already
        // and we calculated a rho of 2, this register's value would stay as 4
        registers[idx] = max(registers[idx], uint8_t(rho));
    }

    int p;
    size_t m;
    vector<uint8_t> registers;
    double alpha;
};

int main() {
    string filename;
    cin >> filename;
    
    // Open the text file
    ifstream file(filename);
    if (!file.is_open()) {
        cerr << "Error opening file: " << filename << endl;
        return 1;
    }
    
    // Read the entire file content
    stringstream buffer;
    buffer << file.rdbuf();
    string content = buffer.str();
    file.close();
    
    // Split by $ to get separate HLL datasets
    vector<string> hllDatasets;
    size_t start = 0;
    size_t end = content.find('$');
    
    while (end != string::npos) {
        hllDatasets.push_back(content.substr(start, end - start));
        start = end + 1;
        end = content.find('$', start);
    }
    // Add the last dataset
    hllDatasets.push_back(content.substr(start));
    
    // Initialize vector to store all HLLs
    vector<HyperLogLog> hlls;
    
    // Process each dataset
    for (const string& dataset : hllDatasets) {
        if (dataset.empty()) continue; // Skip empty datasets
        
        // Create a new HLL for this dataset (using default precision 16)
        HyperLogLog hll(16);
        
        // Process all space-separated items
        istringstream iss(dataset);
        string item;
        while (iss >> item) {
            hll.add(item);
        }
        
        // Add to our collection
        hlls.push_back(hll);
    }
    
    // If we have multiple HLLs, merge them
    if (hlls.empty()) {
        cout << 0 << endl; // No data
    } else if (hlls.size() == 1) {
        cout << hlls[0].cardinality(false) << endl;
    } else {
        // Start with the first HLL
        HyperLogLog mergedHll = hlls[0];
        
        // Merge with each subsequent HLL
        for (size_t i = 1; i < hlls.size(); i++) {
            mergedHll = mergedHll.merge(hlls[i]);
        }
        
        // Print the cardinality of the merged HLL
        cout << mergedHll.cardinality(false) << endl;
    }
    
    return 0;
}