/**
 * SOURCES CITED
 *
 * GeeksForGeeks, "Aho-Corasick Algorithm for Pattern Searching": https://www.geeksforgeeks.org/aho-corasick-algorithm-pattern-searching/
 * Wikipedia, "Aho-Corasick algorithm": https://en.wikipedia.org/wiki/Aho%E2%80%93Corasick_algorithm
 * Stanford, "Aho-Corasick Automata": https://web.stanford.edu/class/archive/cs/cs166/cs166.1166/lectures/02/Slides02.pdf
 * Algorithms for Competitive Programming, "Aho-Corasick algorithm": https://cp-algorithms.com/string/aho_corasick.html
 */

import java.util.*;

public class solution {

    // Trie Node Object represents a "trie" or a prefix tree used to store a set of strings we
    // wish to search for (pattern). The trie will be used to build an automaton that will allow
    // us to search text efficiently for our list of patterns
    static class TrieNode {
        // Children represent outgoing edges between nodes in a Trie. They represent 
        // the structure between all the characters in the patterns
        Map<Character, TrieNode> children = new HashMap<>();

        // This represents the failure/suffix links, which are the path taken by the automaton 
        // when they reach a state of failure (AKA we reach a character not in the pattern). 
        // Rather than reverting back to the root node, the suffix link will lead 
        // us back to the longest proper suffix.
        TrieNode fail = null;

        // Output/Terminal links are every complete pattern (in the list of keywords) that we have
        // currently reached at this node
        List<String> output = new ArrayList<>();
    }

    static class AhoCorasick {
        // Initialize the Trie with an empty root Node
        TrieNode root = new TrieNode();

        // Insert a single "pattern" word into the trie.
        // We will reuse existing edges so common prefixes 
        // such as "EATing" and "EATery" are shared
        void add(String pattern) {
            TrieNode node = root;
            for (char ch : pattern.toCharArray()) {
                node = node.children.computeIfAbsent(ch, k -> new TrieNode());
                }
            // Mark the end of the pattern as a desired output
            node.output.add(pattern);
        }

        // Once all of the patterns are inserted, we must build the automaton using BFS
        // traversal to construct all of the failure links at every state, and 
        // merge the output lists along these links
        void build() {
            // Given a Trie, we assume the Root Node has depth 0 and its final layer has depth D

            // BASE CASE: DEPTH = 1 (Children of Root)
            // All root children fail back to root
            Queue<TrieNode> q = new LinkedList<>();
            for (TrieNode child : root.children.values()) {
                child.fail = root;
                q.add(child);
            }

            // We use BFS to guarantee that the failure (cur.fail) of any given node (cur) 
            // will be known only after the failure of its parent is known so we can 
            // use it to calculate the fail link of each of cur's children
            while (!q.isEmpty()) {
                TrieNode cur = q.poll();
                for (Map.Entry<Character, TrieNode> entry : cur.children.entrySet()) {
                    char ch = entry.getKey();
                    TrieNode nxt = entry.getValue();
                    TrieNode f = cur.fail;

                    // If we fell back to failure state f, could we still consume ch?
                    // If not, keep falling back along f.fail until we can, 
                    // or until we hit the root.
                    while (f != null && !f.children.containsKey(ch)) {
                        f = f.fail;
                    }
                    
                    if (f != null && f.children.containsKey(ch)) {
                        nxt.fail = f.children.get(ch);
                    } else {
                        nxt.fail = root;
                    }

                    // Any pattern that ends at nxt.fail is also a suffix of the path 
                    // that ends at nxt, so we append those patterns to nxt.output
                    nxt.output.addAll(nxt.fail.output);

                    // Finally we push the child onto the queue so its own descendants will get their fail links 
                    // computed later—only after we just ensured nxt.fail is correct.
                    q.add(nxt);
                }
            }
        }

        // This function searches a str "text" to return the total number of pattern occurrences
        // where overlaps are allowed
        int findCount(String text) {
            // "node" represents the automaton once it has been built. It starts out as the
            // root node to represent the start state - no characters have been consumed
            // matches is the number of matches
            TrieNode node = root;
            int matches = 0;

            // For each character ch in the text string
            for (char ch : text.toCharArray()) {
                // If the current state has no edge labeled for  
                // ch, then we jump to its fail link state - the longest proper suffix
                // for as long as we can
                while (node != null && !node.children.containsKey(ch)) {
                    node = node.fail;
                }

                // If we hit a character that can’t follow any suffix, then 
                // reset to the root node and move on
                if (node == null) {
                    node = root;
                    continue;
                }

                // Else we consume ch. We now have matched the prefix by one character 
                // so we step on to the child's (ch's) state
                node = node.children.get(ch);
                matches += node.output.size();
            }
            return matches;
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // The first line of input is the number of books.
        // The next B lines will each contain the title for one book in the catalogue
        int B = Integer.parseInt(scanner.nextLine());
        List<String> books = new ArrayList<>();
        for (int i = 0; i < B; i++) {
            books.add(scanner.nextLine().trim().toLowerCase());
        }

        // The next line will be the number of initial search keywords
        // The next K lines will each contain one search keyword
        int K = Integer.parseInt(scanner.nextLine());
        Set<String> keywords = new HashSet<>();
        for (int i = 0; i < K; i++) {
            keywords.add(scanner.nextLine().trim().toLowerCase());
        }

        // Following line will be a number N that represents the number of ‘actions’ N
        int N = Integer.parseInt(scanner.nextLine());

        // Here we will set up our Aho-Corasick automaton
        // We will also set the dirty boolean flag to true whenever a keyword changes
        AhoCorasick ac = null;
        boolean dirty = true;

        for (int i = 0; i < N; i++) {
            String[] parts = scanner.nextLine().split(" ");
            String cmd = parts[0];

            // A [keyword] should add [keyword] to the dictionary of 
            // search keywords if it is not already present 
            if (cmd.equals("A")) {
                String kw = parts[1].toLowerCase();
                if (!keywords.contains(kw)) {
                    keywords.add(kw);
                    dirty = true;
                }
            }

            // S should search the catalogue for all titles that match at least 
            // one of the search keywords along with the number of matches for each
            else if (cmd.equals("S")) {
                // We rebuild the automaton only when something has actually
                // changed (dirty) or on the very first search.                 
                if (dirty || ac == null) {
                    ac = new AhoCorasick();
                    for (String kw : keywords) {
                        ac.add(kw);
                    }
                    ac.build();
                    dirty = false;
                }

                // Walk every stored book title, count how many keyword occurrences it has 
                // (overlaps allowed), and append (index,count) to results when count > 0.
                List<String> results = new ArrayList<>();
                for (int idx = 0; idx < books.size(); idx++) {
                    int count = ac.findCount(books.get(idx));
                    if (count > 0) {
                        results.add("(" + idx + "," + count + ")");
                    }
                }
                System.out.println(String.join(" ", results));
            }
        }

        scanner.close();
    }
}
