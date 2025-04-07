from itertools import permutations

def rankBwt(bw):
    ''' Given BWT array bw, return parallel list of B-ranks.
        Also returns tots: map from value to # times it appears. '''
    tots = dict()
    ranks = []
    for val in bw:
        if val not in tots:
            tots[val] = 0
        ranks.append(tots[val])
        tots[val] += 1
    return ranks, tots

def firstCol(tots):
    ''' Return map from value to the range of rows prefixed by that value '''
    first = {}
    totc = 0
    for val, count in sorted(tots.items()):
        first[val] = (totc, totc + count)
        totc += count
    return first

def reverseBwt(bw):
    ''' Make original list from BWT list bw '''
    ranks, tots = rankBwt(bw)
    first = firstCol(tots)
    rowi = 0
    sentinel = -1
    t = [sentinel]
    while bw[rowi] != sentinel:
        c = bw[rowi]
        t.append(c) # Prepend to result
        rowi = first[c][0] + ranks[rowi]
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
Test each gap
If the element works, then we add to our answer the number of elements in that gap, as all elements will work if one element works
"""
for item in gaps:
    arr[zero] = item[0]
    original = reverseBwt(arr)
    if len(original) == len(arr): # the original array is valid if there are no missing elements
        ans += item[1] - item[0]+1
print(ans)