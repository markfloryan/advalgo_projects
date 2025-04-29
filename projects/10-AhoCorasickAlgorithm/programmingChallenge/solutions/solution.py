#  SOURCES CITED
#  
#  Bertrand Meyer, "Incremental String Matching": https://se.inf.ethz.ch/~meyer/publications/string/string_matching.pdf
#  GeeksforGeeks, "Aho-Corasick Algorithm in Python": https://www.geeksforgeeks.org/aho-corasick-algorithm-in-python/
#  Wikipedia, "Aho-Corasick algorithm": https://en.wikipedia.org/wiki/Aho%E2%80%93Corasick_algorithm
#  Stanford, "Aho-Corasick Automata": https://web.stanford.edu/class/archive/cs/cs166/cs166.1166/lectures/02/Slides02.pdf
#  Algorithms for Competitive Programming, "Aho-Corasick algorithm": https://cp-algorithms.com/string/aho_corasick.html

#!/usr/bin/env python3
import sys
from collections import deque

""" Trie Node represents the node of a "trie" or a prefix tree used to store a 
    set of strings we wish to search for (pattern). The trie will be used to 
    build an automaton that will allow us to search text efficiently for our 
    list of patterns """
class TrieNode:
    def __init__(self):
        # Children represent outgoing edges between nodes in a Trie. They represent 
         # the structure between al the characters in the patterns
        self.children = {}  
        # This represents the failure/suffix links, which are the path taken by the automaton 
        # when they reach a state of failure (AKA we reach a character not in the pattern). 
        # Rather than reverting back to the root node, the suffix link will lead 
        # us back to the longes proper suffix.
        self.fail     = None 
        # Outputs are every complete pattern (in the list of keywords) that we have
        # currently reached at this node
        self.output   = []   
        # This is a list of all nodes who fail to this current node. This will allow us to 
        # incrementally construct the DFS autumaton
        self.inverse  = []  

"""The Aho-Corasick Algorithm is able to search an input string "text" for a set of
    multiple patterns in parallel (running in linear time) by building a trie or
    prefix tree of the given patterns. Using this, it builds a deterministic 
    automaton using failure and output links."""
class AhoCorasick:
    """ Constructor initialize the Trie with an empty root Node """
    def __init__(self):
        # Root of the trie; root.fail remains None
        self.root = TrieNode()
    
    """ add inserts a new keyword "pattern" into the trie.
        When a new node is created, compute its fail link
        and update any existing nodes that should now point
        to this new node through fail (suffix) links. """
    def add(self, pattern: str):
        # Starting at the root, we examine the current node
        curr = self.root
        # for each character in the pattern word
        for ch in pattern:
            # If that character ch is a not valid path from current nodee
            # AKA the pattern to ch does not yet exist
            if ch not in curr.children:
                # Create a new node child and store it and its character ch
                # as a child of the current node
                child = TrieNode()
                curr.children[ch] = child
                # Call set_fail_link method to find where the child node fails to
                self.set_fail_link(curr, child, ch)

                # Update any existing fail-links for other nodes whose
                # transitions on 'ch' should now point to this new child.
                self.update_fail_links(curr, child, ch)

            # Move down the trie along the new or existing edge.
            curr = curr.children[ch]

        # Any pattern that ends at the curr node after the for loop is now
        # a node that signifies the output of that pattern
        curr.output.append(pattern)

    """This function finds the failure link of a node child given that its
        parent already has a failure link"""
    def set_fail_link(self, parent : TrieNode,
                            child : TrieNode,
                            ch : str) -> None:
        # All children of the root node must fail back to root
        if parent is self.root:
            child.fail = self.root
        else:
            f = parent.fail
            # If we fell back to parent failure state f, could we still consume ch?
            # If not, keep falling back along its failure link (f.fail) for as long
            # as we can, or until we hit the root.
            while f is not None and ch not in f.children:
                f = f.fail
            child.fail = (f.children[ch] if (f and ch in f.children)
                            else self.root)

        # Then we append the child to the list of its fail point's inverse links
        # to speed updates (AKA make sure we also update the inverse suffix tree)
        child.fail.inverse.append(child)
     
    """ update_fail_links() is meant to repair any existing fail-links so that
        when you create a new node for a character ch after the node parent,
        any node whose fall-back on character ch should now fall back 
        on the new node child"""   
    def update_fail_links(self, parent: TrieNode,
                                child: TrieNode,
                                ch: str) -> None:
        # We will perform a BFS ont the inverse-suffix tree making sure 
        # we start at the parent node
        queue = deque([parent]) 
        while queue:
            p = queue.popleft()
            # For each node v whose fail links points to current parent p,
            # we need to check if there is an existing outgoing edge on the
            # character ch
            for v in p.inverse:
                cand = v.children.get(ch)
                # We can skip the very node we just inserted
                if cand is child:
                    continue
                # If such a candidate (cand) exists meaning, 
                # this node v had a 'ch' edge we must fix its faillink.
                if cand:
                    oldf = cand.fail
                    if oldf:
                        # Remove from the old fail-target's inverse list
                        oldf.inverse.remove(cand)
                    # Point its fail to the brand-new node
                    cand.fail = child
                    # Record inverse so future updates can traverse here
                    child.inverse.append(cand)
                else:
                    # There is no direct ch-edge here, but we contnue to examine
                    # down the inverse-tree 
                    queue.append(v)

    """This function searches a str "text" to return the total number of pattern 
    occurences where overlaps are allowed"""
    def find_count(self, text: str) -> int:
        # "node" represents the automaton once it has been built. It starts out as the
        # root node to represent the start stae - no characters have been consumed
        # matches is the numeber of matches
        node = self.root
        matches = 0
        
        # For each character ch in the text string
        for ch in text:
            # If the current state has no edge labeled for ch, then we 
            # jump to it's fail link state - the longest proper suffix
            # for as long as we can
            while node is not self.root and ch not in node.children:
                node = node.fail or self.root
            # Else we consume ch. We now have matched the prefix by one character 
            # so we step on to the child's (ch's) state
            node = node.children.get(ch, self.root)

            # Now we every pattern ending here or in any suffix state.
            t = node
            while t is not None:
                matches += len(t.output)
                t = t.fail

        return matches

def main():
    data = sys.stdin
    ac = AhoCorasick()

    # The first line of input is the number of books.
    # The next B lines will each contain the title for one book in the catalogue    
    B = int(data.readline())
    books = [data.readline().rstrip("\n").lower() for _ in range(B)]

    # The next line will be the number of initial search keywords
    # The next K lines will each contain one search keyword
    K = int(data.readline())
    seen = set()
   
    # Since these words are already given to us, add them to a list of patterns
    # and incrementally build the Aho-Corasick automaton 
    for _ in range(K):
        w = data.readline().strip().lower()
        if w not in seen:
            seen.add(w)
            ac.add(w)

    # Finally, on the following line will be a number N that represents 
    # the number of ‘actions’ N # 3) Process commands ('A' to add, 'S' to search)
    N = int(data.readline())
    for _ in range(N):
        parts = data.readline().split()
        if not parts:
            continue
        # "A [keyword]" should add [keyword] to the dictionary of 
        # search keywords if it is not already present 
        if parts[0] == 'A':
            kw = parts[1].lower()
            if kw not in seen:
                seen.add(kw)
                ac.add(kw)
        # "S" should search the catalogue for all titles that match at least 
        # one of the search keywords along with the number of matches for each
        elif parts[0] == 'S':
            # Walk every stored book title, count how many keyword occurrences it has 
            # (overlaps allowed), and append (index,count) to results when count > 0.
            results = []
            for i, title in enumerate(books):
                c = ac.find_count(title)
                if c:
                    results.append(f"({i},{c})")
            print(" ".join(results))

if __name__ == "__main__":
    main()
