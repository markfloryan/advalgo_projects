#include <iostream>
#include <string>
#include <vector>
#include <algorithm>

/**
 * Manacher's algorithm exploits the idea that there are mirrored palindromes within a larger palindrome.
 *
 * We have three cases:
 *
 * Case 1. If the palindrome at the current center falls completely within the right and left boundaries
 * when calculated with the mirrored index, we have found the palindrome for that particular point.
 *
 * Case 2. If the palindrome at the current center reaches exactly to the right boundary
 * when calculated with the mirrored index, we have to manually expand the palindrome beyond the right boundary.
 *
 * Case 3. If the palindrome at the current center falls outside the right boundary
 * when calculated with the mirrored index, we have to reduce the radius to fit within the right boundary.
 * After that, manually expand the palindrome.
 *
 * Any time we have the current index past the right boundary, we manually expand a palindrome.
 */
std::string manacher(const std::string& s) {
    // Preprocess the string with '#' in between characters to find even-length palindromes
    std::string t = "#";
    for (char c : s) {
        t += c;
        t += '#';
    }

    int n = t.length();

    // Array to store palindrome radii
    std::vector<int> p(n, 0);

    // Center of old rightmost palindrome
    int center = 0;

    // Right boundary of the rightmost palindrome
    int right = 0;

    // Length of the longest palindrome found so far
    int maxLen = 0;

    // Center index of the longest palindrome found
    int centerIndex = 0;

    // Iterate through all indices
    for (int i = 1; i < n - 1; i++) {
        // Find the mirrored index reflected across old center
        int mirror = 2 * center - i;

        // Case 1 and 3
        if (i < right) {
            p[i] = std::min(right - i, p[mirror]);
        }

        // Case 2 and 3
        while (i + (1 + p[i]) < n && i - (1 + p[i]) >= 0 && t[i + (1 + p[i])] == t[i - (1 + p[i])]) {
            p[i]++;
        }

        // In case 2 and 3, if we expand beyond the right boundary, update old center and right
        if (i + p[i] > right) {
            center = i;
            right = i + p[i];
        }

        // Track the longest palindrome
        if (p[i] > maxLen) {
            maxLen = p[i];
            centerIndex = i;
        }
    }

    // Extract the longest palindrome from the original string
    int start = (centerIndex - maxLen) / 2;
    return s.substr(start, maxLen);
}

int main() {
    std::string inputString;
    
    std::cout << "Enter the string:" << std::endl;
    while (std::getline(std::cin, inputString)) {
        if (inputString.empty()) {
            break;
        }
        
        std::string result = manacher(inputString);
        std::cout << "Longest palindromic substring: " << result << std::endl;
    }
    
    return 0;
}
