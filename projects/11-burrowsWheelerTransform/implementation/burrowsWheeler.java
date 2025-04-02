/*
 * Adapted from https://nbviewer.org/github/BenLangmead/comp-genomics-class/blob/master/notebooks/CG_BWT_Reverse.ipynb
 */
import java.util.*;

public class burrowsWheeler {
    public static void main(String[] args){
        Scanner sc = new Scanner(System.in);
        
        burrowsWheeler transformer = new burrowsWheeler();
        String t = sc.next();
        sc.close();
        t += "$";
        String b = transformer.bwtViaSortedRotations(t);
        System.out.println(b);

        Object[] rankBwtOutput = transformer.rankBwt(b);
        List<Integer> ranks = (List<Integer>)rankBwtOutput[0];
        Map<Character, Integer> tots = (Map<Character, Integer>)rankBwtOutput[1];
        // System.out.println(ranks);
        // System.out.println(tots);

        String reverse = transformer.reverseBwt(b);
        System.out.println(reverse.substring(0, reverse.length() - 1));
    }
    public String[] rotations(String t){
        String tt = t + t;
        int strLen = t.length();
        String[] rotations = new String[strLen];
        for(int i = 0; i < strLen; i++){
            rotations[i] = tt.substring(i, i+strLen);
        }
        return rotations;
    }
    public String[] sortedRotations(String t){
        String[] rotations = rotations(t);
        Arrays.sort(rotations);
        return rotations;
    }
    public String bwtViaSortedRotations(String t){
        String bwt = "";
        String[] sortedRots = sortedRotations(t);
        int strLen = t.length();
        for(int i = 0; i < t.length(); i++){
            bwt += sortedRots[i].substring(strLen-1);
        }
        return bwt;
    }
    public Object[] rankBwt(String bw){
        Map<Character, Integer> tots = new HashMap<>();
        List<Integer> ranks = new ArrayList<>();
        for(char c: bw.toCharArray()){
            if(!tots.containsKey(c)){
                tots.put(c, 0);
            }
            int totsC = tots.get(c);
            ranks.add(totsC);
            tots.put(c, totsC + 1);
        }
        Object[] output = new Object[2];
        output[0] = ranks;
        output[1] = tots;
        return output;
    }
    public Map<Character, Integer> firstCol(Map<Character, Integer> tots){
        Map<Character, Integer> first = new HashMap<>();
        int totc = 0;
        List<Character> sortedChars = tots.keySet().stream()
            .sorted()
            .toList();
        for(char c: sortedChars){
            first.put(c, totc);
            totc += tots.get(c);
        }
        return first;
    }
    public String reverseBwt(String bw){
        Object[] rankBwtOutput = rankBwt(bw);
        List<Integer> ranks = (List<Integer>)rankBwtOutput[0];
        Map<Character, Integer> tots = (Map<Character, Integer>)rankBwtOutput[1];
        Map<Character, Integer> first = firstCol(tots);
        int rowi = 0;
        String t = "$";
        while(bw.charAt(rowi) != '$'){
            String c = bw.substring(rowi, rowi+1);
            t = c + t;
            rowi = first.get(c.charAt(0)) + ranks.get(rowi);
        }
        return t;
    }
}