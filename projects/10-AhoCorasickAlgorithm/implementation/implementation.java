package ahoCorasick;
/**
 * SOURCES CITED
 *
 * GeeksForGeeks, "Aho-Corasick Algorithm for Pattern Searching": https://www.geeksforgeeks.org/aho-corasick-algorithm-pattern-searching/
 * Wikipedia, "Aho-Corasick algorithm": https://en.wikipedia.org/wiki/Aho%E2%80%93Corasick_algorithm
 * Stanford, "Aho-Corasick Automata": https://web.stanford.edu/class/archive/cs/cs166/cs166.1166/lectures/02/Slides02.pdf
 * Algorithms for Competitive Programming, "Aho-Corasick algorithm": https://cp-algorithms.com/string/aho_corasick.html
 */

import java.util.*;

class implementation {

    // This represents the max number of states in the matching
    // machine, and it should be equal to the sum of the length of all keywords.
    static int MAXSTATES = 500;

    // This represents the maximum number of characters in input alphabet
    static int MAXCHARACTERS = 128;

    // Output function (out[]) tracks which words are matched at each state
    // If output[state] & (i << i) > 0, it means word[i] is found at this state.
    // Bit i in this mask is one if the word with index i appears when the machine enters this state.
    // This function stores the indexes of all words that end at the current state.
    // Here, we will store the indexes of all matching words as a bitmap for the current state.
    static int[] output = new int[MAXSTATES];

    // Failure function (failureFunction[]) tracks the failure state for each state during matching.
    // This function stores all edges that are followed when current character does not have an edge in the Trie.
    // Here, we will store the next state for the current state.
    static int[] failureFunction = new int[MAXSTATES];

    // Trie function (trie[][]) implements the trie structure for the Aho-Corasick algorithm.
    // trie[state][character] gives the next state when reading the character from state
    // This function follows edges of the Trie of all words in the array.
    // We store the next state for the current state and character in the trie.
    static int[][] trie = new int[MAXSTATES][MAXCHARACTERS];


    /**
     * Builds the matching machine, or automaton, for the Aho-Corasick algorithm using the given dictionary.
     * 
     * @param keywords - Array of keywords to search for. 
     * @param numKeywords - The number of keywords.
     * @return The number of states in the built machine. States are numbered 0 up to the return value - 1, inclusive.
     */
    static int buildAutomaton(String keywords[], int numKeywords) {

        // Initialize all values in output function as 0.
        Arrays.fill(output, 0);

        // Initialize all values in goto function as -1.
        for (int i = 0; i < MAXSTATES; i++)
            Arrays.fill(trie[i], -1);

        // Initially, we just have the start state 0.
        int states = 1;

        // Build the trie (trie[][]) by inserting each keyword in the dictionary (keywords[])
        for (int i = 0; i < numKeywords; ++i) {
            String word = keywords[i];
            int currentState = 0;

            // Here, we traverse the characters of the current keyword and build the trie by inserting
            // all the characters of the current word in trie[][]
            for (int j = 0; j < word.length(); ++j) {
                // This logic maps a character to an index (0-25) to denote the alphabet
                int character = word.charAt(j) - 'a'; 

                // Here, we allocate a new node, thus creating a new state in the trie for the character, if it does not exist
                if (trie[currentState][character] == -1)
                    trie[currentState][character] = states++;

                // We store the next state for the current state and character in the trie, 
                // allowing us to move to the next state
                currentState = trie[currentState][character];
            }

            // Here, we mark the state as an accepting state for the current word by adding it to the output array
            // If output[state] & (i << i) > 0, it means word[i] is found at this state.
            output[currentState] |= (1 << i);
        }

        // For characters without defined transitions from the root state (state 0) in the trie, 
        // create transitions to the root (state 0) itself
        for (int ch = 0; ch < MAXCHARACTERS; ++ch)
            if (trie[0][ch] == -1)
                trie[0][ch] = 0;

        // Here, we will build the failure function computed in breadth-first order using a queue
        // Initialize values in fail function to -1
        Arrays.fill(failureFunction, -1);
        Queue<Integer> q = new LinkedList<>();

        // Set the failure function for all direct children of the root (state 0) or nodes at depth 1 
        // We will iterate over every possible input
        for (int ch = 0; ch < MAXCHARACTERS; ++ch) {

            // All nodes of depth 1 have failure
            // function value as 0.
            if (trie[0][ch] != 0) {
                // The failure state for these nodes is the root
                failureFunction[trie[0][ch]] = 0;
                // Here, we add these states to the queue
                q.add(trie[0][ch]);
            }
        }

        // This queue will contain the direct children of the root (state 0)
        // We will use BFS to compute the failure function for all states
        while (!q.isEmpty()) {

            // Get and remove the front state from queue
            int state = q.peek();
            q.remove();

            // For each character, update the failure state and merge the ouputs
            // For the removed state, we will find the failure function for all the characters for which
            // the trie is not defined
            for (int ch = 0; ch < MAXCHARACTERS; ++ch) {

                // If the trie is defined for character 'ch' and 'state':
                if (trie[state][ch] != -1) {

                    // Find the failure state of removed state
                    int failure = failureFunction[state];

                    // Find the deepest node with a proper suffix from the root to the current state
                    while (trie[failure][ch] == -1)
                        failure = failureFunction[failure];

                    // Update the failure to go to the next state, and then set the failure state for the current state
                    failure = trie[failure][ch];
                    failureFunction[trie[state][ch]] = failure;

                    // Merge the output values
                    output[trie[state][ch]] |= output[failure];

                    // Add the next level of states of the trie to the queue
                    q.add(trie[state][ch]);
                }
            }
        }
        // Here, we return the number of states in the automaton
        return states;
    }

    
    /**
     * Finds the next state to transition to based on the current state and input character
     * using the trie and failure functions.
     * 
     * @param currentState - The current state in the machine. Must be between
    // 0 and the number of states - 1, inclusive.
     * @param nextInput - The next character from the input text that enters into the automaton.
     * @return The next state to transition to using the trie.
     */
    static int findNextState(int currentState, char nextInput) {
        int answer = currentState;
        
        // This logic maps a character to an index (0-25) to denote the alphabet
        int ch = nextInput - 'a';

        // If the goto function is not defined for the character, follow the failure function.
        while (trie[answer][ch] == -1)
            answer = failureFunction[answer];

        // Return the next state
        return trie[answer][ch];
    }

    /**
     * Searches for all occurrences of the words in the dictionary within the input text.
     * 
     * @param dictionary - Array of keywords to search for.
     * @param numKeywords - The number of keywords.
     * @param text - The input text to search.
     */
    static void searchWords(String dictionary[], int numKeywords, String text) {
        // Here, we call the buildAutomaton function that preprocesses the patterns by building the matching automaton
        // with the trie, failure, and output functions
        buildAutomaton(dictionary, numKeywords);

        // Initialize the current state to 0 (start state / root)
        int currentState = 0;

        // Here, we traverse the input text through the automaton to find all matches or occurrences of keywords
        for (int i = 0; i < text.length(); ++i) {
            currentState = findNextState(currentState,
                    text.charAt(i));

            // If match not found, move to next state (continue to next character)
            if (output[currentState] == 0)
                continue;

            // If a match is found, check which keywords match at this state usin gthe output function.
            for (int j = 0; j < numKeywords; ++j) {
                if ((output[currentState] & (1 << j)) > 0) {
                    System.out.print("Word " + dictionary[j] +
                            " appears from " +
                            (i - dictionary[j].length() + 1) +
                            " to " + i + "\n");
                }
            }
        }
    }

    // Main method (will read in text and match to keywords)
    // This implementation will interpret the text as individual text, rather than in the test cases,
    // which go through the text input as a continuous text

    // Therefore with test case 1: 
        // 11: [kitchen, ]  = 11: [kitchen]
        // 32: [oven, ]     = 74: [oven, ]
        // 43: [stove, ]    = 85: [stove, ]
        // 39: [stove, ]    = 230: [stove, ]
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
    
        // Read the amount of keywords
        int numKeywords = Integer.parseInt(scanner.nextLine().trim());
        String[] keywords = new String[numKeywords];
    
        // Read the keywords
        for (int i = 0; i < numKeywords; i++) {
            keywords[i] = scanner.nextLine().trim();
        }
    
        // Build the automaton once with the keywords
        buildAutomaton(keywords, numKeywords);
    
        // Read and process each line of text
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            int currentState = 0;
    
            // Traverse each character in the line
            for (int i = 0; i < line.length(); i++) {
                char ch = line.charAt(i);
    
                if (Character.toLowerCase(ch) < 'a' || Character.toLowerCase(ch) > 'z') {
                    currentState = 0;
                    continue;
                }
    
                currentState = findNextState(currentState, Character.toLowerCase(ch));
    
                if (output[currentState] != 0) {
                    StringBuilder matches = new StringBuilder();
                    for (int j = 0; j < numKeywords; j++) {
                        if ((output[currentState] & (1 << j)) > 0) {
                            matches.append(keywords[j]).append(", ");
                        }
                    }
                    System.out.println(i + ": [" + matches + "]");
                }
            }
        }
    
        scanner.close();
    }    
}
