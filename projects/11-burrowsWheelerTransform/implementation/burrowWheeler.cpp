/*
 * Adapted from https://nbviewer.org/github/BenLangmead/comp-genomics-class/blob/master/notebooks/CG_BWT_Reverse.ipynb
 */
#include <iostream>
#include <vector>
#include <map>
#include <set>
#include <algorithm>
#include <string>

// simplifies calls to std functions such as std::cin and std::cout, used for input and printing
using namespace std;

class BurrowsWheeler
{
public:
    /*
     * Takes in input string t
     * Returns list of cyclical rotations of original string (including $) of length len
     * Done by doubling original string and getting each set of consecutive len characters
     */
    vector<string> rotations(const string &t){
        // double original string
        string tt = t + t;
        int len = t.size();
        vector<string> rots(len);
        // add each rotation to vector, derived by getting consecutive set of len characters starting
        //  with the start of the string and continuing until the last character of the initial string
        for (int i = 0; i < len; ++i){
            // len characters from tt, the doubled string, starting with i, get added to the rots vector
            rots[i] = tt.substr(i, len);
        }
        return rots;
    }

    /*
     * Takes in input string t
     * Returns list of rotations of strings ordered through string comparison (lower value characters first)
     * Not case sensitive in this case, and the '$' will show up first
     */
    vector<string> sortedRotations(const string &t){
        // Get rots from rotations method
        vector<string> rots = rotations(t);
        // sorts all elements from the beginning to the end of the rots vector (all elements)
        // in-place sort, meaning rots is modified
        sort(rots.begin(), rots.end());
        return rots;
    }

    /*
     * Takes in input string t
     * Returns the Burrows-Wheeler transform (BWT) by utilizing the sorted rotations
     * Through a loop through the sorted rotations, the last character of each rotation is added
     * The resulting concatenation of all the last characters is returned
     */
    string bwtViaSortedRotations(const string &t){
        // initializes return string, bwt
        string bwt = "";
        // gets all the sorted rotations for input, t
        vector<string> sorted = sortedRotations(t);
        // for each rotation string s, add the last character of the string (s.back()) to output, bwt
        for (const string &s : sorted){
            bwt += s.back();
        }
        return bwt;
    }

    /*
     * Given an input, bw, which is presumed to be a valid BWT
     * Returns a pair containing ranks and tots
     *  ranks: a vector corresponding to the BWT string that indicates how many times
     *         each character has appeared in the BWT string before
     *  tots: a map mapping each unique character in the BWT string
*    *        to how many times it appears in the BWT string
     */
    pair<vector<int>, map<char, int>> rankBwt(const string &bw){
        // initialize return values, tots and ranks
        vector<int> ranks;
        map<char, int> tots;
        // iterate through each character, c, in the string, bw
        for (char c : bw){
            // for a given character, add the rank as how many times it has appeared previously (tots[c])
            // if a character has not appeared previously, tots[c] is inserted into the map as 0 by default
            int cnt = tots[c];
            ranks.push_back(cnt);
            // add 1 to tots.get(c) (called as totsC) since this is a new appearance of c
            // can be used later if c appears again
            tots[c] = cnt + 1;
        }
        // return pair with ranks and tots
        return {ranks, tots};
    }

    /*
     * Given a map 'tots' mapping characters to their total appearances in a BWT string
     *  a map 'first' is returned mapping characters to the first row in a list of sorted
     *  rotations that they would start off (prefix)
     */
    map<char, int> firstCol(const map<char, int> &tots){
        map<char, int> first;
        // Sets totc initially to 0, since the first character must appear at index 0
        int totc = 0;
        // iterate through map (automatically in sorted order by key, c, by nature of C++ maps)
        // count represents how many times c appears in the BWT
        for (const auto &[c, count] : tots){
            // Sets first[c] to totc, which is the index the current char, c, should first prefix 
            //  in sorted rotations
            first[c] = totc;
            // Adds count to totc, which indicates that the next character should appear after all
            //  instances of the current character, which spans [totc, totc + (count - 1)]
            totc += count;
        }
        return first;
    }

    /*
     * Used to get the final inverse of a BWT string, getting it back to the original string
     * Takes in BWT string, bw, as input and returns original string, t, as output
     */
    string reverseBwt(const string &bw){
        // Make sure to have rank information for each character as well as total instances of each
        // Done by automatically assigning ranks, tots to output of rankBwt, which gives pair returning
        //  ranks, tots for a BWT, bw
        auto [ranks, tots] = rankBwt(bw);
        // Use tots, total instances of chars to determine first instance of prefixing in sorted rotations
        map<char, int> first = firstCol(tots);

        // Start at the first row of the sorted rotations, 0, by setting rowi to 0
        int rowi = 0;
        // Set t to the special character '$' indicating the end of the string
        string t = "$";
        /*
         * BWT reversal relies on the last characters of the sorted strings being the BWT characters
         * Thus, indexing into the BWT at 0 indicates the character at the end of the string starting with '$'
         * This character is also the end of the original string, since it is cyclically before '$'
         * Thus, for each current char, the corresponding BWT char is the previous char in the string
         * The string is built up by prepending each previous character until the cycle ends by reaching
         *   the last character '$'
         */
        while (bw[rowi] != '$'){
            // c is the last character at rowi in the sorted rotations, indexed by bw[rowi]
            char c = bw[rowi];
            // prepends c before the output string t
            // c must come before the starting character of the sorted rotation
            t = c + t;
            // rowi is set to the location of c in the sorted rotations
            // first[c] indicates the first location at which c prefixes the sorted rotations
            // ranks[rowi] indicates how many instances of c have prefixed previous sorted rotations
            rowi = first[c] + ranks[rowi];
        }
        return t;
    }
};

int main(){
    /*
     * Reads in input string into t to later be transformed, adding '$' to indicate end of string
     */
    string t;
    cin >> t;
    t += "$";

    /*
     * Initializes instance of BurrowsWheeler object, uses it to run Burrows Wheeler Transform (BWT)
     *  using the sorted rotations method
     */
    BurrowsWheeler transformer;
    string b = transformer.bwtViaSortedRotations(t);
    // prints out output, b, which represents the BWT of
    cout << b << endl;

    /*
     * Given a valid BWT, b, returns the inverse transform, the original string being transformed
     * and assigns it to reverse
     * reverse is printed without the last character, as the last character will be '$', which is
     * not part of the original string and had been added to keep track of the end of the string
     */
    string reversed = transformer.reverseBwt(b);
    cout << reversed.substr(0, reversed.size() - 1) << endl;

    return 0;
}
