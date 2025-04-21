#include <iostream>
#include <vector>
#include <cmath>
#include <algorithm>
#include <map>

using namespace std;

// returns a number equal to or slighly less than a power of 2 between 32 and 64
struct Ship {
    int x, y, index;
    double r;
    string name;

    //constructor with x pos, y pos, name of ship, and order
    Ship(int x, int y, string name, int index) {
        this->x = x;
        this->y = y;
        this->name = name;
        this->index = index;
        this->r = sqrt(x * x + y * y);
    }
};

// Define a custom comparator for Ship objects
struct ShipComparator {
    bool operator()(const Ship& a, const Ship& b) const {
        // Compare ships by their 'r' values first
        if (a.r != b.r) return a.r < b.r;
        // If 'r' values are equal, compare by 'x'
        if (a.x != b.x) return a.x < b.x;
        // If 'x' values are equal, compare by 'y'
        return a.y < b.y;
    }
};

// returns a number equal to or slighly less than a power of 2 between 32 and 64
int calculateMinRun(int n) {
    int r = 0;
    while (n >= 32) {
        r |= n & 1; // record if a bit will be shifted off
        n >>= 1;    // divide n by 2
    }
    return n + r;
}

// gallop method used to quickly find where x belongs in a sorted array 
int gallop(const Ship& x, vector<Ship>& arr, int start, bool (*key)(const Ship&, const Ship&)) {
    int hi = 1;
    int n = arr.size();
    // exponential search: multilpy hi by 2 until its idx is >= x
    // the current element in the "winning" or greater list or the end of the list is reached
    while (start + hi < n && key(x, arr[start + hi])) {
        hi *= 2;
    }
    // binary search between the second to last element checked and the element selected to pinpoint
    // where x belongs
    int lo = hi / 2;
    hi = min(start + hi, n);
    while (lo < hi) {
        int mid = (lo + hi) / 2;
        if (key(x, arr[mid])) {
            lo = mid + 1;
        } else {
            hi = mid;
        }
    }
    return lo; // x's sorted position
}

// augmented to also detect for descending runs and reverse them in place 
void insertionSort(vector<Ship>& arr, int left, int right, bool (*key)(const Ship&, const Ship&)) {
    // check for descending run if the first two elements are out of order
    if (left < right && key(arr[left + 1], arr[left])) {
        bool isDesc = true;
        for (int k = left; k < right; k++) {
            if (!key(arr[k], arr[k + 1])) {
                // found an increasing segment so run is not descending
                isDesc = false;
                break;
            }
        }
        if (isDesc) {
            // reverse in-place
            reverse(arr.begin() + left, arr.begin() + right + 1);
            return; // sorted run
        }
    }

    // if run is not descending use standard insertionsort implementation
    for (int i = left + 1; i <= right; i++) {
        Ship temp = arr[i];
        int j = i - 1;
        while (j >= left && key(temp, arr[j])) {
            arr[j + 1] = arr[j];
            j--;
        }
        arr[j + 1] = temp;
    }
}

// combine two sorted subarrays [left,mid] and [mid+1,right]
// track consecutive wins to trigger galloping mode to reduce the number of individual comparisons
void merge(vector<Ship>& arr, int left, int mid, int right, bool (*key)(const Ship&, const Ship&)) {
    vector<Ship> leftPart(arr.begin() + left, arr.begin() + mid + 1);
    vector<Ship> rightPart(arr.begin() + mid + 1, arr.begin() + right + 1);

    int i = 0, j = 0, k = left;
    int minGallop = 7;
    int countLeft = 0, countRight = 0;

    // core merge logic
    while (i < leftPart.size() && j < rightPart.size()) {
        if (key(leftPart[i], rightPart[j])) {
            arr[k++] = leftPart[i++];
            countLeft++;    // keeping track of left subarray "wins"
            countRight = 0;
        } else {
            arr[k++] = rightPart[j++];
            countRight++;   // keeping track of right subarray "wins"
            countLeft = 0;
        }

        // one side wins individual comparisons more than min_gallop times
        // if left run is smaller call gallop to find the index of the right one where x fits
        if (countLeft >= minGallop) {
            int pos = gallop(rightPart[j], leftPart, i, key);
            // copy the part of array that was skipped over
            while (i < pos) arr[k++] = leftPart[i++];
            countLeft = 0;
        // if right run is smaller call gallop to find the index of the left one where x fits
        } else if (countRight >= minGallop) {
            int pos = gallop(leftPart[i], rightPart, j, key);
            // copy the part of array that was skipped over
            while (j < pos) arr[k++] = rightPart[j++];
            countRight = 0;  // reset counter
        }
    }

    // copy over any leftover elements, only 1 will run, the other is already done
    while (i < leftPart.size()) arr[k++] = leftPart[i++];
    while (j < rightPart.size()) arr[k++] = rightPart[j++];
}

void mergeCollapse(vector<Ship>& arr, vector<vector<int>>& runStack, bool (*key)(const Ship&, const Ship&)) {
// merge runs at a location on the stack
    while (runStack.size() > 2) {
        // get the length of the first three runs on the stack
        // C is at the top of the stack, A is closest to the bottom
        vector<int> A = runStack[runStack.size() - 3];
        vector<int> B = runStack[runStack.size() - 2];
        vector<int> C = runStack[runStack.size() - 1];
        int lenA = A[1] - A[0] + 1;
        int lenB = B[1] - B[0] + 1;
        int lenC = C[1] - C[0] + 1;

        // per merge policy: 
        // A should be larger than B + C
        // B should be larger than C
        if (lenA <= lenB + lenC || lenB <= lenC) {
            if (lenA < lenC) {
                merge(arr, A[0], A[1], C[1], key);
            } else {
                merge(arr, B[0], B[1], C[1], key);
            }
        } else {
            // merge policy holds, no merges necessary
            break;
        }
    }

    // merge when there are 2 runs left on the stack
    if (runStack.size() == 2) {
        vector<int> A = runStack[0];
        vector<int> B = runStack[1];
        if (A[1] - A[0] + 1 <= B[1] - B[0] + 1) {
            merge(arr, A[0], A[1], B[1], key);
        }
    }
}

void mergeAt(vector<Ship>& arr, vector<vector<int>>& runStack, int i, bool (*key)(const Ship&, const Ship&)) {
    // copy subsections
    vector<int> run1 = runStack[i];
    vector<int> run2 = runStack[i + 1];
    merge(arr, run1[0], run1[1], run2[1], key);
    // update stack
    run1[1] = run2[1];
    runStack.erase(runStack.begin() + i + 1);
}

//polar angle comparator for "twist"
bool polarAngleComparator(const Ship& a, const Ship& b) {
    // Check Above/Below Lighthouse
    bool ha = a.y > 0 || (a.y == 0 && a.x >= 0);
    bool hb = b.y > 0 || (b.y == 0 && b.x >= 0);

    if (ha != hb) return ha ? true : false;

    // Cross product for angle
    long long cross = (long long) a.x * b.y - (long long) a.y * b.x;
    if (cross != 0) return cross > 0;

    // Sort by radius
    if (a.r != b.r) return a.r < b.r;

    // Sort by order of appearance (index)
    return a.index < b.index;
}

// split the array into runs, sort each, then merge according to policy
void timsort(vector<Ship>& arr, bool (*key)(const Ship&, const Ship&)) {
    // initialize variables
    int n = arr.size();
    int minRun = calculateMinRun(n);
    vector<vector<int>> runStack;

    // first pass: split into runs and call the augmented insertionsort
    int i = 0;
    while (i < n) {
        // end of current run is either i + minRun (offset) or the end of the list
        int runEnd = min(i + minRun - 1, n - 1);
        insertionSort(arr, i, runEnd, key);
        runStack.push_back({i, runEnd});   // push the run to the stack
        i = runEnd + 1;     // update offset
        mergeCollapse(arr, runStack, key);
    }

// merge the runs on the stack according to the merge policy until there is 1 run on the stack
    while (runStack.size() > 1) {
        mergeAt(arr, runStack, runStack.size() - 2, key);
    }
}

int main() {
    int numShips, numQueries;
    // read from stdin
    cin >> numShips >> numQueries;

    map<Ship, string, ShipComparator> shipMap;
    vector<Ship> shipsSorted;

    //populate graph
    for (int i = 0; i < numShips; i++) {
        int x, y;
        string name;
        cin >> x >> y >> name;
        Ship s(x, y, name, i);
        shipsSorted.push_back(s);
        shipMap[s] = name;
    }

    //sort ships based on polar angle
    timsort(shipsSorted, polarAngleComparator);

    //output sorted ships for query
    for (int i = 0; i < numQueries; i++) {
        double radius;
        cin >> radius;
        vector<string> queried;
        for (Ship s : shipsSorted) {
            if (s.r <= radius) queried.push_back(s.name);
        }
        if (queried.empty()) cout << -1 << endl;
        else {
            for (size_t j = 0; j < queried.size(); j++) {
                cout << queried[j];
                if (j < queried.size() - 1) cout << " ";
            }
            cout << endl;
        }
    }

    return 0;
}
