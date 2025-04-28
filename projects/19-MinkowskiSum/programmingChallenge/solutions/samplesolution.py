import math

#used for floating point safety
EPS = 1e-8


def read_polygon():
    '''
    This function handles reading the input lines of a single polygon. 
        First line of polygon input is P, number of points
        next P lines contain x, y coordinate of a point in counter clockwise order
    '''
    P = int(input())
    points = []
    for _ in range(P):
        x, y = map(float, input().split())
        points.append((x, y))
    return points

def translate_polygon(polygon, dx, dy):
    '''
    This function applies a translation to polygon and returns the result
    '''
    return [(x+dx, y+dy) for x,y in polygon]

def create_path_volume(robot, theta, distance):
    '''
    This function generates one larger polygon that represents the area the robot will pass through
        First based on the distance and angle, the final expected position of the robot can be calculated
        Then find the convex hull of initial robot and final robot to get the path the robot will take
    '''
    #calculate translation vector and apply to find final robot
    angle_rad = math.radians(theta)
    dx = distance * math.cos(angle_rad)
    dy = distance * math.sin(angle_rad)
    initial_robot = robot
    final_robot = translate_polygon(robot, dx, dy)

    #solve for the convex hull of the initial and final robots together
    all_points = initial_robot + final_robot
    path_volume = convex_hull(all_points)
    return path_volume

def cross(o, a, b):
    '''
    This function solves the cross product of oa and ob
    '''
    return (a[0]-o[0])*(b[1]-o[1]) - (a[1]-o[1])*(b[0]-o[0])

def convex_hull(points):
    '''
    This function uses the Graham's Scan implementation to solve for the convex hull
    '''
    #remove duplicate points
    points = list(set(points))
    if len(points) <= 1:
        return points
    
    #find bottom most pivot point and then sort all points by polar angle to pivot
    pivot = min(points, key=lambda p: (p[1], p[0]))
    points.sort(key=lambda p: (
        0 if p == pivot else math.atan2(p[1]-pivot[1], p[0]-pivot[0]),
        (p[0]-pivot[0])**2 + (p[1]-pivot[1])**2
    ))
    
    #build the convex hull
    hull = []
    for p in points:
        while len(hull) >= 2 and cross(hull[-2], hull[-1], p) <= EPS:
            hull.pop()
        hull.append(p)
    
    return hull

# method to reorder the polygon with the lowest y-coord first
def reorder_polygon(P):
    pos = 0
    # iterate through the points, storing the position of the lowest y coord point (taking the smaller x point if equal)
    for i in range(1, len(P)):
        if (P[i][1] < P[pos][1]) or (P[i][1] == P[pos][1] and P[i][0] < P[pos][0]):
            pos = i
    # rotate the vector until the position of the lowest y coord point is at the 0th index
    P[:] = P[pos:] + P[:pos]

# calculate the minkowski difference of two polygons
# assumes that P and Q (the two polygons) are ordered counter-clockwise
def minkowski_difference(P, Q):
    # Reflect Q across the origin
    Q_reflected = [(-q[0], -q[1]) for q in Q]

    # the first vertex must be the lowest for both polygons
    # this results in the polygons being sorted by polar angle
    reorder_polygon(P)
    reorder_polygon(Q_reflected)

    P_ext = P + [P[0], P[1]]
    Q_ext = Q_reflected + [Q_reflected[0], Q_reflected[1]]

    result = []
    i = j = 0
    # loop until we iterate through all the points of both polygons
    while i < len(P_ext) - 2 or j < len(Q_ext) - 2:
        # add the sum of the two points we are at
        result.append((P_ext[i][0] + Q_ext[j][0], P_ext[i][1] + Q_ext[j][1]))
        # compare the polar angles of the two edges
        cross_val = (P_ext[i + 1][0] - P_ext[i][0]) * (Q_ext[j + 1][1] - Q_ext[j][1]) - \
                    (P_ext[i + 1][1] - P_ext[i][1]) * (Q_ext[j + 1][0] - Q_ext[j][0])
        # increment both points of the polar angles are equal (cross product == 0)
        # if the cross product is > 0, the polar angle of P is less -> increment P
        if cross_val >= 0 and i < len(P_ext) - 2:
            i += 1
        # otherwise the polar angle of Q is less -> increment Q
        if cross_val <= 0 and j < len(Q_ext) - 2:
            j += 1

    return result

def point_on_segment(p, a, b):
    '''Checks if point p is on segment ab'''
    #check if point p is colinear
    cross_val = cross(a, b, p)
    if abs(cross_val) > EPS:
        return False
    #check if point p is actually on the segment
    min_x = min(a[0], b[0])
    max_x = max(a[0], b[0])
    min_y = min(a[1], b[1])
    max_y = max(a[1], b[1])
    return (p[0] >= min_x - EPS and p[0] <= max_x + EPS and
            p[1] >= min_y - EPS and p[1] <= max_y + EPS)

def point_in_polygon(point, polygon):
    '''Checks if point is inside polygon'''
    #check if a point is contained in a polygon using turns method
    n = len(polygon)
    #going counter clockwise, if each point makes a left turn with previous points, convex.
    for i in range(n):
        a = polygon[i]
        b = polygon[(i+1)%n]
        cross_val = cross(a, b, point)
        if cross_val < -EPS:  #point is outside (right turn goes outside convex polygon)
            return False
        elif cross_val <= EPS:  #point is on edge or colinear
            if point_on_segment(point, a, b):
                return True
    return True

def solve():
    #input reading
    O = int(input())
    obstacles = []
    for _ in range(O):
        obstacles.append(read_polygon())
    robot = read_polygon()
    theta = float(input())
    distance = float(input())
    
    #solve for the path that the robot intends to take
    path_volume = create_path_volume(robot, theta, distance)
    
    #if Minkowski diffence of two polygons contains the origin, the two polygons are intersecintg
    #loop to find all obstacles that collide with the robot's path
    colliding_obstacles = []
    for obstacle in obstacles:
        minkowski_diff = minkowski_difference(obstacle, path_volume)
        if point_in_polygon((0,0), minkowski_diff):
            colliding_obstacles.append(obstacle)
    
    #printing output
    if not colliding_obstacles:
        print(0)
    else:
        for obs in colliding_obstacles:
            for point in obs:
                print(f"({point[0]}, {point[1]})", end=' ')
            print()

if __name__ == "__main__":
    solve()