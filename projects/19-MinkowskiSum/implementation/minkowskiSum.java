package implementation;
import java.util.*;

class pt {
    long x, y;

    pt(long x, long y) {
        this.x = x;
        this.y = y;
    }

    pt add(pt p) {
        return new pt(this.x + p.x, this.y + p.y);
    }

    pt subtract(pt p) {
        return new pt(this.x - p.x, this.y - p.y);
    }

    long cross(pt p) {
        return this.x * p.y - this.y * p.x;
    }
}

public class minkowskiSum {

    static void reorderPolygon(List<pt> P) {
        int pos = 0;
        for (int i = 1; i < P.size(); i++) {
            if (P.get(i).y < P.get(pos).y || (P.get(i).y == P.get(pos).y && P.get(i).x < P.get(pos).x)) {
                pos = i;
            }
        }
        Collections.rotate(P, -pos); // rotate left by pos
    }

    static List<pt> minkowski(List<pt> P, List<pt> Q) {
        reorderPolygon(P);
        reorderPolygon(Q);

        P.add(P.get(0));
        P.add(P.get(1));
        Q.add(Q.get(0));
        Q.add(Q.get(1));

        List<pt> result = new ArrayList<>();
        int i = 0, j = 0;

        while (i < P.size() - 2 || j < Q.size() - 2) {
            result.add(P.get(i).add(Q.get(j)));
            long cross = P.get(i + 1).subtract(P.get(i)).cross(Q.get(j + 1).subtract(Q.get(j)));
            if (cross >= 0 && i < P.size() - 2) i++;
            if (cross <= 0 && j < Q.size() - 2) j++;
        }
        return result;
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        List<pt> polyA = new ArrayList<>();
        List<pt> polyB = new ArrayList<>();

        int aSize = sc.nextInt();
        for (int i = 0; i < aSize; i++) {
            int x = sc.nextInt();
            int y = sc.nextInt();
            polyA.add(new pt(x, y));
        }

        int bSize = sc.nextInt();
        for (int i = 0; i < bSize; i++) {
            int x = sc.nextInt();
            int y = sc.nextInt();
            polyB.add(new pt(x, y));
        }

        List<pt> ans = minkowski(polyA, polyB);
        for (pt p : ans) {
            System.out.println(p.x + " " + p.y);
        }
    }
}
