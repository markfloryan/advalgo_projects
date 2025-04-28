"""
# ORIGINAL CODE FROM HERE: https://nbviewer.org/github/BenLangmead/comp-genomics-class/blob/master/notebooks/CG_BWT_Reverse.ipynb
"""

"""
* Takes in input string t
* Returns list of cyclical rotations of original string (including $) of length n
* Done by doubling original string and getting each set of consecutive n characters
"""
def rotations(t):
    tt = t * 2
    return [ tt[i:i+len(t)] for i in range(0, len(t)) ]

"""
* Takes in input string t
* Returns list of rotations of strings ordered through string comparison (lower value characters first)
* Not case sensitive in this case, and the '$' will show up first
"""
def bwm(t):
    return sorted(rotations(t))

"""
* Takes in input string t
* Returns the Burrows-Wheeler transform (BWT) by utilizing the sorted rotations
* Through the lambda function and 'map', each rotation (x) is mapped to its last character (x[-1])
*   through taking in the iterable array of sorted rotations and utilizing a map from x to x[-1]
* The list of last characters is all concatenated in order into a string through the 'join' function
"""
def bwtViaBwm(t):
    ''' Given T, returns BWT(T) by way of the BWM '''
    return ''.join(map(lambda x: x[-1], bwm(t)))

"""
* Takes in an input string, adds a '$' at the end to keep track of the end of the string
* Applies the BWT using the sorted rotations method and prints out the result
"""
t = input()
t = t + '$'
b = bwtViaBwm(t)
print(b)

"""
* Takes in an input BWT string (one that is already in the form of the BWT)
* Returns 'ranks', a list corresponding to the BWT string that indicates how many times
*   each character has appeared in the BWT string before
* Also returns 'tots', a dictionary composed of each unique character in the BWT string
*   and how many times it appears in the BWT string
"""
def rankBwt(bw):
    # initialize tots, the dictionary, and ranks, the list/array
    tots = dict()
    ranks = []
    for c in bw:
        # for each new character, c, add a 0 element to the dictionary to indicate its first appearance
        if c not in tots:
            tots[c] = 0
        # for a given character, add the rank as how many times it has appeared previously (tots[c])
        ranks.append(tots[c])
        # add 1 to tots[c] since this is a new appearance of c (can be used later if c appears again)
        tots[c] += 1
    return ranks, tots

"""
* Calls rankBwt on the BWT string, thus getting the ranks of each character and how many times
*   each character has appeared in the BWT string
"""
ranks, tots = rankBwt(b)
"""Commented, but can be used for debugging purposes to nicely format the tuple representation of
*   how each character in the BWT string corresponds to the rank
* E.g. ((a, 0), (b, 0), (a, 1), ...)
"""
# print(tuple(zip(b, ranks)))

"""
* Given a dictionary 'tots' mapping characters to their total appearances in a BWT string
*   a dictionary 'first' is returned mapping characters to the first row in a list of sorted
*   rotations that they would start off (prefix)
"""
def firstCol(tots):
    first = {}
    # Sets totc initially to 0, since the first character must appear at index 0
    totc = 0
    # The keys in tots (tots.items()) are iterated through in a sorted fashion
    # c, count -> indicates 'c', the character, and 'count', the total appearances
    for c, count in sorted(tots.items()):
        # Sets first[c] to totc, which is the index the current character c should first appear at
        first[c] = totc
        # Adds count to totc, which indicates that the next character should appear after all
        #   instances of the current character, which would span [totc, totc + (count - 1)]
        totc += count
    return first

# Commented, but can be used to see the dictionary containing at what index in the
#   cyclical rotations each character starts being a prefix
# print(firstCol(tots))

# Commented, but can be used to see (with line separation '\n')
#   what the bwm(t), aka the list of rotations for the original string, looks like
# print('\n'.join(bwm(t)))

"""
* Used to get the final inverse of a BWT string, getting it back to the original string
* Takes in BWT string as input and returns original string, 't' as output
"""
def reverseBwt(bw):
    # make sure to have rank information for each character as well as total instances of each
    ranks, tots = rankBwt(bw)
    # use total instances of characters to determine first instance of prefixing in sorted rotations
    first = firstCol(tots)
    # start at the first row of the sorted rotations, 0, by setting rowi to 0
    rowi = 0
    # set t to the special character '$' indicating the end of the string
    t = '$'
    """
    * BWT reversal relies on the fact that the last characters of the sorted strings are the BWT characters
    * Thus, indexing into the BWT at 0 indicates the character at the end of the string starting with '$'
    * This character is also the end of the original string, since it is cyclically before '$'
    * Thus, for each current character, the corresponding BWT character is the previous character in the string
    * The string is built up by prepending each previous character until the cycle ends by reaching
    *   the last character '$'
    """
    while bw[rowi] != '$':
        # c is the last character at rowi in the sorted rotations
        c = bw[rowi]
        # prepends c before the output string t, as c must come before the starting character of the sorted rotation
        t = c + t
        # rowi is set to the location of c in the sorted rotations
        # first[c] indicates the first location at which c prefixes the sorted rotations
        # ranks[rowi] indicates how many instances of c have prefixed previous sorted rotations
        rowi = first[c] + ranks[rowi]
    return t

# by calling reverseBwt on the BWT string 'b', reverse is set to the inverse of the BWT string
reverse = reverseBwt(b)
# the last character of the inverse of the BWT string should be '$', so by cutting it out by taking
#   every character of the string up until and excluding the last one (-1), the original
#   string should be retrieved and printed
print(reverse[:-1])