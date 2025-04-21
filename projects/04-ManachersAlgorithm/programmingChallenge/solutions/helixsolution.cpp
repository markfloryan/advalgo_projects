#include <iostream>
#include <string>
#include <vector>
#include <algorithm>
using namespace std;

// main idea: Normally, manacher's algo can guarantee that the left half of a palindrome is the same as the right half of a palindrome (after the palindrome is checked at a given center). The values on the right half of the center are also at least as much as either the min(right bound (current value to the right bound) or the left half's corresponding value). However, an issue arises with the center value when performing a matching palindrome instead of a normal one. Manacher's algo can guarantee that the left side of the array matches with the right side at a given center. However, the center value of the current is not fully checked in this scenario and some issues may arise that normally don't happen.

// The center may be a part of some other palindrome from the left side, and it may be a match with the corresponding values from left half's palindromes (with respect to each left half's palindromes center) , however, it MUST match with the corresponding values in right hand sided palindromes in order to use the saved results from the left side, and this may not always happen. So, at an index i, and a center c, you must check if the value at 2i - c(corresponding letter in palindrome with index in the center of the palindrome) matches with the value at center c. If it does, you may fully use the saved value, but if it doesn't and if the saved value exceeds 2i-c, you can only use 2i - c- 1 of the saved value as every value before the corresponding value of the center is guaranteed to match, but the center's corresponding value does not. 



// checks if two characters are a match
inline bool matches(char a, char b) {
    if (a == 'A' && b == 'T') return true;
    if (a == 'T' && b == 'A') return true;
    if (a == 'C' && b == 'G') return true;
    if (a == 'G' && b == 'C') return true;
    return a == '#' && b == '#';
}

// Insert '#' between every character and at both ends
string preprocess(const string &s) {
    string t;
    t.reserve(s.size() * 2 + 1);
    t.push_back('#');
    for (char c : s) {
        t.push_back(c);
        t.push_back('#');
    }
    return t;
}

// Remove all '#' characters
string postprocess(const string &s) {
    string t;
    t.reserve(s.size());
    for (char c : s) {
        if (c != '#') t.push_back(c);
    }
    return t;
}

string helix(const string &input) {
    string s = preprocess(input);
    int n = s.size();
    vector<int> radius(n, 0);
    int center = 0, right = 0;

    for (int i = 1; i < n - 1; ++i) {
        // calculate mirroed index with respect to center
        int mirror = 2 * center - i;

        if (i < right) {
            //calculate center position with respect to i and finds the corresponding mirrored position of the center with respect to i
            int dist = i - center;
            int centerMirror = i + dist;
            if (centerMirror < n && matches(s[centerMirror], s[center])) {
                // Safe to inherit full mirror radius
                radius[i] = min(right - i, radius[mirror]);
            } else {
                // Can only inherit up to just before center
                radius[i] = min({ right - i, dist - 1, radius[mirror] });
            }
        }

        // Expand palindrome centered at i
        while (i + radius[i] + 1 < n
            && i - (radius[i] + 1) >= 0
            && matches(s[i + radius[i] + 1], s[i - (radius[i] + 1)]))
        {
            radius[i]++;
        }

        // Update center and right
        if (i + radius[i] > right) {
            center = i;
            right  = i + radius[i];
        }
    }

    // Find the maximum radius and its center
    int maxR = 0, maxC = 0;
    for (int i = 0; i < n; ++i) {
        if (radius[i] > maxR) {
            maxR = radius[i];
            maxC = i;
        }
    }

    // Extract the substring in the transformed string
    int start = maxC - maxR;
    int length = maxR * 2 + 1;
    string candidate = s.substr(start, length);

    // Remove '#' to recover the original DNA palindrome
    return postprocess(candidate);
}

int main() {
    ios::sync_with_stdio(false);
    cin.tie(nullptr);

    string dna;
    if (!getline(cin, dna)) return 0;
    cout << helix(dna) << "\n";
    return 0;
}
