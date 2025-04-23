/**
 * SOURCES CITED
 *
 * Wikipedia, "Aho-Corasick algorithm": https://en.wikipedia.org/wiki/Aho%E2%80%93Corasick_algorithm
 * Stanford, "Aho-Corasick Automata": https://web.stanford.edu/class/archive/cs/cs166/cs166.1166/lectures/02/Slides02.pdf
 * Algorithms for Competitive Programming, "Aho-Corasick algorithm": https://cp-algorithms.com/string/aho_corasick.html
 */

#include <iostream>
#include <vector>
#include <bits/stdc++.h>
using namespace std;

const int UNSET = -2;
const int NONE = -1;
const int ROOT = 0;

const int ALPHABET_SIZE = 26;

/**
 * Each vertex represents the prefix for at least one possible search (dictionary) string in the trie.
 */
struct Vertex
{
public:
    // This value is stored for convenient outputs, but is not used in the search itself.
    string searchString;

    // Defines whether the current node represents a valid search (dictionary) string.
    bool accept = false;

    // Stores the indices of this node's direct children.
    // The special value NONE means that the specified child does not exist.
    int children[ALPHABET_SIZE];

    // Stores the parent index and the most recent character in this prefix.
    // These values are used to find suffix (failure) links.
    int parent;
    char lastChar;

    // The suffix (failure) link represents the index of the node with the largest matching suffix.
    // For example, the node representing 'ab' would point to 'b' if it exists. Otherwise, it points to the root node.
    // See AhoCorasickTrie.setSuffixLink() for more information.
    int suffixLink = UNSET;

    // Stores the most recent suffix (failure) link in the "suffix chain" that accepts, or NONE if none exists.
    // Result links are used to match multiple patterns simultaneously (e.g. 'abc' and 'bc' should match simultaneously)
    // Result links are also used to match partial prefixes (e.g. 'ab' should match during 'laboratory')
    int resultLink = NONE;

    // The default values correspond to the root node
    Vertex(int parent = NONE, char character = '\0')
    {
        this->parent = parent;
        this->lastChar = character;
        fill(begin(children), end(children), NONE);
    }

private:
    friend std::ostream &operator<<(std::ostream &, const Vertex &);
};

// SAMPLE PRINT METHOD
std::ostream &operator<<(std::ostream &strm, const Vertex &v)
{
    strm << "Vertex(\n"
         << "\t" << (v.accept ? "Accept" : "Reject") << ", \n"
         << "\tParent: " << v.parent << ", \n"
         << "\tCharacter: " << v.lastChar << ", \n"
         << "\tSuffix (Failure) Link: " << v.suffixLink << ", \n"
         << "\tResult Link: " << v.resultLink << ", \n";

    strm << "\tChildren: [";
    for (int i = 0; i < ALPHABET_SIZE; i++)
    {
        if (v.children[i] != NONE)
            strm << (char)(i + 'a') << ", ";
    }
    strm << "],\n";

    return strm << ")";
}

/**
 * The Aho-Corasick Algorithm uses a data structure called a trie.
 * Tries are specialized trees where each node represents a prefix (unlike binary search trees, where the exact parent and children are meaningless).
 * In addition to the usual edges, the Aho-Corasick trie contains suffix (failure) links for efficient traversal.
 */
struct AhoCorasickTrie
{
public:
    // Stores the trie as a vector of Vertex objects.
    vector<Vertex> trie;
    int currentState = ROOT;

    // Creates a trie containing the root node.
    AhoCorasickTrie()
    {
        trie.emplace_back();
    }

    /**
     * Adds another valid search string to the trie.
     * Valid search strings must be non-empty.
     * In this implementation, all search strings must be added BEFORE searching with the trie.
     */
    void addString(string const &str)
    {
        if (str.length() == 0)
            return;

        // Iterate through the tree, starting at the root and inserting prefixes as needed.
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

                // Add the new node to this state's children
                trie[state].children[index] = newState;
            }

            // Step to the next node
            state = trie[state].children[index];
        }

        // Once the final node is created, set this node to accept.
        trie[state].accept = true;
        trie[state].searchString = str;
    }

    /**
     * Uses a breadth-first search to add the suffix (failure) links.
     * All suffix (failure) links will point somewhere closer to the root.
     * Result links are set in the same manner as suffix (failure) links.
     */
    void setupLinks()
    {
        vector<int> queue;
        queue.emplace_back(ROOT);

        int i = 0;
        while (i < queue.size())
        {
            setSuffixLink(queue[i]);
            setResultLink(queue[i]);

            for (int child : trie[queue[i]].children)
            {
                if (child != NONE)
                    queue.emplace_back(child);
            }

            i++;
        }
    }

    // Step through the next character of input
    void next(char input)
    {
        currentState = nextState(currentState, input);
    }

    // Check whether any search (dictionary) strings accept at this point
    bool doesSearchStringAccept()
    {
        return trie[currentState].accept || trie[currentState].resultLink != NONE;
    }

    // Get a list of all search (dictionary) strings that accept at this point
    vector<string> acceptingSearchStrings()
    {
        vector<string> searchStrings = {};

        if (trie[currentState].accept)
            searchStrings.push_back(trie[currentState].searchString);

        int resultChain = trie[currentState].resultLink;
        while (resultChain > ROOT)
        {
            searchStrings.push_back(trie[resultChain].searchString);
            resultChain = trie[resultChain].resultLink;
        }

        return searchStrings;
    }

private:
    /**
     * Sets the suffix (failure) link for this node.
     * This implemenetation is eager, and assumes that all ancestor suffix (failure) links are set correctly.
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
         * The suffix link for all direct children of the root is the root node.
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

            if (suffixChain == NONE || suffixChain == UNSET)
            {
                trie[state].suffixLink = ROOT;
                return;
            }
        }

        trie[state].suffixLink = trie[suffixChain].children[index];
    }

    /**
     * Sets the result link for this node by following the chain of suffix (failure) links.
     * This implemenetation is eager, and assumes that all ancestor result links are set correctly.
     */
    void setResultLink(int state)
    {
        if (trie[state].suffixLink == NONE || trie[state].suffixLink == ROOT)
        {
            trie[state].resultLink = NONE;
            return;
        }
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
     * Find the index of the next state based on the given state and character.
     * The first check is for this node's direct descendant, if it exists.
     * Next, propogate up the chain of suffix (failure) links until a direct descendant is found.
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

// SAMPLE PRINT METHOD
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

// SAMPLE PRINT METHOD
std::ostream &operator<<(std::ostream &strm, const vector<string> &v)
{
    strm << "[";

    for (auto str : v)
        strm << str << ", ";

    return strm << "]";
}

int main()
{
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

    AHT.setupLinks();

    cin.ignore(100, '\n');
    string input = "";
    string line;
    while (getline(cin, line))
    {
        input = input + line + "\n";
    }

    for (int i = 0; i < input.size(); i++)
    {
        AHT.next(input[i]);

        if (AHT.doesSearchStringAccept())
            cout << i << ": " << AHT.acceptingSearchStrings() << endl;
    }
}