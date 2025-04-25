import java.util.Arrays;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

public class GaleShapley {

    /**
     * Implements the Gale–Shapley algorithm for the Stable Marriage Problem.
     * 
     * @param n             the number of men (same as the number of women)
     * @param proposerPrefs a 2D array where proposerPrefs[i] is an array of woman
     *                      indices, sorted from most to least preferred by man i.
     * @param acceptorPrefs a 2D array where acceptorPrefs[i] is an array of man
     *                      indices, sorted from most to least preferred by woman i.
     * @return an array "res" where res[i] is the index of the woman matched with
     *         man i.
     */
    public static int[] galeShapley(int n, int[][] proposerPrefs, int[][] acceptorPrefs) {
        // all of the men and women start off as single (free)
        boolean[] menFree = new boolean[n];
        boolean[] womenFree = new boolean[n];
        Arrays.fill(menFree, true);
        Arrays.fill(womenFree, true);

        // res[i] will eventually hold the index of the woman paired with man i.
        int[] res = new int[n];
        // resWomen[i] will hold the index of the man paired with woman i.
        int[] resWomen = new int[n];
        Arrays.fill(res, -1);
        Arrays.fill(resWomen, -1);

        // building womenRef: for each woman and every man that she ranks, store her
        // ranking.
        // lower numbers indicate higher preference.
        int[][] womenRef = new int[n][n];
        for (int w = 0; w < n; w++) {
            int[] prefs = acceptorPrefs[w];
            for (int rank = 0; rank < prefs.length; rank++) {
                int man = prefs[rank];
                womenRef[w][man] = rank;
            }
        }

        // while there is a free man, engage him
        int freeMan = findFreeMan(menFree);
        while (freeMan != -1) {
            // get the free man's preference list.
            int[] manPref = proposerPrefs[freeMan];
            for (int woman : manPref) {
                if (womenFree[woman]) {
                    // if the woman is free, engage her with the man.
                    menFree[freeMan] = false;
                    womenFree[woman] = false;
                    res[freeMan] = woman;
                    resWomen[woman] = freeMan;
                    break;
                } else {
                    // woman is already engaged, so see if she likes the new man potentially
                    int currentMan = resWomen[woman];
                    if (womenRef[woman][freeMan] < womenRef[woman][currentMan]) {
                        // if she prefers the new man, break off her current relationship
                        menFree[currentMan] = true;
                        // engage her with the new man.
                        res[freeMan] = woman;
                        resWomen[woman] = freeMan;
                        menFree[freeMan] = false;
                        break;
                    }
                    // otherwise, she rejects the proposal and the free man moves on to the next
                    // woman.
                }
            }
            // try to find another free man to make a proposal.
            freeMan = findFreeMan(menFree);
        }
        return res;
    }

    /**
     * Helper method to find the index of a free man.
     * 
     * @param menFree an array that indicates whether each man is free.
     * @return the index of a free man, or -1 if none are free.
     */
    private static int findFreeMan(boolean[] menFree) {
        for (int i = 0; i < menFree.length; i++) {
            if (menFree[i]) {
                return i;
            }
        }
        return -1;
    }

    public static void main(String[] args) throws FileNotFoundException {
        String[] testFiles = {
                "io/sample.in.1",
                "io/sample.in.2",
                "io/sample.in.3"
        };
        String[] outputFiles = {
                "io/sample.out.1",
                "io/sample.out.2",
                "io/sample.out.3"
        };
        for (int idx = 0; idx < testFiles.length; idx++) {
            Scanner reader = new Scanner(new File(testFiles[idx]));
            int n = reader.nextInt();
            int[][] proposerPrefs = new int[n][n];
            int[][] acceptorPrefs = new int[n][n];
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    proposerPrefs[i][j] = reader.nextInt();
                }
            }
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    acceptorPrefs[i][j] = reader.nextInt();
                }
            }
            reader.close();
            Scanner outputReader = new Scanner(new File(outputFiles[idx]));
            int[] expectedOutput = new int[n];
            for (int i = 0; i < n; i++) {
                expectedOutput[i] = outputReader.nextInt();
            }
            outputReader.close();

            // Run Gale-Shapley algorithm and assert results
            int[] actualOutput = GaleShapley.galeShapley(n, proposerPrefs, acceptorPrefs);
            assert expectedOutput == actualOutput;
        }
    }
}
