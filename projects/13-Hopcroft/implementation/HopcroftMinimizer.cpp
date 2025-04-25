#include <bits/stdc++.h>
using namespace std;

// A class representing a Deterministic Finite Automaton (DFA)
class DFA {
public:
    // The set of all states in the DFA
    unordered_set<string> states;
    // The set of input symbols the DFA recognizes
    unordered_set<string> alphabet;
    // The transition function: state × symbol → next state
    unordered_map<string, unordered_map<string, string>> tf;
    // The single designated start state
    string start;
    // The set of accepting (final) states
    unordered_set<string> accept;

    // Default constructor
    DFA() = default;

    // Construct a DFA with given components
    DFA(const unordered_set<string>& S,
        const unordered_set<string>& A,
        const unordered_map<string, unordered_map<string, string>>& T,
        const string& st,
        const unordered_set<string>& F)
        : states(S), alphabet(A), tf(T), start(st), accept(F) {}

    // Remove any states that cannot be reached from the start
    void removeUnreachable() {
        // Track which states we can actually get to
        unordered_set<string> reachable;
        queue<string> q;
        // Begin exploring from the start state
        q.push(start);

        // Breadth-first search to find all reachable states
        while (!q.empty()) {
            string s = q.front();
            q.pop();

            // If we haven’t visited this state yet, mark it and explore its transitions
            if (!reachable.count(s)) {
                reachable.insert(s);
                for (const auto& c : alphabet) {
                    auto it = tf[s].find(c);
                    if (it != tf[s].end()) {
                        const string& t = it->second;
                        // Queue any newly discovered state for further exploration
                        if (!reachable.count(t))
                            q.push(t);
                    }
                }
            }
        }

        // Keep only reachable states and adjust accepting set accordingly
        states = reachable;
        unordered_set<string> newAccept;
        for (const auto& s : accept)
            if (reachable.count(s))
                newAccept.insert(s);
        accept = move(newAccept);

        // Remove transitions from states that are no longer present
        for (auto it = tf.begin(); it != tf.end(); ) {
            if (!states.count(it->first))
                it = tf.erase(it);
            else
                ++it;
        }
        // Within each remaining state, drop transitions to removed states
        for (auto& [s, m] : tf) {
            for (auto it = m.begin(); it != m.end(); ) {
                if (!states.count(it->second))
                    it = m.erase(it);
                else
                    ++it;
            }
        }
    }

    // Ensure every state has a defined transition for each symbol by adding a trap state
    void makeTotal() {
        const string trap = "__TRAP__";
        bool needTrap = false;

        // For each state and symbol, add a transition to trap if missing
        for (const auto& s : states) {
            for (const auto& c : alphabet) {
                if (!tf[s].count(c)) {
                    tf[s][c] = trap;
                    needTrap = true;
                }
            }
        }

        // If any transition was missing, include trap in the DFA
        if (needTrap) {
            states.insert(trap);
            for (const auto& c : alphabet)
                tf[trap][c] = trap;
        }
    }

    // Apply Hopcroft’s algorithm to minimize the DFA
    DFA minimize() const {
        // Work on a local copy so we don’t alter the original
        DFA d = *this;
        d.removeUnreachable();  // prune useless states
        d.makeTotal();          // guarantee completeness

        // Start with two groups: accepting vs non-accepting
        vector<unordered_set<string>> P;
        unordered_set<string> nonAcc;
        for (const auto& s : d.states)
            if (!d.accept.count(s)) nonAcc.insert(s);
        if (!d.accept.empty()) P.push_back(d.accept);
        if (!nonAcc.empty())    P.push_back(nonAcc);

        // W holds partitions that still need splitting
        deque<unordered_set<string>> W(P.begin(), P.end());

        // Refine until no further splits occur
        while (!W.empty()) {
            auto A = W.front();
            W.pop_front();

            // For each input symbol, find states that lead into A
            for (const auto& c : d.alphabet) {
                unordered_set<string> X;
                for (const auto& s : d.states) {
                    // If transition on c from s lands in A, mark s
                    if (A.count(d.tf.at(s).at(c)))
                        X.insert(s);
                }

                // Attempt to split each current block Y by intersection with X
                vector<unordered_set<string>> newP;
                for (auto& Y : P) {
                    unordered_set<string> inter, diff;
                    for (auto& s : Y) {
                        // Separate states inside X vs those outside
                        if (X.count(s)) inter.insert(s);
                        else diff.insert(s);
                    }

                    // If Y breaks into two non-empty parts, record the split
                    if (!inter.empty() && !diff.empty()) {
                        newP.push_back(inter);
                        newP.push_back(diff);

                        // Update pending worklist W to reflect the split
                        auto pos = find_if(W.begin(), W.end(), [&](auto& set){ return set == Y; });
                        if (pos != W.end()) {
                            W.erase(pos);
                            W.push_back(inter);
                            W.push_back(diff);
                        } else {
                            // Add the smaller piece to W for efficiency
                            if (inter.size() <= diff.size()) W.push_back(inter);
                            else                          W.push_back(diff);
                        }
                    } else {
                        // No split needed, keep Y intact
                        newP.push_back(Y);
                    }
                }
                P.swap(newP);  // apply all splits at once
            }
        }

        // Assign a numeric label to each final block, sorted by state name
        sort(P.begin(), P.end(), [](auto& a, auto& b) {
            return *min_element(a.begin(), a.end()) < *min_element(b.begin(), b.end());
        });
        unordered_map<string, string> rep;
        for (size_t i = 0; i < P.size(); ++i) {
            string label = to_string(i);
            for (auto& s : P[i]) rep[s] = label;
        }

        // Build the minimized DFA using block labels
        unordered_set<string> S2;
        for (size_t i = 0; i < P.size(); ++i)
            S2.insert(to_string(i));                // new state names
        string start2 = rep[start];                // map old start to new

        unordered_set<string> F2;
        for (auto& s : accept)
            F2.insert(rep[s]);                      // translate accept states

        unordered_map<string, unordered_map<string, string>> tf2;
        for (const auto& block : P) {
            string any = *block.begin();             // pick a representative state
            string from = rep[any];                  // its new block label
            for (const auto& c : alphabet) {
                string to = d.tf.at(any).at(c);      // see where it transitions
                tf2[from][c] = rep[to];             // map that to the new label
            }
        }

        // Return the fresh, minimized automaton
        return DFA(S2, alphabet, tf2, start2, F2);
    }
};

// Read a CSV DFA description and construct the corresponding object
DFA parseDFAFromCSV(const string& filename) {
    ifstream in(filename);
    string line;

    // Read header to discover the input alphabet columns
    getline(in, line);
    vector<string> headers;
    istringstream ss(line);
    for (string tok; getline(ss, tok, ','); )
        headers.push_back(tok);

    // The first three columns are metadata; the rest are symbol names
    vector<string> alpha(headers.begin() + 3, headers.end());
    unordered_set<string> alphabet(alpha.begin(), alpha.end());

    unordered_set<string> states;
    unordered_map<string, unordered_map<string, string>> tf;
    string start;
    unordered_set<string> accept;

    // Parse each row: state,is_start,is_accept,followed by transitions
    while (getline(in, line)) {
        istringstream ss2(line);
        vector<string> parts;
        for (string tok; getline(ss2, tok, ','); )
            parts.push_back(tok);

        string s = parts[0];
        states.insert(s);
        if (parts[1] == "1") start = s;         // mark the start state
        if (parts[2] == "1") accept.insert(s);  // mark accept states

        // Record the transition for each alphabet symbol
        for (size_t i = 3; i < parts.size(); ++i)
            tf[s][alpha[i-3]] = parts[i];
    }

    return DFA(states, alphabet, tf, start, accept);
}

// Write a DFA back out in CSV form for testing and comparison
void writeDFAtoCSV(const DFA& dfa, const string& filename) {
    ofstream out(filename);

    // Ensure a consistent column order for symbols
    vector<string> alpha(dfa.alphabet.begin(), dfa.alphabet.end());
    sort(alpha.begin(), alpha.end());

    // Output header
    out << "state,is_start,is_accept";
    for (auto& c : alpha)
        out << "," << c;
    out << "\n";

    // Write each state row in sorted order to keep tests reproducible
    vector<string> sts(dfa.states.begin(), dfa.states.end());
    sort(sts.begin(), sts.end());
    for (auto& s : sts) {
        out << s << "," << (s == dfa.start ? "1" : "0") << ","
            << (dfa.accept.count(s) ? "1" : "0");
        for (auto& c : alpha)
            out << "," << dfa.tf.at(s).at(c);
        out << "\n";
    }
}

// Compare two CSV files line by line to verify correctness
bool compareCSV(const string& f1, const string& f2) {
    ifstream a(f1), b(f2);
    string la, lb;
    while (true) {
        bool ea = !getline(a, la);
        bool eb = !getline(b, lb);
        // If both end at once, files match
        if (ea || eb) return ea == eb;
        if (la != lb) return false;  // any difference fails
    }
}

// Run a single numbered test by reading input, minimizing, and checking output
void runTest(int index) {
    string base = "io/";
    string in  = base + "sample.in."  + to_string(index);
    string out = base + "sample.out." + to_string(index);
    string gen = base + "generated.out."+ to_string(index);

    cout << "Running Test " << index << "...\n";
    DFA dfa = parseDFAFromCSV(in);
    DFA minDFA = dfa.minimize();
    writeDFAtoCSV(minDFA, gen);
    // Report success or point to mismatched files for debugging
    if (compareCSV(out, gen))
        cout << "✅ Test " << index << " passed.\n";
    else
        cout << "❌ Test " << index << " failed. Compare " << out << " vs " << gen << ".\n";
}

int main() {
    // Execute tests 1 through 3; extend range as needed
    for (int i = 1; i <= 3; ++i)
        runTest(i);
    return 0;
}
