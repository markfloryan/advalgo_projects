import java.util.*;

public class pcSol_java {
    public static List<Integer> rankBwt(List<Integer> bw, Map<Integer, Integer> tots) {
        List<Integer> ranks = new ArrayList<>();
        for (int val : bw) {
            tots.putIfAbsent(val, 0);
            ranks.add(tots.get(val));
            tots.put(val, tots.get(val) + 1);
        }
        return ranks;
    }

    public static Map<Integer, int[]> firstCol(Map<Integer, Integer> tots) {
        Map<Integer, int[]> first = new TreeMap<>();
        int totc = 0;
        for (int val : new TreeSet<>(tots.keySet())) {
            int count = tots.get(val);
            first.put(val, new int[] { totc, totc + count });
            totc += count;
        }
        return first;
    }

    public static List<Integer> reverseBwt(List<Integer> bw) {
        Map<Integer, Integer> tots = new HashMap<>();
        List<Integer> ranks = rankBwt(bw, tots);
        Map<Integer, int[]> first = firstCol(tots);

        int rowi = 0;
        int sentinel = -1;
        List<Integer> t = new ArrayList<>();
        t.add(sentinel);

        while (!bw.get(rowi).equals(sentinel)) {
            int c = bw.get(rowi);
            t.add(c);
            rowi = first.get(c)[0] + ranks.get(rowi);
        }

        Collections.reverse(t);
        return t;
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        int n = sc.nextInt(), k = sc.nextInt();
        List<Integer> arr = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            arr.add(sc.nextInt());
        }

        List<Integer> elements = new ArrayList<>(arr);
        Collections.sort(elements);

        List<int[]> gaps = new ArrayList<>();
        for (int i = 1; i < n - 1; i++) {
            int f = elements.get(i) + 1;
            int s = elements.get(i + 1) - 1;
            if (f <= s) {
                gaps.add(new int[] { f, s });
            }
        }

        if (k > elements.get(n - 1)) {
            gaps.add(new int[] { elements.get(n - 1) + 1, k });
        }

        for (int i = 2; i < n; i++) {
            if (!elements.get(i).equals(elements.get(i - 1))) {
                gaps.add(new int[] { elements.get(i), elements.get(i) });
            }
        }

        int zero = arr.indexOf(0);
        int ans = 0;

        for (int[] item : gaps) {
            arr.set(zero, item[0]);
            List<Integer> original = reverseBwt(arr);
            if (original.size() == arr.size()) {
                ans += item[1] - item[0] + 1;
            }
        }

        System.out.println(ans);
    }
}
