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

def minkowski_difference(A, B):
    #compute the Minkowski difference A - B
    #traditional Minkowski sum adds the points from B to each point in A
    #Minkowski difference uses the coordinates of B reflected across the origin
    diff = []
    for a in A:
        for b in B:
            #for all point combinations, add coordinates together
            diff.append((a[0]-b[0], a[1]-b[1]))
    return convex_hull(diff)

def point_on_segment(p, a, b):
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