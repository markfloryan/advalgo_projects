import java.io.*;
import java.util.*;

public class PcSolJava {
    // simple pair to hold timestamp + username
    static class Message {
        int timestamp;
        String user;
        Message(int t, String u) {
            this.timestamp = t;
            this.user = u;
        }
    }

    // holds a query in index space, plus its original position
    static class Query {
        int l, r, idx;
        Query(int l, int r, int idx) {
            this.l = l;
            this.r = r;
            this.idx = idx;
        }
    }

    static class UniqueUsersData {
        List<Message> messages;
        Map<String,Integer> counts;

        // constructor sorts messages by timestamp so we can binary search later
        UniqueUsersData(List<Message> messages) {
            this.messages = messages;
            this.messages.sort(Comparator.comparingInt(m -> m.timestamp));
        }

        // reset frequency map before processing queries
        void init() {
            counts = new HashMap<>();
        }

        // add messages[idx] into the current window, incrementing that user's count
        void add(int idx) {
            String name = messages.get(idx).user;
            counts.put(name, counts.getOrDefault(name, 0) + 1);
        }

        // remove messages[idx] from the window, decrementing (and maybe deleting)
        // that user's count
        void remove(int idx) {
            String name = messages.get(idx).user;
            int c = counts.get(name) - 1;
            if (c == 0) counts.remove(name);
            else counts.put(name, c);
        }

        // answer is simply the number of distinct keys remaining
        int answer() {
            return counts.size();
        }
    }

    static class Mo {
        int blockSize;
        UniqueUsersData data;

        // blockSize = floor(sqrt(M)), data implements init/add/remove/answer
        Mo(int blockSize, UniqueUsersData data) {
            this.blockSize = blockSize;
            this.data = data;
        }

        int[] query(List<Query> queries) {

            // copy and sort queries by l/blockSize, with r as tie-breaker
            // to minimize pointer moves
            List<Query> sorted = new ArrayList<>(queries);
            Collections.sort(sorted, (q1, q2) -> {
                int b1 = q1.l / blockSize; // get left block of q1
                int b2 = q2.l / blockSize; //get left block of q2
                if (b1 != b2) return b1 - b2;
                return q1.r - q2.r; // tie-break by right index if blocks are equal
            });

            int[] results = new int[queries.size()]; // to store answers in original order
            data.init(); // reset data structure
            int l = 0, r = -1; // current window is empty

            for (Query q : sorted) {
                // handle "no messages in time window" case
                if (q.l == -1 && q.r == -1) {
                    results[q.idx] = 0;
                    continue;
                }
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

    // binary-search lower_bound: first index where arr[idx] >= key
    private static int lowerBound(int[] arr, int key) {
        int lo = 0, hi = arr.length;
        while (lo < hi) {
            int mid = (lo + hi) / 2;
            if (arr[mid] < key) lo = mid + 1;
            else hi = mid;
        }
        return lo;
    }

    // binary-search upper_bound: first index where arr[idx] > key
    private static int upperBound(int[] arr, int key) {
        int lo = 0, hi = arr.length;
        while (lo < hi) {
            int mid = (lo + hi) / 2;
            if (arr[mid] <= key) lo = mid + 1;
            else hi = mid;
        }
        return lo;
    }

    public static void main(String[] args) throws IOException {
        // fast IO setup
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(in.readLine());
        int M = Integer.parseInt(st.nextToken());
        int Q = Integer.parseInt(st.nextToken());

        // read all messages (timestamp + username)
        List<Message> messages = new ArrayList<>();
        for (int i = 0; i < M; i++) {
            st = new StringTokenizer(in.readLine());
            int ts = Integer.parseInt(st.nextToken());
            String user = st.nextToken();
            messages.add(new Message(ts, user));
        }

        // store the raw timestamp ranges so we can print them later
        int[] rawL = new int[Q], rawR = new int[Q];
        for (int i = 0; i < Q; i++) {
            st = new StringTokenizer(in.readLine());
            rawL[i] = Integer.parseInt(st.nextToken());
            rawR[i] = Integer.parseInt(st.nextToken());
        }

        // extract sorted timestamps array for binary-search
        int[] timestamps = new int[M];
        for (int i = 0; i < M; i++) {
            timestamps[i] = messages.get(i).timestamp;
        }

        // translate each timestamp query into an index-range [l_idx, r_idx]
        List<Query> indexed = new ArrayList<>();
        for (int i = 0; i < Q; i++) {
            int l_ts = rawL[i], r_ts = rawR[i];
            int l_idx = lowerBound(timestamps, l_ts); // first msg geq start time
            int r_idx = upperBound(timestamps, r_ts) - 1; // last msg leq end time
            if (l_idx <= r_idx) {
                indexed.add(new Query(l_idx, r_idx, i)); // valid index range; add to list
            } else {
                // “no messages in this time window”
                indexed.add(new Query(-1, -1, i));
            }
        }

        // run Mo’s algorithm on the indexed queries
        UniqueUsersData dataObj = new UniqueUsersData(messages);
        int blockSize = (int) Math.floor(Math.sqrt(M));
        Mo mo = new Mo(blockSize, dataObj);
        int[] answers = mo.query(indexed);

        // print results
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Q; i++) {
            sb.append(answers[i])
              .append("\n");
        }
        System.out.print(sb);
    }
}
