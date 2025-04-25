package pc_solutions;

import java.util.*;


/**
 * Stable matching between research labs (each offering several slots) and students.
 */
public class phds {
    private static final class Slot {
        final int lab;
        final int spot;
        Slot(int lab, int spot) { this.lab = lab; this.spot = spot; }

        @Override public boolean equals(Object o) {
            return o instanceof Slot sl && sl.lab == lab && sl.spot == spot;
        }
        @Override public int hashCode() { return 31 * lab + spot; }
    }

    private record Result(boolean stable,
                          Map<Slot,Integer> slotToStudent,
                          int[] openSpots) {}

    public static void main(String[] args) throws Exception{

        Scanner in = new Scanner(System.in);

        int nlabs = in.nextInt();
        int nStudents = in.nextInt();

        //read lab data
        Map<String,Integer> labIndex = new HashMap<>();
        List<String> labNames = new ArrayList<>(nlabs);
        // capacity per lab
        int[] labSlots = new int[nlabs];
        //each lab's student list
        List<List<String>> labPrefs = new ArrayList<>(nlabs);

        for (int i = 0; i < nlabs; i++) {
            String name = in.next();
            int cap  = in.nextInt();
            //number of acceptable students
            int k = in.nextInt();
            List<String> prefs = new ArrayList<>(k);
            for (int j = 0; j < k; j++) prefs.add(in.next());

            labIndex.put(name, i);
            labNames.add(name);
            labSlots[i] = cap;
            labPrefs.add(prefs);
        }

        //reading student data
        Map<String,Integer> studentIndex = new HashMap<>();
        List<String> studentNames = new ArrayList<>(nStudents);
        List<List<String>> studentPrefs = new ArrayList<>(nStudents);
        boolean[][] willing = new boolean[nStudents][nlabs];

        for (int i = 0; i < nStudents; i++) {
            String name = in.next();
            //number of acceptable labs
            int k = in.nextInt();
            List<String> prefs = new ArrayList<>(k);
            for (int j = 0; j < k; j++) {
                String labName = in.next();
                prefs.add(labName);
                willing[i][labIndex.get(labName)] = true;
            }
            studentIndex.put(name, i);
            studentNames.add(name);
            studentPrefs.add(prefs);
        }

        /* 3 ─ Run the matcher */
        Result result = match(labPrefs, studentPrefs, labSlots,
                              willing, labIndex, studentIndex);

        /* 4 ─ Emit output */
        System.out.println(result.stable() ? "True" : "False");

        if (!result.stable()) {
            for (int l = 0; l < nlabs; l++) {
                if (result.openSpots()[l] > 0)
                    System.out.println(labNames.get(l) + " " + result.openSpots()[l]);
            }
        } else {
            for (int l = 0; l < nlabs; l++) {
                System.out.print(labNames.get(l));
                for (int s = 0; s < labSlots[l]; s++) {
                    int stu = result.slotToStudent().get(new Slot(l, s));
                    System.out.print(" " + studentNames.get(stu));
                }
                System.out.println();
            }
        }

    }

    //matching algorithm
    private static Result match(List<List<String>> labPrefs,
                                List<List<String>> stuPrefs,
                                int[] labCap,
                                boolean[][] willing,
                                Map<String,Integer> labIdx,
                                Map<String,Integer> stuIdx) {

        final int nlabs = labPrefs.size();
        final int nStu = stuPrefs.size();

        //availablity bookeeping
        boolean[][] slotOpen = new boolean[nlabs][];
        for (int l = 0; l < nlabs; l++) {
            slotOpen[l] = new boolean[labCap[l]];
            Arrays.fill(slotOpen[l], true);
        }
        boolean[] stuFree = new boolean[nStu];
        Arrays.fill(stuFree, true);

        //vacancies per lab
        int[] labVacant = labCap.clone();
        //total vacancies
        int   totalVac  = nStu;

        //pre-compute each student’s ranking of every lab (lower = better)
        int[][] rank = new int[nStu][nlabs];
        for (int[] row : rank) Arrays.fill(row, Integer.MAX_VALUE);
        for (int s = 0; s < nStu; s++) {
            int pos = 0;
            for (String labName : stuPrefs.get(s))
                rank[s][labIdx.get(labName)] = pos++;
        }

        //current engagement state
        Map<Slot,Integer> slotToStu = new HashMap<>();
        Slot[]            stuToSlot = new Slot[nStu];
        boolean           stable    = true;

        //proposal loop
        while (totalVac > 0) {
            boolean madeProgress = false;
            for (int l = 0; l < nlabs; l++) {

                // lab already full
                if (labVacant[l] == 0){
                    continue;
                }

                for (int p = 0; p < slotOpen[l].length; p++) {
                    // slot already taken
                    if (!slotOpen[l][p]){
                        continue;
                    }
                    boolean filled = false;

                    // Lab-slot (l,p) proposes down the lab’s preference list
                    for (String stuName : labPrefs.get(l)) {
                        int s = stuIdx.get(stuName);
                        // student unwilling
                        if (!willing[s][l]){
                            continue;
                        }

                        // student free ->ds engage
                        if (stuFree[s]) {
                            engage(l, p, s, slotOpen, stuFree, labVacant,
                                   slotToStu, stuToSlot);
                            totalVac--;
                            filled = true;
                            madeProgress = true;
                            break;
                        }
                        //student matched: check if they prefer this new slot
                        Slot oldSlot = stuToSlot[s];
                        int better = Integer.compare(rank[s][l], rank[s][oldSlot.lab]);
                        boolean prefer = (better < 0) ||
                                         (better == 0 && p < oldSlot.spot);

                        if (prefer) {
                            disengage(oldSlot, slotOpen, labVacant, slotToStu);
                            engage(l, p, s, slotOpen, stuFree, labVacant,
                                   slotToStu, stuToSlot);
                            filled = true;   //totalVac unchanged (1 freed, 1 taken)
                            madeProgress = true;
                            break;
                        }
                    }

                    //slot cannot be matched – mark impossible
                    if (!filled) {
                        slotOpen[l][p] = false;
                        stable = false;
                        totalVac--;
                        madeProgress = true;
                    }
                }
            }
            if (!madeProgress) {
                break;
            }
        }
        return new Result(stable, slotToStu, labVacant);
    }

    //committing the student to the lab
    private static void engage(int l, int p, int s,
                               boolean[][] slotOpen, boolean[] stuFree,
                               int[] labVacant,
                               Map<Slot,Integer> slotToStu,
                               Slot[] stuToSlot) {

        slotOpen[l][p] = false;
        stuFree[s]     = false;
        labVacant[l]--;

        Slot slot = new Slot(l, p);
        slotToStu.put(slot, s);
        stuToSlot[s] = slot;
    }

    //Undo the engagement occupying
    private static void disengage(Slot slot,
                                  boolean[][] slotOpen, int[] labVacant,
                                  Map<Slot,Integer> slotToStu) {

        slotOpen[slot.lab][slot.spot] = true;
        labVacant[slot.lab]++;
        slotToStu.remove(slot);
    }
}
