from itertools import permutations

def rankBwt(bw):
    '''      
    * Given a BWT-transformed array bw, construct:
     * 1. ranks: for each character, how many times it has appeared so far.
     * 2. tots: the total count of each character in the array.
     * This data is essential for reversing the BWT later using LF-mapping. 
     '''
    tots = dict()
    ranks = []
    for val in bw:
        if val not in tots:
            tots[val] = 0
        ranks.append(tots[val]) # assign the current count as rankß
        tots[val] += 1 # update the total count for the value
    return ranks, tots

def firstCol(tots):
    ''' 
    * Given a frequency map tots of character counts,
     * compute the range of row indices (in the first column of the BWT matrix) that each character occupies. This helps simulate sorting without building the full matrix explicitly.
     '''
    first = {}
    totc = 0
    for val, count in sorted(tots.items()):
        first[val] = (totc, totc + count) # assign start and end index range
        totc += count # increment total character count seen so far
    return first

def reverseBwt(bw):
    '''     
    * Reverse the Burrows-Wheeler Transform using LF-mapping
     * Start from the row with sentinel (-1), and repeatedly map backwards through the BWT matrix until the original string is rebuilt in reverse.
     '''
    ranks, tots = rankBwt(bw)
    first = firstCol(tots)
    rowi = 0 #  start at row 0 where sentinel is assumed to be
    sentinel = -1
    t = [sentinel] # initialize the output with the sentinel
    while bw[rowi] != sentinel:
        c = bw[rowi]
        t.append(c) # add character to result
        rowi = first[c][0] + ranks[rowi] # jump to previous row using LF-mapping
    t = t[::-1]
    return t


# print(reverseBwt([1, 4, 10, -1]))
n, k = [int(x) for x in input().split()]
arr = [int(x) for x in input().split()]
elements = sorted(arr) # get the sorted elements in the array
gaps = []
"""
From the sorted elements, get consecutive elements, and if there is a gap, add it to our gaps array
"""
for i in range(1, n-1):
    # we add 1 to the left, and subtract 1 to the right to just get the elements in between
    f = elements[i]+1
    s = elements[i+1]-1
    if f <= s:
        gaps.append((f, s)) 
if k > elements[-1]: # make sure to add a gap between largest element and K
    gaps.append((elements[-1]+1, k))
for i in range(2, n): # also make sure to check the elements in the actual array
    if elements[i] != elements[i-1]:
        gaps.append((elements[i], elements[i]))
zero = arr.index(0)
ans =0
"""
     * For each candidate value range in gaps:
     * Replace the zero with a trial value and try to reverse the BWT.
     * If the reverse operation succeeds (i.e., length matches), add the full gap size to answer.
"""
for item in gaps:
    arr[zero] = item[0]
    original = reverseBwt(arr)
    if len(original) == len(arr): # the original array is valid if there are no missing elements
        ans += item[1] - item[0]+1
print(ans)