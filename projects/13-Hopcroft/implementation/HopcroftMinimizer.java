import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class HopcroftMinimizer {

    // Entry point: run each test case by number
    public static void main(String[] args) {
        try {
            // Loop through test files named sample.in.1, sample.in.2, etc.
            for (int i = 1; i <= 3; i++) {
                runTest(i);
            }
        } catch (IOException e) {
            // Print any I/O errors during parsing or writing
            e.printStackTrace();
        }
    }

    // Inner class to model a DFA’s core components and transformations
    static class DFA {
        Set<String> states;                // All state identifiers
        Set<String> alphabet;              // Valid input symbols
        Map<TransitionKey, String> tf;     // Transition map: (state, symbol) -> next state
        String start;                      // Starting state label
        Set<String> accept;                // Accepting (final) state labels

        // Construct a fresh DFA copying input data to avoid external mutation
        DFA(Set<String> states, Set<String> alphabet,
            Map<TransitionKey, String> tf, String start, Set<String> accept) {
            this.states = new HashSet<>(states);
            this.alphabet = new HashSet<>(alphabet);
            this.tf = new HashMap<>(tf);
            this.start = start;
            this.accept = new HashSet<>(accept);
        }

        // Step 1: Remove states unreachable from start to shrink the machine
        void removeUnreachable() {
            Set<String> reachable = new HashSet<>();
            Deque<String> queue = new ArrayDeque<>();
            queue.add(start);

            // BFS to mark reachable states
            while (!queue.isEmpty()) {
                String s = queue.poll();
                // If not yet visited, explore its outgoing transitions
                if (reachable.add(s)) {
                    for (String c : alphabet) {
                        String t = tf.get(new TransitionKey(s, c));
                        if (t != null && !reachable.contains(t)) {
                            queue.add(t);
                        }
                    }
                }
            }

            // Keep only reachable states in both the state and accept sets
            states.retainAll(reachable);
            accept.retainAll(reachable);
            // Prune transitions that involve any removed state
            tf.entrySet().removeIf(e -> {
                String s = e.getKey().state;
                String t = e.getValue();
                return !reachable.contains(s) || !reachable.contains(t);
            });
        }

        // Step 2: Ensure every state has a transition for each symbol by adding a trap
        void makeTotal() {
            String trap = "__TRAP__";
            boolean needsTrap = false;

            // For each existing state, fill missing symbol transitions to trap
            for (String s : new HashSet<>(states)) {
                for (String c : alphabet) {
                    TransitionKey key = new TransitionKey(s, c);
                    if (!tf.containsKey(key)) {
                        tf.put(key, trap);
                        needsTrap = true;
                    }
                }
            }

            // If any gap was patched, add the trap state looping back to itself
            if (needsTrap) {
                states.add(trap);
                for (String c : alphabet) {
                    tf.put(new TransitionKey(trap, c), trap);
                }
            }
        }

        // Core: apply Hopcroft’s partition refinement to minimize DFA size
        DFA minimize() {
            // Start by pruning and making transitions total
            removeUnreachable();
            makeTotal();

            // Initialize partitions: one for accepting, one for the rest
            List<Set<String>> P = new ArrayList<>();
            Set<String> nonAccept = new HashSet<>(states);
            nonAccept.removeAll(accept);
            if (!accept.isEmpty()) P.add(new HashSet<>(accept));
            if (!nonAccept.isEmpty()) P.add(nonAccept);

            // Worklist of blocks to refine
            Deque<Set<String>> W = new ArrayDeque<>(P);

            // Keep refining until no further splits occur
            while (!W.isEmpty()) {
                Set<String> A = W.poll();
                for (String c : alphabet) {
                    // X = set of states that transition on c into A
                    Set<String> X = new HashSet<>();
                    for (String s : states) {
                        String t = tf.get(new TransitionKey(s, c));
                        if (t != null && A.contains(t)) {
                            X.add(s);
                        }
                    }

                    List<Set<String>> newP = new ArrayList<>();
                    // For each current block, attempt to split by X
                    for (Set<String> Y : P) {
                        Set<String> inter = new HashSet<>(Y);
                        inter.retainAll(X);
                        Set<String> diff = new HashSet<>(Y);
                        diff.removeAll(X);
                        if (!inter.isEmpty() && !diff.isEmpty()) {
                            // Block Y divides into inter and diff
                            newP.add(inter);
                            newP.add(diff);
                            // Maintain refinement worklist
                            if (W.remove(Y)) {
                                W.add(inter);
                                W.add(diff);
                            } else {
                                // Add smaller half to W for balanced splits
                                W.add(inter.size() <= diff.size() ? inter : diff);
                            }
                        } else {
                            // No split needed: keep Y intact
                            newP.add(Y);
                        }
                    }
                    P = newP;
                }
            }

            // Label each final block with a numeric ID sorted by representative name
            List<Set<String>> sortedBlocks = new ArrayList<>(P);
            sortedBlocks.sort(Comparator.comparing(b -> Collections.min(b)));
            Map<Set<String>, String> blockIndex = new HashMap<>();
            for (int i = 0; i < sortedBlocks.size(); i++) {
                blockIndex.put(sortedBlocks.get(i), Integer.toString(i));
            }

            // Build mapping from original state to its block label
            Map<String, String> rep = new HashMap<>();
            for (Set<String> block : sortedBlocks) {
                String idx = blockIndex.get(block);
                for (String s : block) rep.put(s, idx);
            }

            // Assemble new DFA components based on block labels
            Set<String> newStates = new HashSet<>(blockIndex.values());
            String newStart = rep.get(start);
            Set<String> newAccept = new HashSet<>();
            for (String s : accept) newAccept.add(rep.get(s));

            // Define transitions between new states using any representative old state
            Map<TransitionKey, String> newTf = new HashMap<>();
            for (Set<String> block : sortedBlocks) {
                String any = Collections.min(block);
                String from = rep.get(any);
                for (String c : alphabet) {
                    String toState = tf.get(new TransitionKey(any, c));
                    if (toState != null) {
                        newTf.put(new TransitionKey(from, c), rep.get(toState));
                    }
                }
            }

            // Return the minimized automaton as a fresh instance
            return new DFA(newStates, alphabet, newTf, newStart, newAccept);
        }
    }

    // Helper class for state-symbol map keys in transitions
    static class TransitionKey {
        String state;
        String symbol;

        TransitionKey(String state, String symbol) {
            this.state = state;
            this.symbol = symbol;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TransitionKey)) return false;
            TransitionKey that = (TransitionKey) o;
            return state.equals(that.state) && symbol.equals(that.symbol);
        }

        @Override
        public int hashCode() {
            return Objects.hash(state, symbol);
        }
    }

    // Read DFA definition from a CSV file and build the object
    static DFA parseDFAFromCSV(String filename) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            // First line lists column names; symbols start at index 3
            String[] fields = br.readLine().split(",");
            List<String> alphabet = Arrays.asList(fields).subList(3, fields.length);

            Set<String> states = new HashSet<>();
            Map<TransitionKey, String> tf = new HashMap<>();
            String start = null;
            Set<String> accept = new HashSet<>();

            String line;
            // Parse each DFA row into state, flags, and transitions
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                String s = parts[0];
                states.add(s);
                if (parts[1].equals("1")) start = s;
                if (parts[2].equals("1")) accept.add(s);
                // Store each symbol transition for state s
                for (int i = 3; i < parts.length; i++) {
                    tf.put(new TransitionKey(s, alphabet.get(i - 3)), parts[i]);
                }
            }
            return new DFA(states, new HashSet<>(alphabet), tf, start, accept);
        }
    }

    // Write a DFA back to CSV for comparison with expected output
    static void writeDFAtoCSV(DFA dfa, String filename) throws IOException {
        List<String> sortedAlpha = new ArrayList<>(dfa.alphabet);
        Collections.sort(sortedAlpha);
        List<String> sortedStates = new ArrayList<>(dfa.states);
        sortedStates.sort(Comparator.comparingInt(Integer::parseInt));

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename))) {
            // Header row: columns for state and each symbol
            bw.write("state,is_start,is_accept");
            for (String c : sortedAlpha) bw.write("," + c);
            bw.newLine();

            // Each state in numeric order, with start and accept flags
            for (String s : sortedStates) {
                bw.write(s + "," + (s.equals(dfa.start) ? "1" : "0") + ","
                        + (dfa.accept.contains(s) ? "1" : "0"));
                // Followed by transitions under each symbol column
                for (String c : sortedAlpha) {
                    String to = dfa.tf.get(new TransitionKey(s, c));
                    bw.write("," + (to != null ? to : ""));
                }
                bw.newLine();
            }
        }
    }

    // Compare two CSV files line-wise to check for exact matches
    static boolean compareCSVFiles(String f1, String f2) throws IOException {
        List<String> a = Files.readAllLines(new File(f1).toPath());
        List<String> b = Files.readAllLines(new File(f2).toPath());
        return a.equals(b);
    }

    // Run a numbered test: parse, minimize, write result, and compare
    static void runTest(int index) throws IOException {
        String base = "io" + File.separator;
        String in = base + "sample.in." + index;
        String out = base + "sample.out." + index;
        String gen = base + "generated.out." + index;

        System.out.println("Running Test " + index + "...");
        DFA dfa = parseDFAFromCSV(in);
        DFA min = dfa.minimize();
        writeDFAtoCSV(min, gen);
        if (compareCSVFiles(out, gen)) {
            System.out.println("✅ Test " + index + " passed.");
        } else {
            System.out.println("❌ Test " + index + " failed. Compare " + out + " vs " + gen + ".");
        }
    }
}
