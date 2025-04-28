import math

# ------------------------------------------Object Classes-----------------------------------------

# General Classes

class Point:
    def __init__(self, x, y):
        self.x, self.y = x, y
        self.magnitude_squared = math.pow(x, 2) + math.pow(y, 2)
    
    def __str__(self):
        return f"({self.x}, {self.y})"

class Vector:
    def __init__(self, x, y):
        self.x, self.y = x, y

    def __str__(self):
        return f"<{self.x}, {self.y}>"

# Classes for Beachline Tree

class Arc_node:
    def __init__(self, site, parent=None, circle_event=None, edge=None):
        self.parent = parent
        self.site = site
        self.circle_event = circle_event
        self.edge = edge
        self.left, self.right = None, None
    
    def __str__(self):
        return f"Arc Node: (Site) {str(self.site)}, (Circle Event) {str(self.circle_event)}"

class Breakpoint_node:
    def __init__(self, old_site, new_site, parent=None, edge=None):
        self.parent = parent
        self.old_site = old_site
        self.new_site = new_site
        self.edge = edge
        self.left, self.right = None, None

    def __str__(self):
        result = ''
        if self.left:
            result += str(self.left) + '\n'
        result += f"Breakpoint Node: (Old Site) {str(self.old_site)}, (New Site) {str(self.new_site)}"
        if self.right:
            result += '\n' + str(self.right)
        return result

# Class for Event Heap

class Event:
    def __init__(self, category, site: Point=None, leaf: Arc_node=None, circle_bottom=None):
        self.category = category
        self.index = None
        if self.category == 'site':
            self.site = site
            self.key = site.y
        elif self.category == 'circle':
            self.leaf = leaf
            self.key = circle_bottom
    
    def __str__(self):
        if self.category == 'site':
            return f"Event: (Index) {self.index}, (Category) {self.category}, (Site) {str(self.site)}"
        else:
            return f"Event: (Index) {self.index}, (Category) {self.category}, (Leaf) {self.key}"

# classes for Edge List

class Half_edge:
    def __init__(self):
        self.point = None
        self.origin = None
        self.vector = None
        self.prev, self.next = None, None
        self.twin = None

    def __str__(self):
        return f"Half Edge: (Point) {str(self.point)}, (Vector) {str(self.vector)}"
    
#----------------------------------------Geometry Calculations-------------------------------------
    
# Method to calculate the circumcenter of three points
def circumcenter(a: Point, b: Point, c: Point):
    d = 2 * (a.x * (b.y - c.y) + b.x * (c.y - a.y) + c.x * (a.y - b.y))
    x = (1.0 / d) * (a.magnitude_squared * (b.y - c.y) + b.magnitude_squared * (c.y - a.y) + c.magnitude_squared * (a.y - b.y))
    y = (1.0 / d) * (a.magnitude_squared * (c.x - b.x) + b.magnitude_squared * (a.x - c.x) + c.magnitude_squared * (b.x - a.x))
    return Point(x, y)

# Method to calculate the bottom-most point of a circle
def circlebottom(a: Point, b: Point, c: Point):
    point = circumcenter(a, b, c)
    radius = math.pow(math.pow(a.x - point.x, 2) + math.pow(a.y - point.y, 2), 0.5)
    return Point(point.x, point.y - radius)

# Method to calculate when two edges will converge (circle event)
def converge(a: Point, v1: Vector, b: Point, v2: Vector):
    s = (v1.x * a.y - v1.x * b.y + v1.y * b.x - v1.y * a.x) / (v1.x * v2.y - v1.y * v2.x)
    if v1.x != 0: t = (b.x - a.x + s * v2.x) / v1.x
    else: t = (b.y - a.y + s * v2.y) / v1.y
    return (s > 0) and (t > 0)

# Method to calculate where a new arc will be inserted into the beachline tree
def projection(a: Point, focus: Point):
    y = (1.0 / (2 * (focus.y - a.y))) * math.pow(a.x - focus.x, 2) + (a.y + focus.y) / 2.0
    return Point(a.x, y)

def difference(a: Point, b: Point):
    return Vector(b.x - a.x, b.y - a.y)

def quadratic(a, b, c):
    root1 = (-b - math.pow(math.pow(b, 2) - 4 * a * c, 0.5)) / (2 * a)
    root2 = (-b + math.pow(math.pow(b, 2) - 4 * a * c, 0.5)) / (2 * a)
    if root1 > root2:
        root1, root2 = root2, root1
    return root1, root2

# Method for calculating when a new edge will start
def intersect(focus1: Point, focus2: Point, directrix):
    if focus1.y == directrix or focus2.y == directrix:
        higher = focus1 if focus1.y > focus2.y else focus2
        lower = focus2 if focus1.y > focus2.y else focus1
        return projection(lower, higher)
    
    distance1, distance2 = 2 * (focus1.y - directrix), 2 * (focus2.y - directrix)
    b = 2 * (float(focus2.x) / distance2 - float(focus1.x) / distance1)
    c = float(focus1.magnitude_squared - math.pow(directrix, 2)) / distance1 - float(focus2.magnitude_squared - math.pow(directrix, 2)) / distance2

    if focus1.y == focus2.y:
        x = -(c / b)
        y = (1.0 / distance1) * (math.pow(x, 2) - 2 * focus1.x * x + focus1.magnitude_squared - math.pow(directrix, 2))
        return Point(x, y)

    a = (1.0 / distance1) - (1.0 / distance2)

    x1, x2 = quadratic(a, b, c)
    y1 = (1.0 / distance1) * (math.pow(x1, 2) - 2 * focus1.x * x1 + focus1.magnitude_squared - math.pow(directrix, 2))
    y2 = (1.0 / distance1) * (math.pow(x2, 2) - 2 * focus1.x * x2 + focus1.magnitude_squared - math.pow(directrix, 2))
    left, right = Point(x1, y1), Point(x2, y2)

    if focus1.y < focus2.y:
        return right
    return left

def outside(point: Point, left, right, bottom, top):
    return point.x < left or point.x > right or point.y < bottom or point.y > top

def outwards(point: Point, vector: Vector, left, right, bottom, top):
    return point and not extend(point, vector, left, right, bottom, top)

# Calculate which possible bounds will an edge possibly intersect
def bound_intersection(point: Point, vector: Vector, left, right, bottom, top):
    possible_points = []

    if vector.x == 0:
        return Point(point.x, bottom), Point(point.x, top)
    elif vector.y == 0:
        return Point(left, point.y), Point(right, point.y)
    
    slope = float(vector.y / vector.x)
    left_y = slope * (left - point.x) + point.y
    right_y = slope * (right - point.x) + point.y
    bottom_x = (1 / slope) * (bottom - point.y) + point.x
    top_x = (1 / slope) * (top - point.y) + point.x

    if left_y >= bottom and left_y <= top:
        possible_points.append(Point(left, left_y))
    if right_y >= bottom and right_y <= top:
        possible_points.append(Point(right, right_y))
    if bottom_x > left and bottom_x < right:
        possible_points.append(Point(bottom_x, bottom))
    if top_x > left and top_x < right:
        possible_points.append(Point(top_x, top))
    return possible_points

def get_time(displacement: Vector, vector: Vector):
    if vector.x != 0:
        return displacement.x / vector.x
    else:
        return displacement.y / vector.y

# Extends an edge to meet a bound
def extend(point: Point, vector: Vector, left, right, bottom, top):
    possible_points = bound_intersection(point, vector, left, right, bottom, top)
    if not possible_points: return None

    t = get_time(difference(point, possible_points[0]), vector)
    s = get_time(difference(point, possible_points[1]), vector)
    if (t >= 0) and (s >= 0):
        return possible_points[0] if t >= s else possible_points[1]
    elif t >= 0: return possible_points[0]
    elif s >= 0: return possible_points[1]
    else: return None

# Shortens the edge to be within the bounds
def shorten(point: Point, vector: Vector, left, right, bottom, top):
    possible_points = bound_intersection(point, vector, left, right, bottom, top)
    if not possible_points: return None

    t = get_time(difference(point, possible_points[0]), vector)
    s = get_time(difference(point, possible_points[1]), vector)
    if (t >= 0) and (s >= 0):
        return possible_points[0] if t <= s else possible_points[1]
    elif t >= 0: return possible_points[0]
    elif s >= 0: return possible_points[1]
    else: return None

# Check if point is a corner of the bounds
def corner(point: Point, left, right, bottom, top):
    bottom_left = (point.x == left and point.y == bottom)
    top_left = (point.x == left and point.y == top)
    bottom_right = (point.x == right and point.y == bottom)
    top_right = (point.x == right and point.y == top)
    return bottom_left or top_left or bottom_right or top_right

def cross_product(a: Point, b: Point, c: Point):
    return (b.x - a.x) * (c.y - a.y) - (c.x - a.x) * (b.y - a.y)

# Calculates which point requires the farthest left turn, used to build the polygons
def farthest_left_turn(a: Point, b: Point, points):
    left_point = None
    left_value = -1
    for p in points:
        c = cross_product(a, b, p)
        if c > left_value:
            left_point = p
            left_value = c
    return left_point

def binary_search_triangle(point: Point, polygon):
    center = polygon[0]
    left = 1
    right = len(polygon) - 1
    while left <= right:
        mid = (left + right) // 2
        direction = cross_product(center, polygon[mid], point)
        if direction >= 0:
            left = mid + 1
        elif left == mid:
            return mid
        else:
            right = mid
    return -1

def point_on_line(point: Point, a: Point, b: Point):
    if (point.x == a.x and point.y == a.y) or (point.x == b.x and point.y == b.y): return True
    if cross_product(a, b, point) != 0: return False
    return point.y > min(a.y, b.y) and point.y < max(a.y, b.y)

# Triangle speedup to quickly check most cases
def point_in_triangle(point: Point, polygon):
    first_index = 0
    second_index = len(polygon) // 3
    third_index = len(polygon) * 2 // 3
    first_req = cross_product(polygon[first_index], polygon[second_index], point) >= 0
    second_req = cross_product(polygon[second_index], polygon[third_index], point) >= 0
    third_req = cross_product(polygon[third_index], polygon[first_index], point) >= 0
    return first_req and second_req and third_req

# Check if point is in polygon (used to assign polygons)
def point_in_polygon(point: Point, polygon):
    if point_on_line(point, polygon[0], polygon[-1]): return True
    if point_in_triangle(point, polygon): return True
    triangle_index = binary_search_triangle(point, polygon)
    if triangle_index < 0: return False
    return cross_product(polygon[triangle_index], polygon[triangle_index - 1], point) <= 0

def convert_to_points(tuples):
    points = []
    for x, y in tuples:
        points.append(Point(x, y))
    return points

def convert_to_tuples(diagram):
    new_diagram = {}
    for point in diagram:
        new_diagram[(point.x, point.y)] = []
        for polygon_point in diagram[point]:
            new_diagram[(point.x, point.y)].append((polygon_point.x, polygon_point.y))
    return new_diagram

#-------------------------------------------Event Heap---------------------------------------------

class Event_Heap:
    def __init__(self):
        self.heap = [-1]

    def empty(self):
        return len(self.heap) == 1

    def compare(self, a, b):
        a_event, b_event = self.heap[a], self.heap[b]
        if a_event.key > b_event.key:
            return True
        elif a_event.key < b_event.key:
            return False
        elif a_event.category == 'circle' or b_event.category == 'circle':
            return True
        return a_event.site.x < b_event.site.x
    
    def swap(self, a, b):
        self.heap[a], self.heap[b] = self.heap[b], self.heap[a]
        self.heap[a].index, self.heap[b].index = a, b

    def heap_down(self, index):
        if 2 * index >= len(self.heap): return
        left, right = 2 * index, 2 * index + 1
        if right < len(self.heap):
            if self.compare(left, index) and self.compare(left, right):
                self.swap(index, left)
                self.heap_down(left)
            elif self.compare(right, index):
                self.swap(index, right)
                self.heap_down(right)
        else:
            if self.compare(left, index):
                self.swap(index, left)
                self.heap_down(left)
            
    def heap_up(self, index):
        if index == 1: return
        parent = index // 2
        if self.compare(index, parent):
            self.swap(index, parent)
            self.heap_up(parent)

    def heap_insert(self, event: Event):
        index = len(self.heap)
        self.heap.append(event)
        event.index = index
        self.heap_up(index)

    def heap_remove(self, index):
        self.swap(index, len(self.heap) - 1)
        self.heap.pop(-1)
        if index > 1 and index < len(self.heap) and self.compare(index, index//2):
            self.heap_up(index)
        else:
            self.heap_down(index)

    def heap_max(self):
        e = self.heap[1]
        self.heap_remove(1)
        return e

    def heap_peek(self):
        return self.heap[1]
    
    def heap_double_peek(self):
        first = self.heap[1]
        second = self.heap[2] if len(self.heap) <= 3 or self.compare(2, 3) else self.heap[3]
        return first, second
    
    def __str__(self):
        contents = '\n'.join([str(a) for a in self.heap[1:]])
        return f"Event Heap[{contents}]"
    
#------------------------------------------Beachline Tree------------------------------------------

class Beachline_Tree:
    def __init__(self):
        self.root = None

    def is_left(self, node):
        return node.parent.left == node
    
    def is_right(self, node):
        return node.parent.right == node

    def insert_root(self, root):
        self.root = root
        return self.root

    def insert_left(self, parent, node):
        parent.left = node
        node.parent = parent
        return node
    
    def insert_right(self, parent, node):
        parent.right = node
        node.parent = parent
        return node
    
    def remove(self, node):
        parent = node.parent
        if parent:
            if self.is_left(node):
                parent.left = None
            else:
                parent.right = None

    def replace_parent(self, node):
        if node.parent == self.root:
            self.root = node
            return
        if self.is_left(node.parent):
            node.parent.parent.left = node
            node.parent = node.parent.parent
        else:
            node.parent.parent.right = node
            node.parent = node.parent.parent

    def min_node(self, node):
        while node.left:
            node = node.left
        return node

    def max_node(self, node):
        while node.right:
            node = node.right
        return node
    
    def predecessor_node(self, node):
        if node.left: return self.max_node(node.left)
        child, node = node, node.parent
        while node:
            if self.is_right(child): return node
            child, node = node, node.parent
        return node

    def successor_node(self, node):
        if node.right: return self.min_node(node.right)
        child, node = node, node.parent
        while node:
            if self.is_left(child): return node
            child, node = node, node.parent
        return node

    def predecessor_leaf(self, node):
        if node == self.min_node(self.root): return None
        node = self.predecessor_node(node).left
        while True:
            if node.right:
                node = node.right
            elif node.left:
                node = node.left
            else:
                return node
    
    def successor_leaf(self, node):
        if node == self.max_node(self.root): return None
        node = self.successor_node(node).right
        while True:
            if node.left:
                node = node.left
            elif node.right:
                node = node.right
            else:
                return node
    
    def find_arc(self, site: Point):
        node = self.root
        while type(node) == Breakpoint_node:
            intersection = intersect(node.old_site, node.new_site, site.y)
            if site.x < intersection.x:
                node = node.left
            else:
                node = node.right
        return node
    
    def __str__(self):
        return str(self.root)

#--------------------------------------------Edge List---------------------------------------------

class Edge_Set:
    def __init__(self):
        self.edges = set()

    def add_edge(self, point: Point):
        edge = Half_edge()
        edge.twin = Half_edge()
        edge.twin.twin = edge

        edge.point = point
        edge.twin.point = point

        self.edges.add(edge)
        self.edges.add(edge.twin)
        return edge

    def remove_edge(self, edge: Half_edge):
        self.edges.remove(edge)

    # Adds a new edge coming from a circle event
    def circle_vector(self, edge: Half_edge, breakpoint: Breakpoint_node, directrix):
        future_point = intersect(breakpoint.old_site, breakpoint.new_site, directrix - .1)
        edge.vector = difference(edge.point, future_point)
        edge.origin = edge.point
        edge.twin.vector = Vector(-edge.vector.x, -edge.vector.y)

    # Adds a new edge coming from a site event
    def site_vector(self, edge: Half_edge, site1: Point, site2: Point):
        if site1.x > site2.x:
            site1, site2 = site2, site1
        
        if site1.x == site2.x:
            left_vector = Vector(-1, 0)
            right_vector = Vector(1, 0)
        elif site1.y == site2.y:
            left_vector = Vector(0, -1)
            right_vector = Vector(0, 1)
        else:
            slope = (site2.y - site1.y) / (site2.x - site1.x)
            left_vector = Vector(-1, 1.0/slope)
            right_vector = Vector(1, -1.0/slope)
        
        edge.vector = left_vector
        edge.twin.vector = right_vector

    def __str__(self):
        result = ''
        for edge in self.edges:
            result += f"({edge.point}, {edge.vector})"
        return result

#--------------------------------------------Algorithm---------------------------------------------

class Voronoi_Diagram:
    def __init__(self):
        pass

    def create_diagram(self, points, left, right, bottom, top):
        if len(points) < 2: return

        # Initialize the data structures and the bounds
        self.event_heap = Event_Heap()
        self.beachline_tree = Beachline_Tree()
        self.edge_set = Edge_Set()
        self.left, self.right, self.bottom, self.top = left, right, bottom, top

        # Insert the points into the event heap
        points = convert_to_points(points)
        for point in points:
            self.event_heap.heap_insert(Event('site', point))
        
        # Special case if the first two sites share the same y-coordinate
        first, second = self.event_heap.heap_double_peek()
        if first.key == second.key: self.handle_double_site_start()

        # While event heap is not empty, pop off the event with the maximum y-coordinate and handle accordingly
        while not self.event_heap.empty():
            event = self.event_heap.heap_max()
            if event.category == 'site':
                self.handle_site_event(event)
            else:
                self.handle_circle_event(event)

        # Construct the Voronoi diagram based off of the bounds and internal edges
        edges = self.construct_edge_dict()
        polygons = self.construct_polygons(edges)
        voronoi_diagram = self.assign_polygons(points, polygons)
        voronoi_diagram = convert_to_tuples(voronoi_diagram)
        return voronoi_diagram

    def handle_site_event(self, event: Event):
        if not self.beachline_tree.root:
            self.beachline_tree.insert_root(Arc_node(event.site))
            return

        # Site event overrides potential circle event coming from the arc that the site event is inserted into
        old_node = self.beachline_tree.find_arc(event.site)
        self.remove_circle_event(old_node)

        old_site = old_node.site
        new_site = event.site

        # Restructure the beachline tree in the following way:
        #  A    ->    [A,B]
        #             /   \
        #            A    [B,A]
        #                 /   \
        #                B     A
        left_breakpoint = Breakpoint_node(old_site, new_site)
        right_breakpoint = Breakpoint_node(new_site, old_site)
        old_arc_left, new_arc, old_arc_right = Arc_node(old_site), Arc_node(new_site), Arc_node(old_site)
        self.beachline_tree.insert_right(old_node, left_breakpoint)
        self.beachline_tree.insert_right(left_breakpoint, right_breakpoint)
        self.beachline_tree.insert_left(left_breakpoint, old_arc_left)
        self.beachline_tree.insert_left(right_breakpoint, new_arc)
        self.beachline_tree.insert_right(right_breakpoint, old_arc_right)
        self.beachline_tree.replace_parent(left_breakpoint)

        # Insert a new edge corresponding to the breakpoint between the two sites
        point = projection(new_site, old_site)
        left_breakpoint.edge = self.edge_set.add_edge(point)
        right_breakpoint.edge = left_breakpoint.edge.twin
        self.edge_set.site_vector(left_breakpoint.edge, old_site, new_site)

        # Check for new circle events from neighboring arcs
        self.check_circle_event(self.beachline_tree.predecessor_leaf(old_arc_left), old_arc_left, new_arc)
        self.check_circle_event(new_arc, old_arc_right, self.beachline_tree.successor_leaf(old_arc_right))

    def handle_circle_event(self, event: Event):
        # These are initialized to improve readability
        pred_leaf = self.beachline_tree.predecessor_leaf(event.leaf)
        succ_leaf = self.beachline_tree.successor_leaf(event.leaf)
        pred_node = self.beachline_tree.predecessor_node(event.leaf)
        succ_node = self.beachline_tree.successor_node(event.leaf)
        left_site = pred_leaf.site
        center_site = event.leaf.site
        right_site = succ_leaf.site
        circle_center = circumcenter(left_site, center_site, right_site)

        # Remove the arc corresponding to the circle event
        self.beachline_tree.remove(event.leaf)
        pred_node.edge.twin.origin = circle_center
        succ_node.edge.twin.origin = circle_center

        # Restructure the beachline tree in the following way:
        #     [A,B]                   [A,C]
        #     /   \                   /   \
        #   ...    [B,C]            ...   ...
        #   /\     /   \     ->     / \   / \
        # ... A   B    ...        ...  A C  ...
        #              / \
        #             C  ...
        succ_is_parent = (succ_node == event.leaf.parent)
        removed_breakpoint = succ_node if succ_is_parent else pred_node
        remaining_breakpoint = pred_node if succ_is_parent else succ_node
        remaining_child = succ_node.right if succ_is_parent else pred_node.left
        self.beachline_tree.replace_parent(remaining_child)
        remaining_breakpoint.old_site = left_site
        remaining_breakpoint.new_site = right_site

        # Create a new half edge coming out of the circle event
        half_edge = self.edge_set.add_edge(circle_center)
        remaining_breakpoint.edge = half_edge
        bottom = circlebottom(left_site, center_site, right_site)
        self.edge_set.circle_vector(half_edge, remaining_breakpoint, bottom.y)

        # Remove the currently scheduled circle events for the neighboring arcs
        self.remove_circle_event(pred_leaf)
        self.remove_circle_event(succ_leaf)

        # Check for new circle events for neighboring arcs
        self.check_circle_event(self.beachline_tree.predecessor_leaf(pred_leaf), pred_leaf, succ_leaf)
        self.check_circle_event(pred_leaf, succ_leaf, self.beachline_tree.successor_leaf(succ_leaf))

    def handle_double_site_start(self):
        first = self.event_heap.heap_max()
        second = self.event_heap.heap_max()
        root = self.beachline_tree.insert_root(Breakpoint_node(first.site, second.site))
        self.beachline_tree.insert_left(root, Arc_node(first.site))
        self.beachline_tree.insert_right(root, Arc_node(second.site))
        
        p = Point((first.site.x + second.site.x) / 2.0, self.top)
        root.edge = self.edge_set.add_edge(p)
        self.edge_set.site_vector(root.edge, first.site, second.site)

    def remove_circle_event(self, leaf: Arc_node):
        if leaf.circle_event != None:
            self.event_heap.heap_remove(leaf.circle_event.index)
            leaf.circle_event = None

    def check_circle_event(self, left: Arc_node, center: Arc_node, right: Arc_node):
        if (left == None) or (right == None): return
        e1 = self.beachline_tree.predecessor_node(center).edge
        e2 = self.beachline_tree.successor_node(center).edge
        if converge(e1.point, e1.vector, e2.point, e2.vector):
            bottom = circlebottom(left.site, center.site, right.site).y
            event = Event('circle', leaf=center, circle_bottom=bottom)
            self.event_heap.heap_insert(event)
            center.circle_event = event
    
    def construct_edge_dict(self):
        internal_edges = self.prune_edges()
        boundary_points = self.boundary_points(internal_edges)
        edges = {}
        
        # For each internal edge, add it and its reverse
        for point1, point2 in internal_edges:
            if point1 not in edges:
                edges[point1] = set()
            if point2 not in edges:
                edges[point2] = set()
            edges[point1].add(point2)
            edges[point2].add(point1)

        # For each boundary edge, only add in the counter-clockwise direction
        for i in range(-1, len(boundary_points) - 1):
            if boundary_points[i] not in edges:
                edges[boundary_points[i]] = set()
            edges[boundary_points[i]].add(boundary_points[i + 1])
        return edges

    def prune_edges(self):
        internal_edges = set()
        for edge in self.edge_set.edges:
            twin = edge.twin
            # If neither half-edge knows its destination
            if (not edge.origin) and (not twin.origin):
                # If one of the points is outside, cut off the segment that is outside and move the point until it is inside the bounds
                if outside(edge.point, self.left, self.right, self.bottom, self.top):
                    inward = edge if (not extend(twin.point, twin.vector, self.left, self.right, self.bottom, self.top)) else twin
                    inward.origin = shorten(inward.point, inward.vector, self.left, self.right, self.bottom, self.top)
                    inward.twin.origin = extend(inward.point, inward.vector, self.left, self.right, self.bottom, self.top)
                # Otherwise, shorten the endpoint to the bounds
                else:
                    edge.origin = shorten(edge.point, edge.vector, self.left, self.right, self.bottom, self.top)
                    twin.origin = shorten(twin.point, twin.vector, self.left, self.right, self.bottom, self.top)
            else:
                # Check if edge is already confined within the bounds
                if outwards(edge.origin, edge.vector, self.left, self.right, self.bottom, self.top) or outwards(twin.origin, twin.vector, self.left, self.right, self.bottom, self.top): continue
                # If both endpoints exist, confine the edge to the bounds
                elif edge.origin and twin.origin:
                    if outside(edge.origin, self.left, self.right, self.bottom, self.top):
                        edge.origin = shorten(edge.origin, edge.vector, self.left, self.right, self.bottom, self.top)
                    if outside(twin.origin, self.left, self.right, self.bottom, self.top):
                        twin.origin = shorten(twin.origin, twin.vector, self.left, self.right, self.bottom, self.top)
                # Otherwise, find the last endpoint and confine it
                else:
                    existing = edge if edge.origin else twin
                    existing.twin.origin = extend(existing.origin, existing.vector, self.left, self.right, self.bottom, self.top)
                    if outside(existing.origin, self.left, self.right, self.bottom, self.top):
                        existing.origin = shorten(existing.origin, existing.vector, self.left, self.right, self.bottom, self.top)
            if not (twin.origin, edge.origin) in internal_edges:
                internal_edges.add((edge.origin, twin.origin))
        return internal_edges

    def boundary_points(self, internal_edges):
        # Generate all of the boundary edges in counter-clockwise order
        left_points, right_points, bottom_points, top_points = [], [], [], []
        for point1, point2 in internal_edges:
            if not corner(point1, self.left, self.right, self.bottom, self.top):
                if point1.x == self.left: left_points.append(point1)
                elif point1.x == self.right: right_points.append(point1)
                elif point1.y == self.bottom: bottom_points.append(point1)
                elif point1.y == self.top: top_points.append(point1)
            if not corner(point2, self.left, self.right, self.bottom, self.top):
                if point2.x == self.left: left_points.append(point2)
                elif point2.x == self.right: right_points.append(point2)
                elif point2.y == self.bottom: bottom_points.append(point2)
                elif point2.y == self.top: top_points.append(point2)

        bottom_points.sort(key=lambda k: k.x)
        right_points.sort(key=lambda k: k.y)
        top_points.sort(key=lambda k: -k.x)
        left_points.sort(key=lambda k: -k.y)

        result = []
        result.append(Point(self.left, self.bottom))
        result.extend(bottom_points)
        result.append(Point(self.right, self.bottom))
        result.extend(right_points)
        result.append(Point(self.right, self.top))
        result.extend(top_points)
        result.append(Point(self.left, self.top))
        result.extend(left_points)
        return result

    def construct_polygons(self, edges):
        # Construct polygons by taking the farthest left turn on each edge
        polygons = []
        while edges:
            polygon = []
            start_key = list(edges.keys())[0]
            polygon.append(start_key)
            polygon.append(edges[start_key].pop())
            if not edges[start_key]:
                edges.pop(start_key)

            while True:
                point = farthest_left_turn(polygon[-2], polygon[-1], edges[polygon[-1]])
                edges[polygon[-1]].remove(point)
                if not edges[polygon[-1]]:
                    edges.pop(polygon[-1])
                if point == start_key: break
                polygon.append(point)
            polygons.append(polygon)
        return polygons

    def assign_polygons(self, points, polygons):
        # Use point-in-polygon to determine which point is in which polygon
        voronoi_diagram = {}
        for polygon in polygons:
            for point in points:
                if point_in_polygon(point, polygon):
                    voronoi_diagram[point] = polygon
                    points.remove(point)
                    break
        return voronoi_diagram

def main():
    n = int(input())
    f = int(input())

    sites = []
    for _ in range(f):
        x, y = input().split()
        x, y = int(x), int(y)
        sites.append((x, y))

    voronoi_diagram = Voronoi_Diagram().create_diagram(sites, -n, n, -n, n)
    print(voronoi_diagram)

if __name__ == "__main__":
    main()