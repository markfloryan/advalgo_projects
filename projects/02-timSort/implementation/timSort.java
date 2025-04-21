package implementation;

import java.util.*;

public class timSort {

    // returns a number equal to or slighly less than a power of 2 between 32 and 64
    public static int calculateMinRun(int n) {
        int r = 0;
        while (n >= 32) {
            r |= n & 1;     // record if a bit will be shifted off
            n >>= 1;        // divide n by 2
        }
        return n + r;
    }

    // augmented to also detect for descending runs and reverse them in place 
    public static void insertionSort(int[] arr, int left, int right) {
        // check for descending run if the first two elements are out of order
        if (left < right && arr[left] > arr[left + 1]) {
            boolean isDesc = true;
            for (int k = left; k < right; k++) {
                if (arr[k] < arr[k + 1]) {
                    // found an increasing segment so run is not descending
                    isDesc = false;
                    break;
                }
            }
            if (isDesc) {
                // reverse in-place
                int i = left, j = right;
                while (i < j) {
                    int temp = arr[i];
                    arr[i] = arr[j];
                    arr[j] = temp;
                    i++;
                    j--;
                }
                return; // sorted run
            }
        }
        // if run is not descending use standard insertionsort implementation
        for (int i = left + 1; i <= right; i++) {
            int temp = arr[i];
            int j = i - 1;
            while (j >= left && arr[j] > temp) {
                arr[j + 1] = arr[j];
                j--;
            }
            arr[j + 1] = temp;
        }
    }
    // gallop method used to quickly find where x belongs in a sorted array
    public static int gallop(int x, int[] arr, int start) {
        int hi = 1;
        int n = arr.length;
        // exponential search: multilpy hi by 2 until its idx is >= x
        // the current element in the "winning" or greater list or the end of the list is reached
        while (start + hi < n && x > arr[start + hi]) {
            hi *= 2;
        }
        // binary search between the second to last element checked and the element selected to pinpoint
        // where x belongs
        int lo = hi / 2;
        hi = Math.min(start + hi, n);
        while (lo < hi) {
            int mid = (lo + hi) / 2;
            if (x > arr[mid]) {
                lo = mid + 1;
            } else {
                hi = mid;
            }
        }
        return lo;  // x's sorted position
    }

    // combine two sorted subarrays [left,mid] and [mid+1,right]
    // track consecutive wins to trigger galloping mode to reduce the number of individual comparisons
    public static void merge(int[] arr, int left, int mid, int right) {
        int[] leftPart = Arrays.copyOfRange(arr, left, mid + 1);
        int[] rightPart = Arrays.copyOfRange(arr, mid + 1, right + 1);

        int i = 0, j = 0, k = left;
        int minGallop = 7;
        int countLeft = 0, countRight = 0;
        
        // core merge logic
        while (i < leftPart.length && j < rightPart.length) {
            if (leftPart[i] <= rightPart[j]) {
                arr[k] = leftPart[i];
                i++;
                countLeft++;    // keeping track of left subarray "wins"
                countRight = 0;
            } else {
                arr[k] = rightPart[j];
                j++;
                countRight++;   // keeping track of right subarray "wins"
                countLeft = 0;
            }
            k++;

            // one side wins individual comparisons more than min_gallop times
            // if left run is smaller call gallop to find the index of the right one where x fits
            if (countLeft >= minGallop) {
                int pos = gallop(rightPart[j], leftPart, i);
                // copy the part of array that was skipped over
                while (i < pos) {
                    arr[k] = leftPart[i];
                    i++;
                    k++;
                }
                countLeft = 0;  // reset counter

            // if right run is smaller call gallop to find the index of the left one where x fits
            } else if (countRight >= minGallop) {
                int pos = gallop(leftPart[i], rightPart, j);
                // copy the part of array that was skipped over
                while (j < pos) {
                    arr[k] = rightPart[j];
                    j++;
                    k++;
                }
                countRight = 0; // reset counter
            }
        }

        // copy over any leftover elements, only 1 will run, the other is already done
        while (i < leftPart.length) {
            arr[k] = leftPart[i];
            i++;
            k++;
        }

        while (j < rightPart.length) {
            arr[k] = rightPart[j];
            j++;
            k++;
        }
    }

    // core timsort logic: split the array into runs, sort each, then merge according to policy
    public static void timsort(int[] arr) {
        // initialize variables
        int n = arr.length;
        int minRun = calculateMinRun(n);
        List<int[]> runStack = new ArrayList<>();

        // first pass: split into runs and call the augmented insertionsort
        int i = 0;
        while (i < n) {
            // end of current run is either i + minRun (offset) or the end of the list
            int runEnd = Math.min(i + minRun - 1, n - 1);
            insertionSort(arr, i, runEnd);
            runStack.add(new int[]{i, runEnd}); // push the run to the stack
            i = runEnd + 1;     // update offset
            mergeCollapse(arr, runStack);
        }
        // merge the runs on the stack according to the merge policy until there is 1 run on the stack
        while (runStack.size() > 1) {
            mergeAt(arr, runStack, runStack.size() - 2);
        }
    }

    // enforces timsort's merge policy
    public static void mergeCollapse(int[] arr, List<int[]> runStack) {
        while (runStack.size() > 2) {
            // get the length of the first three runs on the stack
            // C is at the top of the stack, A is closest to the bottom
            int[] A = runStack.get(runStack.size() - 3);
            int[] B = runStack.get(runStack.size() - 2);
            int[] C = runStack.get(runStack.size() - 1);
            int lenA = A[1] - A[0] + 1;
            int lenB = B[1] - B[0] + 1;
            int lenC = C[1] - C[0] + 1;

            // per merge policy: 
            // A should be larger than B + C
            // B should be larger than C
            if (lenA <= lenB + lenC || lenB <= lenC) {
                if (lenA < lenC) {
                    mergeAt(arr, runStack, runStack.size() - 3);
                } else {
                    mergeAt(arr, runStack, runStack.size() - 2);
                }
            } else {
                // merge policy holds, no merges necessary
                break;
            }
        }
        // merge when there are 2 runs left on the stack
        if (runStack.size() == 2 && (runStack.get(runStack.size() - 2)[1] - runStack.get(runStack.size() - 2)[0] + 1) <= (runStack.get(runStack.size() - 1)[1] - runStack.get(runStack.size() - 1)[0] + 1)) {
            mergeAt(arr, runStack, runStack.size() - 2);
        }
    }
    // merge runs at a location on the stack
    public static void mergeAt(int[] arr, List<int[]> runStack, int i) {
        // copy subsections
        int[] run1 = runStack.get(i);
        int[] run2 = runStack.get(i + 1);
        merge(arr, run1[0], run1[1], run2[1]);
        // update stack
        runStack.set(i, new int[]{run1[0], run2[1]});
        runStack.remove(i + 1);
    }

    public static void main(String[] args) {
        // read in from stdin
        Scanner sc = new Scanner(System.in);
        int arrSize = sc.nextInt();
        int[] arr = new int[arrSize];
        for (int i = 0; i < arrSize; i++) {
            arr[i] = sc.nextInt();
        }
        sc.close();

        timsort(arr);

        // output to stdout
        for (int i = 0; i < arr.length-1; i++) {
            System.out.print(arr[i] + " ");
        }
        System.out.println(arr[arr.length-1]);
    }
}

