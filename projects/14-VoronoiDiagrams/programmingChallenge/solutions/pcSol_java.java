import java.util.*;

public class pcSol_java {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        int n = Integer.parseInt(sc.nextLine());
        int f = sc.nextInt();
        int p = sc.nextInt();
        sc.nextLine();

        List<Point> points = new ArrayList<>();
        for (int i = 0; i < f; i++) {
            int x = sc.nextInt();
            int y = sc.nextInt();
            points.add(new Point(x, y));
            sc.nextLine();
        }

        VoronoiDiagram v = new VoronoiDiagram();
        Map<Point, List<Point>> voronoiDiagram = v.createDiagram(points, -n, n, -n, n);
        Map<Point, Integer> siteCount = new HashMap<>();
        for (Point site : voronoiDiagram.keySet()) {
            siteCount.put(site, 0);
        }

        for (int i = 0; i < p; i++) {
            int x = sc.nextInt();
            int y = sc.nextInt();
            for (Point site : voronoiDiagram.keySet()) {
                if (v.pointInPolygon(new Point(x, y), voronoiDiagram.get(site))) {
                    siteCount.put(site, siteCount.get(site) + 1);
                }
            }
        }

        int maxCount = 0;
        Point maxPoint = new Point(0, 0);
        for (Point site : siteCount.keySet()) {
            if (siteCount.get(site) > maxCount) {
                maxCount = siteCount.get(site);
                maxPoint = site;
            }
        }
        System.out.println(maxPoint.x + " " + maxPoint.y);
    }
}
