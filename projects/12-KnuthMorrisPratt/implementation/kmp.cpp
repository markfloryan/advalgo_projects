#include <iostream>
#include <vector>
#include <string>

using namespace std;

void constructLps(const string &pat, vector<int> &lps) { // construct longest prefux suffix
    lps[0] = 0; // first element is always 0
    int j = 0; // length of current longest prefix suffix

    for (int i = 1; i < pat.length(); i++) { // fill up lps[]
        j = lps[i - 1]; // reset j based on the previous lps value
        while (j > 0 && pat[i] != pat[j]) {// fall back to shorter prefix until a match is found or j == 0 
            j = lps[j - 1];
        }
        if (pat[i] == pat[j]) {  // if characters match, increase the length of current lps
            j++;
        }
        lps[i] = j;
    }
}

vector<int> search(const string &pat, const string &txt) {
    int m = pat.length(); // pattern length
    int n = txt.length(); // text length

    vector<int> lps(m, 0);
    vector<int> res;

    constructLps(pat, lps); // preprocess pattern to build lps array

    int i = 0, j = 0;

    while (i < n) { // loop through the text
        if (txt[i] == pat[j]) { // move both pointers if characters match
            i++;
            j++;

            if (j == m) { // store index if pattern match
                res.push_back(i - j);
                j = lps[j - 1];
            }
        } else {
            if (j != 0) { // jump to previous possible match using lps
                j = lps[j - 1];
            } else {
                i++;
            }
        }
    }
    return res;
}

int main() {
    // Take input and process it
    string txt, pat;
    cin >> txt;
    cin >> pat;

    vector<int> res = search(pat, txt);

    for (int idx : res) {
        cout << idx << " ";
    }
    cout << endl;
    return 0;
}
