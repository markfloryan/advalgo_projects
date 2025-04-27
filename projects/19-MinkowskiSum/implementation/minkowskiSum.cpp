#include <iostream>
#include <vector>
#include <algorithm>
#include <string>
using namespace std;

// data structure to store points
struct pt {
    long long x, y;
    // define addition of 2 points
    pt operator + (const pt & p) const {
        return pt{x + p.x, y + p.y};
    }
    // define subtraction of 2 points
    pt operator - (const pt & p) const {
        return pt{x - p.x, y - p.y};
    }
    // calculate cross product of 2 points with (x1*y2 - y1*x2) where 1 indicates the 1st point and 2 indictaes the 2nd.
    long long cross(const pt & p) const {
        return x * p.y - y * p.x;
    }
};

// method to reorder the polygon with the lowest y-coord first
void reorder_polygon(vector<pt> & P){
    size_t pos = 0;
    // iterate through the points, storing the position of the lowest y coord point (taking the smaller x point if equal)
    for(size_t i = 1; i < P.size(); i++){
        if(P[i].y < P[pos].y || (P[i].y == P[pos].y && P[i].x < P[pos].x))
            pos = i;
    }
    // rotate the vector until the position of the lowest y coord point is at the 0th index
    rotate(P.begin(), P.begin() + pos, P.end());
};

// calculate the minkowski sum of two polygons
// assumes that P and Q (the two polygons) are ordered counter-clockwise
vector<pt> minkowski(vector<pt> P, vector<pt> Q){
    // the first vertex must be the lowest for both polygons
    // this also sorts the polygons by polar angle
    reorder_polygon(P);
    reorder_polygon(Q);
    // allow cyclic indexing by adding the first two points to the end of the vector 
    // this allows for checking of the last point of the polygon
    P.push_back(P[0]);
    P.push_back(P[1]);
    Q.push_back(Q[0]);
    Q.push_back(Q[1]);
    vector<pt> result;
    size_t i = 0, j = 0;
    // loop until we iterate through all the points of both polygons
    while(i < P.size() - 2 || j < Q.size() - 2){
        // add the sum of the two points we are at
        result.push_back(P[i] + Q[j]);
        // compare the polar angles of the two edges
        auto cross = (P[i + 1] - P[i]).cross(Q[j + 1] - Q[j]);
        // increment both points of the polar angles are equal (cross product == 0)
        // if the cross product is > 0, the polar angle of P is less
        if(cross >= 0 && i < P.size() - 2)
            ++i;
        // otherwise the polar angle of Q is less
        if(cross <= 0 && j < Q.size() - 2)
            ++j;

    }
    return result;
};

// main function to calculate minkowski sum
// expect input of 2 polygons, where a polygon is defined by 
// the first line containing a single integer x indicating the number of points in the polygon
// and the next x lines containing 2 space seperated longs, where the first is the x value and the second is the y value
int main() {
    int aSize, bSize;
    long long x, y;
    vector<pt> polyA, polyB;
    // read size of first polygon
    cin >> aSize;
    // iterate through the points and add to the first polygon vector
    for (int i = 0; i < aSize; i++) {
        cin >> x >> y;
        polyA.push_back(pt{x, y});
    }
    // read size of second polygon
    cin >> bSize;
    // iterate through the points and add to the second polygon vector
    for (int i = 0; i < bSize; i++) {
        cin >> x >> y;
        polyB.push_back(pt{x, y});
    }
    // get minkowski sum
    vector<pt> ans = minkowski(polyA, polyB);
    // iterate through points and print out the polygon with each point on a new line
    // with 2 space seperated values indicating x then y value
    for (int i = 0; i < ans.size(); i++){
        cout << ans[i].x << " " << ans[i].y << "\n";
    }
}
