import java.util.*;

public class pcsol_java {
    // number of digits at which to switch to naive multiplication
    static final int MULT_THRESHOLD = 128;

    // we define a PhiNum as follows:
    // the first element is the sign of the number (1 or -1)
    // the second element is an array of digits representing a base phi number
    // the third element is an integer representing the "offset", that is,
    // the place value of the least significant element of the first array
    // for example, 4 = 100.01 would be an object with the following fields:
    // sign = 1, numArr = [1, 0, 0, 0, 1], offset = -2
    private static class PhiNum {
        int sign;
        ArrayList<Integer> numArr;
        int offset;

        PhiNum(int sign, ArrayList<Integer> numArr, int offset) {
            this.sign = sign;
            this.numArr = numArr;
            this.offset = offset;
        }
    }

    // we will precompute certain values that we will use later
    // in particular, these values are used in the computation of the
    // inverse matrix (which is one of the steps of toom-3)
    // more detail is given about these values in the comments 
    // at the top of main(), where these values are actually computed
    static PhiNum inv_10, inv_11, inv_12, inv_13, inv_14;
    static PhiNum inv_20, inv_21, inv_22, inv_23, inv_24;
    static PhiNum inv_30, inv_33, inv_34;

    // input: an integer size, representing how many zeros to be
    // in the returned arrayList
    // output: an arrayList<Integer> initialized with all zeros
    // with the specified size
    static ArrayList<Integer> zeroVector(int size) {
        ArrayList<Integer> zeros = new ArrayList<>(size);
        for (int i=0; i<size; i++) {
            zeros.add(0);
        }
        return zeros;
    }

    // input: the sign, numArr, and offset of the PhiNum to trim
    // optional parameter left_zeros: how many zeros to force on the left
    // optional parameter right_zeros: how many zeros to force on the right
    // output: the equivalent PhiNum with 0s on the start and end removed
    public static PhiNum trimZeros(int sign, ArrayList<Integer> numArr, int offset, int left_zeros, int right_zeros) {
        // find the first and last nonzero entry in the number
        // also update the offset accordingly
        int first_nonzero = 0;
        while ((first_nonzero < numArr.size()) && (numArr.get(first_nonzero)) == 0) {
            first_nonzero += 1;
        }
        int last_nonzero = numArr.size()-1;
        while ((last_nonzero >= 0) && (numArr.get(last_nonzero) == 0)) {
            last_nonzero -= 1;
            offset += 1;
        }

        // special case for the input PhiNum being zero
        if (first_nonzero > last_nonzero) {
            return new PhiNum(1, zeroVector(left_zeros + right_zeros), (offset - numArr.size()));
        }

        // general case: construct the trimmed PhiNum
        ArrayList<Integer> trimmed_numArr = new ArrayList<>(numArr.subList(first_nonzero, last_nonzero+1));

        // pad it, constructing each part one at a time
        ArrayList<Integer> padded_numArr = new ArrayList<>();
        for (int i=0; i<left_zeros; i++) {
            padded_numArr.add(0); // add the zeros on the left
        }
        for (int elem : trimmed_numArr) {
            padded_numArr.add(elem); // add the elements of the trimmed PhiNum
        }
        for (int i=0; i<right_zeros; i++) {
            padded_numArr.add(0); // add the zeros on the right
        }
        
        // put the trimmed array back into a PhiNum
        offset -= right_zeros;
        return new PhiNum(sign, padded_numArr, offset);
    }

    // input: an array of digits representing a number in base phi
    // all digits must be 0 or 1
    // NOTE: first digit must be 0 
    // this method is a helper method and does not handle padding for carries
    // converts this array into one with the same base-phi value
    // but where no two 1s are next to each other
    // output: none
    static void canonicalize_inplace(ArrayList<Integer> numArr) {
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
            if ((ptr >= 1) && numArr.get(ptr) + numArr.get(ptr-1) == 2) {
                // use the identity 011 = 100 in base phi
                numArr.set(ptr, numArr.get(ptr)-1);
                numArr.set(ptr-1, numArr.get(ptr-1)-1);
                numArr.set(ptr-2, numArr.get(ptr-2)+1);

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

    static PhiNum reduceSumTo01(int sign, ArrayList<Integer> numArr, int offset) {
        // slice out the leading and trailing zeros
        // but create extra leading and trailing zeros
        // to prevent carries from going off the edge
        PhiNum trimmed = trimZeros(sign, numArr, offset, 2, 2);
        ArrayList<Integer> res = trimmed.numArr;

        // applies transformations going from left to right
        // to remove 2s and 3s from the summed number
        int ptr = 0;
        while (ptr < res.size()) {
            // only process 2s and 3s, otherwise move on
            if (res.get(ptr) >= 2) {
                if (res.get(ptr+1) >= 1) {
                    // you have a ..21.. pattern
                    // use the identity 011 = 100 in base phi
                    res.set(ptr+1, res.get(ptr+1)-1);
                    res.set(ptr, res.get(ptr)-1);
                    res.set(ptr-1, res.get(ptr-1)+1);
                } else if (res.get(ptr-1) >= 1) {
                    // you have a ..12.. pattern; handle as before
                    res.set(ptr-1, res.get(ptr-1)-1);
                    res.set(ptr, res.get(ptr)-1);
                    res.set(ptr-2, res.get(ptr-2)+1);
                } else {
                    // you have a ..020.. or ..030.. pattern
                    // use the identity 0200 = 1001 in base phi
                    res.set(ptr, res.get(ptr)-2);
                    res.set(ptr-1, res.get(ptr-1)+1);
                    res.set(ptr+2, res.get(ptr+2)+1);
                }
            }
            ptr += 1;
        }
        canonicalize_inplace(res);
        return new PhiNum(trimmed.sign, res, trimmed.offset);
    }

    // input: an array of digits representing a number in base phi
    // can contain -1s but must be the result of a 
    // positive difference of canonical base phi numbers
    // output: an array of digits representing a number in base phi with equivalent value
    // and all digits 0 or 1, and no two 1s are consecutive
    // also aims to eliminate some unnecessary leading and trailing zeros
    // to prevent them from piling up

    static PhiNum reduceDiffTo01(int sign, ArrayList<Integer> numArr, int offset) {
        // slice out the leading and trailing zeros
        // add 3 trailing zeros to prevent carries from going off the edge
        PhiNum trimmed = trimZeros(sign, numArr, offset, 0, 3);
        ArrayList<Integer> res = trimmed.numArr;

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
            if ((res.get(ptr) < 0) && (res.get(ptr-1) == 0)) {
                // checks for a 0(-1) pattern
                // use the identity 100 = 011 in base phi
                res.set(ptr-2, res.get(ptr-2)-1);
                res.set(ptr-1, res.get(ptr-1)+1);
                res.set(ptr, res.get(ptr)+1);

                // need to check if the -1 in position ptr-2
                // causes more carries to the left
                ptr -= 2;
            } else if (res.get(ptr) < 0) {
                // checks for a 1(-1) or 2(-1) pattern
                // use the same identity 100 = 011 but shifted
                res.set(ptr-1, res.get(ptr-1)-1);
                res.set(ptr, res.get(ptr)+1);
                res.set(ptr+1, res.get(ptr+1)+1);

                // this time it's only possible for carries to propagate to the right
                max_ptr += 1;
                ptr = max_ptr;
            } else {
                max_ptr += 1;
                ptr = max_ptr;
            }
        }

        // get rid of the 2s and adjacent 1s now
        return reduceSumTo01(trimmed.sign, res, trimmed.offset);
    }

    // input: a PhiNum (sign, numArr, offset)
    // numArr is an array of digits representing a number in base phi
    // all digits must be 0 or 1
    // output: an array of digits representing the same number in base phi
    // where all digits are 0 or 1 and no two 1s are adjacent
    // also aims to eliminate some unnecessary leading or trailing zeros
    // to prevent them from piling up
    // note that this WILL modify the given PhiNum
    static PhiNum canonicalize(int sign, ArrayList<Integer> numArr, int offset) {
        // slice out the leading and trailing zeros
        // but create one extra leading 0 for potential carries
        PhiNum trimmed = trimZeros(sign, numArr, offset, 1, 0);
        canonicalize_inplace(trimmed.numArr); // use the helper method as written above
        return new PhiNum(trimmed.sign, trimmed.numArr, trimmed.offset);
    }

    // input: sign, digit array, and offsets of two numbers
    // in base phi, must be in canonical form
    // (all digits 0 or 1, and no two 1s next to each other)
    // output: their sum in base phi, in canonical form
    static PhiNum addHelper(int sign1, ArrayList<Integer> arr1, int offset1, int sign2, ArrayList<Integer> arr2, int offset2) {
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
        int startAlign1 = Math.max(0, (arr2.size())+(endAlign2)-(arr1.size())-(endAlign1));
        int startAlign2 = Math.max(0, (arr1.size())+(endAlign1)-(arr2.size())-(endAlign2));

        // put the start and end padding zeros onto arr1
        ArrayList<Integer> padded1 = new ArrayList<>(startAlign1 + arr1.size() + endAlign1);
        for (int i=0; i<startAlign1; i++) {
            padded1.add(0); // add the start zeros
        }
        for (int elem : arr1) {
            padded1.add(elem); // add the original elements of arr1
        }        
        for (int i=0; i<endAlign1; i++) {
            padded1.add(0); // add the end zeros
        }

        // put the start and end padding zeros onto arr2
        ArrayList<Integer> padded2 = new ArrayList<>(startAlign2 + arr2.size() + endAlign2);
        for (int i=0; i<startAlign2; i++) {
            padded2.add(0); // add the start zeros
        }
        for (int elem : arr2) {
            padded2.add(elem); // add the original elements of arr1
        }        
        for (int i=0; i<endAlign2; i++) {
            padded2.add(0); // add the end zeros
        }

        // check if signs match
        if (sign1 == sign2) {
            // if so, perform an addition and convert back to canonical form
            // new offset is minimum of given offsets
            ArrayList<Integer> summed = new ArrayList<Integer>(padded1.size());
            for (int i=0; i<padded1.size(); i++) {
                summed.add(padded1.get(i) + padded2.get(i));
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
                compare = padded1.get(i) - padded2.get(i);
                if (compare != 0) {
                    break;
                }
            }

            if (compare == 1) {
                // first number has greater absolute value than second: compute first-second
                ArrayList<Integer> subtracted = new ArrayList<>(padded1.size());
                for (int i=0; i<padded1.size(); i++) {
                    subtracted.add(padded1.get(i) - padded2.get(i));
                }
                return reduceDiffTo01(sign1, subtracted, offset1);
            } else if (compare == -1) {
                // second number has greater absolute value than first: compute second-first
                ArrayList<Integer> subtracted = new ArrayList<>(padded1.size());
                for (int i=0; i<padded1.size(); i++) {
                    subtracted.add(padded2.get(i) - padded1.get(i));
                }
                return reduceDiffTo01(sign2, subtracted, offset1);
            } else {
                // numbers are equal, return 0
                return new PhiNum(1, zeroVector(1), offset1);
            }
        }
    }

    // input: two PhiNums in canonical form
    // recall that a PhiNum is a tuple (sign, digit array, offsets)
    // canonical form means all digits 0 or 1, and no two 1s next to each other
    // output: their sum in base phi, in canonical form
    // the output will not contain any leading or trailing zeros
    static PhiNum add(PhiNum phinum1, PhiNum phinum2) {
        PhiNum addRes = addHelper(phinum1.sign, phinum1.numArr, phinum1.offset, phinum2.sign, phinum2.numArr, phinum2.offset);
        return trimZeros(addRes.sign, addRes.numArr, addRes.offset, 0, 0);
    }

    // input: two PhiNums in canonical form
    // recall that a PhiNum is a tuple (sign, digit array, offsets)
    // canonical form means all digits 0 or 1, and no two 1s next to each other
    // output: their difference in base phi, in canonical form
    // the output will not contain any leading or trailing zeros
    static PhiNum subtract(PhiNum phinum1, PhiNum phinum2) {
        PhiNum subRes = addHelper(phinum1.sign, phinum1.numArr, phinum1.offset, -phinum2.sign, phinum2.numArr, phinum2.offset);
        return trimZeros(subRes.sign, subRes.numArr, subRes.offset, 0, 0);
    }

    // input: two PhiNums in canonical form
    // a PhiNum is a tuple (sign, digit array, offsets)
    // canonical form means all digits 0 or 1, and no two 1s next to each other
    // output: their product in base phi, in canonical form
    // the output will not contain any leading or trailing zeros
    static PhiNum multiply(PhiNum phinum1, PhiNum phinum2) {
        // compute the sign of the product
        int sign = phinum1.sign * phinum2.sign;

        // for each 1 in the second digit, add a copy of arr1
        // shifted by the place value of the 1 in arr2
        PhiNum res = new PhiNum(sign, zeroVector(1), phinum1.offset);
        for (int digitIdx = 0; digitIdx < phinum2.numArr.size(); digitIdx++) {
            int digit = phinum2.numArr.get(digitIdx);
            if (digit == 1) {
                // place value of each digit in arr2 is:
                // (offset2) + (distance of digitIdx from the right)
                // distance from the right is (arr2,size()-1) - digitIdx
                int placeValue = phinum2.offset + phinum2.numArr.size() - 1 - digitIdx;
                res = add(res, new PhiNum(sign, phinum1.numArr, phinum1.offset + placeValue));
            }
        }

        return trimZeros(res.sign, res.numArr, res.offset, 0, 0);
    }


    // multiply a PhiNum by a (base-10) whole number constant k
    // runs in quadratic time
    // works by recursively dividing k in half:
    // if k is even, compute (k/2) + (k/2)
    // if k is odd, reduce to even by computing (k-1) + (k)
    static PhiNum intmul(PhiNum phinum, int k) {
        // base cases
        if (k == 0) {
            return new PhiNum(1, zeroVector(1), 0);
        }
        if (k == 1) {
            return phinum;
        }

        // odd case
        if (k%2 == 1) {
            return add(phinum, intmul(phinum, k-1));
        }

        // even case
        PhiNum half = intmul(phinum, k/2);
        return add(half, half);
    }

    // utility function
    // given integers a and b, return a+b*phi in canonical form
    static PhiNum make_PhiNum(int a, int b) {
        // compute the signs of a and b
        int a_sign = a>=0 ? 1 : -1;
        int b_sign = b>=0 ? 1 : -1;
        
        // compute a and b in canonical form, and add them together
        // note that make_tuple(a_sign, single_one, 0)
        // is the number 1 with appropriate sign
        // and make_tuple(b_sign, single_one, 1)
        // is phi with appropriate sign
        ArrayList<Integer> single_one = new ArrayList<Integer>();
        single_one.add(1);
        PhiNum a_canonical = intmul(new PhiNum(a_sign, single_one, 0), Math.abs(a)); 
        PhiNum b_canonical = intmul(new PhiNum(b_sign, single_one, 1), Math.abs(b)); 
        return add(a_canonical, b_canonical);
    }

    // input: any PhiNum (need not be in canonical form)
    // output: a PhiNum whose value is multiplied by phi^amount
    // runs in O(1)
    static PhiNum shift(PhiNum phinum, int amount) {
        return new PhiNum(phinum.sign, new ArrayList<>(phinum.numArr), phinum.offset+amount);
    }

    // input: any PhiNum (need not be in canonical form)
    // output: a PhiNum with same absolute value and opposite sign
    // runs in O(1)
    static PhiNum negate_PhiNum(PhiNum phinum) {
        return new PhiNum(-phinum.sign, new ArrayList<>(phinum.numArr), phinum.offset);
    }

    // Toom-3 multiplication implementation
    // input: two PhiNums in canonical form
    // output: their product, in canonical form
    // runs in O(n^1.49) where the exponent is log(5)/log(3)
    static PhiNum toom_3(PhiNum phinum1, PhiNum phinum2) {
        // base cases: check if the number of digits in either number
        // is less than the threshold, and if so, multiply naively
        // note that naive multiplication in base phi with this implementation
        // is faster when the second argument is guaranteed to have few digits
        if (phinum2.numArr.size() <= MULT_THRESHOLD) {
            return multiply(phinum1, phinum2);
        }
        if (phinum1.numArr.size() <= MULT_THRESHOLD) {
            return multiply(phinum2, phinum1);
        }
        
        // splitting: select a block_size, and split each number into chunks
        // of at most this size (the first chunk can be smaller)
        int block_size = Math.max((phinum1.numArr.size())/3, (phinum2.numArr.size())/3)+1;

        // phinum1 will split into chunks m2, m1, m0 (in that order)
        // phinum2 will split into chunks n2, n1, n0
        // each chunk is also a PhiNum with sign, digits, and offset
        // we index based on the back because we take block_size digits
        // at a time from the right

        // first construct the number arrays in the chunks - do this for arr1
        ArrayList<Integer> arr1_m2 = new ArrayList<>();
        ArrayList<Integer> arr1_m1 = new ArrayList<>();
        ArrayList<Integer> arr1_m0 = new ArrayList<>();

        // go through arr1, keeping track of how far away each element's
        // index is from the end of the array
        // any values at least 2*block_size away from the end go into m_2
        // any values between block_size and 2*block_size away from the end go into m_1
        // any values within block_size of the end go into m_0
        // this way, the blocks are evenly split
        for (int i=0; i<phinum1.numArr.size(); i++) {
            int dist_from_end = phinum1.numArr.size() - i;
            if (dist_from_end > 2*block_size) {
                arr1_m2.add(phinum1.numArr.get(i));
            } else if (dist_from_end > block_size) {
                arr1_m1.add(phinum1.numArr.get(i));
            } else {
                arr1_m0.add(phinum1.numArr.get(i));
            }
        }

        // now do the same thing, but for arr2
        ArrayList<Integer> arr2_n2 = new ArrayList<>();
        ArrayList<Integer> arr2_n1 = new ArrayList<>();
        ArrayList<Integer> arr2_n0 = new ArrayList<>();

        // go through arr2, keeping track of how far away each element's
        // index is from the end of the array
        // any values at least 2*block_size away from the end go into n_2
        // any values between block_size and 2*block_size away from the end go into n_1
        // any values within block_size of the end go into n_0
        // this way, the blocks are evenly split
        for (int i=0; i<phinum2.numArr.size(); i++) {
            int dist_from_end = phinum2.numArr.size() - i;
            if (dist_from_end > 2*block_size) {
                arr2_n2.add(phinum2.numArr.get(i));
            } else if (dist_from_end > block_size) {
                arr2_n1.add(phinum2.numArr.get(i));
            } else {
                arr2_n0.add(phinum2.numArr.get(i));
            }
        }

        // now construct the PhiNums corresponding to these chunks
        PhiNum m2 = new PhiNum(phinum1.sign, arr1_m2, phinum1.offset);
        PhiNum m1 = new PhiNum(phinum1.sign, arr1_m1, phinum1.offset);
        PhiNum m0 = new PhiNum(phinum1.sign, arr1_m0, phinum1.offset);

        PhiNum n2 = new PhiNum(phinum2.sign, arr2_n2, phinum2.offset);
        PhiNum n1 = new PhiNum(phinum2.sign, arr2_n1, phinum2.offset);
        PhiNum n0 = new PhiNum(phinum2.sign, arr2_n0, phinum2.offset);

        // evaluation: this defines the following polynomials
        // p(x) = m2*x^2 + m1*x + m0
        // q(x) = n2*x^2 + n1*x + n0
        // we need to figure out r(x), evaluated at phi

        // we choose points 0, 1, phi, phi^2, infinity
        // where evaluating at infinity just means 
        // taking the limit as (x -> infinity) of p(x)/x^(deg p)
        // this simplifies calculations a little bit

        // p(0) = m0, q(0) = n0
        PhiNum p_0 = m0;
        PhiNum q_0 = n0;

        // p(1) = m2 + m1 + m0, q(1) = n2 + n1 + n0
        PhiNum p_1 = add(m2, add(m1, m0));
        PhiNum q_1 = add(n2, add(n1, n0));

        // p(phi) = phi^2*m2 + phi*m1 + m0, likewise for q(phi)
        PhiNum p_phi = add(shift(m2, 2), add(shift(m1, 1), m0));
        PhiNum q_phi = add(shift(n2, 2), add(shift(n1, 1), n0));

        // p(phi^2) = phi^4*m2 + phi^2*m1 + m0, likewise for q(phi)
        PhiNum p_phi2 = add(shift(m2, 4), add(shift(m1, 2), m0));
        PhiNum q_phi2 = add(shift(n2, 4), add(shift(n1, 2), n0));
        
        // p(infinity) = m2, q(infinity) = n2
        PhiNum p_inf = m2;
        PhiNum q_inf = n2;

        // pointwise multiplication: find r(x) at the values we want
        // by just multiplying through: r(x) = p(x)q(x)
        PhiNum r_0 = toom_3(p_0, q_0);
        PhiNum r_1 = toom_3(p_1, q_1);
        PhiNum r_phi = toom_3(p_phi, q_phi);
        PhiNum r_phi2 = toom_3(p_phi2, q_phi2);
        PhiNum r_inf = toom_3(p_inf, q_inf);

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

        PhiNum v0 = r_0;
        PhiNum v1 = add(add(multiply(r_0, inv_10), multiply(r_1, inv_11)), 
                add(multiply(r_phi, inv_12), add(multiply(r_phi2, inv_13), multiply(r_inf, inv_14))));
        PhiNum v2 = add(add(multiply(r_0, inv_20), multiply(r_1, inv_21)), 
                add(multiply(r_phi, inv_22), add(multiply(r_phi2, inv_23), multiply(r_inf, inv_24))));
        PhiNum v3 = add(add(multiply(r_0, inv_30), r_1), 
                add(negate_PhiNum(r_phi), add(multiply(r_phi2, inv_33), multiply(r_inf, inv_34))));
        PhiNum v4 = r_inf;

        // recomposition: evaluate r(phi^block_size) to get our answer
        // amounts to adding these coefficients shifted by block_size * power

        return add(add(v0, shift(v1, block_size)),
                add(shift(v2, 2*block_size), add(shift(v3, 3*block_size), shift(v4, 4*block_size))));
    }

    // input: a list of PhiNums in INCREASING order of significance
    // (this is reversed from the usual representation)
    // and a base, expressed in base phi
    // output: a single PhiNum representing the value of the list of PhiNums
    // if interpreted as a single number in base b
    // equivalently, this is phinumList[0]*b^0 + phinumList[1]*b^1 + ...
    // NOTE: this method may modify the phinumList
    static PhiNum baseConvHelper(ArrayList<PhiNum> phinumList, PhiNum base) {
        // base case
        if (phinumList.size() == 1) {
            return phinumList.get(0);
        }
        
        // recursive step: combine two numbers at a time
        // reduces to a list of half the size
        // and squares the base
        // if the list has odd length, add a 0 at the end 
        // for the last number to combine with
        if ((phinumList.size())%2 == 1) {
            phinumList.add(make_PhiNum(0, 0));
        }

        // combine each pair of two adjacent numbers
        // put them in a new array combinedList
        ArrayList<PhiNum> combinedList = new ArrayList<>();
        for (int i=0; i<phinumList.size(); i+=2) {
            combinedList.add(add(toom_3(phinumList.get(i+1), base), phinumList.get(i)));
        }
        
        // keep combining until small enough to reduce to the base case
        return baseConvHelper(combinedList, toom_3(base, base));
    }

    // input: a single number in base 10 expressed as a string
    // output: the PhiNum with this value
    // runs in O(n^1.49), same as Toom-Cook
    static PhiNum baseConv(String numStr) {
        // create a list of PhiNums:
        // one PhiNum corresponding to each digit of numStr
        // in reversed order, so that it can be passed to baseConvHelper
        ArrayList<PhiNum> phinumList = new ArrayList<>();
        for (int i=numStr.length()-1; i>=0; i--) {
            char digit_at_i = numStr.charAt(i);
            int val_at_i = Integer.parseInt("" + digit_at_i);
            phinumList.add(make_PhiNum(val_at_i, 0));
        }

        return baseConvHelper(phinumList, make_PhiNum(10, 0));
    }

    // input constraint: each coin has position within [-10000, 10000]
    static final int MAX_ABS_COIN = 10000;

    public static void main(String[] args) {
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

        inv_10 = make_PhiNum(-2, 0);
        inv_11 = make_PhiNum(1, 2);
        inv_12 = make_PhiNum(-1, -1);
        inv_13 = make_PhiNum(2, -1);
        inv_14 = make_PhiNum(-1, -2);

        inv_20 = make_PhiNum(-2, 2);
        inv_21 = make_PhiNum(-1, -2);
        inv_22 = make_PhiNum(2, 1);
        inv_23 = make_PhiNum(1, -1);
        inv_24 = make_PhiNum(2, 4);

        inv_30 = make_PhiNum(3, -2);
        inv_33 = make_PhiNum(-3, 2);
        inv_34 = make_PhiNum(-2, -2);

        // read in inputs
        Scanner scanner = new Scanner(System.in);
        
        // read in k and n
        String k = scanner.next(); // k can be huge, so should be read as a string
        int n = scanner.nextInt();

        // read in inputs: n lines of integers
        ArrayList<ArrayList<Integer>> coin_input = new ArrayList<>();
        for (int i=0; i<n; i++) {
            // figure out how many integers to read, initialize a vector
            // for that test case accordingly
            int test_case_size = scanner.nextInt();
            ArrayList<Integer> test_case = new ArrayList<Integer>(test_case_size);

            // read in all the data for that test case
            for (int j=0; j<test_case_size; j++) {
                int coin_val = scanner.nextInt();
                test_case.add(coin_val);
            }
            coin_input.add(test_case);
        }

        // convert k to base phi
        PhiNum k_phi = baseConv(k);

        // for each input, calculate the product k * (input) in base phi
        for (ArrayList<Integer> coins : coin_input) {
            // convert the list of positions into a number in base phi
            // index 0 represents the most significant possible place value (MAX_ABS_COIN)
            // index 1 represents the next most after that (MAX_ABS_COIN-1)
            // therefore, each coin represents a 1 at index (MAX_ABS_COIN-coin)
            // and there are 2*MAX_ABS_COIN+1 possible coin locations
            ArrayList<Integer> coin_phi = new ArrayList<>(2*MAX_ABS_COIN+1);
            for (int i=0; i<2*MAX_ABS_COIN+1; i++) {
                coin_phi.add(0);
            } 
            for (int coin : coins) {
                coin_phi.set(MAX_ABS_COIN-coin, 1);
            }

            // there could still be multiple consecutive 1s, so deal with this here
            PhiNum canonical_coin_phi = canonicalize(1, coin_phi, -MAX_ABS_COIN);
            
            // multiply!
            PhiNum product = toom_3(canonical_coin_phi, k_phi);

            // print the place values of all the 1s in the result
            for (int idx = 0; idx < product.numArr.size(); idx++) {
                int digit = product.numArr.get(idx);
                if (digit == 1) {
                    // place value is (offset) + (# of places from the right)
                    // which equals (offset) + ((numArr.size() - 1) - idx)
                    System.out.print(product.offset + (((int)(product.numArr.size()) - 1) - idx));
                    System.out.print(" ");
                }
            }
            System.out.println();
        }

        // close the scanner to prevent leak
        scanner.close();
    }
}