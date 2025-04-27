import java.util.*;

public class pcSol_java {

    // returns a number equal to or slighly less than a power of 2 between 32 and 64
    static int calculateMinRun(int n) {
        int r = 0;
        while (n >= 32) {
            r |= n & 1;     // record if a bit will be shifted off
            n >>= 1;        // divide n by 2
        }
        return n + r;
    }

    // augmented to also detect for descending runs and reverse them in place 
    static void insertionSort(List<Ship> arr, int left, int right, Comparator<Ship> key) {
        // check for descending run if the first two elements are out of order
        if (left < right && key.compare(arr.get(left), arr.get(left + 1)) > 0) {
            boolean isDesc = true;
            for (int k = left; k < right; k++) {
                // found an increasing segment so run is not descending
                if (key.compare(arr.get(k), arr.get(k + 1)) < 0) {
                    isDesc = false;
                    break;
                }
            }
            if (isDesc) {
                // reverse in-place
                Collections.reverse(arr.subList(left, right + 1));
                return; // sorted run
            }
        }

        // if run is not descending use standard insertionsort implementation
        for (int i = left + 1; i <= right; i++) {
            Ship temp = arr.get(i);
            int j = i - 1;
            while (j >= left && key.compare(arr.get(j), temp) > 0) {
                arr.set(j + 1, arr.get(j));
                j--;
            }
            arr.set(j + 1, temp);
        }
    }

    // gallop method used to quickly find where x belongs in a sorted array
    static int gallop(Ship x, List<Ship> arr, int start, Comparator<Ship> key) {
        int hi = 1;
        int n = arr.size();
        // exponential search: multilpy hi by 2 until its idx is >= x
        // the current element in the "winning" or greater list or the end of the list is reached
        while (start + hi < n && key.compare(x, arr.get(start + hi)) > 0) {
            hi *= 2;
        }
        // binary search between the second to last element checked and the element selected to pinpoint
        // where x belongs
        int lo = hi / 2;
        hi = Math.min(start + hi, n);
        while (lo < hi) {
            int mid = (lo + hi) / 2;
            if (key.compare(x, arr.get(mid)) > 0) {
                lo = mid + 1;
            } else {
                hi = mid;
            }
        }
        return lo;  // x's sorted position
    }

    // combine two sorted subarrays [left,mid] and [mid+1,right]
    // track consecutive wins to trigger galloping mode to reduce the number of individual comparisons
    static void merge(List<Ship> arr, int left, int mid, int right, Comparator<Ship> key) {
        List<Ship> leftPart = new ArrayList<>(arr.subList(left, mid + 1));
        List<Ship> rightPart = new ArrayList<>(arr.subList(mid + 1, right + 1));

        int i = 0, j = 0, k = left;
        int minGallop = 7;
        int countLeft = 0, countRight = 0;

        // core merge logic
        while (i < leftPart.size() && j < rightPart.size()) {
            if (key.compare(leftPart.get(i), rightPart.get(j)) <= 0) {
                arr.set(k++, leftPart.get(i++));
                countLeft++;    // keeping track of left subarray "wins"
                countRight = 0;
            } else {
                arr.set(k++, rightPart.get(j++));
                countRight++;   // keeping track of right subarray "wins"
                countLeft = 0;
            }

            // one side wins individual comparisons more than min_gallop times
            // if left run is smaller call gallop to find the index of the right one where x fits
            if (countLeft >= minGallop) {
                int pos = gallop(rightPart.get(j), leftPart, i, key);
                // copy the part of array that was skipped over
                while (i < pos) arr.set(k++, leftPart.get(i++));
                countLeft = 0;  // reset counter
            // if right run is smaller call gallop to find the index of the left one where x fits
            } else if (countRight >= minGallop) {
                int pos = gallop(leftPart.get(i), rightPart, j, key);
                while (j < pos) arr.set(k++, rightPart.get(j++));
                countRight = 0;  // reset counter
            }
        }

        // copy over any leftover elements, only 1 will run, the other is already done
        while (i < leftPart.size()) arr.set(k++, leftPart.get(i++));
        while (j < rightPart.size()) arr.set(k++, rightPart.get(j++));
    }

    // core timsort logic: split the array into runs, sort each, then merge according to policy
    static void timsort(List<Ship> arr, Comparator<Ship> key) {
        int n = arr.size();
        int minRun = calculateMinRun(n);
        List<int[]> runStack = new ArrayList<>();

        // first pass: split into runs and call the augmented insertionsort
        int i = 0;
        while (i < n) {
            // end of current run is either i + minRun (offset) or the end of the list
            int runEnd = Math.min(i + minRun - 1, n - 1);
            insertionSort(arr, i, runEnd, key);
            runStack.add(new int[]{i, runEnd}); // push the run to the stack
            i = runEnd + 1;     // update offset
            mergeCollapse(arr, runStack, key);
        }
        // merge the runs on the stack according to the merge policy until there is 1 run on the stack
        while (runStack.size() > 1) {
            mergeAt(arr, runStack, runStack.size() - 2, key);
        }
    }

    // enforces timsort's merge policy
    static void mergeCollapse(List<Ship> arr, List<int[]> runStack, Comparator<Ship> key) {
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
                    mergeAt(arr, runStack, runStack.size() - 3, key);
                } else {
                    mergeAt(arr, runStack, runStack.size() - 2, key);
                }
            } else {
                // merge policy holds, no merges necessary
                break;
            }
        }

        // merge when there are 2 runs left on the stack
        if (runStack.size() == 2) {
            int[] A = runStack.get(0);
            int[] B = runStack.get(1);
            if (A[1] - A[0] + 1 <= B[1] - B[0] + 1) {
                mergeAt(arr, runStack, 0, key);
            }
        }
    }

    // merge runs at a location on the stack
    static void mergeAt(List<Ship> arr, List<int[]> runStack, int i, Comparator<Ship> key) {
        // copy subsections
        int[] run1 = runStack.get(i);
        int[] run2 = runStack.get(i + 1);
        merge(arr, run1[0], run1[1], run2[1], key);
        // update stack
        run1[1] = run2[1];
        runStack.remove(i + 1);
    }

    // polar angle comparator for "twist"
    static Comparator<Ship> polarAngleComparator = (a, b) -> {
        // Check Above/Below Lighthouse
        boolean ha = a.y > 0 || (a.y == 0 && a.x >= 0);
        boolean hb = b.y > 0 || (b.y == 0 && b.x >= 0);

        if (ha != hb) return ha ? -1 : 1;

        // Cross product for angle
        long cross = (long) a.x * b.y - (long) a.y * b.x;
        if (cross != 0) return cross > 0 ? -1 : 1;

        // Sort by radius
        if (a.r != b.r) return Double.compare(a.r, b.r);

        // Sort by order of appearance (index)
        return Integer.compare(a.index, b.index);
    };

    //Ship class
    static class Ship {
        int x, y, index;
        double r;
        String name;

        //constructor with x pos, y pos, name of ship, and order
        Ship(int x, int y, String name, int index) {
            this.x = x;
            this.y = y;
            this.name = name;
            this.index = index;
            this.r = Math.sqrt(x * x + y * y);
        }
    }

    public static void main(String[] args) {
        // read in from stdin
        Scanner sc = new Scanner(System.in);
        int numShips = sc.nextInt();
        int numQueries = sc.nextInt();

        Map<Ship, String> shipMap = new HashMap<>();
        List<Ship> shipsSorted = new ArrayList<>();

        //place ships on grid
        for (int i = 0; i < numShips; i++) {
            int x = sc.nextInt();
            int y = sc.nextInt();
            String name = sc.next();
            Ship s = new Ship(x, y, name, i);
            shipsSorted.add(s);
            shipMap.put(s, name);
        }

        //sort ships
        timsort(shipsSorted, polarAngleComparator);

        //add queried ships to array list queried as the solution
        for (int i = 0; i < numQueries; i++) {
            double radius = sc.nextDouble();
            List<String> queried = new ArrayList<>();
            for (Ship s : shipsSorted) {
                if (s.r <= radius) queried.add(s.name);
            }
            // output to stdout
            if (queried.isEmpty()) System.out.println(-1);
            else System.out.println(String.join(" ", queried));
        }

        sc.close();
    }
}
