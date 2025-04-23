import sys
from PST import PST


def main():
    data = input().split()
    n = int(data[0])
    c = int(data[1])
    scores = list(map(int, input().split()))
    tree = PST(n)
    tree.construct(scores)

    for i in range(c):
        cmd = input().split()
        if cmd[0] == 'S':
            x = int(cmd[1]) - 1
            y = int(cmd[2]) - 1
            tree.swap(x, y)
        elif cmd[0] == 'R':
            pos = int(cmd[1]) - 1
            val = int(cmd[2])
            tree.update(pos, val)
        elif cmd[0] == 'P':
            pos = int(cmd[1]) - 1
            max_val = tree.query_node_maximum(pos)
            tree.update(pos, max_val)
        elif cmd[0] == 'Q':
            l = int(cmd[1]) - 1
            r = int(cmd[2]) - 1
            v1 = int(cmd[3]) - 1
            v2 = int(cmd[4]) - 1
            total1 = tree.query(v1, l, r)
            total2 = tree.query(v2, l, r)
            print(abs(total2 - total1))
        else:
            print("ERROR BAD INPUT PARSING")
            exit(1)


if __name__ == "__main__":
    main()
