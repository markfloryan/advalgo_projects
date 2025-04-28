# data structure to store points
class pt:
    # constructor
    def __init__(self, x, y):
        self.x = x
        self.y = y
    # define addition of 2 points
    def __add__(self, other):
        return pt(self.x + other.x, self.y + other.y)
    # define subtraction of 2 points
    def __sub__(self, other):
        return pt(self.x - other.x, self.y - other.y)
    # calculate cross product of 2 points with (x1*y2 - y1*x2) where 1 indicates the 1st point and 2 indictaes the 2nd.
    def cross(self, other):
        return self.x * other.y - self.y * other.x
    
# method to reorder the polygon with the lowest y-coord first
def reorder_polygon(P):
    pos = 0
    # iterate through the points, storing the position of the lowest y coord point (taking the smaller x point if equal)
    for i in range(1, len(P)):
        if (P[i].y < P[pos].y) or (P[i].y == P[pos].y and P[i].x < P[pos].x):
            pos = i
    # rotate the vector until the position of the lowest y coord point is at the 0th index
    P[:] = P[pos:] + P[:pos]


# calculate the minkowski sum of two polygons
# assumes that P and Q (the two polygons) are ordered counter-clockwise
def minkowski(P, Q):
    # the first vertex must be the lowest for both polygons
    # this results in the polygons being sorted by polar angle
    reorder_polygon(P)
    reorder_polygon(Q)

    # allow cyclic indexing by adding the first two points to the end of the vector 
    # this allows for checking of the last point of the polygon
 
    P.append(P[0])
    P.append(P[1])
    Q.append(Q[0])
    Q.append(Q[1])

    result = []
    i = j = 0
    # loop until we iterate through all the points of both polygons
    while i < len(P) - 2 or j < len(Q) - 2:
        # add the sum of the two points we are at
        result.append(P[i] + Q[j])
        # compare the polar angles of the two edges
        cross = (P[i + 1] - P[i]).cross(Q[j + 1] - Q[j])
        # increment both points of the polar angles are equal (cross product == 0)
        # if the cross product is > 0, the polar angle of P is less -> increment P
        if cross >= 0 and i < len(P) - 2:
            i += 1
        # otherwise the polar angle of Q is less -> increment Q
        if cross <= 0 and j < len(Q) - 2:
            j += 1
    return result


# expect input of 2 polygons, where a polygon is defined by 
# the first line containing a single integer x indicating the number of points in the polygon
# and the next x lines containing 2 space seperated integers, where the first is the x value and the second is the y value
    
polyA = []
polyB = []

# read size of first polygon
aSize = int(input())
# iterate through the points and add to the first polygon vector
for _ in range(aSize):
    x, y = map(int, input().split())
    polyA.append(pt(x, y))

# read size of second polygon
bSize = int(input())
# iterate through the points and add to the second polygon vector
for _ in range(bSize):
    x, y = map(int, input().split())
    polyB.append(pt(x, y))

# get minkowski sum
ans = minkowski(polyA, polyB)

# iterate through points and print out the polygon with each point on a new line
# with 2 space seperated values indicating x then y value
for p in ans:
    print(p.x, p.y)
