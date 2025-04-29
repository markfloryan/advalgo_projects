import java.io.*;
import java.util.*;

public class Solution {
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st;
        int n = Integer.parseInt(br.readLine().trim());
        int[][] delta = new int[n][2];
        for (int i = 0; i < 2 * n; i++) {
            st = new StringTokenizer(br.readLine());
            int p = Integer.parseInt(st.nextToken());
            char c = st.nextToken().charAt(0);
            int q = Integer.parseInt(st.nextToken());
            delta[p][c == 'a' ? 0 : 1] = q;
        }
        st = new StringTokenizer(br.readLine());
        int s1 = Integer.parseInt(st.nextToken());
        int s2 = Integer.parseInt(st.nextToken());
        int fcount = Integer.parseInt(br.readLine().trim());
        boolean[] isFinal = new boolean[n];
        for (int i = 0; i < fcount; i++) {
            isFinal[Integer.parseInt(br.readLine().trim())] = true;
        }
        // Build reachable pairs
        Map<Long, Integer> pairIndex = new HashMap<>();
        List<long[]> pairs = new ArrayList<>();
        Queue<long[]> queue = new LinkedList<>();
        long startKey = (((long) s1) << 32) | (s2 & 0xffffffffL);
        pairIndex.put(startKey, 0);
        pairs.add(new long[]{s1, s2});
        queue.add(new long[]{s1, s2});
        while (!queue.isEmpty()) {
            long[] cur = queue.poll();
            int p = (int) cur[0], q = (int) cur[1];
            for (int c = 0; c < 2; c++) {
                int np = delta[p][c];
                int nq = delta[q][c];
                long key = (((long) np) << 32) | (nq & 0xffffffffL);
                if (!pairIndex.containsKey(key)) {
                    pairIndex.put(key, pairs.size());
                    pairs.add(new long[]{np, nq});
                    queue.add(new long[]{np, nq});
                }
            }
        }
        int m = pairs.size();
        // Classification: 0=reject,1=half,2=accept
        int[] cls = new int[m];
        for (int i = 0; i < m; i++) {
            long[] pr = pairs.get(i);
            boolean f1 = isFinal[(int) pr[0]];
            boolean f2 = isFinal[(int) pr[1]];
            cls[i] = f1 ? (f2 ? 2 : 1) : (f2 ? 1 : 0);
        }
        // Build transitions over product states
        int[][] prodDelta = new int[m][2];
        for (int i = 0; i < m; i++) {
            long[] pr = pairs.get(i);
            for (int c = 0; c < 2; c++) {
                int np = delta[(int) pr[0]][c];
                int nq = delta[(int) pr[1]][c];
                long key = (((long) np) << 32) | (nq & 0xffffffffL);
                prodDelta[i][c] = pairIndex.get(key);
            }
        }
        // Inverse transitions: for each symbol, list of predecessors for each state
        List<Set<Integer>>[] inv = new List[2];
        for (int c = 0; c < 2; c++) {
            inv[c] = new ArrayList<>();
            for (int i = 0; i < m; i++) inv[c].add(new HashSet<>());
        }
        for (int i = 0; i < m; i++) {
            for (int c = 0; c < 2; c++) {
                inv[c].get(prodDelta[i][c]).add(i);
            }
        }
        // Hopcroft's algorithm
        List<Set<Integer>> P = new ArrayList<>();
        Set<Integer> rset = new HashSet<>(), hset = new HashSet<>(), aset = new HashSet<>();
        for (int i = 0; i < m; i++) {
            if (cls[i] == 0) rset.add(i);
            else if (cls[i] == 1) hset.add(i);
            else aset.add(i);
        }
        if (!rset.isEmpty()) P.add(rset);
        if (!hset.isEmpty()) P.add(hset);
        if (!aset.isEmpty()) P.add(aset);
        Deque<Pair> work = new ArrayDeque<>();
        for (Set<Integer> block : P) {
            for (int c = 0; c < 2; c++) {
                work.add(new Pair(block, c));
            }
        }
        while (!work.isEmpty()) {
            Pair pr = work.poll();
            Set<Integer> A = pr.block;
            int c = pr.sym;
            // X = predecessors of A under c
            Set<Integer> X = new HashSet<>();
            for (int qstate : A) {
                X.addAll(inv[c].get(qstate));
            }
            List<Set<Integer>> newP = new ArrayList<>();
            for (Set<Integer> Y : P) {
                Set<Integer> inter = new HashSet<>(Y);
                inter.retainAll(X);
                if (inter.isEmpty() || inter.size() == Y.size()) {
                    newP.add(Y);
                } else {
                    Set<Integer> diff = new HashSet<>(Y);
                    diff.removeAll(X);
                    newP.add(inter);
                    newP.add(diff);
                    // update worklist
                    for (int sym = 0; sym < 2; sym++) {
                        Pair yPair = new Pair(Y, sym);
                        if (work.remove(yPair)) {
                            work.add(new Pair(inter, sym));
                            work.add(new Pair(diff, sym));
                        } else {
                            // add smaller
                            if (inter.size() <= diff.size()) work.add(new Pair(inter, sym));
                            else work.add(new Pair(diff, sym));
                        }
                    }
                }
            }
            P = newP;
        }
        // Assign block IDs
        int numBlocks = P.size();
        int[] blockId = new int[m];
        for (int i = 0; i < numBlocks; i++) {
            for (int s : P.get(i)) blockId[s] = i;
        }
        // Build minimized transitions
        int[][] minDelta = new int[numBlocks][2];
        for (int i = 0; i < numBlocks; i++) {
            int rep = P.get(i).iterator().next();
            for (int c = 0; c < 2; c++) {
                minDelta[i][c] = blockId[prodDelta[rep][c]];
            }
        }
        // Determine partitions of blocks
        List<Integer> rejBlocks = new ArrayList<>();
        List<Integer> halfBlocks = new ArrayList<>();
        List<Integer> accBlocks = new ArrayList<>();
        for (int i = 0; i < numBlocks; i++) {
            int rep = P.get(i).iterator().next();
            int t = cls[rep];
            if (t == 0) rejBlocks.add(i);
            else if (t == 1) halfBlocks.add(i);
            else accBlocks.add(i);
        }
        // Output
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));
        bw.write(numBlocks + "\n");
        for (int i = 0; i < numBlocks; i++) {
            bw.write(i + " a " + minDelta[i][0] + "\n");
            bw.write(i + " b " + minDelta[i][1] + "\n");
        }
        bw.write(blockId[0] + "\n"); // start block (block of start pair index 0)
        bw.write(rejBlocks.size() + "\n");
        for (int b : rejBlocks) bw.write(b + "\n");
        bw.write(halfBlocks.size() + "\n");
        for (int b : halfBlocks) bw.write(b + "\n");
        bw.write(accBlocks.size() + "\n");
        for (int b : accBlocks) bw.write(b + "\n");
        bw.flush();
    }

    static class Pair {
        Set<Integer> block;
        int sym;
        Pair(Set<Integer> b, int s) {
            block = b;
            sym = s;
        }
        public boolean equals(Object o) {
            if (!(o instanceof Pair)) return false;
            Pair p = (Pair) o;
            return block == p.block && sym == p.sym;
        }
        public int hashCode() {
            return System.identityHashCode(block) * 31 + sym;
        }
    }
}
