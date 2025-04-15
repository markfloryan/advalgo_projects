#include <iostream>
#include <fstream>
#include <vector>
#include <string>
#include <filesystem>
#include <sstream>

using namespace std;

vector<int> rabinKarp(const string& text, const string& pattern, int prime = 101) {
    int n = text.length();
    int m = pattern.length();
    int d = 256; // Number of characters in the input alphabet
    int h = 1;

    for (int i = 0; i < m - 1; i++)
        h = (h * d) % prime;

    int p = 0, t = 0;
    vector<int> result;

    // Preprocessing: hash pattern and first text window
    for (int i = 0; i < m; i++) {
        p = (d * p + pattern[i]) % prime;
        t = (d * t + text[i]) % prime;
    }

    // Slide the pattern over text
    for (int i = 0; i <= n - m; i++) {
        if (p == t) {
            if (text.substr(i, m) == pattern) {
                result.push_back(i);
            }
        }

        if (i < n - m) {
            t = (d * (t - text[i] * h) + text[i + m]) % prime;
            if (t < 0)
                t += prime;
        }
    }

    return result;
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
    string baseName = filesystem::path(inputFile).filename().string();
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
