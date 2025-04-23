
#  SOURCES CITED
#  
#  Wikipedia, "Aho-Corasick algorithm": https://en.wikipedia.org/wiki/Aho%E2%80%93Corasick_algorithm
#  Stanford, "Aho-Corasick Automata": https://web.stanford.edu/class/archive/cs/cs166/cs166.1166/lectures/02/Slides02.pdf
#  Algorithms for Competitive Programming, "Aho-Corasick algorithm": https://cp-algorithms.com/string/aho_corasick.html
 
#!/usr/bin/env python3
from collections import deque
import sys

# Trie Node Object represents a "trie" or a prefix tree used to store a set of strings we
# wish to search for (pattern). The trie will be used to build an automaton that will allow
# us to search text efficiently for our list of patterns
class TrieNode:
    def __init__(self):
         # Children represent outgoing edges between nodes in a Trie. They represent 
         # the structure between al the characters in the patterns
        self.children = {}
        # This represents the failure/suffix links, which are the path taken by the automaton 
        # when they reach a state of failure (AKA we reach a character not in the pattern). 
        # Rather than reverting back to the root node, the suffix link will lead 
        # us back to the longes proper suffix.
        self.fail = None  
        # Outputs are every complete pattern (in the list of keywords) that we have
        # currently reached at this node
        self.output = []

class AhoCorasick:
    # Initialize the Trie with an empty root Node
    def __init__(self):
        self.root = TrieNode()

    # Insert a single "pattern" word into the trie.
    # We will reuse existing edges so common prefixes 
    # such as "EATing" and "EATery" are shared
    def add(self, pattern: str):
        node = self.root
        for ch in pattern:
            node = node.children.setdefault(ch, TrieNode())
        node.output.append(pattern) # Mark the end of the pattern as a desired output

    # Once all of the patterns are inserted, we must build the automaton using BFS
    # tracersal to construct all of the failure links at every state, and 
    # merge the output lists along these links
    
    def build(self) -> None:
        # Given a Trie, we assume the Root Node has depth 0 and its final layer has depth D
        
        # DEPTH = 1 (Childeen of Root) --> BASE CASE
        # All root children fail back to root
        q = deque()
        for child in self.root.children.values():
            child.fail = self.root
            q.append(child)

        # DEPTH = i from 2 to D -> ITERATIVE CASE
        # We use BFS to gurantee that the failure (cur.fail) of any given node (cur) 
        # will be known only after the failure of its parent is known so we can 
        # use it to calculate the fail link of each of cur's children
        while q:
            cur = q.popleft() 
            # For each character ch that leaves cur, nxt is the child state
            # reached by that character. We need to assign nxt.fail
            for ch, nxt in cur.children.items():
                f = cur.fail
                # If we fell back to failure state f, could we still consume ch?
                # If not, keep falling back along f.fail until we can, 
                # or until we hit the root.
                while f and ch not in f.children:
                    f = f.fail
                nxt.fail = f.children[ch] if f and ch in f.children else self.root
                # Any pattern that ends at nxt.fail is also a suffix of the path 
                # that ends at nxt, so we append those patterns to nxt.output
                nxt.output += nxt.fail.output
                # Finally we push the child onto the queue so its own descendants will get their fail links 
                # computed later—only after we just ensured nxt.fail is correct.
                q.append(nxt)

    # This function searches a str "text" to return all of the outputs found with their
    # given index in the location of the text
    def find(self, text: str):
        # "node" represents the automaton once it has been built. It starts out as the
        # root node to represent the start stae - no characters have been consumed
        node = self.root
        
        # For each character ch at index i in the text string
        for i, ch in enumerate(text):
            # NO MATCH (FAIL CHAIN) -> If the current state has no edge labeled for  
            # ch, then we jump to it's fail link state - the longest proper suffix
            while node and ch not in node.children:
                node = node.fail
            # If We hit a character that can’t follow any suffix, then 
            # reset to the node and move on
            if not node:                
                node = self.root
                continue
            # Else we consume ch. We now have matched the prefix by one character 
            # so we step on to the child's (ch's) state
            node = node.children[ch]
            # If any pattern's output was in this node, report the list of patterns
            # ending here, and the index i of their locations
            if node.output:
                yield i, node.output   


def main():
    # Read in all lines
    lines = sys.stdin.read().splitlines()
    if not lines:
        return

    # The first line tells you the number of patterns
    num_patterns = int(lines[0])
    # Make a list of patterns
    patterns = [line.strip() for line in lines[1 : 1 + num_patterns]]
    
    # The remaining lines are the search string (text). We concatenate them
    # using "\n" to form a literal text string/
    text_lines = lines[1 + num_patterns:]
    text = "\n".join(text_lines) 
    if text_lines:      
        text += "\n"
    
    # Build an Aho-Corasick automaton using the list of patterns
    ac = AhoCorasick()
    for pat in patterns:
        ac.add(pat)
    ac.build()

    # Report all found patterns and their corresponding indexes
    for idx, matches in ac.find(text):
        print(f"{idx}: [{', '.join(matches)}, ]")


if __name__ == "__main__":
    main()
