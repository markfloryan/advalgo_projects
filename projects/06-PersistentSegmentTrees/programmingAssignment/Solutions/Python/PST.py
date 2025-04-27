from Node import Node


class PST:
    MAX = 100001

    def __init__(self, n):
        self.n = n
        self.version = 0
        self.root = [None] * self.MAX

    def construct(self, val):
        self.root[0] = self._construct(val, 0, self.n - 1)

    def _construct(self, val, l, r):
        node = Node()
        if l == r:
            node.value = val[l] if val else 0
            node.maximum = val[l] if val else 0
            return node
        middle = (l + r) // 2
        node.leftChild = self._construct(val, l, middle)
        node.rightChild = self._construct(val, middle + 1, r)
        node.value = node.leftChild.value + node.rightChild.value
        return node

    def _update(self, l, r, prev, idx, new_val, new_max):
        node = Node()
        if l == r:
            node.value = new_val
            node.maximum = new_max
        else:
            middle = (l + r) // 2
            if idx <= middle:
                node.leftChild = self._update(l, middle, prev.leftChild, idx, new_val, new_max)
                node.rightChild = prev.rightChild
            else:
                node.rightChild = self._update(middle + 1, r, prev.rightChild, idx, new_val, new_max)
                node.leftChild = prev.leftChild
            node.value = node.leftChild.value + node.rightChild.value
        return node

    def _query(self, node, l, r, ql, qr):
        if l > qr or r < ql:
            return 0
        if ql <= l and qr >= r:
            return node.value
        middle = (l + r) // 2
        return self._query(node.leftChild, l, middle, ql, qr) + \
            self._query(node.rightChild, middle + 1, r, ql, qr)

    def _query_node_maximum(self, node, idx, l, r):
        if idx < l or idx > r:
            return -1
        if l == idx and r == idx:
            return node.maximum
        middle = (l + r) // 2
        left = self._query_node_maximum(node.leftChild, idx, l, middle)
        right = self._query_node_maximum(node.rightChild, idx, middle + 1, r)
        return max(left, right)

    def update(self, idx, val):
        old_max = self.query_node_maximum(idx)
        new_max = max(val, old_max)
        self.version += 1
        self.root[self.version] = self._update(0, self.n - 1, self.root[self.version - 1], idx, val, new_max)

    def query(self, query_version, l, r):
        return self._query(self.root[query_version], 0, self.n - 1, l, r)

    def query_node_maximum(self, idx):
        return self._query_node_maximum(self.root[self.version], idx, 0, self.n - 1)

    def swap(self, idx1, idx2):
        val1 = self.query(self.version, idx1, idx1)
        val2 = self.query(self.version, idx2, idx2)
        max1 = self.query_node_maximum(idx1)
        max2 = self.query_node_maximum(idx2)
        self.version += 1
        temp = self._update(0, self.n - 1, self.root[self.version - 1], idx1, val2, max(val2, max1))
        self.root[self.version] = self._update(0, self.n - 1, temp, idx2, val1, max(val1, max2))

    def print_tree(self, node=None, l=None, r=None):
        if node is None:
            node = self.root[self.version]
            l = 0
            r = self.n - 1
            print(f"VERSION: {self.version}")

        if l == r:
            print(node.value, node.maximum)
        else:
            middle = (l + r) // 2
            self.print_tree(node.leftChild, l, middle)
            self.print_tree(node.rightChild, middle + 1, r)
