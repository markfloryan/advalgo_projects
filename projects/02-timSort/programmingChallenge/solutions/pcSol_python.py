from functools import cmp_to_key
import math

#function for calculating MinRun, it divides the size of the 
#dataset in two until it's less than 32 and then keeps the remainder so it's close to a power of two
def calculate_min_run(n):
    r = 0
    while n >= 32:
        r |= n & 1
        n >>= 1
    return n + r

#basic insertion sort
def insertion_sort(arr, left, right, key):
    if left < right and key(arr[left]) > key(arr[left+1]):
        #in place descending run detection
        is_desc = True
        for k in range(left, right):
            if key(arr[k]) < key(arr[k+1]):
                is_desc = False #no longer descending, continue with insertion
                break
        #in place run reversal
        if is_desc:
            i, j = left, right
            while i < j:
                arr[i], arr[j] = arr[j], arr[i]
                i += 1
                j -= 1
            return
    #basic insertion sort
    for i in range(left + 1, right + 1):
        temp = arr[i]
        j = i - 1
        while j >= left and key(arr[j]) > key(temp):
            arr[j + 1] = arr[j]
            j -= 1
        arr[j + 1] = temp

# exponential search to find where a given element x belongs in arr
def gallop(x, arr, start, key):
    hi = 1
    n = len(arr)
    # multilpy hi by 2 until its idx is >= x, the current element in the "winning" or greater list,
    # or the end of the list is reached
    while start + hi < n and key(x) > key(arr[start + hi]):
        hi *= 2
    # binary search between the second to last element checked and the element selected to pinpoint
    # where x belongs
    lo = hi // 2
    hi = min(start + hi, n)
    while lo < hi:
        mid = (lo + hi) // 2
        if key(x) > key(arr[mid]):
            lo = mid + 1
        else:
            hi = mid
    return lo # x's sorted position

#  combine two sorted subarrays [left,mid] and [mid+1,right]
#  track consecutive wins to trigger galloping mode to reduce the number of individual comparisons
def merge(arr, left, mid, right, key):
    # copy runs so we can merge in place
    left_part = arr[left:mid + 1]
    right_part = arr[mid + 1:right + 1]

    i = j = 0
    k = left
    min_gallop = 7 #galloping threshold
    count_left = count_right = 0

    #core merge logic
    while i < len(left_part) and j < len(right_part):
        if key(left_part[i]) <= key(right_part[j]):
            arr[k] = left_part[i]
            i += 1
            count_left += 1 # keeping track of left subarray "wins" for gallop mode
            count_right = 0
        else:
            arr[k] = right_part[j]
            j += 1
            count_right += 1 # keeping track of right subarray "wins" for gallop
            count_left = 0
        k += 1

        # one side wins individual comparisons more than min_gallop times
        # if left run is smaller call gallop to find the index of the right one where x fits
        if count_left >= min_gallop:
            pos = gallop(right_part[j], left_part, i, key)
            #  copy the part of array that was skipped over
            while i < pos:
                arr[k] = left_part[i]
                i += 1
                k += 1
            count_left = 0 # reset the counter 

        # if right run is smaller call gallop to find the index of the left one where x fits
        elif count_right >= min_gallop:
            pos = gallop(left_part[i], right_part, j, key)
            #  copy the part of array that was skipped over
            while j < pos:
                arr[k] = right_part[j]
                j += 1
                k += 1
            count_right = 0 # reset the counter 

    # copy over any leftover elements, only 1 will run, the other is already done
    while i < len(left_part):
        arr[k] = left_part[i]
        i += 1
        k += 1

    while j < len(right_part):
        arr[k] = right_part[j]
        j += 1
        k += 1

def timsort(arr, key=lambda x: x):
    # define vars
    n = len(arr)
    min_run = calculate_min_run(n)
    run_stack = []

    # first pass: split into runs and call insertionsort
    i = 0
    while i < n:
        # end of current run is either i + minRun (offset) or the end of the list
        run_end = min(i + min_run - 1, n - 1)
        insertion_sort(arr, i, run_end, key)
        run_stack.append((i, run_end))  # push run to stack
        i = run_end + 1  # push run to stack
        merge_collapse(arr, run_stack, key)
    # merge runs according to merge policy until there is 1 run on the stack
    while len(run_stack) > 1:
        merge_at(arr, run_stack, len(run_stack) - 2, key)

# enforces timsort's merge policy
def merge_collapse(arr, run_stack, key):
    while len(run_stack) > 2:
        # get the length of the first three runs on the stack
        # C is at the top of the stack, A is closest to the bottom.
        A = run_stack[-3]
        B = run_stack[-2]
        C = run_stack[-1]
        lenA = A[1] - A[0] + 1
        lenB = B[1] - B[0] + 1
        lenC = C[1] - C[0] + 1

        # per merge policy: 
        # A should be larger than B + C
        # B should be larger than C
        if lenA <= lenB + lenC or lenB <= lenC:
            if lenA < lenC:
                merge_at(arr, run_stack, len(run_stack) - 3, key)
            else:
                merge_at(arr, run_stack, len(run_stack) - 2, key)
        else:
            # merge policy holds, no merges necessary
            break

    # merge when there are 2 runs left on the stack
    if len(run_stack) == 2 and (run_stack[-2][1] - run_stack[-2][0] + 1) <= (run_stack[-1][1] - run_stack[-1][0] + 1):
        merge_at(arr, run_stack, len(run_stack) - 2, key)

# merge runs at a location on the stack
def merge_at(arr, run_stack, i, key):
    # copy subsections
    start1, end1 = run_stack[i]
    start2, end2 = run_stack[i + 1]
    merge(arr, start1, end1, end2, key)
    # update stack
    run_stack[i] = (start1, end2)
    del run_stack[i + 1]

#polar angle comparator for "twist"
def polar_angle_comparator(a, b):
    ax, ay, ra, ia = a
    bx, by, rb, ib = b

    # Check Above/Below Lighthouse
    def half(x, y):
        return y > 0 or (y == 0 and x >= 0)

    ha = half(ax, ay)
    hb = half(bx, by)

    if ha != hb:
        return -1 if ha else 1

    # Cross product for angle
    cross = ax * by - ay * bx
    if cross != 0:
        return -1 if cross > 0 else 1

    # Sort by radius
    if ra != rb:
        return -1 if ra < rb else 1

    # Sort by order of appearance (i)
    if ia != ib:
        return -1 if ia < ib else 1

    return 0

# calculate radius for every point and append index (x, y) -> (x, y, r, i)
# and make a hashmap with a key of points "(x, y, r, i)" and value of "boatname"
num_ships, num_queries = list(map(int, input().split()))
ships = {}
ships_sorted_by_r = []

for i in range(num_ships):
    x, y, ship_name = input().split()
    x, y = int(x), int(y)
    r = math.sqrt(x**2 + y**2)
    point = (x, y, r, i)
    ships[point] = ship_name
    ships_sorted_by_r.append(point)

# timsort points by polar angle, radius, and index
timsort(ships_sorted_by_r, key=cmp_to_key(polar_angle_comparator))

for q in range(num_queries):
    # query ships under given radius
    radius = float(input())
    ships_queried = [ships[point] for point in ships_sorted_by_r if point[2] <= radius]
    print(*ships_queried if ships_queried else [-1])