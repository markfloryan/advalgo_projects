#include <iostream>
#include <vector>
#include <algorithm>
#include <cmath>
#include <set>
#include <iomanip>

#define _USE_MATH_DEFINES
#include <cmath>
#ifndef M_PI
#define M_PI 3.14159265358979323846
#endif

using namespace std;
using namespace std;
//used for floating point precision
const double EPS = 1e-8;

struct Point {
    double x, y;
    //2d point representaiton with x and y coordinates
    Point(double x = 0, double y = 0) : x(x), y(y) {}
    
    bool operator==(const Point& other) const {
        return fabs(x - other.x) < EPS && fabs(y - other.y) < EPS;
    }
    
    bool operator<(const Point& other) const {
        if (fabs(x - other.x) < EPS)
            return y < other.y - EPS;
        return x < other.x - EPS;
    }
};

/*
This function handles reading the input lines of a single polygon. 
    First line of polygon input is P, number of points
    next P lines contain x, y coordinate of a point in counter clockwise order
*/
vector<Point> readPolygon() {
    int P;
    cin >> P;
    vector<Point> points(P);
    for (int i = 0; i < P; ++i) {
        cin >> points[i].x >> points[i].y;
    }
    return points;
}

/*
This function applies a translation to polygon and returns the result
*/
vector<Point> translatePolygon(const vector<Point>& polygon, double dx, double dy) {
    vector<Point> translated;
    for (const auto& p : polygon) {
        translated.emplace_back(p.x + dx, p.y + dy);
    }
    return translated;
}

/*
This function solves the cross product of oa and ob
*/
double cross(const Point& o, const Point& a, const Point& b) {
    return (a.x - o.x) * (b.y - o.y) - (a.y - o.y) * (b.x - o.x);
}

/*
This function uses the Graham's Scan implementation to solve for the convex hull
*/
vector<Point> convexHull(vector<Point> points) {
    //remove duplicates
    sort(points.begin(), points.end());
    points.erase(unique(points.begin(), points.end()), points.end());
    
    //find pivot point
    auto pivot_it = min_element(points.begin(), points.end(), [](const Point& a, const Point& b) {
        if (fabs(a.y - b.y) < EPS)
            return a.x < b.x;
        return a.y < b.y;
    });
    Point pivot = *pivot_it;
    points.erase(pivot_it);
    
    //sort by polar angle based on pivot
    sort(points.begin(), points.end(), [pivot](const Point& a, const Point& b) {
        double angleA = atan2(a.y - pivot.y, a.x - pivot.x);
        double angleB = atan2(b.y - pivot.y, b.x - pivot.x);
        
        if (fabs(angleA - angleB) < EPS) {
            double distA = pow(a.x - pivot.x, 2) + pow(a.y - pivot.y, 2);
            double distB = pow(b.x - pivot.x, 2) + pow(b.y - pivot.y, 2);
            return distA < distB;
        }
        return angleA < angleB;
    });
    
    //build convex hull
    vector<Point> hull;
    hull.push_back(pivot);
    for (const auto& p : points) {
        while (hull.size() >= 2) {
            const Point& a = hull[hull.size() - 2];
            const Point& b = hull.back();
            if (cross(a, b, p) <= EPS) {
                hull.pop_back();
            } else {
                break;
            }
        }
        hull.push_back(p);
    }
    
    return hull;
}

/*
This function generates one larger polygon that represents the area the robot will pass through
    First based on the distance and angle, the final expected position of the robot can be calculated
    Then find the convex hull of initial robot and final robot to get the path the robot will take
*/
vector<Point> createPathVolume(const vector<Point>& robot, double theta, double distance) {
    //calculate translation vector and apply to find final robot
    double angleRad = theta * M_PI / 180.0;
    double dx = distance * cos(angleRad);
    double dy = distance * sin(angleRad);
    
    vector<Point> initialRobot = robot;
    vector<Point> finalRobot = translatePolygon(robot, dx, dy);

    //solve for the convex hull of the initial and final robots together
    vector<Point> allPoints = initialRobot;
    allPoints.insert(allPoints.end(), finalRobot.begin(), finalRobot.end());
    
    return convexHull(allPoints);
}

bool pointOnSegment(const Point& p, const Point& a, const Point& b) {
    //check if point p is colinear
    double crossVal = cross(a, b, p);
    if (fabs(crossVal) > EPS) {
        return false;
    }
    //check if point p is actually on the segment
    double minX = min(a.x, b.x);
    double maxX = max(a.x, b.x);
    double minY = min(a.y, b.y);
    double maxY = max(a.y, b.y);
    
    return (p.x >= minX - EPS && p.x <= maxX + EPS &&
            p.y >= minY - EPS && p.y <= maxY + EPS);
}

//check if a point is contained in a polygon using turns method
bool pointInPolygon(const Point& point, const vector<Point>& polygon) {
    //going counter clockwise, if each point makes a left turn with previous points, convex.
    int n = polygon.size();
    for (int i = 0; i < n; ++i) {
        const Point& a = polygon[i];
        const Point& b = polygon[(i + 1) % n];
        double crossVal = cross(a, b, point);
        if (crossVal < -EPS) { //point is outside (right turn goes outside convex polygon)
            return false;
        } else if (fabs(crossVal) < EPS) {//point is on edge or colinear
            if (pointOnSegment(point, a, b)) {
                return true;
            }
        }
    }
    return true;
}

vector<Point> minkowskiDifference(const vector<Point>& A, const vector<Point>& B) {
    //compute the Minkowski difference A - B
    //traditional Minkowski sum adds the points from B to each point in A
    //Minkowski difference uses the coordinates of B reflected across the origin
    vector<Point> diff;
    for (const auto& a : A) {
        for (const auto& b : B) {
            //for all point combinations, add coordinates together
            diff.emplace_back(a.x - b.x, a.y - b.y);
        }
    }
    return convexHull(diff);
}

int main() {
    //input reading
    ios_base::sync_with_stdio(false);
    cin.tie(nullptr);
    cout << fixed << setprecision(6);
    
    int O;
    cin >> O;
    vector<vector<Point>> obstacles(O);
    for (int i = 0; i < O; ++i) {
        obstacles[i] = readPolygon();
    }
    
    vector<Point> robot = readPolygon();
    double theta, distance;
    cin >> theta >> distance;
    
    //solve for the path that the robot intends to take
    vector<Point> pathVolume = createPathVolume(robot, theta, distance);
    
    //if Minkowski diffence of two polygons contains the origin, the two polygons are intersecintg
        //loop to find all obstacles that collide with the robot's path
    vector<vector<Point>> collidingObstacles;
    for (const auto& obstacle : obstacles) {
        vector<Point> minkowskiDiff = minkowskiDifference(obstacle, pathVolume);
        if (pointInPolygon(Point(0, 0), minkowskiDiff)) {
            collidingObstacles.push_back(obstacle);
        }
    }
    
    //printing output
    if (collidingObstacles.empty()) {
        cout << 0 << endl;
    } else {
        for (const auto& obs : collidingObstacles) {
            for (const auto& p : obs) {
                cout << "(" << p.x << ", " << p.y << ") ";
            }
            cout << endl;
        }
    }
    
    return 0;
}