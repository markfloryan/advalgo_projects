import java.util.*;

public class pcSol_java {
    public static List<Integer> rankBwt(List<Integer> bw, Map<Integer, Integer> tots) {
        /*
         * Given a BWT-transformed array bw, construct:
         * 1. ranks: for each character, how many times it has appeared so far.
         * 2. tots: the total count of each character in the array.
         * This data is essential for reversing the BWT later using LF-mapping.
         */
        List<Integer> ranks = new ArrayList<>();
        for (int val : bw) {
            tots.putIfAbsent(val, 0);
            ranks.add(tots.get(val)); // assign the current count as rank
            tots.put(val, tots.get(val) + 1); // update the total count for the value
        }
        return ranks;
    }

    public static Map<Integer, int[]> firstCol(Map<Integer, Integer> tots) {
        /*
         * Given a frequency map tots of character counts,
         * compute the range of row indices (in the first column of the BWT matrix) that
         * each character occupies. This helps simulate sorting without building the
         * full matrix explicitly.
         */
        Map<Integer, int[]> first = new TreeMap<>();
        int totc = 0;
        for (int val : new TreeSet<>(tots.keySet())) {
            int count = tots.get(val);
            first.put(val, new int[] { totc, totc + count });// assign start and end index range
            totc += count; // increment total character count seen so far
        }
        return first;
    }

    public static List<Integer> reverseBwt(List<Integer> bw) {
        /*
         * Reverse the Burrows-Wheeler Transform using LF-mapping
         * Start from the row with sentinel (-1), and repeatedly map backwards through
         * the BWT matrix until the original string is rebuilt in reverse.
         */
        Map<Integer, Integer> tots = new HashMap<>();
        List<Integer> ranks = rankBwt(bw, tots);
        Map<Integer, int[]> first = firstCol(tots);

        int rowi = 0; // start at row 0 where sentinel is assumed to be
        int sentinel = -1;
        List<Integer> t = new ArrayList<>(); // initialize the output with the sentinel
        t.add(sentinel);

        while (!bw.get(rowi).equals(sentinel)) {
            int c = bw.get(rowi);
            t.add(c); // add character to result
            rowi = first.get(c)[0] + ranks.get(rowi); // jump to previous row using LF-mapping
        }

        Collections.reverse(t); // reverse to restore original order
        return t;
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        int n = sc.nextInt(), k = sc.nextInt();
        List<Integer> arr = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            arr.add(sc.nextInt()); // input the BWT-transformed array
        }

        List<Integer> elements = new ArrayList<>(arr);
        Collections.sort(elements);

        List<int[]> gaps = new ArrayList<>();
        /*
         * From the sorted elements, get consecutive elements, and if there is a gap,
         * add it to our gaps array
         */
        for (int i = 1; i < n - 1; i++) {
            // add 1 to the left, and subtract 1 to right to just get elements in between
            int f = elements.get(i) + 1;
            int s = elements.get(i + 1) - 1;
            if (f <= s) {
                gaps.add(new int[] { f, s });
            }
        }

        if (k > elements.get(n - 1)) { // make sure to add a gap between largest element and K
            gaps.add(new int[] { elements.get(n - 1) + 1, k });
        }

        for (int i = 2; i < n; i++) { // # also make sure to check the elements in the actual array
            if (!elements.get(i).equals(elements.get(i - 1))) {
                gaps.add(new int[] { elements.get(i), elements.get(i) });
            }
        }

        int zero = arr.indexOf(0);
        int ans = 0;
        /*
         * For each candidate value range in gaps:
         * Replace the zero with a trial value and try to reverse the BWT.
         * If the reverse operation succeeds (i.e., length matches), add the full gap
         * size to answer.
         */
        for (int[] item : gaps) {
            arr.set(zero, item[0]);
            List<Integer> original = reverseBwt(arr);
            if (original.size() == arr.size()) { // # the original array is valid if there are no missing elements
                ans += item[1] - item[0] + 1; // all values in this range are valid
            }
        }

        System.out.println(ans);
    }
}
