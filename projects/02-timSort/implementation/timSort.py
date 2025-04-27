# returns a number equal to or slighly less than a power of 2 between 32 and 64
def calculate_min_run(n):
    r = 0
    while n >= 32:
        r |= n & 1  # record if a bit will be shifted off
        n >>= 1     # divide n by 2
    return n + r

def insertion_sort(arr, left, right):
    # detect for descending runs
    if left < right and arr[left] > arr[left+1]:
        is_desc = True
        for k in range(left, right):
            if arr[k] < arr[k+1]:
                is_desc = False # no longer a descending run, continue insertion sort as normal
                break
        if is_desc:
            # reverse in-place
            i, j = left, right
            while i < j:
                arr[i], arr[j] = arr[j], arr[i]
                i += 1
                j -= 1
            return # sorted run
    # if hte runs is not descending, use standard insertion sort implementation
    for i in range(left + 1, right + 1):
        temp = arr[i]
        j = i - 1
        while j >= left and arr[j] > temp:
            arr[j + 1] = arr[j]
            j -= 1
        arr[j + 1] = temp

# exponential search to find where a given element x belongs in arr
def gallop(x, arr, start):
    hi = 1
    n = len(arr)
    # multilpy hi by 2 until its idx is >= x, the current element in the "winning" or greater list,
    # or the end of the list is reached
    while start + hi < n and x > arr[start + hi]:
        hi *= 2
    # binary search between the second to last element checked and the element selected to pinpoint
    # where x belongs
    lo = hi // 2
    hi = min(start + hi, n)
    while lo < hi:
        mid = (lo + hi) // 2
        if x > arr[mid]:
            lo = mid + 1
        else:
            hi = mid
    return lo # x's sorted position

#  combine two sorted subarrays [left,mid] and [mid+1,right]
#  track consecutive wins to trigger galloping mode to reduce the number of individual comparisons
def merge(arr, left, mid, right):
    # copy runs so we can merge in place
    left_part = arr[left:mid + 1]
    right_part = arr[mid + 1:right + 1]

    i = j = 0
    k = left
    min_gallop = 7 # threshold to switch to galloping mode
    count_left = count_right = 0

    # core merge logic
    while i < len(left_part) and j < len(right_part):
        if left_part[i] <= right_part[j]:
            arr[k] = left_part[i]
            i += 1
            count_left += 1     # keeping track of left subarray "wins"
            count_right = 0
        else:
            arr[k] = right_part[j]
            j += 1
            count_right += 1    # keeping track of right subarray "wins"
            count_left = 0
        k += 1

        # one side wins individual comparisons more than min_gallop times
        # if left run is smaller call gallop to find the index of the right one where x fits
        if count_left >= min_gallop:
            pos = gallop(right_part[j], left_part, i)
            #  copy the part of array that was skipped over
            while i < pos:
                arr[k] = left_part[i]
                i += 1
                k += 1
            count_left = 0  # reset the counter
        
        # if right run is smaller call gallop to find the index of the left one where x fits
        elif count_right >= min_gallop:
            pos = gallop(left_part[i], right_part, j)
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

def timsort(arr):
    # define vars
    n = len(arr)
    min_run = calculate_min_run(n)
    run_stack = []

    # first pass: split into runs and call insertionsort
    i = 0
    while i < n:
        # end of current run is either i + minRun (offset) or the end of the list
        run_end = min(i + min_run - 1, n - 1)
        insertion_sort(arr, i, run_end)
        run_stack.append((i, run_end))  # push run to stack
        i = run_end + 1     # update offsest
        merge_collapse(arr, run_stack)
    # merge runs according to merge policy until there is 1 run on the stack
    while len(run_stack) > 1:
        merge_at(arr, run_stack, len(run_stack) - 2)

# enforces timsort's merge policy
def merge_collapse(arr, run_stack):
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
                merge_at(arr, run_stack, len(run_stack) - 3)
            else:
                merge_at(arr, run_stack, len(run_stack) - 2)
        else:
            # merge policy holds, no merges necessary
            break

    # merge when there are 2 runs left on the stack
    if len(run_stack) == 2 and (run_stack[-2][1] - run_stack[-2][0] + 1) <= (run_stack[-1][1] - run_stack[-1][0] + 1):
        merge_at(arr, run_stack, len(run_stack) - 2)

# merge runs at a location on the stack
def merge_at(arr, run_stack, i):
    # copy subsections
    start1, end1 = run_stack[i]
    start2, end2 = run_stack[i + 1]
    merge(arr, start1, end1, end2)
    # update stack
    run_stack[i] = (start1, end2)
    del run_stack[i + 1]

# main:
arr_size = int(input())
arr = [int(input()) for i in range(arr_size)]
timsort(arr)
print(*arr)
