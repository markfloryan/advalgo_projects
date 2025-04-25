# Input structure:
# prefs is a list of lists of lists,
#   prefs[0][i] is lab i's list ranking each student name it would take
#   prefs[1][i] is student i's list ranking each lab name they would join
# nstuds is the number of students. This is equivalent to the number of slots being filled
# lab_spots is the list of each lab to the number of students they are looking to accept
# labs is a dictionary of lab names to their index
# studs is a dictionary of student names to their index


# returns three things
# a boolean representing whether a stable matching was reached
# a dictionary of labs to their matched students
# a list representing the unfilled slots of each lab
def gale_shapley(prefs, nstuds, lab_spots, studs_willing, labs, studs):

    status = True

    lab_spots_avail = lab_spots.copy()

    # two boolean arrays: labs_free[i][j] is False if spot j of lab i is taken
    # studs_free[i] is False if stud i is in a lab
    labs_free = [[True] * i for i in lab_spots]
    studs_free = [True] * nstuds

    # we initialize a dictionary of (lab, spot) to its student
    # and a dictionary of student to their (lab, spot)
    labs_to_studs = {}
    studs_to_labs = {}

    # reference dictionary for each student's ranking of a lab
    # we treat lower rankings as better
    studs_rank = {}
    for studi, stud_prefs in enumerate(prefs[1]):
        for rank, labname in enumerate(stud_prefs):
            studs_rank[(studi, labs[labname])] = rank

    num_open_spots = nstuds

    # while there is a free spot left, we look for someone to propose to
    while num_open_spots > 0:
        made_progress = False
        # find the free lab spot
        for labi, spot_list in enumerate(labs_free):

            # none of this labs spots are open
            if lab_spots_avail[labi] == 0:
                continue

            # there exists a free lab spot, find the spot and look for a student to accept your proposal
            # proposing to students in the order of lab's preferences
            lab_prefs = prefs[0][labi]

            for spoti, spot_free in enumerate(spot_list):

                if not spot_free:  # this spot is not open, continue
                    continue

                for i in range(len(lab_prefs)):

                    studi = studs[lab_prefs[i]]

                    # if student is not willing to join this lab; continue
                    if not studs_willing[studi][labi]:
                        continue

                    # if this student is also free, engage the labspot and the student
                    if studs_free[studi]:

                        studs_free[studi] = False
                        labs_free[labi][spoti] = False

                        # decrement the number of available spots in this lab and overall
                        lab_spots_avail[labi] -= 1
                        num_open_spots -= 1

                        # update the dictionaries with this paring
                        labs_to_studs[(labi, spoti)] = studi
                        studs_to_labs[studi] = (labi, spoti)
                        made_progress = True
                        break

                    # else, check if the student prefers the new lab to the lab she is already paired with.
                    # if the student prefers, break off the old lab+spot and pair them with this new lab+spot
                    else:

                        old_lab, old_spot = studs_to_labs[studi]
                        old_lab_rank = studs_rank[(studi, old_lab)]
                        new_lab_rank = studs_rank[(studi, labi)]

                        # we will say that a slot with a lower index is more desirable than one with a higher index
                        # this prevents cycling and ensures sorting by lab's ranking of applicants
                        if new_lab_rank < old_lab_rank or (
                            new_lab_rank == old_lab_rank and spoti < old_spot
                        ):

                            labs_free[old_lab][old_spot] = True
                            labs_free[labi][spoti] = False

                            # increment/decrement availability
                            lab_spots_avail[labi] -= 1
                            lab_spots_avail[old_lab] += 1

                            # update with new pairings and remove old pairing
                            labs_to_studs[(labi, spoti)] = studi
                            labs_to_studs.pop((old_lab, old_spot))
                            studs_to_labs[studi] = (labi, spoti)
                            made_progress = True
                            break

                # this lab could not be matched. No matching exists
                # we still decrement open spots to avoid an infinite loop
                # this information will be represented in the list of available spots
                if labs_free[labi][spoti]:
                    labs_free[labi][spoti] = False
                    status = False
                    num_open_spots -= 1
                    made_progress = True

        if not made_progress:
            break

    return status, labs_to_studs, lab_spots_avail


nlabs = int(input())
nstuds = int(input())
# map of lab names to their index number
# list of lab indices to their name
lab_indices = {}
lab_names = []
# map of student names to their index number
# list of student indices to their name
stud_indices = {}
stud_names = []
# preference of labs, students
prefs = [[], []]
# amount of students each lab is willing to take
lab_spots = []

# studs_willing[i][j] = True if student i willing to be in lab j
studs_willing = [[False for j in range(nlabs)] for i in range(nstuds)]

for i in range(nlabs):
    # get input
    values = input().strip().split()
    name = values[0]
    spots = int(values[1])
    # store index and spots available
    lab_indices[name] = i
    lab_names.append(name)
    lab_spots.append(spots)
    # add preferences
    prefs[0].append(values[3:])

for i in range(nstuds):
    # get input
    values = input().strip().split()
    name = values[0]
    # store index
    stud_indices[name] = i
    stud_names.append(name)
    # add preferences
    prefs[1].append(values[2:])

# add every lab student is willing to be in to set
for stud in range(nstuds):
    for lname in prefs[1][stud]:
        studs_willing[stud][lab_indices[lname]] = True

status, result, final_avail_spots = gale_shapley(
    prefs, nstuds, lab_spots, studs_willing, lab_indices, stud_indices
)

print(status)
# no pairing exists
if not status:
    # print the unpaired labs with number of slots to fill
    for labi in range(nlabs):
        if final_avail_spots[labi] > 0:
            print(lab_names[labi], final_avail_spots[labi], sep=" ")

else:
    # print the dictionary of labs to their students
    for labi in range(nlabs):
        print(lab_names[labi], end=" ")
        nspots = lab_spots[labi]
        for spoti in range(nspots - 1):
            print(stud_names[result[(labi, spoti)]], end=" ")
        print(stud_names[result[(labi, nspots - 1)]])
