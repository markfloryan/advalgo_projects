#include <iostream>
#include <fstream>
#include <vector>
#include <string>
#include <filesystem>
#include <sstream>
#include <algorithm>

using namespace std;

// Helper function for modular exponentiation
long long modPow(long long base, long long exp, long long mod) {
    long long result = 1;
    base = base % mod;
    while (exp > 0) {
        if (exp & 1) {
            result = (result * base) % mod;
        }
        base = (base * base) % mod;
        exp >>= 1;
    }
    return result;
}

// Function to count palindromic substrings
int countPalindromicSubstrings(const string& s) {
    // Reverse the string for comparison later
    string r = string(s.rbegin(), s.rend());

    int base = 26;
    int q = 10007;
    int ans = 0;

    // Iterate over all possible starting indices of substrings
    for (int i = 0; i < s.length(); i++) {
        // Initialize hash values for original and reversed substrings
        long long originalHash = 0;
        long long reversedHash = 0;

        // Iterate over all possible ending indices of substrings
        for (int j = i; j < s.length(); j++) {
            // Convert current character to a number based on unicode normalized by 'a'
            int currentCharValue = s[j] - 'a' + 1;

            // Update rolling hash for original string, we are adding a character to the end so left shift and make space for lower order bit to be added
            originalHash = (originalHash * base + currentCharValue) % q;

            // Compute position of corresponding character in the reversed string
            int reverseCharIndex = r.length() - j - 1;
            int reverseCharValue = r[reverseCharIndex] - 'a' + 1;

            // For reversed hash the new character added is at the beginning, so we need to set/add higher order bit to hash
            reversedHash = (reversedHash + reverseCharValue * modPow(base, j - i, q)) % q;

            // If hashes match, we might have a palindrome.
            if (originalHash == reversedHash) {
                // Confirm it is actually a palindrome by manually checking, avoid false positives and spurious hits
                string substring = s.substr(i, j - i + 1);
                string reversedSubstring = string(substring.rbegin(), substring.rend());
                if (substring == reversedSubstring) {
                    ans++;
                }
            }
        }
    }

    // Final count of palindromic substrings
    return ans;
}

// Parse expected output from file (first number in file)
int parseOutput(const string& filepath) {
    ifstream file(filepath);
    if (!file.is_open()) return -1;

    string line;
    getline(file, line);
    stringstream ss(line);
    string token;
    ss >> token;
    return stoi(token);
}

int main(int argc, char* argv[]) {
    if (argc < 2) {
        cout << "Usage: ./pcSol_cpp.out ../io/test.in.#" << endl;
        return 1;
    }

    string inputFile = argv[1];
    string expectedOutputFile = inputFile;
    size_t pos = expectedOutputFile.find("in");
    if (pos != string::npos) {
        expectedOutputFile.replace(pos, 2, "out");
    }

    ifstream infile(inputFile);
    if (!infile.is_open()) {
        cerr << "Failed to open input file." << endl;
        return 1;
    }

    string inputString;
    getline(infile, inputString);
    infile.close();

    // Run the algorithm
    int actualOutput = countPalindromicSubstrings(inputString);

    // Try to read expected output
    int expectedOutput = -1;
    try {
        expectedOutput = parseOutput(expectedOutputFile);
    } catch (...) {
        cout << "Expected output file not found or invalid. Skipping comparison." << endl;
    }

    // Print results
    if (expectedOutput != -1) {
        cout << "Expected Output: " << expectedOutput << endl;
    }
    cout << "Actual Output:   " << actualOutput << endl;

    if (expectedOutput != -1) {
        cout << "Test Passed:     " << (actualOutput == expectedOutput ? "true" : "false") << endl;
    }

    return 0;
}
