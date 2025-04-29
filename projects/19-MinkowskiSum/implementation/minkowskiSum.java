package implementation;
import java.util.*;

// data structure to store points
class pt {
    long x, y;
    // constructor
    pt(long x, long y) {
        this.x = x;
        this.y = y;
    }
    // define addition of 2 points
    pt add(pt p) {
        return new pt(this.x + p.x, this.y + p.y);
    }
    // define subtraction of 2 points
    pt subtract(pt p) {
        return new pt(this.x - p.x, this.y - p.y);
    }
    // calculate cross product of 2 points with (x1*y2 - y1*x2) where 1 indicates the 1st point and 2 indictaes the 2nd.
    long cross(pt p) {
        return this.x * p.y - this.y * p.x;
    }
}

public class minkowskiSum {

    // method to reorder the polygon with the lowest y-coord first
    static void reorderPolygon(List<pt> P) {
        int pos = 0;
        // iterate through the points, storing the position of the lowest y coord point (taking the smaller x point if equal)
        for (int i = 1; i < P.size(); i++) {
            if (P.get(i).y < P.get(pos).y || (P.get(i).y == P.get(pos).y && P.get(i).x < P.get(pos).x)) {
                pos = i;
            }
        }
        // rotate the vector until the position of the lowest y coord point is at the 0th index
        Collections.rotate(P, -pos);
    }
    // calculate the minkowski sum of two polygons
    // assumes that P and Q (the two polygons) are ordered counter-clockwise
    static List<pt> minkowski(List<pt> P, List<pt> Q) {
        // the first vertex must be the lowest for both polygons
        // this results in the polygons being sorted by polar angle
        reorderPolygon(P);
        reorderPolygon(Q);

        // allow cyclic indexing by adding the first two points to the end of the vector 
        // this allows for checking of the last point of the polygon
        P.add(P.get(0));
        P.add(P.get(1));
        Q.add(Q.get(0));
        Q.add(Q.get(1));

        List<pt> result = new ArrayList<>();
        int i = 0, j = 0;
        // loop until we iterate through all the points of both polygons
        while (i < P.size() - 2 || j < Q.size() - 2) {
            // add the sum of the two points we are at
            result.add(P.get(i).add(Q.get(j)));
            // compare the polar angles of the two edges
            long cross = P.get(i + 1).subtract(P.get(i)).cross(Q.get(j + 1).subtract(Q.get(j)));
            // increment both points of the polar angles are equal (cross product == 0)
            // if the cross product is > 0, the polar angle of P is less -> increment P
            if (cross >= 0 && i < P.size() - 2) i++;
            // otherwise the polar angle of Q is less -> increment Q
            if (cross <= 0 && j < Q.size() - 2) j++;
        }
        return result;
    }

    // main function to calculate minkowski sum
    // expect input of 2 polygons, where a polygon is defined by 
    // the first line containing a single integer x indicating the number of points in the polygon
    // and the next x lines containing 2 space seperated integers, where the first is the x value and the second is the y value
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        List<pt> polyA = new ArrayList<>();
        List<pt> polyB = new ArrayList<>();

        // read size of first polygon
        int aSize = sc.nextInt();
        // iterate through the points and add to the first polygon vector
        for (int i = 0; i < aSize; i++) {
            int x = sc.nextInt();
            int y = sc.nextInt();
            polyA.add(new pt(x, y));
        }
        // read size of second polygon
        int bSize = sc.nextInt();
        // iterate through the points and add to the second polygon vector
        for (int i = 0; i < bSize; i++) {
            int x = sc.nextInt();
            int y = sc.nextInt();
            polyB.add(new pt(x, y));
        }
        // get minkowski sum
        List<pt> ans = minkowski(polyA, polyB);
        // iterate through points and print out the polygon with each point on a new line
        // with 2 space seperated values indicating x then y value
        for (pt p : ans) {
            System.out.println(p.x + " " + p.y);
        }
    }
}
