import java.util.*;
import java.math.*;

class Point {
    double x;
    double y;
    double magnitudeSquared;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
        this.magnitudeSquared = Math.pow(x, 2) + Math.pow(y, 2);
    }

    public boolean equals(Point other) {
        return x == other.x && y == other.y;
    }

    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}

class SortPoints implements Comparator<Point>
{
    // Compare by roll number in ascending order
    public int compare(Point a, Point b) {
        if (a.x < b.x) {
            return -1;
        } else if (a.x > b.x) {
            return 1;
        } else if (a.y < b.y) {
            return -1;
        }
        return 1;
    }
}

class Vector {
    double x;
    double y;

    public Vector(double x, double y) {
        this.x = x;
        this.y = y;
    }
}

class Node {
    Node left, right;
    Node parent;

    public Node(Node parent) {
        this.parent = parent;
        left = null;
        right = null;
    }
}

class ArcNode extends Node {
    Point site;
    Event circleEvent; // Replace Object with the actual type of circle_event
    HalfEdge edge; // Replace Object with the actual type of edge

    public ArcNode(Point site, Node parent, Event circleEvent, HalfEdge edge) {
        super(parent);
        this.site = site;
        this.circleEvent = circleEvent;
        this.edge = edge;
    }

    public ArcNode(Point site) {
        super(null);
        this.site = site;
        this.circleEvent = null;
        this.edge = null;
    }

    public String toString() {
        return "Arc Node: " + site;
    }
}

class BreakpointNode extends Node {
    Point oldSite; // Replace Object with the actual type of old_site
    Point newSite; // Replace Object with the actual type of new_site
    HalfEdge edge; // Replace Object with the actual type of edge

    public BreakpointNode(Point oldSite, Point newSite, Node parent, HalfEdge edge) {
        super(parent);
        this.oldSite = oldSite;
        this.newSite = newSite;
        this.edge = edge;
    }

    public BreakpointNode(Point oldSite, Point newSite) {
        super(null);
        this.oldSite = oldSite;
        this.newSite = newSite;
        this.edge = null;
    }

    public Point getOldSite() {
        return oldSite;
    }

    public Point getNewSite() {
        return newSite;
    }

    public String toString() {
        String result = "";
        if (left != null) {
            result += left.toString() + "\n";
        }
        result += "Breakpoint Node: [" + oldSite + ", " + newSite + "]\n";
        if (right != null) {
            result += right.toString() + "\n";
        }
        return result;
    }
}

class Event {
    String category;
    Integer index;
    Point site;
    ArcNode leaf;
    Double key;

    public Event(String category, Point site) {
        this.category = category;
        this.index = null;
        this.site = site;
        this.key = site.y;
    }

    public Event(String category, ArcNode leaf, Double circle_bottom) {
        this.category = category;
        this.index = null;
        this.leaf = leaf;
        this.key = circle_bottom;
    }

    public String toString() {
        if (site != null) {
            return "Site: " + site.toString();
        } else {
            return "Circle: " + leaf.toString();
        }
    }
}

class HalfEdge {
    Point point;
    Point origin; // Replace with actual type if known
    Vector vector; // Replace with actual type if known
    HalfEdge prev, next;
    HalfEdge twin;

    public HalfEdge() {
        this.point = null;
        this.origin = null;
        this.vector = null;
        this.prev = null;
        this.next = null;
        this.twin = null;
    }

    public HalfEdge getTwin() {
        return twin;
    }

    public void setTwin(HalfEdge twin) {
        this.twin = twin;
    }

    public Point getPoint() {
        return point;
    }

    public void setPoint(Point point) {
        this.point = point;
    }

    public Vector getVector() {
        return vector;
    }

    public void setVector(Vector vector) {
        this.vector = vector;
    }

    public Point getOrigin() {
        return origin;
    }

    public void setOrigin(Point origin) {
        this.origin = origin;
    }

    public String toString() {
        if (origin != null) {
            return "[" + point.toString() + ", " + origin.toString() + "]";
        } else {
            return "[" + point.toString() + ", null]";
        }
    }
}

class Edge {
    Point start;
    Point end;
    public Edge(Point start, Point end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public boolean equals(Object obj) {
        Edge other = (Edge) obj;
        return start.equals(other.start) && end.equals(other.end);
    }

    @Override
    public int hashCode() {
        return ((int) (start.x * 7919) + (int) (start.y * Math.pow(7867, 2)) + (int) (end.x * Math.pow(7817, 3)) + (int) (end.x * Math.pow(7019, 4))) % 1000000007;
    }

    public String toString() {
        return "(" + start.toString() + ", " + end.toString() + ")";
    }
}

class EventHeap {
    private List<Event> heap;

    public EventHeap() {
        heap = new ArrayList<>();
        heap.add(null); // Placeholder for 1-based index
    }

    public boolean empty() {
        return heap.size() == 1;
    }

    public boolean compare(int a, int b) {
        Event aEvent = heap.get(a);
        Event bEvent = heap.get(b);
        if (aEvent.key > bEvent.key) {
            return true;
        } else if (aEvent.key < bEvent.key) {
            return false;
        } else if (aEvent.category.equals("circle") || bEvent.category.equals("circle")) {
            return true;
        }
        return aEvent.site.x < bEvent.site.x;
    }

    public void swap(int a, int b) {
        Event temp = heap.get(a);
        heap.set(a, heap.get(b));
        heap.set(b, temp);
        heap.get(a).index = a;
        heap.get(b).index = b;
    }

    public void heapDown(int index) {
        if (2 * index >= heap.size()) return;
        int left = 2 * index;
        int right = 2 * index + 1;
        if (right < heap.size()) {
            if (compare(left, index) && compare(left, right)) {
                swap(index, left);
                heapDown(left);
            } else if (compare(right, index)) {
                swap(index, right);
                heapDown(right);
            }
        } else {
            if (compare(left, index)) {
                swap(index, left);
                heapDown(left);
            }
        }
    }

    public void heapUp(int index) {
        if (index == 1) return;
        int parent = index / 2;
        if (compare(index, parent)) {
            swap(index, parent);
            heapUp(parent);
        }
    }

    public void heapInsert(Event event) {
        int index = heap.size();
        heap.add(event);
        event.index = index;
        heapUp(index);
    }

    public void heapRemove(int index) {
        swap(index, heap.size() - 1);
        heap.remove(heap.size() - 1);
        if (index > 1 && index < heap.size() && compare(index, index / 2)) {
            heapUp(index);
        } else {
            heapDown(index);
        }
    }

    public Event heapMax() {
        Event e = heap.get(1);
        heapRemove(1);
        return e;
    }

    public Event heapPeek() {
        return heap.get(1);
    }

    public Event[] heapDoublePeek() {
        Event first = heap.get(1);
        Event second = (heap.size() <= 3 || compare(2, 3)) ? heap.get(2) : heap.get(3);
        return new Event[]{first, second};
    }

    public String toString() {
        String result = "[";
        for (Event event : heap) {
            if (event != null) {
                result += event.toString() + ",";
            }
        }
        return result + "]";
    }
}

class BeachlineTree {
    public Node root;

    public BeachlineTree() {
        this.root = null;
    }

    private boolean isLeft(Node node) {
        return node.parent.left == node;
    }

    private boolean isRight(Node node) {
        return node.parent.right == node;
    }

    public Node insertRoot(Node root) {
        this.root = root;
        return this.root;
    }

    public Node insertLeft(Node parent, Node node) {
        parent.left = node;
        node.parent = parent;
        return node;
    }

    public Node insertRight(Node parent, Node node) {
        parent.right = node;
        node.parent = parent;
        return node;
    }

    public void remove(Node node) {
        Node parent = node.parent;
        if (parent != null) {
            if (isLeft(node)) {
                parent.left = null;
            } else {
                parent.right = null;
            }
        }
    }

    public void replaceParent(Node node) {
        if (node.parent == this.root) {
            this.root = node;
            return;
        }
        if (isLeft(node.parent)) {
            node.parent.parent.left = node;
            node.parent = node.parent.parent;
        } else {
            node.parent.parent.right = node;
            node.parent = node.parent.parent;
        }
    }

    public Node minNode(Node node) {
        while (node.left != null) {
            node = node.left;
        }
        return node;
    }

    public Node maxNode(Node node) {
        while (node.right != null) {
            node = node.right;
        }
        return node;
    }

    public Node predecessorNode(Node node) {
        if (node.left != null) return maxNode(node.left);
        Node child = node;
        node = node.parent;
        while (node != null) {
            if (isRight(child)) return node;
            child = node;
            node = node.parent;
        }
        return node;
    }

    public Node successorNode(Node node) {
        if (node.right != null) return minNode(node.right);
        Node child = node;
        node = node.parent;
        while (node != null) {
            if (isLeft(child)) return node;
            child = node;
            node = node.parent;
        }
        return node;
    }

    public ArcNode predecessorLeaf(Node node) {
        if (node == minNode(this.root)) return null;
        node = predecessorNode(node).left;
        while (true) {
            if (node.right != null) {
                node = node.right;
            } else if (node.left != null) {
                node = node.left;
            } else {
                return (ArcNode) node;
            }
        }
    }

    public ArcNode successorLeaf(Node node) {
        if (node == maxNode(this.root)) return null;
        node = successorNode(node).right;
        while (true) {
            if (node.left != null) {
                node = node.left;
            } else if (node.right != null) {
                node = node.right;
            } else {
                return (ArcNode) node;
            }
        }
    }

    public ArcNode findArc(Point site) {
        Node node = this.root;
        while (node instanceof BreakpointNode) {
            Point intersection = intersect(((BreakpointNode) node).getOldSite(), ((BreakpointNode) node).getNewSite(), site.y);
            if (site.x < intersection.x) {
                node = node.left;
            } else {
                node = node.right;
            }
        }
        return (ArcNode) node;
    }

    public static Point intersect(Point focus1, Point focus2, double directrix) {
        if (focus1.y == directrix || focus2.y == directrix) {
            Point higher = focus1.y > focus2.y ? focus1 : focus2;
            Point lower = focus1.y > focus2.y ? focus2 : focus1;
            return projection(lower, higher);
        }

        double distance1 = 2 * (focus1.y - directrix);
        double distance2 = 2 * (focus2.y - directrix);
        double b = 2 * (focus2.x / distance2 - focus1.x / distance1);
        double c = (focus1.magnitudeSquared - Math.pow(directrix, 2)) / distance1 -
                (focus2.magnitudeSquared - Math.pow(directrix, 2)) / distance2;

        if (focus1.y == focus2.y) {
            double x = -(c / b);
            double y = (1.0 / distance1) * (Math.pow(x, 2) - 2 * focus1.x * x +
                    focus1.magnitudeSquared - Math.pow(directrix, 2));
            return new Point(x, y);
        }

        double a = (1.0 / distance1) - (1.0 / distance2);

        double[] roots = quadratic(a, b, c);
        double x1 = roots[0];
        double x2 = roots[1];
        double y1 = (1.0 / distance1) * (Math.pow(x1, 2) - 2 * focus1.x * x1 +
                focus1.magnitudeSquared - Math.pow(directrix, 2));
        double y2 = (1.0 / distance1) * (Math.pow(x2, 2) - 2 * focus1.x * x2 +
                focus1.magnitudeSquared - Math.pow(directrix, 2));
        Point left = new Point(x1, y1);
        Point right = new Point(x2, y2);

        return focus1.y < focus2.y ? right : left;
    }

    public static Point projection(Point a, Point focus) {
        double y = (1.0 / (2 * (focus.y - a.y))) * Math.pow(a.x - focus.x, 2) + (a.y + focus.y) / 2.0;
        return new Point(a.x, y);
    }

    public static double[] quadratic(double a, double b, double c) {
        double root1 = (-b - Math.pow(Math.pow(b, 2) - 4 * a * c, 0.5)) / (2 * a);
        double root2 = (-b + Math.pow(Math.pow(b, 2) - 4 * a * c, 0.5)) / (2 * a);
        if (root1 > root2) {
            double temp = root1;
            root1 = root2;
            root2 = temp;
        }
        return new double[]{root1, root2};
    }

    public String toString() {
        return root.toString();
    }
}

class EdgeList {
    public Set<HalfEdge> edges;

    public EdgeList() {
        edges = new HashSet<>();
    }

    public HalfEdge addEdge(Point point) {
        HalfEdge edge = new HalfEdge();
        HalfEdge twin = new HalfEdge();
        edge.setTwin(twin);
        twin.setTwin(edge);

        edge.setPoint(point);
        twin.setPoint(point);

        edges.add(edge);
        edges.add(twin);
        return edge;
    }

    public void removeEdge(HalfEdge edge) {
        edges.remove(edge);
    }

    // Adds a new edge coming from a circle event
    public void circleVector(HalfEdge edge, BreakpointNode breakpoint, double directrix) {
        Point futurePoint = intersect(breakpoint.getOldSite(), breakpoint.getNewSite(), directrix - 0.1);
        edge.setVector(difference(edge.getPoint(), futurePoint));
        edge.setOrigin(edge.getPoint());
        edge.getTwin().setVector(new Vector(-edge.getVector().x, -edge.getVector().y));
    }

    // Adds a new edge coming from a site event
    public void siteVector(HalfEdge edge, Point site1, Point site2) {
        if (site1.x > site2.x) {
            Point temp = site1;
            site1 = site2;
            site2 = temp;
        }

        Vector leftVector, rightVector;
        if (site1.x == site2.x) {
            leftVector = new Vector(-1, 0);
            rightVector = new Vector(1, 0);
        } else if (site1.y == site2.y) {
            leftVector = new Vector(0, -1);
            rightVector = new Vector(0, 1);
        } else {
            double slope = (site2.y - site1.y) / (site2.x - site1.x);
            leftVector = new Vector(-1, 1.0 / slope);
            rightVector = new Vector(1, -1.0 / slope);
        }

        edge.setVector(leftVector);
        edge.getTwin().setVector(rightVector);
    }

    public static Point intersect(Point focus1, Point focus2, double directrix) {
        if (focus1.y == directrix || focus2.y == directrix) {
            Point higher = focus1.y > focus2.y ? focus1 : focus2;
            Point lower = focus1.y > focus2.y ? focus2 : focus1;
            return projection(lower, higher);
        }

        double distance1 = 2 * (focus1.y - directrix);
        double distance2 = 2 * (focus2.y - directrix);
        double b = 2 * (focus2.x / distance2 - focus1.x / distance1);
        double c = (focus1.magnitudeSquared - Math.pow(directrix, 2)) / distance1 -
                (focus2.magnitudeSquared - Math.pow(directrix, 2)) / distance2;

        if (focus1.y == focus2.y) {
            double x = -(c / b);
            double y = (1.0 / distance1) * (Math.pow(x, 2) - 2 * focus1.x * x +
                    focus1.magnitudeSquared - Math.pow(directrix, 2));
            return new Point(x, y);
        }

        double a = (1.0 / distance1) - (1.0 / distance2);

        double[] roots = quadratic(a, b, c);
        double x1 = roots[0];
        double x2 = roots[1];
        double y1 = (1.0 / distance1) * (Math.pow(x1, 2) - 2 * focus1.x * x1 +
                focus1.magnitudeSquared - Math.pow(directrix, 2));
        double y2 = (1.0 / distance1) * (Math.pow(x2, 2) - 2 * focus1.x * x2 +
                focus1.magnitudeSquared - Math.pow(directrix, 2));
        Point left = new Point(x1, y1);
        Point right = new Point(x2, y2);

        return focus1.y < focus2.y ? right : left;
    }

    public static Point projection(Point a, Point focus) {
        double y = (1.0 / (2 * (focus.y - a.y))) * Math.pow(a.x - focus.x, 2) + (a.y + focus.y) / 2.0;
        return new Point(a.x, y);
    }

    public static double[] quadratic(double a, double b, double c) {
        double root1 = (-b - Math.pow(Math.pow(b, 2) - 4 * a * c, 0.5)) / (2 * a);
        double root2 = (-b + Math.pow(Math.pow(b, 2) - 4 * a * c, 0.5)) / (2 * a);
        if (root1 > root2) {
            double temp = root1;
            root1 = root2;
            root2 = temp;
        }
        return new double[]{root1, root2};
    }

    public static Vector difference(Point a, Point b) {
        return new Vector(b.x - a.x, b.y - a.y);
    }
}

public class VoronoiDiagram {

    private EventHeap eventHeap;
    private BeachlineTree beachlineTree;
    private EdgeList edgeList;
    private double left, right, bottom, top;

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        int n = Integer.parseInt(sc.nextLine());
        int f = sc.nextInt();
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
        System.out.println(voronoiDiagram);
    }

    public Map<Point, List<Point>> createDiagram(List<Point> points, double left, double right, double bottom, double top) {
        if (points.size() < 2) return null;

        // Initialize the data structures and the bounds
        this.eventHeap = new EventHeap();
        this.beachlineTree = new BeachlineTree();
        this.edgeList = new EdgeList();
        this.left = left;
        this.right = right;
        this.bottom = bottom;
        this.top = top;

        for (Point point : points) {
            eventHeap.heapInsert(new Event("site", point));
        }

        // Special case if the first two sites share the same y-coordinate
        Event[] twoEvents = eventHeap.heapDoublePeek();
        Event first = twoEvents[0];
        Event second = twoEvents[1];
        if (first.key.equals(second.key)) specialStartCase();

        // While event heap is not empty, pop off the event with the maximum y-coordinate and handle accordingly
        while (!eventHeap.empty()) {
            Event event = eventHeap.heapMax();
            if (event.category.equals("site")) {
                handleSiteEvent(event);
            } else {
                handleCircleEvent(event);
            }
        }
        // Construct the Voronoi diagram based off of the bounds and internal edges
        Map<Point, Set<Point>> edges = constructEdgeDict();
        List<List<Point>> polygons = constructPolygons(edges);
        Map<Point, List<Point>> voronoiDiagram = assignPolygons(points, polygons);
        return voronoiDiagram;
    }

    // Handle the site event
    public void handleSiteEvent(Event event) {
        if (beachlineTree.root == null) {
            beachlineTree.insertRoot(new ArcNode(event.site, null, null, null));
            return;
        }

        // Site event overrides potential circle event coming from the arc that the site event is inserted into
        ArcNode oldNode = beachlineTree.findArc(event.site);
        removeCircleEvent(oldNode);

        Point oldSite = oldNode.site;
        Point newSite = event.site;

        // Restructure the beachline tree in the following way:
        //  A    ->    [A,B]
        //             /   \
        //            A    [B,A]
        //                 /   \
        //                B     A
        BreakpointNode leftBreakpoint = new BreakpointNode(oldSite, newSite);
        BreakpointNode rightBreakpoint = new BreakpointNode(newSite, oldSite);
        ArcNode oldArcLeft = new ArcNode(oldSite);
        ArcNode newArc = new ArcNode(newSite);
        ArcNode oldArcRight = new ArcNode(oldSite);
        beachlineTree.insertRight(oldNode, leftBreakpoint);
        beachlineTree.insertRight(leftBreakpoint, rightBreakpoint);
        beachlineTree.insertLeft(leftBreakpoint, oldArcLeft);
        beachlineTree.insertLeft(rightBreakpoint, newArc);
        beachlineTree.insertRight(rightBreakpoint, oldArcRight);
        beachlineTree.replaceParent(leftBreakpoint);

        // Insert a new edge corresponding to the breakpoint between the two sites
        Point point = projection(newSite, oldSite);
        leftBreakpoint.edge = edgeList.addEdge(point);
        rightBreakpoint.edge = leftBreakpoint.edge.twin;
        edgeList.siteVector(leftBreakpoint.edge, oldSite, newSite);

        // Check for new circle events from neighboring arcs
        beachlineTree.predecessorLeaf(oldArcLeft);
        checkCircleEvent(beachlineTree.predecessorLeaf(oldArcLeft), oldArcLeft, newArc);
        checkCircleEvent(newArc, oldArcRight, beachlineTree.successorLeaf(oldArcRight));
    }

    public void handleCircleEvent(Event event) {
        // These are initialized to improve readability
        ArcNode predLeaf = beachlineTree.predecessorLeaf(event.leaf);
        ArcNode succLeaf = beachlineTree.successorLeaf(event.leaf);
        BreakpointNode predNode = (BreakpointNode) beachlineTree.predecessorNode(event.leaf);
        BreakpointNode succNode = (BreakpointNode) beachlineTree.successorNode(event.leaf);
        Point leftSite = predLeaf.site;
        Point centerSite = event.leaf.site;
        Point rightSite = succLeaf.site;
        Point circleCenter = circumcenter(leftSite, centerSite, rightSite);

        // Remove the arc corresponding to the circle event
        beachlineTree.remove(event.leaf);
        predNode.edge.getTwin().setOrigin(circleCenter);
        succNode.edge.getTwin().setOrigin(circleCenter);

        // Restructure the beachline tree
        boolean succIsParent = (succNode == event.leaf.parent);
        BreakpointNode removedBreakpoint = succIsParent ? succNode : predNode;
        BreakpointNode remainingBreakpoint = succIsParent ? predNode : succNode;
        Node remainingChild = succIsParent ? succNode.right : predNode.left;
        beachlineTree.replaceParent(remainingChild);
        remainingBreakpoint.oldSite = leftSite;
        remainingBreakpoint.newSite = rightSite;

        // Create a new half edge coming out of the circle event
        HalfEdge halfEdge = edgeList.addEdge(circleCenter);
        remainingBreakpoint.edge = halfEdge;
        Point bottom = circlebottom(leftSite, centerSite, rightSite);
        edgeList.circleVector(halfEdge, remainingBreakpoint, bottom.y);

        // Remove the currently scheduled circle events for the neighboring arcs
        removeCircleEvent(predLeaf);
        removeCircleEvent(succLeaf);

        // Check for new circle events for neighboring arcs
        checkCircleEvent(beachlineTree.predecessorLeaf(predLeaf), predLeaf, succLeaf);
        checkCircleEvent(predLeaf, succLeaf, beachlineTree.successorLeaf(succLeaf));
    }

    public void specialStartCase() {
        Event first = eventHeap.heapMax();
        Event second = eventHeap.heapMax();
        BreakpointNode root = (BreakpointNode) beachlineTree.insertRoot(new BreakpointNode(first.site, second.site));
        beachlineTree.insertLeft(root, new ArcNode(first.site));
        beachlineTree.insertRight(root, new ArcNode(second.site));

        Point p = new Point((first.site.x + second.site.x) / 2.0, 999999999);
        root.edge = edgeList.addEdge(p);
        edgeList.siteVector(root.edge, first.site, second.site);
    }

    public void removeCircleEvent(ArcNode leaf) {
        if (leaf.circleEvent != null) {
            eventHeap.heapRemove(leaf.circleEvent.index);
            leaf.circleEvent = null;
        }
    }

    public void checkCircleEvent(ArcNode left, ArcNode center, ArcNode right) {
        if (left == null || right == null) return;
        HalfEdge e1 = ((BreakpointNode) beachlineTree.predecessorNode(center)).edge;
        HalfEdge e2 = ((BreakpointNode) beachlineTree.successorNode(center)).edge;
        if (converge(e1.getPoint(), e1.getVector(), e2.getPoint(), e2.getVector())) {
            double bottom = circlebottom(left.site, center.site, right.site).y;
            Event event = new Event("circle", center, bottom);
            eventHeap.heapInsert(event);
            center.circleEvent = event;
        }
    }

    public Set<Edge> pruneEdges() {
        Set<Edge> internalEdges = new HashSet<>();
        for (HalfEdge edge : edgeList.edges) {
            HalfEdge twin = edge.twin;
            if (edge.origin == null && twin.origin == null) {
                if (outside(edge.point)) {
                    HalfEdge inward = extend(twin.point, twin.vector) == null ? edge : twin;
                    inward.origin = shorten(inward.point, inward.vector);
                    inward.twin.origin = extend(inward.point, inward.vector);
                } else {
                    edge.origin = shorten(edge.point, edge.vector);
                    twin.origin = shorten(twin.point, twin.vector);
                }
            } else {
                if (outwards(edge.origin, edge.vector) || outwards(twin.origin, twin.vector)) {
                    continue;
                } else if (edge.origin != null && twin.origin != null) {
                    if (outside(edge.origin)) {
                        edge.origin = shorten(edge.origin, edge.vector);
                    }
                    if (outside(twin.origin)) {
                        twin.origin = shorten(twin.origin, twin.vector);
                    }
                } else {
                    HalfEdge existing = edge.origin != null ? edge : twin;
                    existing.twin.origin = extend(existing.origin, existing.vector);
                    if (outside(existing.origin)) {
                        existing.origin = shorten(existing.origin, existing.vector);
                    }
                }
            }
            if (!internalEdges.contains(new Edge(twin.origin, edge.origin))) {
                internalEdges.add(new Edge(edge.origin, twin.origin));
            }
        }
        return internalEdges;
    }

    public List<Point> boundaryPoints(Set<Edge> internalEdges) {
        List<Point> leftPoints = new ArrayList<>();
        List<Point> rightPoints = new ArrayList<>();
        List<Point> bottomPoints = new ArrayList<>();
        List<Point> topPoints = new ArrayList<>();

        for (Edge edge : internalEdges) {
            if (!corner(edge.start)) {
                if (edge.start.x == left) {
                    leftPoints.add(edge.start);
                } else if (edge.start.x == right) {
                    rightPoints.add(edge.start);
                } else if (edge.start.y == bottom) {
                    bottomPoints.add(edge.start);
                } else if (edge.start.y == top) {
                    topPoints.add(edge.start);
                }
            }
            if (!corner(edge.end)) {
                if (edge.end.x == left) {
                    leftPoints.add(edge.end);
                } else if (edge.end.x == right) {
                    rightPoints.add(edge.end);
                } else if (edge.end.y == bottom) {
                    bottomPoints.add(edge.end);
                } else if (edge.end.y == top) {
                    topPoints.add(edge.end);
                }
            }
        }
        leftPoints.sort(new SortPoints());
        rightPoints.sort(new SortPoints());
        bottomPoints.sort(new SortPoints());
        topPoints.sort(new SortPoints());
        Collections.reverse(leftPoints);
        Collections.reverse(topPoints);

        List<Point> result = new ArrayList<>();
        result.add(new Point(left, bottom));
        result.addAll(bottomPoints);
        result.add(new Point(right, bottom));
        result.addAll(rightPoints);
        result.add(new Point(right, top));
        result.addAll(topPoints);
        result.add(new Point(left, top));
        result.addAll(leftPoints);
        return result;
    }

    public Map<Point, Set<Point>> constructEdgeDict() {
        Set<Edge> internalEdges = pruneEdges();
        List<Point> boundaryPoints = boundaryPoints(internalEdges);
        Map<Point, Set<Point>> edges = new HashMap<>();

        for (Edge edge : internalEdges) {
            if (!edges.containsKey(edge.start)) {
                edges.put(edge.start, new HashSet<>());
            }
            if (!edges.containsKey(edge.end)) {
                edges.put(edge.end, new HashSet<>());
            }
            edges.get(edge.start).add(edge.end);
            edges.get(edge.end).add(edge.start);
        }

        for (int i = 0; i < boundaryPoints.size() - 1; i++) {
            if (!edges.containsKey(boundaryPoints.get(i))) {
                edges.put(boundaryPoints.get(i), new HashSet<>());
            }
            edges.get(boundaryPoints.get(i)).add(boundaryPoints.get(i + 1));
        }
        if (!edges.containsKey(boundaryPoints.get(boundaryPoints.size() - 1))) {
            edges.put(boundaryPoints.get(boundaryPoints.size() - 1), new HashSet<>());
        }
        edges.get(boundaryPoints.get(boundaryPoints.size() - 1)).add(boundaryPoints.get(0));
        return edges;
    }

    public List<List<Point>> constructPolygons(Map<Point, Set<Point>> edges) {
        List<List<Point>> polygons = new ArrayList<>();
        while (edges.keySet().size() > 0) {
            List<Point> polygon = new ArrayList<>();
            Point startKey = (Point) edges.keySet().toArray()[0];
            polygon.add(startKey);
            polygon.add((Point) edges.get(startKey).toArray()[0]);
            edges.get(startKey).remove(polygon.get(polygon.size() - 1));
            if (edges.get(startKey).size() == 0) {
                edges.remove(startKey);
            }

            while (true) {
                int n = polygon.size();
                Point point = farthestLeftTurn(polygon.get(n - 2), polygon.get(n - 1), edges.get(polygon.get(n - 1)));
                edges.get(polygon.get(n - 1)).remove(point);
                if (edges.get(polygon.get(n - 1)).size() == 0) {
                    edges.remove(polygon.get(n - 1));
                }
                if (point.equals(startKey)) break;
                polygon.add(point);
            }
            polygons.add(polygon);
        }
        return polygons;
    }

    public Map<Point, List<Point>> assignPolygons(List<Point> points, List<List<Point>> polygons) {
        Map<Point, List<Point>> voronoiDiagram = new HashMap<>();
        for (List<Point> polygon : polygons) {
            for (Point point : points) {
                if (pointInPolygon(point, polygon)) {
                    voronoiDiagram.put(point, polygon);
                    break;
                }
            }
        }
        return voronoiDiagram;
    }

    public Point projection(Point a, Point focus) {
        double y = (1.0 / (2 * (focus.y - a.y))) * Math.pow(a.x - focus.x, 2) + (a.y + focus.y) / 2.0;
        return new Point(a.x, y);
    }

    public Point circumcenter(Point a, Point b, Point c) {
        double d = 2 * (a.x * (b.y - c.y) + b.x * (c.y - a.y) + c.x * (a.y - b.y));
        double x = (1.0 / d) * (a.magnitudeSquared * (b.y - c.y) + b.magnitudeSquared * (c.y - a.y) + c.magnitudeSquared * (a.y - b.y));
        double y = (1.0 / d) * (a.magnitudeSquared * (c.x - b.x) + b.magnitudeSquared * (a.x - c.x) + c.magnitudeSquared * (b.x - a.x));
        return new Point(x, y);
    }

    public Point circlebottom(Point a, Point b, Point c) {
        Point point = circumcenter(a, b, c);
        double radius = Math.pow(Math.pow(a.x - point.x, 2) + Math.pow(a.y - point.y, 2), 0.5);
        return new Point(point.x, point.y - radius);
    }

    public boolean converge(Point a, Vector v1, Point b, Vector v2) {
        double s = (v1.x * a.y - v1.x * b.y + v1.y * b.x - v1.y * a.x) / (v1.x * v2.y - v1.y * v2.x);
        double t;
        if (v1.x != 0) {
            t = (b.x - a.x + s * v2.x) / v1.x;
        } else {
            t = (b.y - a.y + s * v2.y) / v1.y;
        }
        return (s > 0) && (t > 0);
    }

    public Vector difference(Point a, Point b) {
        return new Vector(b.x - a.x, b.y - a.y);
    }

    public boolean outside(Point point) {
        return point.x < left || point.x > right || point.y < bottom || point.y > top;
    }

    public boolean outwards(Point point, Vector vector) {
        return point != null && extend(point, vector) == null;
    }

    public List<Point> boundIntersection(Point point, Vector vector) {
        List<Point> possiblePoints = new ArrayList<>();

        if (vector.x == 0) {
            possiblePoints.add(new Point(point.x, bottom));
            possiblePoints.add(new Point(point.x, top));
        } else if (vector.y == 0) {
            possiblePoints.add(new Point(left, point.y));
            possiblePoints.add(new Point(right, point.y));
        } else {
            double slope = (double) vector.y / vector.x;
            double leftY = slope * (left - point.x) + point.y;
            double rightY = slope * (right - point.x) + point.y;
            double bottomX = (1 / slope) * (bottom - point.y) + point.x;
            double topX = (1 / slope) * (top - point.y) + point.x;

            if (leftY >= bottom && leftY <= top) {
                possiblePoints.add(new Point(left, leftY));
            }
            if (rightY >= bottom && rightY <= top) {
                possiblePoints.add(new Point(right, rightY));
            }
            if (bottomX > left && bottomX < right) {
                possiblePoints.add(new Point(bottomX, bottom));
            }
            if (topX > left && topX < right) {
                possiblePoints.add(new Point(topX, top));
            }
        }
        return possiblePoints;
    }

    public double getTime(Vector displacement, Vector vector) {
        if (vector.x != 0) {
            return displacement.x / vector.x;
        } else {
            return displacement.y / vector.y;
        }
    }

    public Point extend(Point point, Vector vector) {
        List<Point> possiblePoints = boundIntersection(point, vector);
        if (possiblePoints.isEmpty()) return null;

        double t = getTime(difference(point, possiblePoints.get(0)), vector);
        double s = getTime(difference(point, possiblePoints.get(1)), vector);
        if (t >= 0 && s >= 0) {
            return t >= s ? possiblePoints.get(0) : possiblePoints.get(1);
        } else if (t >= 0) {
            return possiblePoints.get(0);
        } else if (s >= 0) {
            return possiblePoints.get(1);
        } else {
            return null;
        }
    }

    public Point shorten(Point point, Vector vector) {
        List<Point> possiblePoints = boundIntersection(point, vector);
        if (possiblePoints.isEmpty()) return null;

        double t = getTime(difference(point, possiblePoints.get(0)), vector);
        double s = getTime(difference(point, possiblePoints.get(1)), vector);
        if (t >= 0 && s >= 0) {
            return t <= s ? possiblePoints.get(0) : possiblePoints.get(1);
        } else if (t >= 0) {
            return possiblePoints.get(0);
        } else if (s >= 0) {
            return possiblePoints.get(1);
        } else {
            return null;
        }
    }

    // Check if point is a corner of the bounds
    public boolean corner(Point point) {
        boolean bottomLeft = (point.x == left && point.y == bottom);
        boolean topLeft = (point.x == left && point.y == top);
        boolean bottomRight = (point.x == right && point.y == bottom);
        boolean topRight = (point.x == right && point.y == top);
        return bottomLeft || topLeft || bottomRight || topRight;
    }

    public double crossProduct(Point a, Point b, Point c) {
        return (b.x - a.x) * (c.y - a.y) - (c.x - a.x) * (b.y - a.y);
    }

    // Calculates which point requires the farthest left turn, used to build the polygons
    public Point farthestLeftTurn(Point a, Point b, Set<Point> points) {
        Point leftPoint = null;
        double leftValue = -1;
        for (Point p : points) {
            double c = crossProduct(a, b, p);
            if (c > leftValue) {
                leftPoint = p;
                leftValue = c;
            }
        }
        return leftPoint;
    }

    public int binarySearchTriangle(Point point, List<Point> polygon) {
        Point center = polygon.get(0);
        int left = 1;
        int right = polygon.size() - 1;
        while (left <= right) {
            int mid = (left + right) / 2;
            double direction = crossProduct(center, polygon.get(mid), point);
            if (direction >= 0) {
                left = mid + 1;
            } else if (left == mid) {
                return mid;
            } else {
                right = mid;
            }
        }
        return -1;
    }

    public boolean pointOnLine(Point point, Point a, Point b) {
        if ((point.x == a.x && point.y == a.y) || (point.x == b.x && point.y == b.y)) return true;
        if (crossProduct(a, b, point) != 0) return false;
        return point.y > Math.min(a.y, b.y) && point.y < Math.max(a.y, b.y);
    }

    // Triangle speedup to quickly check most cases
    public boolean pointInTriangle(Point point, List<Point> polygon) {
        int firstIndex = 0;
        int secondIndex = polygon.size() / 3;
        int thirdIndex = polygon.size() * 2 / 3;
        boolean firstReq = crossProduct(polygon.get(firstIndex), polygon.get(secondIndex), point) >= 0;
        boolean secondReq = crossProduct(polygon.get(secondIndex), polygon.get(thirdIndex), point) >= 0;
        boolean thirdReq = crossProduct(polygon.get(thirdIndex), polygon.get(firstIndex), point) >= 0;
        return firstReq && secondReq && thirdReq;
    }

    // Check if point is in polygon (used to assign polygons)
    public boolean pointInPolygon(Point point, List<Point> polygon) {
        if (pointOnLine(point, polygon.get(0), polygon.get(polygon.size() - 1))) return true;
        if (pointInTriangle(point, polygon)) return true;
        int triangleIndex = binarySearchTriangle(point, polygon);
        if (triangleIndex < 0) return false;
        return crossProduct(polygon.get(triangleIndex), polygon.get(triangleIndex - 1), point) <= 0;
    }
}