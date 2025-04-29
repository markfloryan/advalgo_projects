import csv
import os
from collections import deque, defaultdict

# A lightweight class to model a Deterministic Finite Automaton (DFA)
class DFA:
    def __init__(self, states, alphabet, transition_function, start_state, accept_states):
        # Initialize DFA components: state set, input alphabet, transition map,
        # designated start state, and set of accepting states.
        self.states = states
        self.alphabet = alphabet
        self.tf = transition_function
        self.start = start_state
        self.accept = set(accept_states)

    def remove_unreachable_states(self):
        # Identify all states reachable from the start via breadth-first search
        reachable = set()
        queue = deque([self.start])

        while queue:
            state = queue.popleft()
            if state not in reachable:
                reachable.add(state)
                # Follow each possible input symbol to find next states
                for c in self.alphabet:
                    target = self.tf.get((state, c))
                    if target and target not in reachable:
                        queue.append(target)

        # Discard unreachable states and prune accept set accordingly
        self.states = reachable
        self.accept &= reachable

        # Rebuild transition function to include only reachable transitions
        self.tf = {
            (s, c): t for (s, c), t in self.tf.items()
            if s in reachable and t in reachable
        }

    def make_total(self):
        # Ensure every state has an outgoing transition for each symbol
        trap = "__TRAP__"
        needs_trap = False

        # Fill missing transitions with a trap placeholder
        for state in self.states:
            for symbol in self.alphabet:
                if (state, symbol) not in self.tf:
                    self.tf[(state, symbol)] = trap
                    needs_trap = True

        # If any gap was filled, add the trap state itself and loop it
        if needs_trap:
            self.states.add(trap)
            for symbol in self.alphabet:
                self.tf[(trap, symbol)] = trap

    def minimize(self):
        # Step 1: Prune unreachable states and complete the machine
        self.remove_unreachable_states()
        self.make_total()

        # Step 2: Initialize partitions: accepting vs non-accepting
        P = [self.accept, self.states - self.accept]
        W = deque(P.copy())  # worklist of partitions to refine

        # Step 3: Refine partitions until stable
        while W:
            A = W.popleft()

            # For each symbol, find states transitioning into A
            for c in self.alphabet:
                X = {s for s in self.states if self.tf.get((s, c)) in A}

                new_P = []
                for Y in P:
                    # Split Y into those in X vs outside X
                    inter = X & Y
                    diff = Y - X

                    if inter and diff:
                        # Replace Y with its two parts
                        new_P.extend([inter, diff])

                        # Manage worklist updates for efficiency
                        if Y in W:
                            W.remove(Y)
                            W.extend([inter, diff])
                        else:
                            # Always split the smaller block next
                            W.append(inter if len(inter) <= len(diff) else diff)
                    else:
                        # No split needed, carry Y forward
                        new_P.append(Y)

                # Commit all splits at once to current partition list
                P = new_P

        # Step 4: Assign each block a numeric label for new states
        sorted_blocks = sorted([sorted(block) for block in P], key=lambda b: b[0])
        block_map = {frozenset(block): i for i, block in enumerate(sorted_blocks)}

        # Build reverse map: state -> block index
        representative = {
            state: block_map[frozenset(block)]
            for block in sorted_blocks for state in block
        }

        # Identify new DFA components based on blocks
        new_states = set(block_map.values())
        new_start = representative[self._find_block(self.start, P)]
        new_accept = {representative[s] for s in self.accept}
        new_tf = {}

        # For each block, pick any member to determine outgoing transitions
        for block in P:
            rep = next(iter(block))
            for c in self.alphabet:
                target = self.tf.get((rep, c))
                if target is not None:
                    tgt_block = block_map[frozenset(self._find_block(target, P))]
                    new_tf[(representative[rep], c)] = tgt_block

        # Return a fresh DFA object representing the minimized machine
        return DFA(new_states, self.alphabet, new_tf, new_start, new_accept)

    def _find_block(self, state, partition):
        # Locate which block in the partition contains the given state
        for block in partition:
            if state in block:
                return tuple(block)  # return a sample member of that block
        return None

# Helpers for CSV I/O and testing follow the same pattern

def parse_dfa_from_csv(filename):
    """
    Read a DFA description from CSV with columns:
    state, is_start, is_accept, <symbol1>, <symbol2>, ...
    """
    with open(filename, 'r') as f:
        reader = csv.DictReader(f)
        rows = list(reader)
        alphabet = reader.fieldnames[3:]

        states = set()
        tf = {}
        start_state = None
        accept_states = set()

        # Gather states, start/accept flags, and transition entries
        for row in rows:
            state = row['state']
            states.add(state)
            if row['is_start'] == '1':
                start_state = state
            if row['is_accept'] == '1':
                accept_states.add(state)

            for sym in alphabet:
                tf[(state, sym)] = row[sym]

    return DFA(states, set(alphabet), tf, start_state, accept_states)


def write_dfa_to_csv(dfa, filename):
    # Prepare rows by grouping transitions per state
    state_data = defaultdict(dict)
    for (state, symbol), target in dfa.tf.items():
        state_data[state][symbol] = target

    # Build a sorted list of rows for predictable ordering
    rows = []
    for state in sorted(dfa.states):
        row = {
            'state': state,
            'is_start': '1' if state == dfa.start else '0',
            'is_accept': '1' if state in dfa.accept else '0'
        }
        for sym in sorted(dfa.alphabet):
            row[sym] = state_data[state].get(sym, '')
        rows.append(row)

    # Write out to file with header plus transition columns
    fieldnames = ['state', 'is_start', 'is_accept'] + sorted(dfa.alphabet)
    with open(filename, 'w', newline='') as f:
        writer = csv.DictWriter(f, fieldnames=fieldnames)
        writer.writeheader()
        writer.writerows(rows)


def compare_csv_files(file1, file2):
    # Load each CSV as a list of dicts and compare equality
    def read_csv_as_dict(file):
        with open(file, 'r') as f:
            return list(csv.DictReader(f))
    return read_csv_as_dict(file1) == read_csv_as_dict(file2)


def run_test(index):
    # Automate a test run given an index for input/output filenames
    base = 'io'
    input_file = os.path.join(base, f'sample.in.{index}')
    expected_output = os.path.join(base, f'sample.out.{index}')
    generated_output = os.path.join(base, f'generated.out.{index}')

    print(f"Running Test {index}...")
    dfa = parse_dfa_from_csv(input_file)
    minimized = dfa.minimize()
    write_dfa_to_csv(minimized, generated_output)

    if compare_csv_files(expected_output, generated_output):
        print(f"✅ Test {index} passed.")
    else:
        print(f"❌ Test {index} failed. Compare {expected_output} vs {generated_output}.")


if __name__ == '__main__':
    # Execute tests 1–3 by default; adjust range as needed
    for i in range(1, 4):
        run_test(i)
