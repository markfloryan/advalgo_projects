#include <iostream>
#include <vector>
#include <map>
#include <set>
#include <algorithm>
#include <string>

using namespace std;

class BurrowsWheeler
{
public:
    vector<string> rotations(const string &t)
    {
        string tt = t + t;
        int len = t.size();
        vector<string> rots(len);
        for (int i = 0; i < len; ++i)
        {
            rots[i] = tt.substr(i, len);
        }
        return rots;
    }

    vector<string> sortedRotations(const string &t)
    {
        vector<string> rots = rotations(t);
        sort(rots.begin(), rots.end());
        return rots;
    }

    string bwtViaSortedRotations(const string &t)
    {
        string bwt = "";
        vector<string> sorted = sortedRotations(t);
        for (const string &s : sorted)
        {
            bwt += s.back();
        }
        return bwt;
    }

    pair<vector<int>, map<char, int>> rankBwt(const string &bw)
    {
        vector<int> ranks;
        map<char, int> tots;
        for (char c : bw)
        {
            int cnt = tots[c];
            ranks.push_back(cnt);
            tots[c] = cnt + 1;
        }
        return {ranks, tots};
    }

    map<char, int> firstCol(const map<char, int> &tots)
    {
        map<char, int> first;
        int totc = 0;
        for (const auto &[c, count] : tots)
        {
            first[c] = totc;
            totc += count;
        }
        return first;
    }

    string reverseBwt(const string &bw)
    {
        auto [ranks, tots] = rankBwt(bw);
        map<char, int> first = firstCol(tots);

        int rowi = 0;
        string t = "$";
        while (bw[rowi] != '$')
        {
            char c = bw[rowi];
            t = c + t;
            rowi = first[c] + ranks[rowi];
        }
        return t;
    }
};

int main()
{
    string t;
    cin >> t;
    t += "$";

    BurrowsWheeler transformer;
    string b = transformer.bwtViaSortedRotations(t);
    cout << b << endl;

    auto [ranks, tots] = transformer.rankBwt(b);
    // for (int r : ranks) cout << r << " "; cout << endl;
    // for (const auto& [c, n] : tots) cout << c << ":" << n << " "; cout << endl;

    string reversed = transformer.reverseBwt(b);
    cout << reversed.substr(0, reversed.size() - 1) << endl;

    return 0;
}
