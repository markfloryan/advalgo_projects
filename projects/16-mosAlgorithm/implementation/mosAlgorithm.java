import java.util.*;
import java.util.LinkedHashSet;

public class MoAlgorithm {

    public interface QueryData {
        void init();
        void add(int idx);
        void remove(int idx);
        int[] answer();
    }

    // get the block of the left index of each query, then tie-break by right index
    private static int sortWithBlockSize(Query q1, Query q2, int blockSize) {
        int b1 = q1.l / blockSize; // block of left index for q1
        int b2 = q2.l / blockSize; // block of left index for q2
        if (b1 != b2) return b1 - b2;
        return q1.r - q2.r; // if blocks equal, sort by right index
    }

    private static class Query {
        int l, r, idx;
        Query(int l, int r, int idx) {
            this.l = l; this.r = r; this.idx = idx;
        }
    }

    public static class Mo {
        private int blockSize;
        private QueryData data; // data is an object that has init(), add(idx), remove(idx), and answer() functions

        public Mo(int blockSize, QueryData data) {
            this.blockSize = blockSize;
            this.data = data;
        }

        public int[][] query(int[][] queries) {
            int n = queries.length;

            // create objects bundling each query [l,r] with its original index
            List<Query> list = new ArrayList<>(n);
            for (int i = 0; i < n; i++) {
                list.add(new Query(queries[i][0], queries[i][1], i));
            }
            Collections.sort(list, (q1, q2) -> sortWithBlockSize(q1, q2, blockSize));

            int[][] results = new int[n][2];
            data.init();
            int l = 0, r = -1;

            for (Query q : list) {
                // while the current query's left index is less than the current query range, decrement the 
                // query range left index and add to the range data structure
                while (q.l < l) { 
                    l--;
                    data.add(l);
                }
                // while the current query's right index is greater than the current query range, increment 
                // the query range right index and add to the range data structure
                while (r < q.r) { 
                    r++;
                    data.add(r);
                }
                // while the current query's left index is greater than the current query range, increment 
                // the query range left index and remove from the range data structure
                while (l < q.l) { 
                    data.remove(l);
                    l++;
                }
                // while the current query's right index is less than the current query range, decrement the 
                // query range right index and remove from the range data structure
                while (q.r < r) { 
                    data.remove(r);
                    r--;
                }
                results[q.idx] = data.answer(); // get the current answer and write it to the index of the query's original position
            }
            return results;
        }
    }

    public static class ModeData implements QueryData {
        private int[] array;
        private Map<Integer,Integer> frequencies;
        private List<Set<Integer>> buckets;
        private int modeFreq;

        public ModeData(int[] array) {
            this.array = array;
        }

        @Override
        public void init() {
            frequencies = new HashMap<>(); // maps numbers to current frequencies
            
            // array of frequency buckets, index 0 represents frequency 1 (so numbers with "zero" 
            // frequency are not stored in any buckets)
            buckets = new ArrayList<>(array.length); 
            for (int i = 0; i < array.length; i++) {
                buckets.add(new LinkedHashSet<>());
            }
            modeFreq = 0; // stores the frequency of the current mode
        }

        // convenience functions for handling frequencies being offset by 1 from bucket indices
        private void addToBucket(int freq, int item) {
            if (freq - 1 < 0) return;
            buckets.get(freq - 1).add(item);
        }

        private void removeFromBucket(int freq, int item) {
            if (freq - 1 < 0) return;
            buckets.get(freq - 1).remove(item);
        }

        @Override
        public void add(int idx) {
            int val = array[idx];
            int oldF = frequencies.getOrDefault(val, 0); // add to frequencies map if not yet seen
            
            // remove from current bucket and insert into next bucket (next 3 lines)
            removeFromBucket(oldF, val);
            int newF = oldF + 1;
            frequencies.put(val, newF);
            addToBucket(newF, val);
            if (newF > modeFreq) modeFreq = newF; // if this is the new mode, increment the mode
        }

        @Override
        public void remove(int idx) {
            int val = array[idx];
            int oldF = frequencies.getOrDefault(val, 0);
            removeFromBucket(oldF, val);
            
            // if val's frequency is the mode and its the last in its bucket, decrement the mode
            if (oldF == modeFreq && buckets.get(oldF - 1).isEmpty()) {
                modeFreq--;
            }
            int newF = oldF - 1;
            frequencies.put(val, newF);
            addToBucket(newF, val); // insert val into the previous bucket
        }

        @Override
        public int[] answer() {
            // get an arbitrary element of buckets[modeFreq]
            Iterator<Integer> it = buckets.get(modeFreq - 1).iterator();
            if (it.hasNext())
                return new int[]{ it.next(), modeFreq };
            else
                return new int[]{ -1, 0 };
            }
        }

    public static void main(String[] args) {
        int[][] qs = {{0,4}, {0,6}, {0,3}, {1,4}, {1,6}, {4,9}, {3,10}};
        int[] arr = {8,3,4,5,3,2,3,1,3,2,8,10,11,3,2};
        ModeData data = new ModeData(arr);
        Mo mo = new Mo(3, data);
        int[][] res = mo.query(qs);
        for (int i = 0; i < qs.length; i++) {
            System.out.println(Arrays.toString(qs[i]) + " -> " + Arrays.toString(res[i]));
        }
    }
}
