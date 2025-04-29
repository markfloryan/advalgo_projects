import java.util.*;

class TextSearcher {
    /* The TextSearcher class represents the current state of searching for a given substring
       n a text. Because KNP is an online algorithm, we can maintain information on just the
       current state of the search independent of the text, reducing space complexity to just
       O(n), where n is the length of the substring. We query this class every time 
       we process a new character, and we check whether that new character resulted in a match. */
    String substring;
    int i, j;
    boolean match_found;
    int[] lps;

    public TextSearcher(String substring) {
        // constructs prefix function for given substring
        this.construct_lps(substring);
        // initializes variables for traversing text/substring used in read_character
        this.substring = substring;
        this.i = 0;
        this.j = 0;
        this.match_found = false;
    }

    public void construct_lps(String substring) {
        this.lps = new int[substring.length()]; // will never go further than the length of the substring

        for (int i = 1; i < substring.length(); i++) {
            int j = this.lps[i - 1]; // reset j based on previous lps value
            // fall back to shorter prefix until a match is found or j == 0
            while (j > 0 && substring.charAt(i) != substring.charAt(j)) {
                j = this.lps[j-1];
            }
            // if characters match, increase the length of the current lps
            if (substring.charAt(i) == substring.charAt(j)) {
                j++;
            }
            this.lps[i] = j;
        }
    }

    public void read_character(char c) {
        // If characters match, move pointers forward
        if (c == this.substring.charAt(this.j)) {
            this.j += 1;
            // If the entire pattern is matched, store the start index in result
            if (this.j == this.substring.length()) {
                this.j = this.lps[this.j - 1];
                this.match_found = true;
            }
            else {
                this.match_found = false;
            }
        }
        // If there is a mismatch
        else {
            this.match_found = false;
            // Use lps value of previous index to avoid redundant comparisons
            while (this.j > 0 && c != this.substring.charAt(this.j)) {
                this.j = this.lps[this.j - 1];
            }
            if (c == this.substring.charAt(this.j)) {
                this.j += 1;
            }
        }
        this.i += 1;
    }

    public boolean just_found_match() {
        // returns whether the last read character resulted in a match
        return this.match_found;
    }
}

class GeneralSearcher {
    String text;
    String[] jewels, bombs;
    TextSearcher[] jewel_objects, bomb_objects;
    public GeneralSearcher(String text, String[] jewels, String[] bombs) {
        // initializes variables for class
        this.text = text;
        this.jewels = jewels;
        this.bombs = bombs;
        //creates a TextSearcher object for every substring (jewel/bomb)
        this.jewel_objects = new TextSearcher[jewels.length];
        for (int i = 0; i < jewels.length; i++) {
            this.jewel_objects[i] = new TextSearcher(jewels[i]);
        }
        this.bomb_objects = new TextSearcher[bombs.length];
        for (int i = 0; i < bombs.length; i++) {
            this.bomb_objects[i] = new TextSearcher(bombs[i]);
        }
    }

    public HashSet<Integer> get_indices() {
        // initializes the set of results (mines), current mine index (space-separated), and position in the text
        HashSet<Integer> results = new HashSet<Integer>();
        int mine_index = 0;
        int i = 0;
        // loops through every character of the text
        while (i < this.text.length()) {
            char c = this.text.charAt(i);
            // if the current letter is a space, advance word index and move on to the next letter
            if (c == ' ') {
                mine_index++;
                i++;
            }
            else {
                // for every jewel, process the new character and check if it resulted in a jewel being found
                for (TextSearcher jo : this.jewel_objects) {
                    jo.read_character(c);
                    // if jewel was ofund, add current mine index
                    if (jo.just_found_match()) {
                        results.add(mine_index);
                    }
                }
                // for every bomb, process the new character and check if it resulted in a bomb being found
                for (TextSearcher bo : this.bomb_objects) {
                    bo.read_character(c);
                    // if bomb was found, remove mine index and move onto next mine (since we should no longer consider this mine)
                    if (bo.just_found_match()) {
                        results.remove(mine_index);
                        while (i < this.text.length() && this.text.charAt(i) != ' ') {
                            i += 1;
                        }
                        mine_index += 1;
                    }
                }
                // advance the position in the string
                i += 1;
            }
        }
        // returns all identified mine indices
        return results;
    }
}

public class IPCMining {
    // input processing logic
    public static void main(String[] args) {
        // take in the number of jewels and store them in an array
        Scanner scanner = new Scanner(System.in);
        int jewel_count = scanner.nextInt();
        scanner.nextLine();
        String[] jewels = new String[jewel_count];
        for (int i = 0; i < jewel_count; i++) {
            jewels[i] = scanner.nextLine();
        }
        // take in the number of bombs and store them in an array
        int bomb_count = scanner.nextInt();
        scanner.nextLine();
        String[] bombs = new String[bomb_count];
        for (int i = 0; i < bomb_count; i++) {
            bombs[i] = scanner.nextLine();
        }
        // read in the string representing all mines
        String text = scanner.nextLine();
        // search for jewels and bombs in text
        GeneralSearcher gs = new GeneralSearcher(text, jewels, bombs);
        // get and print all indices of mines of interest
        HashSet<Integer> indices = gs.get_indices();
        for (int index : indices) {
            System.out.println(index);
        }
        scanner.close();
    }
}