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

    // Here is the Trie Node. It represents a state in the Aho-Corasick automaton created.
    // The Trie Node stores a map of character transitions to child nodes,
    // a fail link to fall back to when a match fails, 
    // and a list of output keywords that end at this node
    static class TrieNode {
        Map<Character, TrieNode> children = new HashMap<>();
        TrieNode failLink = null;
        List<String> outputs = new ArrayList<>();
    }

    // Here is the main class, AhoCorasick, which implements the Aho-Corasick automaton
    // The class supports inserting keywords, building failure links for fast backtracking during a search,
    // and searching a string for keyword occurrences
    static class AhoCorasick {
        // Here, we instantiate the root, or start node, of the Trie
        TrieNode root = new TrieNode();

        // This function adds a keyword to the Trie, building the initial structure of the Trie
        // The keyword is inserted character by character, creating new nodes when necessary
        public void addKeyword(String keyword) {
            TrieNode node = root;
            // We use lowercase to make the search case-insensitive.
            for (char ch : keyword.toLowerCase().toCharArray()) {
                // This will add a children node to the node if it is not already present
                node = node.children.computeIfAbsent(ch, k -> new TrieNode());
            }
            // This will add the full keyword to the output list at the terminal node
            node.outputs.add(keyword.toLowerCase());
        }

        // Here, we build the failure links for fallback transitions. These links allow the automaton
        // to recover from mismatches efficiently by connecting each node to the longest suffix that is
        // also a prefix in the Trie
        public void buildFailLinks() {
            // Here, we will build the failure function computed in breadth-first order using a queue
            Queue<TrieNode> queue = new LinkedList<>();

            // Initialize the children directly under the root (start state), or level 1 children
            // Their failure link should always point back to the root
            for (TrieNode child : root.children.values()) {
                child.failLink = root;
                queue.add(child);
            }

            // We will use a BFS traversal to construct failure links for the rest of the Trie
            while (!queue.isEmpty()) {
                TrieNode current = queue.poll();
                for (Map.Entry<Character, TrieNode> entry : current.children.entrySet()) {
                    char ch = entry.getKey();
                    TrieNode child = entry.getValue();

                    // Here, we follow the failure link chain until we find a node with the same character
                    // transition or reach the root
                    TrieNode fail = current.failLink;
                    while (fail != null && !fail.children.containsKey(ch)) {
                        fail = fail.failLink;
                    }

                    // We set the child's failure link to the next closest match or root
                    child.failLink = (fail != null) ? fail.children.get(ch) : root;

                    // Then we append the outputs from the fail link node to inherit matches
                    child.outputs.addAll(child.failLink.outputs);

                    // Add the child to the BFS queue to continue processing the Trie
                    queue.add(child);
                }
            }
        }

        // This function performs the keyword search on a given input string
        // It returns the total number of keyword matches found
        public int search(String text) {
            TrieNode node = root;
            int count = 0;

            // We convert to lowercase to ensure a case-insensitive search
            text = text.toLowerCase();

            for (char ch : text.toCharArray()) {
                // If the current character is not a valid transition, follow the
                // fail links until a match is found or we return to the root of the automaton
                while (node != root && !node.children.containsKey(ch)) {
                    node = node.failLink;
                }

                // Take the matching transition or return to the root
                node = node.children.getOrDefault(ch, root);

                // Add the number of matching keywords ending at this node
                count += node.outputs.size();
            }

            return count;
        }
    }

    // Here is our main function, which reads the input and processes actions as stated in the instructions
    // We initialize the book database, add keywords dynamically, and perform search queries on books,
    // as specified in the instructions
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        // Here, we read the number of books B and load the titles into a list
        int B = Integer.parseInt(sc.nextLine());
        List<String> books = new ArrayList<>();
        for (int i = 0; i < B; i++) {
            books.add(sc.nextLine());
        }

        // Here, we read the number of initial search keywords and insert them into the automaton
        int K = Integer.parseInt(sc.nextLine());
        AhoCorasick ac = new AhoCorasick();
        for (int i = 0; i < K; i++) {
            ac.addKeyword(sc.nextLine());
        }
        // We build failure links after all intiial keywords are inserted
        ac.buildFailLinks();

        // Here, we process N actions, which may be adding a new keyword dynamically using A,
        // or searching all books for keyword matches using S
        int N = Integer.parseInt(sc.nextLine());
        for (int i = 0; i < N; i++) {
            String[] action = sc.nextLine().split(" ", 2);

            if (action[0].equals("A")) {
                // Here, we add the new keyword and rebuild the fail links to ensure correctness
                ac.addKeyword(action[1]);
                // This rebuild is expensive, but necessary
                ac.buildFailLinks();
            } else if (action[0].equals("S")) {
                List<String> result = new ArrayList<>();

                // For each book, count how many keywords it matches
                for (int j = 0; j < books.size(); j++) {
                    int matches = ac.search(books.get(j));

                    // If any matches are found, record the book index and the count
                    if (matches > 0) {
                        result.add("(" + j + "," + matches + ")");
                    }
                }

                // Finally, output results as a space-separated string of tuples
                System.out.println(String.join(" ", result));
            }
        }
    }
}
