from typing import List


# returns an array of n: each index i has a value res[i] which means that
# man i is paired with woman res[i]
def gale_shapley(
    n: int, proposer_prefs: List[List[int]], acceptor_prefs: List[List[int]]
) -> List[int]:
    # boolean arrays to keep track of free proposers and acceptors
    proposer_free = [True] * n
    acceptor_free = [True] * n
    res = [-1] * n
    res_acceptors = [-1] * n

    # dictionary to keep track of the pointers that each proposer keeps to track which acceptor he has proposed to
    proposer_current = {i: 0 for i in range(len(proposer_free))}

    # set to keep track of available proposers with constant time
    available_proposers = set({i for i in range(len(proposer_free))})

    # 2d matrix to store the preferences of the acceptors for fast access for each proposer
    # the key is a tuple (i_a, i_p) where i_a is the index of the acceptor and i_p is the index of the proposer
    # lower the rank, the more preferred the proposer is
    acceptor_reference = {}
    for i_a, pref in enumerate(acceptor_prefs):
        for rank, i_p in enumerate(pref):
            acceptor_reference[(i_a, i_p)] = rank

    while True:
        if available_proposers:
            i_p = available_proposers.pop()
            current_prefs = proposer_prefs[i_p]
            for index in range(proposer_current[i_p], len(current_prefs)):
                i_a = current_prefs[index]
                proposer_current[i_p] = proposer_current[i_p] + 1
                # first case: if this acceptor is free, engage the proposer with the acceptor
                if acceptor_free[i_a]:
                    acceptor_free[i_a] = False
                    proposer_free[i_p] = False
                    res[i_p] = i_a
                    res_acceptors[i_a] = i_p
                    break
                # second case: if the acceptor is not free, check if the new proposer is preferred
                else:
                    old_proposer = res_acceptors[i_a]
                    old_proposer_index = acceptor_reference[(i_a, old_proposer)]
                    new_proposer_index = acceptor_reference[(i_a, i_p)]
                    # if the new proposer is preferred over the old one, redo the
                    # pairing so that the old proposer needs to find a new match
                    # Add the old proposer back to the available proposers set if the old 
                    # proposer needs to find a new match. Otherwise, add the new proposer back 
                    # to the available proposers set if the new proposer is not matched.
                    if new_proposer_index < old_proposer_index:
                        proposer_free[old_proposer] = True
                        proposer_free[i_p] = False
                        res[i_p] = i_a
                        res_acceptors[i_a] = i_p
                        available_proposers.add(old_proposer)
                        break
                    else:
                        available_proposers.add(i_p)

        # if there is no free proposer, return the result
        # it is proven that the algorithm will terminate
        if True not in proposer_free:
            return res


def main():
    for idx, file in enumerate(
        [
            "io/sample.in.1",
            "io/sample.in.2",
            "io/sample.in.3",
        ]
    ):
        with open(file, "r") as f:
            n = int(f.readline().strip())
            proposer_prefs = []
            acceptor_prefs = []
            for i in range(n):
                line = list(map(int, f.readline().strip().split()))
                # print(line)
                proposer_prefs.append(line)
            for i in range(n):
                line = list(map(int, f.readline().strip().split()))
                acceptor_prefs.append(line)
            with open("io/sample.out." + str(idx + 1), "r") as f:
                res = list(map(int, f.readline().strip().split()))
            # print(res, gale_shapley(n, proposer_prefs, acceptor_prefs))
            assert res == gale_shapley(n, proposer_prefs, acceptor_prefs)


if __name__ == "__main__":
    main()
