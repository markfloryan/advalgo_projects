#include <bits/stdc++.h>

#define endln '\n'
#define print(x) cout << (x) << endln

using namespace std;
using ll = long long;

const int INF = INT_MAX - 1;
const ll LINF = LLONG_MAX - 1;

pair<vector<int>, map<int, int>> rankBwt(vector<int> &bw)
{
    /*
     * Given a BWT-transformed array bw, construct:
     * 1. ranks: for each character, how many times it has appeared so far.
     * 2. tots: the total count of each character in the array.
     * This data is essential for reversing the BWT later using LF-mapping.
     */
    map<int, int> tots;
    vector<int> ranks;

    for (int val : bw)
    {
        ranks.push_back(tots[val]); // assign the current count as rank
        tots[val]++;                // update the total count for the value
    }
    return {ranks, tots};
}

map<int, pair<int, int>> firstCol(map<int, int> &tots)
{
    /*
     * Given a frequency map tots of character counts,
     * compute the range of row indices (in the first column of the BWT matrix) that each character occupies. This helps simulate sorting without building the full matrix explicitly.
     */
    map<int, pair<int, int>> first;
    int totc = 0;
    for (const auto &[val, count] : tots)
    {
        first[val] = {totc, totc + count}; // assign start and end index range
        totc += count;                     // increment total character count seen so far
    }
    return first;
}

vector<int> reverseBwt(vector<int> &bw)
{
    /*
     * Reverse the Burrows-Wheeler Transform using LF-mapping
     * Start from the row with sentinel (-1), and repeatedly map backwards through the BWT matrix until the original string is rebuilt in reverse.
     */
    pair<vector<int>, map<int, int>> rt = rankBwt(bw);
    vector<int> ranks = rt.first;
    map<int, int> tots = rt.second;
    map<int, pair<int, int>> first = firstCol(tots);
    int rowi = 0; // start at row 0 where sentinel is assumed to be
    int sentinel = -1;
    vector<int> t = {-1}; // initialize the output with the sentinel
    while (bw[rowi] != sentinel)
    {
        int c = bw[rowi];
        t.push_back(c);                      // add character to result
        rowi = first[c].first + ranks[rowi]; // jump to previous row using LF-mapping
    }
    reverse(t.begin(), t.end()); // reverse to restore original order
    return t;
}

void solve()
{
    int n, k;
    cin >> n >> k;
    vector<int> arr(n);
    for (int i = 0; i < n; i++)
    {
        cin >> arr[i]; // input the BWT-transformed array
    }
    set<int> elements;
    for (int i = 0; i + 1 < n; i++)
    {
        if (arr[i] > 0)
        {
            elements.insert(arr[i]); // collect all nonzero characters (excluding sentinel and placeholder)
        }
    }
    int last = 0;
    vector<pair<int, int>> gaps;
    /*
     * From the sorted elements, get consecutive elements, and if there is a gap,
     * add it to our gaps array
     */
    for (int i : elements)
    {
        if (last + 1 <= i - 1)
        {
            // add 1 to the left, and subtract 1 to right to just get elements in between
            gaps.push_back({last + 1, i - 1});
        }
        gaps.push_back({i, i}); // # also make sure to check the elements in the actual array
        last = i;
    }
    int mx = 0;
    if (elements.size())
    {
        mx = *elements.rbegin();
    }
    if (k > mx)
    {
        // make sure to add a gap between largest element and K
        gaps.push_back({mx + 1, k});
    }
    int zero = find(arr.begin(), arr.end(), 0) - arr.begin();
    /*
     * For each candidate value range in gaps:
     * Replace the zero with a trial value and try to reverse the BWT.
     * If the reverse operation succeeds (i.e., length matches), add the full gap size to answer.
     */
    int ans = 0;
    for (pair<int, int> item : gaps)
    {
        arr[zero] = item.first;
        vector<int> original = reverseBwt(arr);
        if (original.size() == arr.size())
        {
            // the original array is valid if there are no missing elements
            ans += item.second - item.first + 1; // all values in this range are valid
        }
    }
    print(ans);
}

signed main()
{
    ios_base::sync_with_stdio(false);
    cin.tie(NULL);
    solve();
}
