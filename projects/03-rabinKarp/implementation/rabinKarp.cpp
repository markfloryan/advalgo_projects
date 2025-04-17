#include <iostream>
#include <fstream>
#include <vector>
#include <string>
#include <filesystem>
#include <sstream>

using namespace std;

vector<int> rabinKarp(const string& text, const string& pattern, int prime = 101) {
    vector<int> indices;
    int base = 26;
    int q = 1000000007;
    long long target_hash = 0, curr_hash = 0;
    int l = 0;

    // Compute the hash of the pattern
    for (int i = 0; i < pattern.size(); i++) {
        // Go in reverse of pattern to represent leftmost character as highest order
        target_hash = (target_hash * base + (pattern[i] - 'a' + 1)) % q;
    }

    // Sliding window template
    for (int r = 0; r < text.size(); r++) {
        // Left shift hash to make room for new character in base 26, then add new character's unicode normalized by 'a'
        curr_hash = (curr_hash * base + (text[r] - 'a' + 1)) % q;

        // Update hash value with rolling hash technique when window becomes oversized
        if (r - l + 1 > pattern.size()) {
            // Remove leftmost highest order character at position l
            curr_hash = (curr_hash - ((long long)(text[l] - 'a' + 1) * (long long)pow(base, pattern.size())) % q + q) % q;
            l += 1;
        }

        // Check if the current window matches the pattern
        if (r - l + 1 == pattern.size() && curr_hash == target_hash) {
            if (text.substr(l, r - l + 1) == pattern) {      // Manual check to avoid false positives and spurious hits
                indices.push_back(l);
            }
        }
    }

    // Final list of starting indices where the pattern is found in the text
    return indices;
}

vector<int> parseOutput(const string& filepath) {
    ifstream file(filepath);
    vector<int> output;

    if (!file.is_open()) return output;

    string line;
    getline(file, line);

    // Strip brackets and split
    line.erase(remove(line.begin(), line.end(), '['), line.end());
    line.erase(remove(line.begin(), line.end(), ']'), line.end());

    stringstream ss(line);
    string temp;
    while (getline(ss, temp, ',')) {
        if (!temp.empty()) {
            output.push_back(stoi(temp));
        }
    }

    return output;
}

int main(int argc, char* argv[]) {
    if (argc < 2) {
        cout << "Usage: ./rabinKarp <input_file_path>" << endl;
        return 1;
    }

    string inputFile = argv[1];
    string baseName = std::filesystem::path(inputFile).filename().string();
    string testNumber = baseName.substr(baseName.find_last_of('.') + 1);
    string expectedOutputFile = "io/sample.out." + testNumber;

    ifstream infile(inputFile);
    if (!infile.is_open()) {
        cerr << "Failed to open input file." << endl;
        return 1;
    }

    string pattern, text;
    getline(infile, pattern);
    getline(infile, text);

    vector<int> expectedOutput = parseOutput(expectedOutputFile);
    vector<int> actualOutput = rabinKarp(text, pattern);

    // Print
    cout << "Expected Output: [";
        for (size_t i = 0; i < expectedOutput.size(); ++i) {
            cout << expectedOutput[i];
            if (i != expectedOutput.size() - 1) cout << ", ";
        }
        cout << "]" << endl;

    cout << "Actual Output:   [";
    for (size_t i = 0; i < actualOutput.size(); ++i) {
        cout << actualOutput[i];
        if (i != actualOutput.size() - 1) cout << ", ";
    }
    cout << "]" << endl;

    cout << "Test Passed:     " << (actualOutput == expectedOutput ? "true" : "false") << endl;

    return 0;
}
