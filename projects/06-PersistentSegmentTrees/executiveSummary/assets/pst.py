class Node:
    def __init__(self, value=0, left=None, right=None):
        self.value = value
        self.leftChild = left
        self.rightChild = right


class PST:
    MAX = 100

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

    def _update(self, l, r, prev, idx, new_val):
        node = Node()
        if l == r:
            node.value = new_val
        else:
            middle = (l + r) // 2
            if idx <= middle:
                node.leftChild = self._update(l, middle, prev.leftChild, idx, new_val)
                node.rightChild = prev.rightChild
            else:
                node.rightChild = self._update(
                    middle + 1, r, prev.rightChild, idx, new_val
                )
                node.leftChild = prev.leftChild
            node.value = node.leftChild.value + node.rightChild.value
        return node

    def _query(self, node, l, r, ql, qr):
        if l > qr or r < ql:
            return 0
        if ql <= l and qr >= r:
            return node.value
        middle = (l + r) // 2
        return self._query(node.leftChild, l, middle, ql, qr) + self._query(
            node.rightChild, middle + 1, r, ql, qr
        )

    def update(self, idx, val):
        self.version += 1
        self.root[self.version] = self._update(
            0, self.n - 1, self.root[self.version - 1], idx, val
        )

    def query(self, query_version, l, r):
        return self._query(self.root[query_version], 0, self.n - 1, l, r)
