#include <bits/stdc++.h>
using namespace std;
#define ll long long
typedef tuple<int, vector<int>, int> phiNum;
typedef vector<int> vi;
typedef vector<vector<int>> vii;

// number of digits at which to switch to naive multiplication
int MULT_THRESHOLD = 128;

// we define a phiNum to be a 3-tuple:
// the first element is the sign of the number (1 or -1)
// the second element is an array of digits representing a base phi number
// the third element is an integer representing the "offset", that is,
// the place value of the least significant element of the first array
// for example, 4 = 100.01 would be represented as (1, [1, 0, 0, 0, 1], -2)

// we will precompute certain values that we will use later
// in particular, these values are used in the computation of the
// inverse matrix (which is one of the steps of toom-3)
// more detail is given about these values in the comments 
// at the top of main(), where these values are actually computed
phiNum inv_10, inv_11, inv_12, inv_13, inv_14;
phiNum inv_20, inv_21, inv_22, inv_23, inv_24;
phiNum inv_30, inv_33, inv_34;

// input: the sign, numArr, and offset of the phiNum to trim
// optional parameter left_zeros: how many zeros to force on the left
// optional parameter right_zeros: how many zeros to force on the right
// output: the equivalent phiNum with 0s on the start and end removed
phiNum trimZeros(int sign, vi& numArr, int offset, int left_zeros, int right_zeros) {
    // find the first and last nonzero entry in the number
    // also update the offset accordingly
    int first_nonzero = 0;
    while ((first_nonzero < numArr.size()) && (numArr[first_nonzero]) == 0) {
        first_nonzero += 1;
    }
    int last_nonzero = numArr.size()-1;
    while ((last_nonzero >= 0) && (numArr[last_nonzero] == 0)) {
        last_nonzero -= 1;
        offset += 1;
    }

    // special case for the input phiNum being zero
    if (first_nonzero > last_nonzero) {
        vi zero_vector(left_zeros + right_zeros, 0);
        return make_tuple(1, zero_vector, (offset - numArr.size()));
    }

    // general case: construct the trimmed phiNum
    vi trimmed_numArr(numArr.begin()+first_nonzero, numArr.begin()+last_nonzero+1);

    // pad it, constructing each part one at a time
    vi padded_numArr(left_zeros, 0); // add the zeros on the left
    for (int elem : trimmed_numArr) {
        padded_numArr.push_back(elem); // add the elements of the trimmed phiNum
    }
    for (int i=0; i<right_zeros; i++) {
        padded_numArr.push_back(0); // add the zeros on the right
    }
    
    // put the trimmed array back into a phiNum
    offset -= right_zeros;
    return make_tuple(sign, padded_numArr, offset);
}

// input: an array of digits representing a number in base phi
// all digits must be 0 or 1
// NOTE: first digit must be 0 
// this method is a helper method and does not handle padding for carries
// converts this array into one with the same base-phi value
// but where no two 1s are next to each other
// output: none
void canonicalize_inplace(vi& numArr) {
    // whenever you see two 1s next to each other, replace them
    // and move to the left
    int ptr = 1;
    // when we've moved left because of a chain of two 1s next to each other
    // and the chain ends,
    // we keep track of where the chain started so we can jump back there
    int max_ptr = 1;

    // sweep through the number left to right
    while (ptr < numArr.size()) {
        // check if adjacent bits are both 1
        if (ptr >= 1 && (numArr[ptr] + numArr[ptr-1] == 2)) {
            // use the identity 011 = 100 in base phi
            numArr[ptr] -= 1;
            numArr[ptr-1] -= 1;
            numArr[ptr-2] += 1;

            // need to check if the 1 in position ptr-2
            // causes more carries to the left
            ptr -= 2;
        } else {
            max_ptr += 1;
            ptr = max_ptr;
        }
    }
}

// input: an array of digits representing a number in base phi
// can contain 2s, but consecutive digits may add up to no more than 2
// output: an array of digits representing a number in base phi with equivalent value
// and all digits 0 or 1, and no two 1s are consecutive
// will eliminate some unnecessary leading and trailing zeros
// to prevent them from piling up, but result may still contain some
// leading and trailing zeros

phiNum reduceSumTo01(int sign, vi& numArr, int offset) {
    // slice out the leading and trailing zeros
    // but create extra leading and trailing zeros
    // to prevent carries from going off the edge
    phiNum trimmed = trimZeros(sign, numArr, offset, 2, 2);
    sign = get<0>(trimmed);
    vi res = get<1>(trimmed);
    offset = get<2>(trimmed);

    // applies transformations going from left to right
    // to remove 2s and 3s from the summed number
    int ptr = 0;
    while (ptr < res.size()) {
        // only process 2s and 3s, otherwise move on
        if (res[ptr] >= 2) {
            if (res[ptr+1] >= 1) {
                // you have a ..21.. pattern
                // use the identity 011 = 100 in base phi
                res[ptr+1] -= 1;
                res[ptr] -= 1;
                res[ptr-1] += 1;
            } else if (res[ptr-1] >= 1) {
                // you have a ..12.. pattern; handle as before
                res[ptr-1] -= 1;
                res[ptr] -= 1;
                res[ptr-2] += 1;
            } else {
                // you have a ..020.. or ..030.. pattern
                // use the identity 0200 = 1001 in base phi
                res[ptr] -= 2;
                res[ptr-1] += 1;
                res[ptr+2] += 1;
            }
        }
        ptr += 1;
    }
    canonicalize_inplace(res);
    return make_tuple(sign, res, offset);
}

// input: an array of digits representing a number in base phi
// can contain -1s but must be the result of a 
// positive difference of canonical base phi numbers
// output: an array of digits representing a number in base phi with equivalent value
// and all digits 0 or 1, and no two 1s are consecutive
// also aims to eliminate some unnecessary leading and trailing zeros
// to prevent them from piling up

phiNum reduceDiffTo01(int sign, vi& numArr, int offset) {
    // slice out the leading and trailing zeros
    // add 3 trailing zeros to prevent carries from going off the edge
    phiNum trimmed = trimZeros(sign, numArr, offset, 0, 3);
    sign = get<0>(trimmed);
    vi res = get<1>(trimmed);
    offset = get<2>(trimmed);

    // applies transformations going from left to right
    // to remove -1s from the difference
    int ptr = 1;
    // when we've moved left because of a 0(-1) pattern, 
    // and the chain ends, keep track so we can jump back
    // to where we left off
    int max_ptr = 1;

    // this process can create 2s, but they will be surrounded by 0s
    // so they can be eliminated by reduceSumTo01()
    // we basically reduce this to a problem we've already solved
    while (ptr < res.size()) {
        if (res[ptr] < 0 and res[ptr-1] == 0) {
            // checks for a 0(-1) pattern
            // use the identity 100 = 011 in base phi
            res[ptr-2] -= 1;
            res[ptr-1] += 1;
            res[ptr] += 1;

            // need to check if the -1 in position ptr-2
            // causes more carries to the left
            ptr -= 2;
        } else if (res[ptr] < 0) {
            // checks for a 1(-1) or 2(-1) pattern
            // use the same identity 100 = 011 but shifted
            res[ptr-1] -= 1;
            res[ptr] += 1;
            res[ptr+1] += 1;

            // this time it's only possible for carries to propagate to the right
            max_ptr += 1;
            ptr = max_ptr;
        } else {
            max_ptr += 1;
            ptr = max_ptr;
        }
    }

    // get rid of the 2s and adjacent 1s now
    return reduceSumTo01(sign, res, offset);
}

// input: a phiNum (sign, numArr, offset)
// numArr is an array of digits representing a number in base phi
// all digits must be 0 or 1
// output: an array of digits representing the same number in base phi
// where all digits are 0 or 1 and no two 1s are adjacent
// also aims to eliminate some unnecessary leading or trailing zeros
// to prevent them from piling up
phiNum canonicalize(int sign, vi& numArr, int offset) {
    // slice out the leading and trailing zeros
    // but create one extra leading 0 for potential carries
    phiNum trimmed = trimZeros(sign, numArr, offset, 1, 0);
    sign = get<0>(trimmed);
    vi res = get<1>(trimmed);
    offset = get<2>(trimmed);

    canonicalize_inplace(res); // use the helper method as written above
    return make_tuple(sign, res, offset);
}

// input: sign, digit array, and offsets of two numbers
// in base phi, must be in canonical form
// (all digits 0 or 1, and no two 1s next to each other)
// output: their sum in base phi, in canonical form
phiNum addHelper(int sign1, vi& arr1, int offset1, int sign2, vi& arr2, int offset2) {
    // align offsets; they must be the same to add correctly
    // this is analogous to normal addition where you have to align the decimal point
    int endAlign1 = 0;
    int endAlign2 = 0;
    while (offset1 > offset2) {
        endAlign1++;
        offset1 -= 1;
    }
    while (offset2 > offset1) {
        endAlign2++;
        offset2 -= 1;
    }

    // determine how many 0s to add at the start of arr1 and arr2
    // to make them the same length, to aid in adding them together
    int startAlign1 = max(0, (int)(arr2.size())+(endAlign2)-(int)(arr1.size())-(endAlign1));
    int startAlign2 = max(0, (int)(arr1.size())+(endAlign1)-(int)(arr2.size())-(endAlign2));

    // put the start and end padding zeros onto arr1
    vi padded1(startAlign1, 0); // add the start zeros
    for (int elem : arr1) {
        padded1.push_back(elem); // add the original elements of arr1
    }
    for (int i=0; i<endAlign1; i++) {
        padded1.push_back(0); // add the end zeros
    }

    // put the start and end padding zeros onto arr2
    vi padded2(startAlign2, 0); // add the start zeros
    for (int elem : arr2) {
        padded2.push_back(elem); // add the original elements of arr2
    }
    for (int i=0; i<endAlign2; i++) {
        padded2.push_back(0); // add the end zeros
    }

    // check if signs match
    if (sign1 == sign2) {
        // if so, perform an addition and convert back to canonical form
        // new offset is minimum of given offsets
        vi summed(padded1.size());
        for (int i=0; i<padded1.size(); i++) {
            summed[i] = padded1[i] + padded2[i];
        }
        return reduceSumTo01(sign1, summed, offset1);
    } else {
        // if not, perform a subtraction and convert back to canonical form

        // first, determine which number has larger absolute value
        // (so we subtract large-small)
        // two numbers in canonical form can be compared in 
        // the usual lexicographical way
        int compare = 0;
        for (int i=0; i<padded1.size(); i++) {
            compare = padded1[i] - padded2[i];
            if (compare != 0) {
                break;
            }
        }

        if (compare == 1) {
            // first number has greater absolute value than second: compute first-second
            vi subtracted(padded1.size());
            for (int i=0; i<padded1.size(); i++) {
                subtracted[i] = padded1[i] - padded2[i];
            }
            return reduceDiffTo01(sign1, subtracted, offset1);
        } else if (compare == -1) {
            // second number has greater absolute value than first: compute second-first
            vi subtracted(padded1.size());
            for (int i=0; i<padded1.size(); i++) {
                subtracted[i] = padded2[i] - padded1[i];
            }
            return reduceDiffTo01(sign2, subtracted, offset1);
        } else {
            // numbers are equal, return 0
            vi single_zero(1, 0);
            return make_tuple(1, single_zero, offset1);
        }
    }
}

// input: two phiNums in canonical form
// recall that a phiNum is a tuple (sign, digit array, offsets)
// canonical form means all digits 0 or 1, and no two 1s next to each other
// output: their sum in base phi, in canonical form
// the output will not contain any leading or trailing zeros
phiNum add(phiNum phiNum1, phiNum phiNum2) {
    phiNum addRes = addHelper(get<0>(phiNum1), get<1>(phiNum1), get<2>(phiNum1), get<0>(phiNum2), get<1>(phiNum2), get<2>(phiNum2));
    int sign = get<0>(addRes);
    vi arr = get<1>(addRes);
    int offset = get<2>(addRes);
    return trimZeros(sign, arr, offset, 0, 0);
}

// input: two phiNums in canonical form
// recall that a phiNum is a tuple (sign, digit array, offsets)
// canonical form means all digits 0 or 1, and no two 1s next to each other
// output: their difference in base phi, in canonical form
// the output will not contain any leading or trailing zeros
phiNum subtract(phiNum phiNum1, phiNum phiNum2) {
    phiNum subRes = addHelper(get<0>(phiNum1), get<1>(phiNum1), get<2>(phiNum1), -get<0>(phiNum2), get<1>(phiNum2), get<2>(phiNum2));
    int sign = get<0>(subRes);
    vi arr = get<1>(subRes);
    int offset = get<2>(subRes);
    return trimZeros(sign, arr, offset, 0, 0);
}

// input: two phiNums in canonical form
// a phiNum is a tuple (sign, digit array, offsets)
// canonical form means all digits 0 or 1, and no two 1s next to each other
// output: their product in base phi, in canonical form
// the output will not contain any leading or trailing zeros
phiNum multiply(phiNum phiNum1, phiNum phiNum2) {
    int sign1 = get<0>(phiNum1);
    vi arr1 = get<1>(phiNum1);
    int offset1 = get<2>(phiNum1);

    int sign2 = get<0>(phiNum2);
    vi arr2 = get<1>(phiNum2);
    int offset2 = get<2>(phiNum2);

    // compute the sign of the product
    int sign = sign1 * sign2;

    // for each 1 in the second digit, add a copy of arr1
    // shifted by the place value of the 1 in arr2
    vi single_zero(1, 0);
    phiNum res = make_tuple(sign, single_zero, offset1);
    for (int digitIdx = 0; digitIdx < arr2.size(); digitIdx++) {
        int digit = arr2[digitIdx];
        if (digit == 1) {
            // place value of each digit in arr2 is:
            // (offset2) + (distance of digitIdx from the right)
            // distance from the right is (arr2,size()-1) - digitIdx
            int placeValue = offset2 + arr2.size() - 1 - digitIdx;
            res = add(res, make_tuple(sign, arr1, offset1 + placeValue));
        }
    }

    int res_sign = get<0>(res);
    vi res_arr = get<1>(res);
    int res_offset = get<2>(res);
    return trimZeros(res_sign, res_arr, res_offset, 0, 0);
}


// multiply a PhiNum by a (base-10) whole number constant k
// runs in quadratic time
// works by recursively dividing k in half:
// if k is even, compute (k/2) + (k/2)
// if k is odd, reduce to even by computing (k-1) + (k)
phiNum intmul(phiNum phinum, int k) {
    // base cases
    if (k == 0) {
        vi single_zero(1, 0);
        return make_tuple(1, single_zero, 0);
    }
    if (k == 1) {
        return phinum;
    }

    // odd case
    if (k%2 == 1) {
        return add(phinum, intmul(phinum, k-1));
    }

    // even case
    phiNum half = intmul(phinum, k/2);
    return add(half, half);
}

// utility function
// given integers a and b, return a+b*phi in canonical form
phiNum make_phiNum(int a, int b) {
    // compute the signs of a and b
    int a_sign = a>=0 ? 1 : -1;
    int b_sign = b>=0 ? 1 : -1;
    
    // compute a and b in canonical form, and add them together
    // note that make_tuple(a_sign, single_one, 0)
    // is the number 1 with appropriate sign
    // and make_tuple(b_sign, single_one, 1)
    // is phi with appropriate sign
    vi single_one(1, 1);
    phiNum a_canonical = intmul(make_tuple(a_sign, single_one, 0), abs(a)); 
    phiNum b_canonical = intmul(make_tuple(b_sign, single_one, 1), abs(b)); 
    return add(a_canonical, b_canonical);
}

// input: any phiNum (need not be in canonical form)
// output: a phiNum whose value is multiplied by phi^amount
// runs in O(1)
phiNum shift(phiNum phiNum1, int amount) {
    int sign1 = get<0>(phiNum1);
    vi arr1 = get<1>(phiNum1); // make a copy
    int offset1 = get<2>(phiNum1);

    return make_tuple(sign1, arr1, offset1+amount);
}

// input: any phiNum (need not be in canonical form)
// output: a phiNum with same absolute value and opposite sign
// runs in O(1)
phiNum negate_phinum(phiNum phiNum1) {
    int sign1 = get<0>(phiNum1);
    vi arr1 = get<1>(phiNum1); // make a copy
    int offset1 = get<2>(phiNum1);

    return make_tuple(-sign1, arr1, offset1);
}

// Toom-3 multiplication implementation
// input: two phiNums in canonical form
// output: their product, in canonical form
// runs in O(n^1.49) where the exponent is log(5)/log(3)
phiNum toom_3(phiNum phiNum1, phiNum phiNum2) {
    int sign1 = get<0>(phiNum1);
    vi arr1 = get<1>(phiNum1);
    int offset1 = get<2>(phiNum1);

    int sign2 = get<0>(phiNum2);
    vi arr2 = get<1>(phiNum2);
    int offset2 = get<2>(phiNum2);

    // base cases: check if the number of digits in either number
    // is less than the threshold, and if so, multiply naively
    // note that naive multiplication in base phi with this implementation
    // is faster when the second argument is guaranteed to have few digits
    if (arr2.size() <= MULT_THRESHOLD) {
        return multiply(phiNum1, phiNum2);
    }
    if (arr1.size() <= MULT_THRESHOLD) {
        return multiply(phiNum2, phiNum1);
    }
    
    // splitting: select a block_size, and split each number into chunks
    // of at most this size (the first chunk can be smaller)
    int block_size = max((arr1.size())/3, (arr2.size())/3)+1;

    // phiNum1 will split into chunks m2, m1, m0 (in that order)
    // phiNum2 will split into chunks n2, n1, n0
    // each chunk is also a phiNum with sign, digits, and offset
    // we index based on the back because we take block_size digits
    // at a time from the right

    // first construct the number arrays in the chunks - do this for arr1
    vi arr1_m2;
    vi arr1_m1;
    vi arr1_m0;

    // go through arr1, keeping track of how far away each element's
    // index is from the end of the array
    // any values more than 2*block_size away from the end go into m_2
    // any values between block_size and 2*block_size away from the end go into m_1
    // any values at most block_size away from the end go into m_0
    // this way, the blocks are evenly split
    for (int i=0; i<arr1.size(); i++) {
        int dist_from_end = arr1.size() - i;
        if (dist_from_end > 2*block_size) {
            arr1_m2.push_back(arr1[i]);
        } else if (dist_from_end > block_size) {
            arr1_m1.push_back(arr1[i]);
        } else {
            arr1_m0.push_back(arr1[i]);
        }
    }

    // now do the same thing, but for arr2
    vi arr2_n2;
    vi arr2_n1;
    vi arr2_n0;

    // go through arr2, keeping track of how far away each element's
    // index is from the end of the array
    // any values at least 2*block_size away from the end go into n_2
    // any values between block_size and 2*block_size away from the end go into n_1
    // any values within block_size of the end go into n_0
    // this way, the blocks are evenly split
    for (int i=0; i<arr2.size(); i++) {
        int dist_from_end = arr2.size() - i;
        if (dist_from_end > 2*block_size) {
            arr2_n2.push_back(arr2[i]);
        } else if (dist_from_end > block_size) {
            arr2_n1.push_back(arr2[i]);
        } else {
            arr2_n0.push_back(arr2[i]);
        }
    }

    // now construct the phiNums corresponding to these chunks
    phiNum m2 = make_tuple(sign1, arr1_m2, offset1);
    phiNum m1 = make_tuple(sign1, arr1_m1, offset1);
    phiNum m0 = make_tuple(sign1, arr1_m0, offset1);

    phiNum n2 = make_tuple(sign2, arr2_n2, offset2);
    phiNum n1 = make_tuple(sign2, arr2_n1, offset2);
    phiNum n0 = make_tuple(sign2, arr2_n0, offset2);

    // evaluation: this defines the following polynomials
    // p(x) = m2*x^2 + m1*x + m0
    // q(x) = n2*x^2 + n1*x + n0
    // we need to figure out r(x), evaluated at phi

    // we choose points 0, 1, phi, phi^2, infinity
    // where evaluating at infinity just means 
    // taking the limit as (x -> infinity) of p(x)/x^(deg p)
    // this simplifies calculations a little bit

    // p(0) = m0, q(0) = n0
    phiNum p_0 = m0;
    phiNum q_0 = n0;

    // p(1) = m2 + m1 + m0, q(1) = n2 + n1 + n0
    phiNum p_1 = add(m2, add(m1, m0));
    phiNum q_1 = add(n2, add(n1, n0));

    // p(phi) = phi^2*m2 + phi*m1 + m0, likewise for q(phi)
    phiNum p_phi = add(shift(m2, 2), add(shift(m1, 1), m0));
    phiNum q_phi = add(shift(n2, 2), add(shift(n1, 1), n0));

    // p(phi^2) = phi^4*m2 + phi^2*m1 + m0, likewise for q(phi)
    phiNum p_phi2 = add(shift(m2, 4), add(shift(m1, 2), m0));
    phiNum q_phi2 = add(shift(n2, 4), add(shift(n1, 2), n0));
    
    // p(infinity) = m2, q(infinity) = n2
    phiNum p_inf = m2;
    phiNum q_inf = n2;

    // pointwise multiplication: find r(x) at the values we want
    // by just multiplying through: r(x) = p(x)q(x)
    phiNum r_0 = toom_3(p_0, q_0);
    phiNum r_1 = toom_3(p_1, q_1);
    phiNum r_phi = toom_3(p_phi, q_phi);
    phiNum r_phi2 = toom_3(p_phi2, q_phi2);
    phiNum r_inf = toom_3(p_inf, q_inf);

    // interpolation: determine the coefficients of r(x)
    // store them in variables v0, v1, v2, v3, v4
    // notice that if we had the coefficients in a vector v, we could
    // multiply it by the following matrix to extract the evaluated points:
    //
    // A = [[1, 0, 0, 0, 0], 
    //      [1, 1, 1, 1, 1], 
    //      [1, phi, phi^2, phi^3, phi^4], 
    //      [1, phi^2, phi^4, phi^6, phi^8], 
    //      [0, 0, 0, 0, 1]]
    //
    // A*v = [r_0, r_1, r_phi, r_phi2, r_inf]
    //
    // the inverse is precomputed, as follows:
    // note: the evaluation points 0, 1, phi, phi^2, infinity
    // were carefully chosen such that all entries in the inverse
    // have finite representations in base phi
    // this is not the case, for example, for the set {0, 1, -1, 2, infinity}
    //
    // A^-1 = [[1, 0, 0, 0, 0], 
    //        [-2, 1+2*phi, -1-phi, 2-phi, -2*phi-1], 
    //        [2*phi-2, -2*phi-1, 2+phi, 1-phi, 2+4*phi], 
    //        [3-2*phi, 1, -1, 2*phi-3, -2*phi-2], 
    //        [0, 0, 0, 0, 1]]
    //
    // here we make use of the values we precomputed earlier
    // so we don't have to recompute them every time
    // the values 1 and -1 are special and don't require a multiplication
    //
    // A^-1 = [[1, 0, 0, 0, 0], 
    //        [inv_10, inv_11, inv_12, inv_13, inv_14], 
    //        [inv_20, inv_21, inv_22, inv_23, inv_24], 
    //        [inv_30, 1, -1, inv_33, inv_34], 
    //        [0, 0, 0, 0, 1]]
    //
    // (A^-1)(r_0, r_1, r_phi, r_phi2, r_inf) = (v0, v1, v2, v3, v4)

    phiNum v0 = r_0;
    phiNum v1 = add(add(multiply(r_0, inv_10), multiply(r_1, inv_11)), 
             add(multiply(r_phi, inv_12), add(multiply(r_phi2, inv_13), multiply(r_inf, inv_14))));
    phiNum v2 = add(add(multiply(r_0, inv_20), multiply(r_1, inv_21)), 
             add(multiply(r_phi, inv_22), add(multiply(r_phi2, inv_23), multiply(r_inf, inv_24))));
    phiNum v3 = add(add(multiply(r_0, inv_30), r_1), 
             add(negate_phinum(r_phi), add(multiply(r_phi2, inv_33), multiply(r_inf, inv_34))));
    phiNum v4 = r_inf;

    // recomposition: evaluate r(phi^block_size) to get our answer
    // amounts to adding these coefficients shifted by block_size * power

    return add(add(v0, shift(v1, block_size)),
               add(shift(v2, 2*block_size), add(shift(v3, 3*block_size), shift(v4, 4*block_size))));
}

// input: a list of phiNums in INCREASING order of significance
// (this is reversed from the usual representation)
// and a base, expressed in base phi
// output: a single phiNum representing the value of the list of phiNums
// if interpreted as a single number in base b
// equivalently, this is phiNumList[0]*b^0 + phiNumList[1]*b^1 + ...
// NOTE: this method may modify the phiNumList
phiNum baseConvHelper(vector<phiNum>& phiNumList, phiNum base) {
    // base case
    if (phiNumList.size() == 1) {
        return phiNumList[0];
    }
    
    // recursive step: combine two numbers at a time
    // reduces to a list of half the size
    // and squares the base
    // if the list has odd length, add a 0 at the end 
    // for the last number to combine with
    if ((phiNumList.size())%2 == 1) {
        phiNumList.push_back(make_phiNum(0, 0));
    }

    // combine each pair of two adjacent numbers
    // put them in a new array combinedList
    vector<phiNum> combinedList;
    for (int i=0; i<phiNumList.size(); i+=2) {
        combinedList.push_back(add(toom_3(phiNumList[i+1], base), phiNumList[i]));
    }
    
    // keep combining until small enough to reduce to the base case
    return baseConvHelper(combinedList, toom_3(base, base));
}

// input: a single number in base 10 expressed as a string
// output: the phiNum with this value
// runs in O(n^1.49), same as Toom-Cook
phiNum baseConv(string numStr) {
    // create a list of phiNums:
    // one phiNum corresponding to each digit of numStr
    // in reversed order, so that it can be passed to baseConvHelper
    vector<phiNum> phiNumList;
    for (int i=numStr.size()-1; i>=0; i--) {
        char digit_at_i = numStr[i];
        int val_at_i = digit_at_i - '0';
        phiNumList.push_back(make_phiNum(val_at_i, 0));
    }

    return baseConvHelper(phiNumList, make_phiNum(10, 0));
}

// input constraint: each coin has position within [-10000, 10000]
int MAX_ABS_COIN = 10000;

signed main() {
    // fill in the variables that we wanted to precompute:
    // in toom-3 we need to multiply by this matrix:
    //
    // A^-1 = [[1, 0, 0, 0, 0], 
    //        [-2, 1+2*phi, -1-phi, 2-phi, -1-2*phi], 
    //        [-2+2*phi, -1-2*phi, 2+phi, 1-phi, 2+4*phi], 
    //        [3-2*phi, 1, -1, -3+2*phi, -2-2*phi], 
    //        [0, 0, 0, 0, 1]]
    //
    // we will precompute the middle three rows to save a little time later
    // for an explanation of how this matrix is used, see the interpolation step
    // in the toom_3 implementation

    inv_10 = make_phiNum(-2, 0);
    inv_11 = make_phiNum(1, 2);
    inv_12 = make_phiNum(-1, -1);
    inv_13 = make_phiNum(2, -1);
    inv_14 = make_phiNum(-1, -2);

    inv_20 = make_phiNum(-2, 2);
    inv_21 = make_phiNum(-1, -2);
    inv_22 = make_phiNum(2, 1);
    inv_23 = make_phiNum(1, -1);
    inv_24 = make_phiNum(2, 4);

    inv_30 = make_phiNum(3, -2);
    inv_33 = make_phiNum(-3, 2);
    inv_34 = make_phiNum(-2, -2);

    // read in inputs: k and n
    string k; // k can be huge, so should be read as a string
    int n;
    cin >> k >> n;

    // read in inputs: n lines of integers
    vii coin_input;
    for (int i=0; i<n; i++) {
        // figure out how many integers to read, initialize a vector
        // for that test case accordingly
        int test_case_size;
        cin >> test_case_size;
        vi test_case(test_case_size);

        // read in all the data for that test case
        for (int j=0; j<test_case_size; j++) {
            int coin_val;
            cin >> coin_val;
            test_case[j] = coin_val;
        }
        coin_input.push_back(test_case);
    }

    // convert k to base phi
    phiNum k_phi = baseConv(k);

    // for each input, calculate the product k * (input) in base phi
    for (vi coins : coin_input) {
        // convert the list of positions into a number in base phi
        // index 0 represents the most significant possible place value (MAX_ABS_COIN)
        // index 1 represents the next most after that (MAX_ABS_COIN-1)
        // therefore, each coin represents a 1 at index (MAX_ABS_COIN-coin)
        // and there are 2*MAX_ABS_COIN+1 possible coin locations
        vi coin_phi(2*MAX_ABS_COIN+1, 0); 
        for (int coin : coins) {
            coin_phi[MAX_ABS_COIN-coin] = 1;
        }

        // there could still be multiple consecutive 1s, so deal with this here
        phiNum canonical_coin_phi = canonicalize(1, coin_phi, -MAX_ABS_COIN);
        
        // multiply!
        phiNum product = toom_3(canonical_coin_phi, k_phi);
        vi numArr = get<1>(product);
        int offset = get<2>(product);

        // print the place values of all the 1s in the result
        for (int idx = 0; idx < numArr.size(); idx++) {
            int digit = numArr[idx];
            if (digit == 1) {
                // place value is (offset) + (# of places from the right)
                // which equals (offset) + ((numArr.size() - 1) - idx)
                cout << offset + (((int)(numArr.size()) - 1) - idx) << " ";
            }
        }
        cout << "\n";
    }
}