class pt:
    def __init__(self, x, y):
        self.x = x
        self.y = y

    def __add__(self, other):
        return pt(self.x + other.x, self.y + other.y)

    def __sub__(self, other):
        return pt(self.x - other.x, self.y - other.y)

    def cross(self, other):
        return self.x * other.y - self.y * other.x

def reorder_polygon(P):
    pos = 0
    for i in range(1, len(P)):
        if (P[i].y < P[pos].y) or (P[i].y == P[pos].y and P[i].x < P[pos].x):
            pos = i
    P[:] = P[pos:] + P[:pos]

def minkowski(P, Q):
    reorder_polygon(P)
    reorder_polygon(Q)

    P.append(P[0])
    P.append(P[1])
    Q.append(Q[0])
    Q.append(Q[1])

    result = []
    i = j = 0
    while i < len(P) - 2 or j < len(Q) - 2:
        result.append(P[i] + Q[j])
        cross = (P[i + 1] - P[i]).cross(Q[j + 1] - Q[j])
        if cross >= 0 and i < len(P) - 2:
            i += 1
        if cross <= 0 and j < len(Q) - 2:
            j += 1
    return result

polyA = []
polyB = []

aSize = int(input())
for _ in range(aSize):
    x, y = map(int, input().split())
    polyA.append(pt(x, y))

bSize = int(input())
for _ in range(bSize):
    x, y = map(int, input().split())
    polyB.append(pt(x, y))

ans = minkowski(polyA, polyB)

for p in ans:
    print(p.x, p.y)
