#include <iostream>
#include <fstream>
#include <vector>

using namespace std;

const int MAX_N = 1e5;
const int MAX_Q = 1e5;
const int MAX_VAL = 1e6;

int randint(int low, int high) {
    return low + rand() % (high - low + 1);
}

int gcd_helper(int A,int B) {
    if (A == 0) return B;
    if (B == 0) return A;

    A = abs(A);
    B = abs(B);

    if (A>B) {
        return gcd_helper(B,A);
    } else {
        return gcd_helper(B % A,A);
    }
}

int range_gcd(const vector<int>& A, int l, int r) {
    int g = A[l];
    for (int i = l + 1; i <= r; ++i)
        g = gcd_helper(g, A[i]);
    return g;
}

int main(int argc, char* argv[]) {
    srand(time(0));

    if (argc < 2) {
        cerr << "Usage: " << argv[0] << " <test number>" << endl;
        return 1;
    }

    int test_num = atoi(argv[1]);
    string in_file = "./io/test.in." + to_string(test_num);
    string out_file = "./io/test.out." + to_string(test_num);

    ofstream fout(in_file);
    ofstream fout_ans(out_file);

    // For super large cases, bump to the max vals
    int N = randint(50000, 100000);         
    int Q = randint(50000, 100000);

    // generate random array
    vector<int> A(N);
    for (int& x : A) x = randint(1, MAX_VAL);

    fout << N << " " << Q << "\n";
    for (int i = 0; i < N; ++i) fout << A[i] << (i + 1 == N ? "\n" : " ");

    for (int i = 0; i < Q; ++i) {
        bool is_gcd = (rand() % 100) < 75; // 75% chance for GCD
        int l = randint(0, N - 1);
        int r = randint(l, N - 1);

        if (is_gcd) {
            fout << "GCD " << l << " " << r << "\n";
            int result = range_gcd(A, l, r);
            fout_ans << result << "\n";
        } else {
            int delta = randint(-100, 100);
            fout << "ADD " << l << " " << r << " " << delta << "\n";
            for (int j = l; j <= r; ++j)
                A[j] += delta;
        }
    }

    fout.close();
    fout_ans.close();

    cerr << "Generated test.in." << test_num << " and test.out." << test_num << "\n";
    return 0;
}
