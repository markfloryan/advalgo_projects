#include <iostream>
#include <vector>
#include <string>
#include <unordered_set>
#include <algorithm>

using namespace std;

class TextSearcher { // Class to search for a substring in a streaming fashion using KMP algorithm
public:
    TextSearcher(const string& substr) : substr(substr), i(0), j(0), match_found(false) {
        construct_lps(substr);
    }

    void read_character(char c) {
        if (j < substr.size() && c == substr[j]) { // If current character matches the substring
            j++;
            if (j == substr.length()) { // Full match found
                j = lps[j - 1]; // Reset j using LPS array for next potential match
                match_found = true;
            } else {
                match_found = false;
            }
        } else {
            match_found = false;
            // If character doesn't match, move `j` back using the LPS array
            while (j > 0 && (j >= substr.size() || c != substr[j])) {
                j = lps[j - 1];
            }
            // If partial match found again
            if (j < substr.size() && c == substr[j]) {
                j++;
            }
        }
        i++;
    }

    bool just_found_match() {// Returns true if a match was just found
        return match_found; 
    }

private:
string substr;        // Substring to search for
vector<int> lps;      // LPS array used in KMP algorithm
int i, j;             // i is total characters read, j is current match length
bool match_found;     // Flag to indicate if a match has just occurred

    void construct_lps(const string& substr) { // Builds the LPS array (preprocessing step of KMP algorithm)
        lps = vector<int>(substr.length(), 0);
        int j = 0;
        for (int i = 1; i < substr.length(); i++) {
            while (j > 0 && substr[i] != substr[j]) {
                j = lps[j - 1];
            }
            if (substr[i] == substr[j]) {
                j++;
            }
            lps[i] = j;
        }
    }
};

class GeneralSearcher { // Class to manage the search process over the full text
public:
    GeneralSearcher(const string& text, const vector<string>& jewels, const vector<string>& bombs)
        : text(text), jewel_objects(), bomb_objects() {
        for (const auto& jewel : jewels) {
            jewel_objects.emplace_back(jewel);
        }
        for (const auto& bomb : bombs) {
            bomb_objects.emplace_back(bomb);
        }
    }
    // Returns the set of valid mine indices (jewel but not bomb)
    unordered_set<int> get_indices() {
        unordered_set<int> results;
        int mine_index = 0;
        int i = 0;

        while (i < text.length()) {
            char c = text[i];

            if (c == ' ') { // Move to the next mine on space
                mine_index++;
                i++;
            } else {
                // Feed character to all jewel searchers
                for (auto& jo : jewel_objects) {
                    jo.read_character(c);
                    if (jo.just_found_match()) {
                        results.insert(mine_index); // Store index if jewel match is found
                    }
                }
                // Feed character to all bomb searchers
                for (auto& bo : bomb_objects) {
                    bo.read_character(c);
                    if (bo.just_found_match()) {
                        results.erase(mine_index); // Remove index if bomb match is found
                        // Skip remaining characters in the current mine
                        while (i < text.length() && text[i] != ' ') {
                            i++;
                        }
                        mine_index++;
                    }
                }
                i++;
            }
        }

        return results;
    }

private:
    string text;
    vector<TextSearcher> jewel_objects;
    vector<TextSearcher> bomb_objects;
};

int main() {
    int jewel_count, bomb_count;
    // Read jewels
    cin >> jewel_count;
    cin.ignore(); // clear newline
    vector<string> jewels(jewel_count);
    for (int i = 0; i < jewel_count; ++i) {
        cin >> jewels[i];
    }

    // Read bombs
    cin >> bomb_count;
    cin.ignore(); // clear newline
    vector<string> bombs(bomb_count);
    for (int i = 0; i < bomb_count; ++i) {
        cin >> bombs[i];
    }

    // Read in the text
    cin.ignore();
    string text;
    getline(cin, text);

    GeneralSearcher gs(text, jewels, bombs);
    unordered_set<int> indices = gs.get_indices();

    vector<int> sorted_indices(indices.begin(), indices.end());
    sort(sorted_indices.begin(), sorted_indices.end());
    for (int index : sorted_indices) {
        cout << index << endl;
    }

    return 0;
}
