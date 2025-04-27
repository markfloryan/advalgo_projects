import java.io.*;
import java.nio.file.*;
import java.util.*;

public class pcSol_java {
    public static int countPalindromicSubstrings(String s) {
        // Reverse the string for comparison later
        String r = new StringBuilder(s).reverse().toString();

        int base = 26;
        int q = (int)1e9 + 7;
        int ans = 0;

        // Iterate over all possible starting indices of substrings
        for (int i = 0; i < s.length(); i++) {
            // Initialize hash values for original and reversed substrings
            long originalHash = 0;
            long reversedHash = 0;

            // Iterate over all possible ending indices of substrings
            for (int j = i; j < s.length(); j++) {
                // Convert current character to a number based on unicode normalized by 'a'
                int currentCharValue = s.charAt(j) + 1;

                // Update rolling hash for original string, we are adding a character to the end so left shift and make space for lower order bit to be added
                originalHash = (originalHash * base + currentCharValue) % q;

                // Compute position of corresponding character in the reversed string
                int reverseCharIndex = r.length() - j - 1;
                int reverseCharValue = r.charAt(reverseCharIndex) + 1;

                // For reversed hash the new character added is at the beginning, so we need to set/add higher order bit to hash
                reversedHash = (reversedHash + reverseCharValue * modPow(base, j - i, q)) % q;

                // If hashes match, we might have a palindrome.
                if (originalHash == reversedHash) {
                    // Confirm it is actually a palindrome by manually checking, avoid false positives and spurious hits
                    if (s.substring(i, j + 1).equals(new StringBuilder(s.substring(i, j + 1)).reverse().toString())) {
                        ans++;
                    }
                }
            }
        }

        // Final count of palindromic substrings
        return ans;
    }

    // Helper function for modular exponentiation
    private static long modPow(long base, long exp, long mod) {
        long result = 1;
        base = base % mod;
        while (exp > 0) {
            if ((exp & 1) == 1) {
                result = (result * base) % mod;
            }
            base = (base * base) % mod;
            exp >>= 1;
        }
        return result;
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.out.println("Usage: java RabinKarp <input_file_path>");
            return;
        }

        String inputFile = args[0];
        // String baseName = Paths.get(inputFile).getFileName().toString();
        // String[] parts = baseName.split("\\.");
        // String testNumber = parts[parts.length - 1];
        String expectedOutputFile = inputFile.replace("in", "out");

        // Read input file
        List<String> lines = Files.readAllLines(Paths.get(inputFile));
        if (lines.size() < 1) {
            System.out.println("Invalid input file format. Expected 1 lines.");
            return;
        }
        String inputString = lines.get(0).trim();

        // Run Rabin-Karp
        int actualOutput = countPalindromicSubstrings(inputString);

        // Read expected output
        int expectedOutput = -1;
        try {
            String outputContent = new String(Files.readAllBytes(Paths.get(expectedOutputFile))).trim();
            expectedOutput = parseOutput(outputContent);
        } catch (IOException e) {
            System.out.println("Expected output file not found. Skipping comparison.");
        }

        // Print results
        if (expectedOutput != -1) {
            System.out.println("Expected Output: " + expectedOutput);
        }
        System.out.println("Actual Output:   " + actualOutput);

        if (expectedOutput != -1) {
            System.out.println("Test Passed:     " + (actualOutput == expectedOutput));
        }
    }

    private static int parseOutput(String outputContent) {
        String[] parts = outputContent.split("\\s+");
        if (parts.length == 0) {
            throw new IllegalArgumentException("Invalid output format.");
        }
        return Integer.parseInt(parts[0]);
    }
}
