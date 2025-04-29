from voronoi_diagram import Voronoi_Diagram, Point, convert_to_points, point_in_polygon

n = int(input())
f, p = input().split()
f, p = int(f), int(p)

sites = []
for _ in range(f):
    x, y = input().split()
    x, y = int(x), int(y)
    sites.append((x, y))

voronoi_diagram = Voronoi_Diagram().create_diagram(sites, -n, n, -n, n)

site_count = {site:0 for site in sites}

for _ in range(p):
    x, y = input().split()
    x, y = int(x), int(y)
    for site in sites:
        if point_in_polygon(Point(x, y), convert_to_points(voronoi_diagram[site])):
            site_count[site] += 1

max_count = 0
max_site = (-1, -1)
for site in site_count:
    if site_count[site] > max_count:
        max_count = site_count[site]
        max_site = site
print(max_site[0], max_site[1])