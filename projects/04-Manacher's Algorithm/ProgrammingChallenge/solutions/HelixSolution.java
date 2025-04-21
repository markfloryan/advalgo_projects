package solutions;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
//main idea: Normally, manacher's algo can guarantee that the left half of a palindrome is the same as the right half of a palindrome (after the palindrome is checked at a given center). The values on the right half of the center are also at least as much as either the min(right bound (current value to the right bound) or the left half's corresponding value). However, an issue arises with the center value when performing a matching palindrome instead of a normal one. Manacher's algo can guarantee that the left side of the array matches with the right side at a given center. However, the center value of the current is not fully checked in this scenario and some issues may arise that normally don't happen.

// The center may be a part of some other palindrome from the left side, and it may be a match with the corresponding values from left half's palindromes (with respect to each left half's palindromes center) , however, it MUST match with the corresponding values in right hand sided palindromes in order to use the saved results from the left side, and this may not always happen. So, at an index i, and a center c, you must check if the value at 2i - c(corresponding letter in palindrome with index in the center of the palindrome) matches with the value at center c. If it does, you may fully use the saved value, but if it doesn't and if the saved value exceeds 2i-c, you can only use 2i - c- 1 of the saved value as every value before the corresponding value of the center is guaranteed to match, but the center's corresponding value does not. 
public class HelixSolution {
    // checks if two characters are a match
    private static boolean matches(char a, char b) {
        switch (a) {
            case 'A': return b == 'T';
            case 'T': return b == 'A';
            case 'C': return b == 'G';
            case 'G': return b == 'C';
            case '#': return b == '#';
            default:  return false;
        }
    }

    // Insert '#' between every character and at the beginning and end of the string
    private static String preprocess(String s) {
        StringBuilder sb = new StringBuilder(s.length() * 2 + 1);
        sb.append('#');
        for (char c : s.toCharArray()) {
            sb.append(c).append('#');
        }
        return sb.toString();
    }

    // Remove all '#' characters
    private static String postprocess(String s) {
        return s.replace("#", "");
    }

    public static String helix(String input) {
        String s = preprocess(input);
        int n = s.length();
        int[] radius = new int[n];
        int center = 0, right = 0;

        for (int i = 1; i < n - 1; i++) {
            //calculate mirrored position of i with respect to center
            int mirroredPos = 2 * center - i;

            if (i < right) {
                //calculate center position with respect to i and finds the corresponding mirroed position of the center with respect to i
                int distFromCenter = i - center;
                int centerMirror = i + distFromCenter;
                if (centerMirror < n && matches(s.charAt(centerMirror), s.charAt(center))) {
                    //center matches so we can proceed as normally
                    radius[i] = Math.min(right - i, radius[mirroredPos]);
                } else {
                    //# If the center character doesn't match, we can only use the distance up to the center -1
                    radius[i] = Math.min(
                        Math.min(right - i, distFromCenter - 1),
                        radius[mirroredPos]
                    );
                }
            }

            // Expand the palindrome centered at i
            while (i + radius[i] + 1 < n
                && i - (radius[i] + 1) >= 0
                && matches(
                    s.charAt(i + radius[i] + 1),
                    s.charAt(i - (radius[i] + 1))
                )) {
                radius[i]++;
            }

            // Update center/right
            if (i + radius[i] > right) {
                center = i;
                right = i + radius[i];
            }
        }

        // Find maximum radius
        int maxRadius = 0, maxCenter = 0;
        for (int i = 0; i < n; i++) {
            if (radius[i] > maxRadius) {
                maxRadius = radius[i];
                maxCenter = i;
            }
        }

        // Extract and postprocess
        int start = maxCenter - maxRadius;
        int end   = maxCenter + maxRadius + 1; 
        String candidate = s.substring(start, end);
        return postprocess(candidate);
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String dna = br.readLine().trim();
        System.out.println(helix(dna));
    }
}