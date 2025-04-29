# number of digits at which to switch to naive multiplication
MULT_THRESHOLD = 128

# we define a PhiNum to be a 3-tuple:
# the first element is the sign of the number (1 or -1)
# the second element is an array of digits representing a base phi number
# the third element is an integer representing the "offset", that is,
# the place value of the least significant element of the first array
# for example, 4 = 100.01 would be represented as (1, [1, 0, 0, 0, 1], -2)

# input: the sign, numArr, and offset of the phiNum to trim
# optional parameter left_zeros: how many zeros to force on the left
# optional parameter right_zeros: how many zeros to force on the right
# output: the equivalent phiNum with 0s on the start and end removed
def trimZeros(sign, numArr, offset, left_zeros=0, right_zeros=0):
    # find the first and last nonzero entry in the number
    # also update the offset accordingly
    first_nonzero = 0
    while first_nonzero < len(numArr) and not numArr[first_nonzero]:
        first_nonzero += 1
    last_nonzero = len(numArr)-1
    while last_nonzero >= 0 and not numArr[last_nonzero]:
        last_nonzero -= 1
        offset += 1

    # special case for the input phiNum being zero
    if first_nonzero > last_nonzero:
        return (1, [0] * (left_zeros + right_zeros), (offset - len(numArr)))
    
    # general case: construct the resulting phiNum
    trimmed_numArr = [0] * left_zeros + numArr[first_nonzero : last_nonzero+1] + [0] * right_zeros
    offset -= right_zeros
    return (sign, trimmed_numArr, offset)

# input: an array of digits representing a number in base phi
# can contain 2s, but consecutive digits may add up to no more than 2
# output: an array of digits representing a number in base phi with equivalent value
# and all digits 0 or 1, and no two 1s are consecutive
# will eliminate some unnecessary leading and trailing zeros
# to prevent them from piling up, but result may still contain some
# leading and trailing zeros

def reduceSumTo01(sign, numArr, offset):
    # slice out the leading and trailing zeros
    # but create extra leading and trailing zeros
    # to prevent carries from going off the edge
    sign, res, offset = trimZeros(sign, numArr, offset, 2, 2)

    # applies transformations going from left to right
    # to remove 2s and 3s from the summed number
    ptr = 0
    while (ptr < len(res)):
        # only process 2s and 3s, otherwise move on
        if (res[ptr] >= 2):
            if (res[ptr+1] >= 1):
                # you have a ..21.. pattern
                # use the identity 011 = 100 in base phi
                res[ptr+1] -= 1
                res[ptr] -= 1
                res[ptr-1] += 1
            elif (res[ptr-1] >= 1):
                # you have a ..12.. pattern; handle as before
                res[ptr-1] -= 1
                res[ptr] -= 1
                res[ptr-2] += 1
            else:
                # you have a ..020.. or ..030.. pattern
                # use the identity 0200 = 1001 in base phi
                res[ptr] -= 2
                res[ptr-1] += 1
                res[ptr+2] += 1
        ptr += 1
    canonicalize_inplace(res)
    return sign, res, offset

# input: an array of digits representing a number in base phi
# can contain -1s but must be the result of a 
# positive difference of canonical base phi numbers
# output: an array of digits representing a number in base phi with equivalent value
# and all digits 0 or 1, and no two 1s are consecutive
# also aims to eliminate some unnecessary leading and trailing zeros
# to prevent them from piling up

def reduceDiffTo01(sign, numArr, offset):
    # slice out the leading and trailing zeros
    # add 3 trailing zeros to prevent carries from going off the edge
    sign, res, offset = trimZeros(sign, numArr, offset, 0, 3)

    # applies transformations going from left to right
    # to remove -1s from the difference
    ptr = 1
    # when we've moved left because of a 0(-1) pattern, 
    # and the chain ends, keep track so we can jump back
    # to where we left off
    max_ptr = 1

    # this process can create 2s, but they will be surrounded by 0s
    # so they can be eliminated by reduceSumTo01()
    # we basically reduce this to a problem we've already solved
    while (ptr < len(res)):
        if (res[ptr] < 0 and res[ptr-1] == 0):
            # checks for a 0(-1) pattern
            # use the identity 100 = 011 in base phi
            res[ptr-2] -= 1
            res[ptr-1] += 1
            res[ptr] += 1

            # need to check if the -1 in position ptr-2
            # causes more carries to the left
            ptr -= 2
        elif (res[ptr] < 0):
            # checks for a 1(-1) or 2(-1) pattern
            # use the same identity 100 = 011 but shifted
            res[ptr-1] -= 1
            res[ptr] += 1
            res[ptr+1] += 1

            # this time it's only possible for carries to propagate to the right
            max_ptr += 1
            ptr = max_ptr
        else:
            max_ptr += 1
            ptr = max_ptr

    # get rid of the 2s and adjacent 1s now
    return reduceSumTo01(sign, res, offset)

# input: an array of digits representing a number in base phi
# all digits must be 0 or 1
# NOTE: first digit must be 0 
# this method is a helper method and does not handle padding for carries
# converts this array into one with the same base-phi value
# but where no two 1s are next to each other
# output: none
def canonicalize_inplace(numArr):
    # whenever you see two 1s next to each other, replace them
    # and move to the left
    ptr = 1
    # when we've moved left because of a chain of two 1s next to each other
    # and the chain ends,
    # we keep track of where the chain started so we can jump back there
    max_ptr = 1

    # sweep through the number left to right
    while (ptr < len(numArr)):
        # check if adjacent bits are both 1
        if (ptr >= 1 and numArr[ptr] + numArr[ptr-1] == 2):
            # use the identity 011 = 100 in base phi
            numArr[ptr] -= 1
            numArr[ptr-1] -= 1
            numArr[ptr-2] += 1

            # need to check if the 1 in position ptr-2
            # causes more carries to the left
            ptr -= 2
        else:
            max_ptr += 1
            ptr = max_ptr

# input: a phiNum (sign, numArr, offset)
# numArr is an array of digits representing a number in base phi
# all digits must be 0 or 1
# output: an array of digits representing the same number in base phi
# where all digits are 0 or 1 and no two 1s are adjacent
# also aims to eliminate some unnecessary leading or trailing zeros
# to prevent them from piling up
def canonicalize(sign, numArr, offset):
    # slice out the leading and trailing zeros
    # but create one extra leading 0 for potential carries
    sign, res, offset = trimZeros(sign, numArr, offset, 1, 0)

    canonicalize_inplace(res) # use the helper method as written above
    return (sign, res, offset)

# input: sign, digit array, and offsets of two numbers
# in base phi, must be in canonical form
# (all digits 0 or 1, and no two 1s next to each other)
# output: their sum in base phi, in canonical form
def addHelper(sign1, arr1, offset1, sign2, arr2, offset2):
    # align offsets; they must be the same to add correctly
    # this is analogous to normal addition where you have to align the decimal point
    endAlign1 = []
    endAlign2 = []
    while offset1 > offset2:
        endAlign1.append(0)
        offset1 -= 1
    while offset2 > offset1:
        endAlign2.append(0)
        offset2 -= 1

    # determine how many 0s to add at the start of arr1 and arr2
    # to make them the same length, to aid in adding them together
    startAlign1 = [0]*(len(arr2)+len(endAlign2)-len(arr1)-len(endAlign1))
    startAlign2 = [0]*(len(arr1)+len(endAlign1)-len(arr2)-len(endAlign2))

    # put the start and end padding zeros onto arr1 and arr2
    padded1 = startAlign1 + arr1 + endAlign1
    padded2 = startAlign2 + arr2 + endAlign2

    # check if signs match
    if sign1 == sign2:
        # if so, perform an addition and convert back to canonical form
        # new offset is minimum of given offsets
        return reduceSumTo01(sign1, [i+j for i,j in zip(padded1, padded2)], offset1)
    else:
        # if not, perform a subtraction and convert back to canonical form

        # first, determine which number has larger absolute value
        # (so we subtract large-small)
        # two numbers in canonical form can be compared in 
        # the usual lexicographical way
        compare = 0
        for i,j in zip(padded1, padded2):
            compare = i-j
            if compare:
                break

        if compare == 1:
            # first number has greater absolute value than second: compute first-second
            return reduceDiffTo01(sign1, [i-j for i,j in zip(padded1, padded2)], offset1)
        elif compare == -1:
            # second number has greater absolute value than first: compute second-first
            return reduceDiffTo01(sign2, [j-i for i,j in zip(padded1, padded2)], offset1)
        else:
            # numbers are equal, return 0
            return (1, [0], offset1)

# input: two PhiNums in canonical form
# a PhiNum is a tuple (sign, digit array, offsets)
# canonical form means all digits 0 or 1, and no two 1s next to each other
# output: their sum in base phi, in canonical form
def add(phiNum1, phiNum2):
    return trimZeros(*addHelper(phiNum1[0], phiNum1[1], phiNum1[2], phiNum2[0], phiNum2[1], phiNum2[2]))

# input: two PhiNums in canonical form
# a PhiNum is a tuple (sign, digit array, offsets)
# canonical form means all digits 0 or 1, and no two 1s next to each other
# output: their difference in base phi, in canonical form
def subtract(phiNum1, phiNum2):
    return trimZeros(*addHelper(phiNum1[0], phiNum1[1], phiNum1[2], -phiNum2[0], phiNum2[1], phiNum2[2]))

# input: two PhiNums in canonical form
# a PhiNum is a tuple (sign, digit array, offsets)
# canonical form means all digits 0 or 1, and no two 1s next to each other
# output: their product in base phi, in canonical form
def multiply(phiNum1, phiNum2):
    sign1, arr1, offset1 = phiNum1
    sign2, arr2, offset2 = phiNum2

    # compute the sign of the product
    sign = sign1 * sign2

    # for each 1 in the second digit, add a copy of arr1
    # shifted by the place value of the 1 in arr2
    res = (sign, [0], phiNum1[2])
    for digitIdx, digit in enumerate(arr2):
        if digit == 1:
            # place value of each digit in arr2 is:
            # (offset2) + (distance of digitIdx from the right)
            # distance from the right is (len(arr2)-1) - digitIdx
            placeValue = offset2 + len(arr2) - 1 - digitIdx
            res = add(res, (sign, arr1, offset1 + placeValue))

    return trimZeros(*res);


# multiply a PhiNum by a (base-10) whole number constant k
# runs in quadratic time
# works by recursively dividing k in half:
# if k is even, compute (k/2) + (k/2)
# if k is odd, reduce to even by computing (k-1) + (k)
def intmul(phinum, k):
    # base cases
    if k == 0: return (1, [0], 0)
    if k == 1: return phinum

    # odd case
    if k%2 == 1: return add(phinum, intmul(phinum, k-1))

    # even case
    half = intmul(phinum, k//2)
    return add(half, half)

# utility function
# given integers a and b, return a+b*phi in canonical form
def phiNum(a, b=0):
    # compute the signs of a and b
    a_sign = 1 if a>=0 else -1
    b_sign = 1 if b>=0 else -1
    
    # compute a and b in canonical form, and add them together
    # note that (a_sign, [1], 0) is the number 1 with appropriate sign
    # and (b_sign, [1], 1) is phi with appropriate sign
    a_canonical = intmul((a_sign, [1], 0), abs(a))
    b_canonical = intmul((b_sign, [1], 1), abs(b))
    return add(a_canonical, b_canonical)

# input: any phiNum (need not be in canonical form)
# output: a phiNum whose value is multiplied by phi^amount
# runs in O(1)
def shift(phiNum1, amount):
    sign, arr, offset = phiNum1
    return (sign, list(arr), offset+amount)

# input: any phiNum (need not be in canonical form)
# output: a phiNum with same absolute value and opposite sign
# runs in O(1)
def negate(phiNum1):
    sign, arr, offset = phiNum1
    return (-sign, list(arr), offset)

# precompute certain values that we will use later
# in particular, we will eventually need to multiply by this matrix:
#
# A^-1 = [[1, 0, 0, 0, 0], 
#        [-2, 1+2*phi, -1-phi, 2-phi, -1-2*phi], 
#        [-2+2*phi, -1-2*phi, 2+phi, 1-phi, 2+4*phi], 
#        [3-2*phi, 1, -1, -3+2*phi, -2-2*phi], 
#        [0, 0, 0, 0, 1]]
#
# we will precompute the middle three rows to save a little time later
# for an explanation of how this matrix is used, see the interpolation step
# in the toom_3 implementation
inv_10 = phiNum(-2)
inv_11 = phiNum(1, 2)
inv_12 = phiNum(-1, -1)
inv_13 = phiNum(2, -1)
inv_14 = phiNum(-1, -2)

inv_20 = phiNum(-2, 2)
inv_21 = phiNum(-1, -2)
inv_22 = phiNum(2, 1)
inv_23 = phiNum(1, -1)
inv_24 = phiNum(2, 4)

inv_30 = phiNum(3, -2)
inv_33 = phiNum(-3, 2)
inv_34 = phiNum(-2, -2)

# Toom-3 multiplication implementation
# input: two phiNums in canonical form
# output: their product, in canonical form
# runs in O(n^1.49) where the exponent is log(5)/log(3)
def toom_3(phiNum1, phiNum2):
    sign1, arr1, offset1 = phiNum1
    sign2, arr2, offset2 = phiNum2

    # base cases: check if the number of digits in either number
    # is less than the threshold, and if so, multiply naively
    # note that naive multiplication in base phi with this implementation
    # is faster when the second argument is guaranteed to have few digits
    if len(arr2) <= MULT_THRESHOLD:
        return multiply(phiNum1, phiNum2)
    if len(arr1) <= MULT_THRESHOLD:
        return multiply(phiNum2, phiNum1)
    
    # splitting: select a block_size, and split each number into chunks
    # of at most this size (the first chunk can be smaller)
    block_size = max(len(arr1)//3, len(arr2)//3)+1

    # phiNum1 will split into chunks m2, m1, m0 (in that order)
    # phiNum2 will split into chunks n2, n1, n0
    # each chunk is also a phiNum with sign, digits, and offset
    # we use negative indexing because we take block_size digits
    # at a time from the right
    m2 = (sign1, arr1[:-2*block_size], offset1)
    m1 = (sign1, arr1[-2*block_size:-block_size], offset1)
    m0 = (sign1, arr1[-block_size:], offset1)

    n2 = (sign2, arr2[:-2*block_size], offset2)
    n1 = (sign2, arr2[-2*block_size:-block_size], offset2)
    n0 = (sign2, arr2[-block_size:], offset2)

    # evaluation: this defines the following polynomials
    # p(x) = m2*x^2 + m1*x + m0
    # q(x) = n2*x^2 + n1*x + n0
    # we need to figure out r(x), evaluated at phi

    # we choose points 0, 1, phi, phi^2, infinity
    # where evaluating at infinity just means 
    # taking the limit as (x -> infinity) of p(x)/x^(deg p)
    # this simplifies calculations a little bit

    # p(0) = m0, q(0) = n0
    p_0 = m0 
    q_0 = n0

    # p(1) = m2 + m1 + m0, q(1) = n2 + n1 + n0
    p_1 = add(m2, add(m1, m0)) 
    q_1 = add(n2, add(n1, n0))

    # p(phi) = phi^2*m2 + phi*m1 + m0, likewise for q(phi)
    p_phi = add(shift(m2, 2), add(shift(m1, 1), m0))
    q_phi = add(shift(n2, 2), add(shift(n1, 1), n0))

    # p(phi^2) = phi^4*m2 + phi^2*m1 + m0, likewise for q(phi)
    p_phi2 = add(shift(m2, 4), add(shift(m1, 2), m0))
    q_phi2 = add(shift(n2, 4), add(shift(n1, 2), n0))
    
    # p(infinity) = m2, q(infinity) = n2
    p_inf = m2
    q_inf = n2

    # pointwise multiplication: find r(x) at the values we want
    # by just multiplying through: r(x) = p(x)q(x)
    r_0 = toom_3(p_0, q_0)
    r_1 = toom_3(p_1, q_1)
    r_phi = toom_3(p_phi, q_phi)
    r_phi2 = toom_3(p_phi2, q_phi2)
    r_inf = toom_3(p_inf, q_inf)

    # interpolation: determine the coefficients of r(x)
    # store them in variables v0, v1, v2, v3, v4
    # notice that if we had the coefficients in a vector v, we could
    # multiply it by the following matrix to extract the evaluated points:
    #
    # A = [[1, 0, 0, 0, 0], 
    #      [1, 1, 1, 1, 1], 
    #      [1, phi, phi^2, phi^3, phi^4], 
    #      [1, phi^2, phi^4, phi^6, phi^8], 
    #      [0, 0, 0, 0, 1]]
    #
    # A*v = [r_0, r_1, r_phi, r_phi2, r_inf]
    #
    # the inverse is precomputed, as follows:
    # note: the evaluation points 0, 1, phi, phi^2, infinity
    # were carefully chosen such that all entries in the inverse
    # have finite representations in base phi
    # this is not the case, for example, for the set {0, 1, -1, 2, infinity}
    #
    # A^-1 = [[1, 0, 0, 0, 0], 
    #        [-2, 1+2*phi, -1-phi, 2-phi, -2*phi-1], 
    #        [2*phi-2, -2*phi-1, 2+phi, 1-phi, 2+4*phi], 
    #        [3-2*phi, 1, -1, 2*phi-3, -2*phi-2], 
    #        [0, 0, 0, 0, 1]]
    #
    # here we make use of the values we precomputed earlier
    # so we don't have to recompute them every time
    # the values 1 and -1 are special and don't require a multiplication
    #
    # A^-1 = [[1, 0, 0, 0, 0], 
    #        [inv_10, inv_11, inv_12, inv_13, inv_14], 
    #        [inv_20, inv_21, inv_22, inv_23, inv_24], 
    #        [inv_30, 1, -1, inv_33, inv_34], 
    #        [0, 0, 0, 0, 1]]
    #
    # (A^-1)(r_0, r_1, r_phi, r_phi2, r_inf) = (v0, v1, v2, v3, v4)

    v0 = r_0
    v1 = add(add(multiply(r_0, inv_10), multiply(r_1, inv_11)), 
             add(multiply(r_phi, inv_12), add(multiply(r_phi2, inv_13), multiply(r_inf, inv_14))))
    v2 = add(add(multiply(r_0, inv_20), multiply(r_1, inv_21)), 
             add(multiply(r_phi, inv_22), add(multiply(r_phi2, inv_23), multiply(r_inf, inv_24))))
    v3 = add(add(multiply(r_0, inv_30), r_1), 
             add(negate(r_phi), add(multiply(r_phi2, inv_33), multiply(r_inf, inv_34))))
    v4 = r_inf

    # recomposition: evaluate r(phi^block_size) to get our answer
    # amounts to adding these coefficients shifted by block_size * power

    return add(add(v0, shift(v1, block_size)),
               add(shift(v2, 2*block_size), add(shift(v3, 3*block_size), shift(v4, 4*block_size)))) 

# input: a list of phiNums in INCREASING order of significance
# (this is reversed from the usual representation)
# and a base, expressed in base phi
# output: a single phiNum representing the value of the list of phiNums
# if interpreted as a single number in base b
# equivalently, this is phiNumList[0]*b^0 + phiNumList[1]*b^1 + ...
# NOTE: this method may modify the phiNumList
def baseConvHelper(phiNumList, base):
    # base case
    if len(phiNumList) == 1:
        return phiNumList[0]
    
    # recursive step: combine two numbers at a time
    # reduces to a list of half the size
    # and squares the base
    # if the list has odd length, add a 0 at the end 
    # for the last number to combine with
    if len(phiNumList)%2 == 1:
        phiNumList.append(phiNum(0))
    
    combinedList = [add(toom_3(phiNumList[i+1], base), phiNumList[i]) for i in range(0, len(phiNumList), 2)]
    return baseConvHelper(combinedList, toom_3(base, base))

# input: a single number in base 10 expressed as a string
# output: the phiNum with this value
# runs in O(n^1.49), same as Toom-Cook
def baseConv(numStr):
    phiNumList = [phiNum(int(digit)) for digit in reversed(numStr)]
    return baseConvHelper(phiNumList, phiNum(10))

# input constraint: each coin has position within [-10000, 10000]
MAX_ABS_COIN = 10000

# read in inputs: k and n
k = input().strip()
n = int(input().strip())

# read in inputs: n lines of integers
coin_input = []
for _ in range(n):
    # ignore the first integer, because that just tells us
    # how many integers to expect
    coin_input.append(list(map(int, input().split()))[1:])

# convert k to base phi
k_phi = baseConv(k)

# for each input, calculate the product k * (input) in base phi
for coins in coin_input:
    # convert the list of positions into a number in base phi
    # index 0 represents the most significant possible place value (MAX_ABS_COIN)
    # index 1 represents the next most after that (MAX_ABS_COIN-1)
    # therefore, each coin represents a 1 at index (MAX_ABS_COIN-coin)
    coin_phi = [0 for i in range(-MAX_ABS_COIN, MAX_ABS_COIN+1)]
    for coin in coins:
        coin_phi[MAX_ABS_COIN-coin] = 1

    # there could still be multiple consecutive 1s, so deal with this here
    canonical_coin_phi = canonicalize(1, coin_phi, -MAX_ABS_COIN)

    # multiply!
    sign, numArr, offset = toom_3(canonical_coin_phi, k_phi)

    # print the place values of all the 1s in the result
    for idx, digit in enumerate(numArr):
        # place value is (offset) + (# of places from the right)
        # which equals (offset) + ((len(numArr) - 1) - idx)
        if digit == 1:
            print(offset + ((len(numArr) - 1) - idx), end=" ")
    print()