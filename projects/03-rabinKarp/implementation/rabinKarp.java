import java.io.*;
import java.nio.file.*;
import java.util.*;

public class rabinKarp {

    public static List<Integer> rabinKarp(String text, String pattern, int prime) {
        int n = text.length();
        int m = pattern.length();
        int d = 256; // number of characters in the input alphabet
        int h = 1;
        int p = 0; // hash value for pattern
        int t = 0; // hash value for text window
        List<Integer> result = new ArrayList<>();

        // Calculate the hash factor h = pow(d, m-1) % prime
        for (int i = 0; i < m - 1; i++) {
            h = (h * d) % prime;
        }

        // Preprocessing: calculate hash value for pattern and first window
        for (int i = 0; i < m; i++) {
            p = (d * p + pattern.charAt(i)) % prime;
            t = (d * t + text.charAt(i)) % prime;
        }

        // Slide the pattern over the text
        for (int i = 0; i <= n - m; i++) {
            if (p == t) {
                if (text.substring(i, i + m).equals(pattern)) {
                    result.add(i);
                }
            }

            if (i < n - m) {
                t = (d * (t - text.charAt(i) * h) + text.charAt(i + m)) % prime;
                if (t < 0) t += prime;
            }
        }

        return result;
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.out.println("Usage: java RabinKarp <input_file_path>");
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
        List<Integer> actualOutput = rabinKarp(text, pattern, 101);

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
