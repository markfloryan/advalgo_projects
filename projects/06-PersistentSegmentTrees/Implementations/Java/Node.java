// node class that stores a long as well as the left and right children
// can be updated to store other data types; would potentially require updating the merge function
public class Node {
    long value;
    Node leftChild;
    Node rightChild;
    // default constructor sets values to null
    public Node() {
        this.leftChild = this.rightChild = null;
        this.value = 0;
    }
    public Node(long value, Node leftChild, Node rightChild) {
        this.leftChild = leftChild;
        this.rightChild = rightChild;
        this.value = value;
    }
}
