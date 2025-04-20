import java.io.*;
import java.nio.file.*;
import java.util.*;

public class rabinKarp {
    public static List<Integer> rabin_karp(String text, String pattern) {
        List<Integer> indices = new ArrayList<>();
        int base = 26;
        int q = 101;
        long target_hash = 0, curr_hash = 0;
        int l = 0;

        // Compute the hash of the pattern
        for (int i = 0; i < pattern.length(); i++) {
            // Go in reverse of pattern to represent leftmost character as highest order
            target_hash = (target_hash * base + (pattern.charAt(i) - 'a' + 1)) % q;
        }

        // Sliding window template
        for (int r = 0; r < text.length(); r++) {
            // Left shift hash to make room for new character in base 26, then add new character's unicode normalized by 'a'
            curr_hash = (curr_hash * base + (text.charAt(r) - 'a' + 1)) % q;

            // Update hash value with rolling hash technique when window becomes oversized
            if (r - l + 1 > pattern.length()) {
                // Remove leftmost highest order character at position l
                curr_hash = (curr_hash - (text.charAt(l) - 'a' + 1) * modPow(base, pattern.length(), q)) % q;
                // Java might return negative mod
                if (curr_hash < 0) curr_hash += q;
                l += 1;
            }

            // Check if the current window matches the pattern
            if (r - l + 1 == pattern.length() && curr_hash == target_hash) {
                if (text.substring(l, r + 1).equals(pattern)) {      // Manual check to avoid false positives and spurious hits
                    indices.add(l);
                }
            }
        }

        // Final list of starting indices where the pattern is found in the text
        return indices;
    }

    // Helper method for modular exponentiation (equivalent to Python's pow(base, exponent, modulus))
    private static long modPow(long base, long exponent, long modulus) {
        if (modulus == 1) return 0;
        
        long result = 1;
        base = base % modulus;
        while (exponent > 0) {
            if (exponent % 2 == 1) {
                result = (result * base) % modulus;
            }
            exponent = exponent >> 1;
            base = (base * base) % modulus;
        }
        return result;
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.out.println("Usage: java rabinKarp <input_file_path>");
            return;
        }

        String inputFile = args[0];
        String baseName = Paths.get(inputFile).getFileName().toString();
        String[] parts = baseName.split("\\.");
        String testNumber = parts[parts.length - 1];
        String expectedOutputFile = "io/sample.out." + testNumber;

        // Read input file
        List<String> lines = Files.readAllLines(Paths.get(inputFile));
        if (lines.size() < 2) {
            System.out.println("Invalid input file format. Expected 2 lines.");
            return;
        }
        String pattern = lines.get(0).trim();
        String text = lines.get(1).trim();

        // Run Rabin-Karp
        List<Integer> actualOutput = rabin_karp(text, pattern);

        // Read expected output
        List<Integer> expectedOutput = null;
        try {
            String outputContent = new String(Files.readAllBytes(Paths.get(expectedOutputFile))).trim();
            expectedOutput = parseOutput(outputContent);
        } catch (IOException e) {
            System.out.println("Expected output file not found. Skipping comparison.");
        }

        // Print results
        if (expectedOutput != null) {
            System.out.println("Expected Output: " + expectedOutput);
        }
        System.out.println("Actual Output:   " + actualOutput);

        if (expectedOutput != null) {
            System.out.println("Test Passed:     " + actualOutput.equals(expectedOutput));
        }
    }

    private static List<Integer> parseOutput(String outputContent) {
        outputContent = outputContent.replaceAll("\\[|\\]", "");
        String[] tokens = outputContent.split(",");
        List<Integer> result = new ArrayList<>();
        for (String token : tokens) {
            if (!token.trim().isEmpty()) {
                result.add(Integer.parseInt(token.trim()));
            }
        }
        return result;
    }
}
