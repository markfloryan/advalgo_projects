import java.util.*;
import java.util.stream.Collectors;

public class samplesolution {
    //used for floating point precision
    static final double EPS = 1e-8;
    static class Point {
        double x, y;
        
        //2d point representaiton with x and y coordinates
        Point(double x, double y) {
            this.x = x;
            this.y = y;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Point point = (Point) obj;
            return Math.abs(x - point.x) < EPS && Math.abs(y - point.y) < EPS;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
        
        @Override
        public String toString() {
            return "(" + x + ", " + y + ")";
        }
    }
    
    /*
    This function handles reading the input lines of a single polygon. 
        First line of polygon input is P, number of points
        next P lines contain x, y coordinate of a point in counter clockwise order
    */
    static List<Point> readPolygon(Scanner sc) {
        int P = sc.nextInt();
        List<Point> points = new ArrayList<>();
        for (int i = 0; i < P; i++) {
            double x = sc.nextDouble();
            double y = sc.nextDouble();
            points.add(new Point(x, y));
        }
        return points;
    }
    
    /*
    This function applies a translation to polygon and returns the result
    */
    static List<Point> translatePolygon(List<Point> polygon, double dx, double dy) {
        List<Point> translated = new ArrayList<>();
        for (Point p : polygon) {
            translated.add(new Point(p.x + dx, p.y + dy));
        }
        return translated;
    }
    
    /*
    This function solves the cross product of oa and ob
    */
    static double cross(Point o, Point a, Point b) {
        return (a.x - o.x) * (b.y - o.y) - (a.y - o.y) * (b.x - o.x);
    }
    
    /*
    This function uses the Graham's Scan implementation to solve for the convex hull
    */
    static List<Point> convexHull(List<Point> points) {
        //remove duplicates
        Set<Point> uniquePoints = new HashSet<>(points);
        if (uniquePoints.size() <= 1) {
            return new ArrayList<>(uniquePoints);
        }
        
        //find pivot point
        Point pivot = Collections.min(uniquePoints, (a, b) -> {
            if (Math.abs(a.y - b.y) < EPS) {
                return Double.compare(a.x, b.x);
            }
            return Double.compare(a.y, b.y);
        });
        
        //sort by polar angle based on pivot
        List<Point> sorted = new ArrayList<>(uniquePoints);
        sorted.sort((a, b) -> {
            if (a.equals(pivot)) return -1;
            if (b.equals(pivot)) return 1;
            
            double angleA = Math.atan2(a.y - pivot.y, a.x - pivot.x);
            double angleB = Math.atan2(b.y - pivot.y, b.x - pivot.x);
            
            if (Math.abs(angleA - angleB) < EPS) {
                double distA = (a.x - pivot.x) * (a.x - pivot.x) + (a.y - pivot.y) * (a.y - pivot.y);
                double distB = (b.x - pivot.x) * (b.x - pivot.x) + (b.y - pivot.y) * (b.y - pivot.y);
                return Double.compare(distA, distB);
            }
            return Double.compare(angleA, angleB);
        });
        
        //build convex hull
        List<Point> hull = new ArrayList<>();
        for (Point p : sorted) {
            while (hull.size() >= 2) {
                Point a = hull.get(hull.size() - 2);
                Point b = hull.get(hull.size() - 1);
                if (cross(a, b, p) <= EPS) {
                    hull.remove(hull.size() - 1);
                } else {
                    break;
                }
            }
            hull.add(p);
        }
        
        return hull;
    }
    
    /*
    This function generates one larger polygon that represents the area the robot will pass through
        First based on the distance and angle, the final expected position of the robot can be calculated
        Then find the convex hull of initial robot and final robot to get the path the robot will take
    */
    static List<Point> createPathVolume(List<Point> robot, double theta, double distance) {
        //calculate translation vector and apply to find final robot
        double angleRad = Math.toRadians(theta);
        double dx = distance * Math.cos(angleRad);
        double dy = distance * Math.sin(angleRad);
        List<Point> initialRobot = robot;
        List<Point> finalRobot = translatePolygon(robot, dx, dy);

        //solve for the convex hull of the initial and final robots together
        List<Point> allPoints = new ArrayList<>(initialRobot);
        allPoints.addAll(finalRobot);
        
        return convexHull(allPoints);
    }
    
    static boolean pointOnSegment(Point p, Point a, Point b) {
        //check if point p is colinear
        double crossVal = cross(a, b, p);
        if (Math.abs(crossVal) > EPS) {
            return false;
        }
        //check if point p is actually on the segment
        double minX = Math.min(a.x, b.x);
        double maxX = Math.max(a.x, b.x);
        double minY = Math.min(a.y, b.y);
        double maxY = Math.max(a.y, b.y);
        
        return (p.x >= minX - EPS && p.x <= maxX + EPS &&
                p.y >= minY - EPS && p.y <= maxY + EPS);
    }
    
    //check if a point is contained in a polygon using turns method
    static boolean pointInPolygon(Point point, List<Point> polygon) {
        //going counter clockwise, if each point makes a left turn with previous points, convex.
        int n = polygon.size();
        for (int i = 0; i < n; i++) {
            Point a = polygon.get(i);
            Point b = polygon.get((i + 1) % n);
            double crossVal = cross(a, b, point);
            if (crossVal < -EPS) { //point is outside (right turn goes outside convex polygon)
                return false;
            } else if (Math.abs(crossVal) < EPS) { //point is on edge or colinear
                if (pointOnSegment(point, a, b)) {
                    return true;
                }
            }
        }
        return true;
    }
    // method to reorder the polygon with the lowest y-coord first
    static void reorderPolygon(List<Point> polygon) {
        int pos = 0;
        //iterate through the points, storing the position of the lowest y coord point (taking the smaller x point if equal)
        for (int i = 1; i < polygon.size(); i++) {
            Point current = polygon.get(i);
            Point minPoint = polygon.get(pos);
            if (current.y < minPoint.y || 
                (Math.abs(current.y - minPoint.y) < EPS && current.x < minPoint.x)) {
                pos = i;
            }
        }
        //rotate the vector until the position of the lowest y coord point is at the 0th index
        Collections.rotate(polygon, -pos);
    }
    
    // calculate the minkowski difference of two polygons
    // assumes that P and Q (the two polygons) are ordered counter-clockwise
    static List<Point> minkowskiDifference(List<Point> P, List<Point> Q) {
        // Reflect Q across the origin
        List<Point> QReflected = Q.stream()
                                 .map(q -> new Point(-q.x, -q.y))
                                 .collect(Collectors.toList());
        
        List<Point> Pcopy = new ArrayList<>(P);
        List<Point> Qcopy = new ArrayList<>(QReflected);

        // the first vertex must be the lowest for both polygons
        // this results in the polygons being sorted by polar angle        
        reorderPolygon(Pcopy);
        reorderPolygon(Qcopy);
        
        List<Point> Pext = new ArrayList<>(Pcopy);
        Pext.add(Pcopy.get(0));
        Pext.add(Pcopy.get(1));
        
        List<Point> Qext = new ArrayList<>(Qcopy);
        Qext.add(Qcopy.get(0));
        Qext.add(Qcopy.get(1));
        
        List<Point> result = new ArrayList<>();
        int i = 0, j = 0;
        //loop until we iterate through all the points of both polygons
        while (i < Pext.size() - 2 || j < Qext.size() - 2) {
            //add the sum of the two points we are at
            result.add(new Point(
                Pext.get(i).x + Qext.get(j).x,
                Pext.get(i).y + Qext.get(j).y
            ));
            
            Point a = Pext.get(i);
            Point b = Pext.get(i+1);
            Point c = Qext.get(j);
            Point d = Qext.get(j+1);
            // compare the polar angles of the two edges
            double crossVal = (b.x - a.x) * (d.y - c.y) - (b.y - a.y) * (d.x - c.x);
            //increment both points of the polar angles are equal (cross product == 0)
            // if the cross product is > 0, the polar angle of P is less -> increment P
            if (crossVal >= 0 && i < Pext.size() - 2) {
                i++;
            }
            //otherwise the polar angle of Q is less -> increment Q
            if (crossVal <= 0 && j < Qext.size() - 2) {
                j++;
            }
        }
        
        return result;
    }
    
    public static void main(String[] args) {
        //input reading
        Scanner sc = new Scanner(System.in);
        
        int O = sc.nextInt();
        List<List<Point>> obstacles = new ArrayList<>();
        for (int i = 0; i < O; i++) {
            obstacles.add(readPolygon(sc));
        }
        
        List<Point> robot = readPolygon(sc);
        double theta = sc.nextDouble();
        double distance = sc.nextDouble();
        
        //solve for the path that the robot intends to take
        List<Point> pathVolume = createPathVolume(robot, theta, distance);
        
        //if Minkowski diffence of two polygons contains the origin, the two polygons are intersecintg
        //loop to find all obstacles that collide with the robot's path
        List<List<Point>> collidingObstacles = new ArrayList<>();
        for (List<Point> obstacle : obstacles) {
            List<Point> minkowskiDiff = minkowskiDifference(obstacle, pathVolume);
            if (pointInPolygon(new Point(0, 0), minkowskiDiff)) {
                collidingObstacles.add(obstacle);
            }
        }
        
        //printing output
        if (collidingObstacles.isEmpty()) {
            System.out.println(0);
        } else {
            for (List<Point> obs : collidingObstacles) {
                for (Point p : obs) {
                    System.out.print(p + " ");
                }
                System.out.println();
            }
        }
    }
}