import sys
import threading
import random

sys.setrecursionlimit(1 << 25)

# Each node represents a banana: (position, ready time)
class Node:
    def __init__(self, p, t):
        self.p = p # Position of banana
        self.t = t # Time when banana becomes eatable
        self.left = None
        self.right = None
        self.priority = random.randint(1, 1 << 30)  # Heap priority
        self.size = 1  # Subtree size (not used here but standard in Treap)

# Update the size field based on left and right children
def update_size(node):
    if node:
        node.size = 1 + (node.left.size if node.left else 0) + (node.right.size if node.right else 0)

# Split treap into (< key) and (≥ key)
def split(node, key):
    if not node:
        return (None, None)
    if key < node.p:
        left, right = split(node.left, key)
        node.left = right
        update_size(node)
        return (left, node)
    else:
        left, right = split(node.right, key)
        node.right = left
        update_size(node)
        return (node, right)

# Merge two treaps assuming all keys in a < keys in b
def merge(a, b):
    if not a or not b:
        return a or b
    if a.priority > b.priority:
        a.right = merge(a.right, b)
        update_size(a)
        return a
    else:
        b.left = merge(a, b.left)
        update_size(b)
        return b

# Insert new_node into treap rooted at node
def insert(node, new_node):
    if not node:
        return new_node
    if new_node.priority > node.priority:
        left, right = split(node, new_node.p)
        new_node.left = left
        new_node.right = right
        update_size(new_node)
        return new_node
    if new_node.p < node.p:
        node.left = insert(node.left, new_node)
    else:
        node.right = insert(node.right, new_node)
    update_size(node)
    return node

# Remove node with given key from treap
def remove(node, key):
    if not node:
        return None
    if node.p == key:
        return merge(node.left, node.right)
    elif key < node.p:
        node.left = remove(node.left, key)
    else:
        node.right = remove(node.right, key)
    update_size(node)
    return node

# Simulate the monkey moving and eating bananas within time T
def simulate(node, T):
    stack = []
    cur = node
    time = 0 # current time
    pos = 0 # current position
    cnt = 0 # bananas eaten

    # In-order traversal to simulate left-to-right banana positions
    while cur or stack:
        while cur:
            stack.append(cur)
            cur = cur.left
        cur = stack.pop()

        move = cur.p - pos # time needed to reach this banana
        time += move
        pos = cur.p

        if time > T:
            break  # too late to reach this one

        if time >= cur.t:
            if time < T:
                cnt += 1 # eat immediately
        elif cur.t < T:
            time = cur.t # wait until it's ready, then eat
            cnt += 1
        else:
            cur = cur.right # can't eat, skip to next
            continue

        cur = cur.right

    return cnt

def main():
    input = sys.stdin.readline
    q = int(input())
    root = None
    for _ in range(q):
        tok = input().strip().split()
        if tok[0] == "ADD":
            p, t = int(tok[1]), int(tok[2])
            root = insert(root, Node(p, t))
        elif tok[0] == "REMOVE":
            p = int(tok[1])
            root = remove(root, p)
        elif tok[0] == "QUERY":
            T = int(tok[1])
            print(simulate(root, T))

# Run in thread to avoid recursion limit crashes on deep trees
threading.Thread(target=main).start()