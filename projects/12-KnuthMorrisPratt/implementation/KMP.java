import java.util.*;

public class KMP {

    static void constructLps(String pat, int[] lps) { // construct longest prefix suffix
        lps[0] = 0; // first element is always 0 (since prefix must be proper)
        int j = 0; // length of current longest prefix suffix

        for (int i = 1; i < pat.length(); i++) { // fill up lps[]
            j = lps[i - 1]; // reset j based on the previous lps value

            while (j > 0 && pat.charAt(i) != pat.charAt(j)) {// fall back to shorter prefix until a match is found or j == 0 
                j = lps[j - 1];
            }

            if (pat.charAt(i) == pat.charAt(j)) { // if characters match, increase the length of current lps
                j++;
            }

            lps[i] = j;
        }
    }

    static List<Integer> search(String pat, String txt) {
        int m = pat.length(); // pattern length
        int n = txt.length(); // text length

        int[] lps = new int[m];
        List<Integer> res = new ArrayList<>();

        constructLps(pat, lps); // preprocess pattern to build lps array

        int i = 0, j = 0;

        // loop through the text
        while (i < n) {
            // move both pointers if characters match
            if (txt.charAt(i) == pat.charAt(j)) {
                i++;
                j++;

                if (j == m) { // store index if pattern match
                    res.add(i - j);
                    j = lps[j - 1];
                }
            } else {
                if (j != 0) {
                    j = lps[j - 1]; // jump to previous possible match using lps
                } else {
                    i++;
                }
            }
        }
        return res;
    }

    public static void main(String[] args) {
        // Take input and process it
        Scanner scanner = new Scanner(System.in);
        String txt = scanner.nextLine();
        String pat = scanner.nextLine();

        List<Integer> res = search(pat, txt);
        for (int idx : res) {
            System.out.print(idx + " ");
        }
        System.out.println();
    }
}
