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

    # This function searches a str "text" to return the total number of pattern occurences
    # where overlaps are allowed
    def find_count(self, text: str) -> int:
        # "node" represents the automaton once it has been built. It starts out as the
        # root node to represent the start stae - no characters have been consumed
        # matches is the numeber of matches
        node, matches = self.root, 0
        
        # For each character ch in the text string
        for ch in text:
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
            matches += len(node.output)
        return matches

def main() -> None:
    # The first line of input is the number of books.
    # The next B lines will each contain the title for one book in the catalogue
    B = int(sys.stdin.readline())
    books = [sys.stdin.readline().rstrip("\n").lower() for _ in range(B)]

    # The next line will be the number of initial search keywords
    # The next K lines will each contain one search keyword
    K = int(sys.stdin.readline())
    keywords = {sys.stdin.readline().strip().lower() for _ in range(K)}

    # Finally, on the following line will be a number N that represents the number of ‘actions’ 𝑁
    N = int(sys.stdin.readline())

    ac = None          # cached automaton
    dirty = True       # Flag set to True whenever keywords change

    for _ in range(N):
        parts = sys.stdin.readline().split()
        cmd = parts[0]

        # A [keyword] should add [keyword] to the dictionary of 
        # search keywords if it is not already present 
        if cmd == 'A':                         
            kw = parts[1].lower()
            if kw not in keywords:
                keywords.add(kw)
                dirty = True

        # S  should search the catalogue for all titles that match at least 
        # one of the search keywords along with the number of matches for each
        elif cmd == 'S':        
            # We rebuild the automaton only when something has actually
            # changed (dirty) or on the very first search.                 
            if dirty or ac is None:           
                ac = AhoCorasick()
                for kw in keywords:
                    ac.add(kw)
                ac.build()
                dirty = False

            # Walk every stored book title, count how many keyword occurrences it has 
            # (overlaps allowed), and append (index,count) to results when count > 0.
            results = []
            for idx, title in enumerate(books):
                count = ac.find_count(title)
                if count:
                    results.append(f"({idx},{count})")
            print(" ".join(results))


if __name__ == "__main__":
    main()
