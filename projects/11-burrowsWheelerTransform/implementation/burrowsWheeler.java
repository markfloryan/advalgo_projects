/*
 * Adapted from https://nbviewer.org/github/BenLangmead/comp-genomics-class/blob/master/notebooks/CG_BWT_Reverse.ipynb
 */
import java.util.*;

public class burrowsWheeler {
    public static void main(String[] args){
        // Initializes a file reader that reads what is inputted by the user
        Scanner sc = new Scanner(System.in);
        
        // Initializes an instance of the burrowsWheeler class, which has methods to
        //  run the Burrows-Wheeler Transform (BWT) on a string
        burrowsWheeler transformer = new burrowsWheeler();
        // Sets t to the user input (and an input must contain no spaces or newline characters)
        String t = sc.next();
        // Closes the scanner/file reader, since there is no more user input to read
        sc.close();
        // Adds a '$' at the end of t to keep track of the end of the string
        t += "$";
        // Applies the BWT using the sorted rotations method to t, the input
        String b = transformer.bwtViaSortedRotations(t);
        // Prints the BWT output, b
        System.out.println(b);

        /*
         * Given a valid BWT, b, returns the inverse transform, the original string being transformed
         * and assigns it to reverse
         * reverse is printed without the last character, as the last character will be '$', which is
         * not part of the original string and had been added to keep track of the end of the string
         */
        String reverse = transformer.reverseBwt(b);
        System.out.println(reverse.substring(0, reverse.length() - 1));
    }
    /*
     * Takes in input string t
     * Returns list of cyclical rotations of original string (including $) of length n
     * Done by doubling original string and getting each set of consecutive n characters
     */
    public String[] rotations(String t){
        // double original string
        String tt = t + t;
        int strLen = t.length();
        String[] rotations = new String[strLen];
        // add each rotation to array, derived by getting consecutive set of n characters starting
        //  with the start of the string and continuing until the last character of the initial string
        for(int i = 0; i < strLen; i++){
            rotations[i] = tt.substring(i, i+strLen);
        }
        return rotations;
    }
    /*
     * Takes in input string t
     * Returns list of rotations of strings ordered through string comparison (lower value characters first)
     * Not case sensitive in this case, and the '$' will show up first
     */
    public String[] sortedRotations(String t){
        // Get rotations from rotations method
        String[] rotations = rotations(t);
        // Conduct in-place sort on rotations
        Arrays.sort(rotations);
        // Return rotations
        return rotations;
    }
    /*
     * Takes in input string t
     * Returns the Burrows-Wheeler transform (BWT) by utilizing the sorted rotations
     * Through a loop through the sorted rotations, the last character is added to the the lambda function and 'map', each rotation (x) is mapped to its last character (x[-1])
     *  through taking in the iterable array of sorted rotations and utilizing a map from x to x[-1]
     * The list of last characters is all concatenated in order into a string through the 'join' function
     */
    public String bwtViaSortedRotations(String t){
        // initializes return string, bwt
        String bwt = "";
        // gets all the sorted rotations for input, t
        String[] sortedRots = sortedRotations(t);
        int strLen = t.length();
        // for all rotations, add the last character of the string (.substring(strLen-1)) to output, bwt
        for(int i = 0; i < t.length(); i++){
            bwt += sortedRots[i].substring(strLen-1);
        }
        return bwt;
    }
    /*
     * Given an input, bw, which is presumed to be a valid BWT
     * Returns an object array, containing at element 0, ranks, and at element 1, tots
     *  ranks: a list corresponding to the BWT string that indicates how many times
     *         each character has appeared in the BWT string before
     *  tots: a map mapping each unique character in the BWT string
*    *        to how many times it appears in the BWT string
     */
    public Object[] rankBwt(String bw){
        // initialize return values, tots, and ranks
        Map<Character, Integer> tots = new HashMap<>();
        List<Integer> ranks = new ArrayList<>();
        // iterate through each character in the string, bw
        for(char c: bw.toCharArray()){
            // for each new character, add a 0 element to the dictionary to indicate its first appearance
            if(!tots.containsKey(c)){
                tots.put(c, 0);
            }
            // for a given character, add the rank as how many times it has appeared previously (tots[c])
            int totsC = tots.get(c);
            ranks.add(totsC);
            // add 1 to tots.get(c) (called as totsC) since this is a new appearance of c
            // can be used later if c appears again
            tots.put(c, totsC + 1);
        }
        // create object array, output, containing ranks at element 0 and tots at element 1
        // return object array, output
        Object[] output = new Object[2];
        output[0] = ranks;
        output[1] = tots;
        return output;
    }
    /*
     * Given a map 'tots' mapping characters to their total appearances in a BWT string
     *  a map 'first' is returned mapping characters to the first row in a list of sorted
     *  rotations that they would start off (prefix)
     */
    public Map<Character, Integer> firstCol(Map<Character, Integer> tots){
        Map<Character, Integer> first = new HashMap<>();
        // Sets totc initially to 0, since the first character must appear at index 0
        int totc = 0;
        // sortedChars is initialized as the sorted version of the keys in tots, where the keys
        //  represent all the characters in a BWT string
        List<Character> sortedChars = tots.keySet().stream()
            .sorted()
            .toList();
        // for each character, c, in sorted order, iterate and find the first row it prefixes
        //  tots.get(c) represents the count, or number of appearances, of each character, c
        for(char c: sortedChars){
            // Sets first.get(c) to totc, which is the index the current character c should first appear at
            first.put(c, totc);
            // Adds tots.get(c) to totc, which indicates that the next character should appear after all
            //  instances of the current character, which would span [totc, totc + (tots.get(c) - 1)]
            totc += tots.get(c);
        }
        return first;
    }
    /*
     * Used to get the final inverse of a BWT string, getting it back to the original string
     * Takes in BWT string, bw, as input and returns original string, t, as output
     */
    public String reverseBwt(String bw){
        // Make sure to have rank information for each character as well as total instances of each
        // Done by getting object array from rankBwt, which returns ranks at element 0 and tots at element 1
        Object[] rankBwtOutput = rankBwt(bw);
        List<Integer> ranks = (List<Integer>)rankBwtOutput[0];
        Map<Character, Integer> tots = (Map<Character, Integer>)rankBwtOutput[1];
        // Use total instances of characters to determine first instance of prefixing in sorted rotations
        Map<Character, Integer> first = firstCol(tots);
        // Start at the first row of the sorted rotations, 0, by setting rowi to 0
        int rowi = 0;
        // Set t to the special character '$' indicating the end of the string
        String t = "$";
        /*
         * BWT reversal relies on the last characters of the sorted strings being the BWT characters
         * Thus, indexing into the BWT at 0 indicates the character at the end of the string starting with '$'
         * This character is also the end of the original string, since it is cyclically before '$'
         * Thus, for each current char, the corresponding BWT char is the previous char in the string
         * The string is built up by prepending each previous character until the cycle ends by reaching
         *   the last character '$'
         */
        while(bw.charAt(rowi) != '$'){
            // c is the last character at rowi in the sorted rotations, indexed by using
            //  bw.substring(rowi, rowi+1), which gets just the last character of bw
            String c = bw.substring(rowi, rowi+1);
            // prepends c before the output string t
            // c must come before the starting character of the sorted rotation
            t = c + t;
            // rowi is set to the location of c in the sorted rotations
            // first.get(c.charAt(0)) indicates the first location at which c prefixes the sorted rotations
            //  c.charAt(0) simply extracts the only character in the string c
            // ranks.get(rowi) indicates how many instances of c have prefixed previous sorted rotations
            rowi = first.get(c.charAt(0)) + ranks.get(rowi);
        }
        return t;
    }
}