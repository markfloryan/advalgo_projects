class TextSearcher:
    # The TextSearcher class represents the current state of searching for a given substring
    # n a text. Because KNP is an online algorithm, we can maintain information on just the
    # current state of the search independent of the text, reducing space complexity to just
    # O(n), where n is the length of the substring. We query this class every time 
    # we process a new character, and we check whether that new character resulted in a match.

    def __init__(self, substr):
        # constructs prefix function for given substring
        self.construct_lps(substr)
        # initializes variables for traversing text/substring used in read_character
        self.substr = substr
        self.i = 0
        self.j = 0
        self.match_found = False

    def construct_lps(self, substr):
        self.lps = [0 for _ in range(len(substr))] # will never go further than the length of the substring
        self.lps[0] = 0 # first element is always 0 (since prefix must be proper)

        for i in range(1, len(substr)):
            j = self.lps[i-1] # reset j based on previous lps value
            while (j > 0 and substr[i] != substr[j]): # fall back to shorter prefix until a match is found or j == 0
                j = self.lps[j-1]
            if (substr[i] == substr[j]): # if characters match, increase the length of the current lps
                j += 1
            self.lps[i] = j

    def read_character(self, c):
        if c == self.substr[self.j]: # If characters match, move pointers forward
            self.j += 1
            # If the entire pattern is matched, store the start index in result
            if self.j == len(self.substr):
                self.j = self.lps[self.j - 1]
                self.match_found = True
            else:
                self.match_found = False
        # If there is a mismatch
        else:
            self.match_found = False
            # Use lps value of previous index
            # to avoid redundant comparisons
            while (self.j > 0 and c != self.substr[self.j]):
                self.j = self.lps[self.j-1]
            if (c == self.substr[self.j]):
                self.j += 1
        self.i += 1
    
    def just_found_match(self):
        # returns whether the last read character resulted in a match
        return self.match_found

    
class GeneralSearcher:
    def __init__(self, text, jewels, bombs):
        # initializes variables for class
        self.text = text
        self.jewels = jewels
        self.bombs = bombs
        # creates a TextSearcher object for every substring (jewel/bomb)
        self.jewel_objects = [TextSearcher(jewel) for jewel in jewels]
        self.bomb_objects = [TextSearcher(bomb) for bomb in bombs]

    def get_indices(self):
        # initializes the set of results (mines), current mine index (space-separated), and position in the text
        results = set()
        mine_index = 0
        i = 0
        # loops through every character of the text
        while i < len(self.text):
            c = self.text[i]
            # if the current letter is a space, advance word index and move on to the next letter
            if c == ' ':
                mine_index += 1
                i += 1
            else:
                # for every jewel, process the new character and check if it resulted in a jewel being found
                for jo in self.jewel_objects:
                    jo.read_character(c)
                    # if jewel was found, add current mine index
                    if jo.just_found_match():
                        results.add(mine_index)
                # for every bomb, process the new character and check if it resulted in a bomb being found
                for bo in self.bomb_objects:
                    bo.read_character(c)
                    # if bomb was found, remove mine index and move onto next mine (since we should no longer consider this mine)
                    if bo.just_found_match():
                        results.discard(mine_index)
                        while i < len(self.text) and self.text[i] != ' ':
                            i += 1
                        mine_index += 1
                i += 1
        # returns all identified mine indices
        return results
    
# input processing logic
def main():
    # take in the number of jewels and store them in an array
    jewel_count = int(input())
    jewels = []
    for _ in range(jewel_count):
        jewels.append(input())
    # take in the number of bombs and store them in an array
    bomb_count = int(input())
    bombs = []
    for _ in range(bomb_count):
        bombs.append(input())
    # read in the string representing all mines
    text = input()
    # search for jewels and bombs in text
    gs = GeneralSearcher(text, jewels, bombs)
    # get and print all indices of mines of interest
    indices = gs.get_indices()
    for index in indices:
        print(index)

if __name__ == "__main__":
    main()
