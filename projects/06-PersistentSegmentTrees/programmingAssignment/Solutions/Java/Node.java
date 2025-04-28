class Node {
    long value;
    long maximum;
    Node leftChild, rightChild;

    Node() {
        this.value = 0;
        this.maximum = 0;
        this.leftChild = null;
        this.rightChild = null;
    }

    Node(Node l, Node r, long v) {
        this.leftChild = l;
        this.rightChild = r;
        this.value = v;
        this.maximum = v;
    }
}