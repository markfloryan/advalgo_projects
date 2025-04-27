import java.util.Scanner;

public class Main{
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int n = scanner.nextInt();
        int c = scanner.nextInt();
        long[] scores = new long[n];
        for (int i = 0; i < n; i++) {
            scores[i] = scanner.nextLong();
        }

        PST tree = new PST(n);
        tree.construct(scores);

        for (int i = 0; i < c; i++) {
            String command = scanner.next();
            switch (command) {
                case "S" -> {
                    int x = scanner.nextInt();
                    int y = scanner.nextInt();
                    tree.swap(x - 1, y - 1);
                }
                case "R" -> {
                    int idx = scanner.nextInt();
                    long val = scanner.nextLong();
                    tree.update(idx - 1, val);
                }
                case "P" -> {
                    int idx = scanner.nextInt();
                    long max = tree.queryNodeMaximum(idx - 1);
                    tree.update(idx - 1, max);
                }
                case "Q" -> {
                    int l = scanner.nextInt();
                    int r = scanner.nextInt();
                    int v1 = scanner.nextInt();
                    int v2 = scanner.nextInt();
                    long v1Total = tree.query(v1 - 1, l - 1, r - 1);
                    long v2Total = tree.query(v2 - 1, l - 1, r - 1);
                    System.out.println(Math.abs(v2Total - v1Total));
                }
                default -> {
                    System.out.println("ERROR BAD INPUT PARSING");
                    System.exit(1);
                }
            }
        }
        scanner.close();
    }
}

