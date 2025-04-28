#include <fstream>
#include <iostream>
#include <queue>
#include <string>
#include <vector>

using ll = long long;

std::vector<int>
gale_shapley(int num_pairs,
             const std::vector<std::vector<int>> &proposer_preferences,
             const std::vector<std::vector<int>> &acceptor_preferences)
{
  // vector to store who each proposer is currently matched to (–1 = unmatched)
  std::vector<int> match_for_proposer(num_pairs, -1);
  std::vector<int> match_for_acceptor(num_pairs, -1);

  // vector to store index of next acceptor that a proposer will propose to
  std::vector<int> next_proposal_index(num_pairs, 0);

  // 2d matrix of proposer rankings given an acceptor
  // fast (O(1)) lookup for "what is proposer p's rank in acceptor a's ranking?"
  // https://mr-easy.github.io/2018-08-19-programming-gale-shapley-algorithm-in-cpp/
  std::vector<std::vector<int>> acceptor_rank(num_pairs,
                                              std::vector<int>(num_pairs));
  for (int a = 0; a < num_pairs; ++a)
  {
    for (int rank_pos = 0; rank_pos < num_pairs; ++rank_pos)
    {
      int p = acceptor_preferences[a][rank_pos];
      acceptor_rank[a][p] = rank_pos;
    }
  }

  // we begin with all of the proposers being free. We will process each of
  // these one by one.
  std::queue<int> free_proposers;
  for (int p = 0; p < num_pairs; ++p)
  {
    free_proposers.push(p);
  }

  while (!free_proposers.empty())
  {
    int p = free_proposers.front();
    free_proposers.pop();

    // pick the next acceptor on p's list
    int a = proposer_preferences[p][next_proposal_index[p]++];

    // first case: if a is free, then match p and a
    if (match_for_acceptor[a] == -1)
    {
      match_for_proposer[p] = a;
      match_for_acceptor[a] = p;
      continue;
    }

    // second case: if a isn't free, check if a favors p more than its current
    // match. if so, choose that
    int p2 = match_for_acceptor[a];
    if (acceptor_rank[a][p] < acceptor_rank[a][p2])
    {
      // reassign matches to reflect p being the best assignment for a
      match_for_proposer[p] = a;
      match_for_proposer[p2] = -1;
      match_for_acceptor[a] = p;
      // p2 becomes free again
      free_proposers.push(p2);
    }
    else
    {
      // if acceptor rejects, we must reconsider this proposer
      free_proposers.push(p);
    }
  }

  return match_for_proposer;
}

int main()
{
  std::vector<std::string> input_files = {"io/sample.in.1",
                                          "io/sample.in.2",
                                          "io/sample.in.3"};
  std::vector<std::string> output_files = {"io/sample.out.1",
                                           "io/sample.out.2",
                                           "io/sample.out.3"};
  int T = input_files.size();
  for (int i = 0; i < T; ++i)
  {
    std::ifstream in(input_files[i]);
    std::ifstream exp_out(output_files[i]);
    int n;
    in >> n;

    // read in preferences
    std::vector<std::vector<int>> proposer_preferences(n, std::vector<int>(n));
    for (int p = 0; p < n; ++p)
      for (int j = 0; j < n; ++j)
        in >> proposer_preferences[p][j];

    std::vector<std::vector<int>> acceptor_preferences(n, std::vector<int>(n));
    for (int a = 0; a < n; ++a)
      for (int j = 0; j < n; ++j)
        in >> acceptor_preferences[a][j];

    auto match_for_proposer =
        gale_shapley(n, proposer_preferences, acceptor_preferences);

    std::vector<int> expected_matching(n);
    for (int j = 0; j < n; ++j)
    {
      exp_out >> expected_matching[j];
    }

    // assert matching is equivalent to output file
    if (match_for_proposer != expected_matching)
    {
      std::cerr << "Tests failed." << std::endl;
      return 1;
    }
  }

  std::cout << "All " << T << " tests passed with flying colors." << std::endl;
  return 0;
}
