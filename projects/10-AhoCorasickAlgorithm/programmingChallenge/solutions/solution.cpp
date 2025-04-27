/**
 * SOURCES CITED
 *
 * Wikipedia, "Aho-Corasick algorithm": https://en.wikipedia.org/wiki/Aho%E2%80%93Corasick_algorithm
 * Stanford, "Aho-Corasick Automata": https://web.stanford.edu/class/archive/cs/cs166/cs166.1166/lectures/02/Slides02.pdf
 * Algorithms for Competitive Programming, "Aho-Corasick algorithm": https://cp-algorithms.com/string/aho_corasick.html
 * Bertrand Meyer, "Incremental String Matching": https://se.inf.ethz.ch/~meyer/publications/string/string_matching.pdf
 *
 * FOR MORE IMPLEMENTATION DETAILS, CHECK /implementation/implementation.cpp
 */

#include <iostream>
#include <vector>
#include <bits/stdc++.h>
using namespace std;

const int UNSET = -2;
const int NONE = -1;
const int ROOT = 0;

const int ALPHABET_SIZE = 26;

struct Vertex
{
public:
    // Defines whether the current node represents a valid search string.
    bool accept = false;

    // Stores the indices of this node's direct children in the trie.
    // The special value NONE means that the specified child does not exist.
    int children[ALPHABET_SIZE];

    // Stores values related to finding suffix links.
    int parent;
    char lastChar;

    // The suffix link represents the node with the largest matching suffix.
    // For example, the node representing 'ab' would point to 'b', if it exists, or the root node.
    // See AhoCorasickTrie.setSuffixLink() for more information.
    int suffixLink = UNSET;

    // Stores the most recent link in the suffix chain that accepts, or NONE if none exists.
    // Result links are used to match multiple patterns simultaneously (e.g. 'abc' and 'bc')
    // Result links are also used to match partial prefixes (e.g. 'ab' should match during 'laboratory')
    int resultLink = NONE;

    // The inverse suffix tree is used to re-calculate suffix links when new strings are added.
    // All nodes with suffix links pointing to this node are stored here.
    // Interestingly, this tree structure is akin to the trie, except with suffixes instead of prefixes.
    vector<int> inverseSuffixes;

    // The default values correspond to the root node
    Vertex(int parent = NONE, char character = '$')
    {
        this->parent = parent;
        this->lastChar = character;
        fill(begin(children), end(children), NONE);
    }

private:
    friend std::ostream &operator<<(std::ostream &, const Vertex &);
};

std::ostream &operator<<(std::ostream &strm, const Vertex &v)
{
    strm << "Vertex(\n"
         << "\t" << (v.accept ? "Accept" : "Reject") << ", \n"
         << "\tParent: " << v.parent << ", \n"
         << "\tCharacter: " << v.lastChar << ", \n"
         << "\tSuffix Link: " << v.suffixLink << ", \n"
         << "\tResult Link: " << v.resultLink << ", \n";

    strm << "\tChildren: [";
    for (int i = 0; i < ALPHABET_SIZE; i++)
    {
        if (v.children[i] != NONE)
            strm << (char)(i + 'a') << ", ";
    }
    strm << "],\n";

    strm << "\tInverse Suffixes: [";
    for (auto i : v.inverseSuffixes)
    {
        strm << i << ", ";
    }
    strm << "],\n";

    return strm << ")";
}

/**
 * The Aho-Corasick Algorithm uses a data structure called a trie.
 * Generally tries are specialized trees where each node represents a prefix.
 * In addition to the usual edges, the Aho-Corasick trie contains suffix links for efficient traversal.
 */
struct AhoCorasickTrie
{
public:
    // Stores the trie as a vector of Vertex objects.
    vector<Vertex> trie;
    int currentState = ROOT;

    AhoCorasickTrie()
    {
        // trie.reserve(1000);
        trie.emplace_back();
    }

    /**
     * Adds another valid substring to the trie.
     * Valid substrings must be non-empty.
     * Strings can be added at any time.
     */
    void addString(string const &str)
    {
        if (str.length() == 0)
            return;

        int state = ROOT;
        for (char character : str)
        {
            int index = character - 'a';

            if (index < 0 || index > ALPHABET_SIZE - 1)
                throw invalid_argument("Search string contains an invalid character.");

            // Child node does not yet exist
            if (trie[state].children[index] == NONE)
            {
                // Create new node
                int newState = trie.size();
                trie.emplace_back(state, character);
                setSuffixLink(newState);
                setResultLink(newState);

                // Update suffix links using the inverse suffix tree
                updateSuffixLinks(trie[newState].parent, newState, index);

                // Add the new node to the inverse suffix tree
                if (trie[newState].suffixLink != NONE)
                    trie[trie[newState].suffixLink].inverseSuffixes.push_back(newState);

                // Add the new node to this state's children
                trie[state].children[character - 'a'] = newState;
            }

            // Step to the next node
            state = trie[state].children[character - 'a'];
        }

        trie[state].accept = true;
        updateResultLinks(state);
    }

    void reset()
    {
        currentState = 0;
    }

    void next(char input)
    {
        currentState = nextState(currentState, input);
    }

    bool doesSearchStringAccept()
    {
        return trie[currentState].accept || trie[currentState].resultLink != NONE;
    }

    /**
     * Traces the result links to find the number of matching search strings ending here.
     */
    int numMatchingSubstrings()
    {
        int matches = 0;

        if (trie[currentState].accept)
            matches++;

        int resultChain = trie[currentState].resultLink;
        while (resultChain > ROOT)
        {
            matches++;
            resultChain = trie[resultChain].resultLink;
        }

        return matches;
    }

private:
    /**
     * Sets the suffix link for this node.
     * This implemenetation is eager, and assumes that all other suffix links are correct.
     */
    void setSuffixLink(int state)
    {
        /**
         * BASE CASE: ROOT NODE
         * The root node does not have a suffix link.
         * All suffix link chains must end at the root.
         */
        if (trie[state].parent == NONE)
        {
            trie[state].suffixLink = NONE;
            return;
        }

        /**
         * BASE CASE: DIRECT CHILD OF THE ROOT NODE
         * The suffix link for all direct children is the root node.
         */
        if (trie[state].parent == ROOT)
        {
            trie[state].suffixLink = ROOT;
            return;
        }

        /**
         * STANDARD CASE
         * The suffix link is the node's parent's suffix link's child, if it exists.
         * Otherwise, follow that chain of suffix links until such a child exists.
         * Finally, if no child exists, the suffix link points directly to the root node.
         */
        int index = trie[state].lastChar - 'a';
        int suffixChain = trie[trie[state].parent].suffixLink;

        while (trie[suffixChain].children[index] == NONE)
        {
            suffixChain = trie[suffixChain].suffixLink;

            if (suffixChain == UNSET)
            {
                trie[state].suffixLink = ROOT;
                return;
            }
        }

        trie[state].suffixLink = trie[suffixChain].children[index];
    }

    /**
     * Sets the result link for this node.
     * This implemenetation is eager, and assumes that all other result links are correct.
     */
    void setResultLink(int state)
    {
        /**
         * If the suffix link accepts, that becomes the new result link.
         * Otherwise, copy the suffix link's result link.
         */
        if (trie[trie[state].suffixLink].accept)
            trie[state].resultLink = trie[state].suffixLink;

        else
            trie[state].resultLink = trie[trie[state].suffixLink].resultLink;
    }

    /**
     * This function handles dynamically updating suffix links using the inverse suffix tree.
     * The only nodes that need to be checked are the children of the inverse suffix descendants of the new state's parent.
     * For example, the new node 'help' should check all the nodes whose suffix links point to 'hel' for children that accept 'p'.
     */
    void updateSuffixLinks(int parent, int newState, int index)
    {
        vector<int> nodesToCheck = {parent};
        vector<int> nodesToUpdate;

        int i = 0;
        while (i < nodesToCheck.size())
        {
            int check = nodesToCheck[i];

            for (auto state : trie[check].inverseSuffixes)
            {
                int candidate = trie[state].children[index];

                // Avoids circular logic.
                if (candidate == newState)
                    continue;

                // If the candidate exists, then its suffix link should point to newState.
                if (candidate != NONE)
                    nodesToUpdate.push_back(candidate);

                // Check recursively on the inverse suffixes of this node.
                else
                    nodesToCheck.push_back(state);
            }

            i++;
        }

        for (int state : nodesToUpdate)
        {
            // Vectors are stupid. Just removes the candidates from their original position in the inverse suffix tree.
            for (int j = 0; j < trie[trie[state].suffixLink].inverseSuffixes.size(); j++)
            {
                if (trie[trie[state].suffixLink].inverseSuffixes[j] == state)
                {
                    trie[trie[state].suffixLink].inverseSuffixes.erase(trie[trie[state].suffixLink].inverseSuffixes.begin() + j);
                    break;
                }
            }

            // Update the suffix link and inverse suffix tree.
            trie[state].suffixLink = newState;
            trie[newState].inverseSuffixes.push_back(state);
        }
    }

    void updateResultLinks(int newState)
    {
        vector<int> nodes = {newState};

        int i = 0;
        while (i < nodes.size())
        {
            int check = nodes[i];
            for (auto state : trie[check].inverseSuffixes)
            {
                trie[state].resultLink = newState;

                if (!trie[state].accept)
                    nodes.push_back(state);
            }

            i++;
        }
    }

    /**
     * Find the index of the next state based on the given state and character.
     * The first check is for this node's direct descendant, if it exists.
     * Next, propogate up the chain of suffix links until a direct descendant is found.
     * Finally, if even the root node does not have a direct descendant, return the root index.
     */
    int nextState(int state, char character)
    {
        int index = character - 'a';

        // BASE CASE: NOT SEARCHABLE
        if (index < 0 || index > ALPHABET_SIZE - 1)
            return 0;

        // BASE CASE: Check for a valid direct descendant
        if (trie[state].children[index] != NONE)
            return trie[state].children[index];

        // BASE CASE: Root node does not have a valid direct descendant
        if (trie[state].parent == NONE)
            return 0;

        // STANDARD CASE: Follow the suffix link
        return nextState(trie[state].suffixLink, character);
    }

    friend std::ostream &operator<<(std::ostream &, const AhoCorasickTrie &);
};

std::ostream &operator<<(std::ostream &strm, const AhoCorasickTrie &AHT)
{
    strm << "Trie(\n";

    strm << "Current State: " << AHT.currentState << "\n";

    strm << "Nodes: (\n";
    for (int i = 0; i < AHT.trie.size(); i++)
        strm << AHT.trie[i] << ",\n";
    strm << ")\n)";

    return strm;
}

std::ostream &operator<<(std::ostream &strm, const vector<string> &v)
{
    strm << "[";

    for (auto str : v)
        strm << str << ", ";

    return strm << "]";
}

int main()
{
    int B;
    cin >> B;
    cin.ignore(100, '\n');

    vector<string> books;
    for (int i = 0; i < B; i++)
    {
        string book;
        getline(cin, book);
        for (auto &c : book)
            c = tolower(c);

        books.emplace_back(book);
    }

    AhoCorasickTrie AHT = AhoCorasickTrie();

    int K;
    cin >> K;
    cin.ignore(100, '\n');

    for (int i = 0; i < K; i++)
    {
        string str;
        cin >> str;

        AHT.addString(str);
    }

    int N;
    cin >> N;

    for (int i = 0; i < N; i++)
    {
        char command;
        cin >> command;

        string keyword;

        switch (command)
        {
        case 'A':
            cin >> keyword;
            AHT.addString(keyword);
            break;

        case 'S':
            for (int j = 0; j < B; j++)
            {
                int matches = 0;
                AHT.reset();

                for (char c : books[j])
                {
                    AHT.next(c);
                    matches += AHT.numMatchingSubstrings();
                }

                if (matches > 0)
                {
                    cout << "(" << j << "," << matches << ") ";
                }
            }
            cout << "\n";
            break;
        }
    }
}