#include <iostream>
#include <map>
#include <string>
#include <tuple>
#include <unordered_map>
#include <vector>
using namespace std;

using int_pair = pair<int, int>;

// Unfortunately, cpp does not natively support hashing pairs or tuples, like
// python does, which we will use to store lab/spots to their students This
// method was copied from the following article
// https://www.geeksforgeeks.org/how-to-create-an-unordered_map-of-pairs-in-c/
struct hash_pair {
  template <class T1, class T2> size_t operator()(const pair<T1, T2> &p) const {
    // Hash the first element
    size_t hash1 = hash<T1>{}(p.first);
    // Hash the second element
    size_t hash2 = hash<T2>{}(p.second);
    // Combine the two hash values
    return hash1 ^ (hash2 + 0x9e3779b9 + (hash1 << 6) + (hash1 >> 2));
  }
};

// Input structure:
// prefs is a vector of vectors of vectors,
//   prefs[0][i] is lab i's vector ranking each student name it would take
//   prefs[1][i] is student i's vector ranking each lab name they would join
// nstuds is the number of students. This is equivalent to the number of slots
// being filled lab_spots is the list of each lab to the number of students they
// are looking to accept labs is a map of lab names to their index studs is a
// map of student names to their index

// returns three things
// a boolean representing whether a stable matching was reached
// a dictionary of labs to their matched students
// a list representing the unfilled slots of each lab

tuple<bool, unordered_map<int_pair, int, hash_pair>, vector<int>>
gale_shapley(const vector<vector<vector<string>>> &prefs, int nstuds,
             vector<int> lab_spots, const vector<vector<bool>> &studs_willing,
             const unordered_map<string, int> &labs,
             const unordered_map<string, int> &studs) {

  bool status = true;
  // we pass in lab_spots by value, so this is a copy
  vector<int> lab_spots_avail = lab_spots;

  // two boolean arrays: labs_free[i][j] is False if spot j of lab i is taken
  // studs_free[i] is False if stud i is in a lab
  vector<vector<bool>> labs_free;
  for (int i : lab_spots) {
    labs_free.push_back(vector<bool>(i, true));
  }
  vector<bool> studs_free(nstuds, true);

  // we initialize a dictionary of (lab, spot) to its student
  // and a dictionary of student to their (lab, spot)
  unordered_map<int_pair, int, hash_pair> labs_to_studs;
  unordered_map<int, int_pair> studs_to_labs;

  // reference dictionary for each student's ranking of a lab
  // we treat lower ranking as better
  unordered_map<int_pair, int, hash_pair> studs_rank;

  for (int studi = 0; studi < prefs[1].size(); ++studi) {
    const vector<string> &stud_prefs = prefs[1][studi];
    for (int rank = 0; rank < stud_prefs.size(); ++rank) {
      int labi = labs.at(stud_prefs[rank]);
      studs_rank[{studi, labi}] = rank;
    }
  }

  // while there is a free spot left, we look for someone to propose to
  int num_open_spots = nstuds;
  while (num_open_spots > 0) {
    bool made_progress = false;
    // iterate to find the free lab spot
    for (int labi = 0; labi < labs_free.size(); ++labi) {

      // none of this labs spots are open
      if (lab_spots_avail[labi] == 0) {
        continue;
      }

      // there exists a free lab spot, find the spot and look for a student to
      // accept your proposal proposing to students in the order of lab's
      // preferences
      const vector<string> &lab_prefs = prefs[0][labi];

      for (int spoti = 0; spoti < labs_free[labi].size(); ++spoti) {

        if (!labs_free[labi][spoti]) {
          continue;
        } // this spot is not open, continue

        for (const string student_name : lab_prefs) {

          int studi = studs.at(
              student_name); // get the index of this student from their name

          // if student is not willing to join this lab; continue
          if (!studs_willing[studi][labi])
            continue;

          // if this student is also free, engage the labspot and the student
          if (studs_free[studi]) {
            studs_free[studi] = false;
            labs_free[labi][spoti] = false;

            // decrement the number of available spots in this lab and overall
            lab_spots_avail[labi]--;
            num_open_spots--;

            // update the dictionaries with this paring
            labs_to_studs[{labi, spoti}] = studi;
            studs_to_labs[studi] = {labi, spoti};
            made_progress = true;
            break;

            // else, check if the student prefers the new lab to the lab she is
            // already paired with. if the student prefers, break off the old
            // lab+spot and pair them with this new lab+spot
          } else {

            int_pair old_lab_info = studs_to_labs[studi];
            int old_lab = old_lab_info.first;
            int old_spot = old_lab_info.second;
            int old_rank = studs_rank[{studi, old_lab}];
            int new_rank = studs_rank[{studi, labi}];

            // we will say that a slot with a lower index is more desirable than
            // one with a higher index this prevents cycling and ensures sorting
            // by lab's ranking of applicants
            if (new_rank < old_rank ||
                (new_rank == old_rank && spoti < old_spot)) {
              labs_free[old_lab][old_spot] = true;
              labs_free[labi][spoti] = false;

              // increment/decrement availability
              lab_spots_avail[labi]--;
              lab_spots_avail[old_lab]++;

              // update with new pairings and remove old pairing
              labs_to_studs.erase({old_lab, old_spot});
              labs_to_studs[{labi, spoti}] = studi;
              studs_to_labs[studi] = {labi, spoti};
              made_progress = true;
              break;
            }
          }
        }

        // this lab could not be matched. No matching exists
        // we still decrement open spots to avoid an infinite loop
        // this information will be represented in the list of available spots
        if (labs_free[labi][spoti]) {
          labs_free[labi][spoti] = false;
          status = false;
          num_open_spots--;
          made_progress = true;
        }
      }
    }
    if (!made_progress)
      break;
  }

  return {status, labs_to_studs, lab_spots_avail};
}

int main() {

  int nlabs;
  int nstuds;

  cin >> nlabs;
  cin >> nstuds;

  // map of lab names to their index number
  // list of lab indices to their name
  unordered_map<string, int> lab_indices;
  vector<string> lab_names;

  // map of student names to their index number
  // list of student indices to their name
  unordered_map<string, int> stud_indices;
  vector<string> stud_names;

  // preference of labs, students
  vector<vector<vector<string>>> prefs(2);

  // amount of students each lab is willing to take
  vector<int> lab_spots;

  // studs_willing[i][j] = True if student i willing to be in lab j
  vector<vector<bool>> studs_willing;
  for (int i = 0; i < nstuds; ++i) {
    studs_willing.push_back(vector<bool>(nlabs, false));
  }

  for (int i = 0; i < nlabs; ++i) {

    // get input
    vector<string> temp;
    string name;
    int spots;
    int nacceptable;
    cin >> name >> spots >> nacceptable;

    // store index name and spots available
    lab_indices[name] = i;
    lab_names.push_back(name);
    lab_spots.push_back(spots);

    // get students will take
    for (int j = 0; j < nacceptable; ++j) {
      string sname;
      cin >> sname;
      temp.push_back(sname);
    }

    // add the preference list
    prefs[0].push_back(temp);
  }

  for (int i = 0; i < nstuds; ++i) {

    // get input
    vector<string> temp;
    string name;
    int nacceptable;
    cin >> name >> nacceptable;

    // store index and name
    stud_indices[name] = i;
    stud_names.push_back(name);

    // get labs would join
    for (int j = 0; j < nacceptable; ++j) {
      string sname;
      cin >> sname;
      temp.push_back(sname);
    }

    // add the preference list
    prefs[1].push_back(temp);
  }

  // add every lab student is willing to be in to set
  for (int studi = 0; studi < nstuds; ++studi) {
    for (string lname : prefs[1][studi]) {
      studs_willing[studi][lab_indices.at(lname)] = true;
    }
  }

  tuple<bool, unordered_map<int_pair, int, hash_pair>, vector<int>>
      result_tuple = gale_shapley(prefs, nstuds, lab_spots, studs_willing,
                                  lab_indices, stud_indices);

  bool status = get<0>(result_tuple);
  unordered_map<int_pair, int, hash_pair> result = get<1>(result_tuple);
  vector<int> final_avail_spots = get<2>(result_tuple);

  // No pairing exists
  if (!status) {
    cout << "False" << endl;
    // No pairing exists
    for (int labi = 0; labi < nlabs; ++labi) {
      // if this lab is unfilled, print name and how many spots left
      if (final_avail_spots[labi] > 0) {
        cout << lab_names.at(labi) << " " << final_avail_spots[labi] << endl;
      }
    }
  } else {
    cout << "True" << endl;
    // print the paired labs and students
    for (int labi = 0; labi < nlabs; ++labi) {
      cout << lab_names.at(labi) << " ";
      int nspots = lab_spots[labi];

      for (int spoti = 0; spoti < nspots - 1; ++spoti) {
        cout << stud_names[result.at({labi, spoti})] << " ";
      }

      cout << stud_names[result.at({labi, nspots - 1})] << endl;
    }
  }
}
