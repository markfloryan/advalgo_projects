package implementation;

import java.io.*;
import java.util.*;

class Node {
    int l, r, val;
    Node lChild, rChild;
    boolean marked; // For SegTreeAssignAndGet
    int lazy; // For SegTreeAdditionAndMax

    Node(int left, int right) {
        this.l = left;
        this.r = right;
        this.val = 0;
        this.marked = false;
        this.lazy = 0;
    }
}


// SegTreeStandard: Basic segment tree for range sum queries
class SegTreeStandard {

}


// SegTreeAdditionAndGet: Supports range addition and point queries
class SegTreeAdditionAndGet {

}


// SegTreeAssignAndGet: Supports range assignment and point queries
class SegTreeAssignAndGet {

}


// Main class for testing
public class SegTreeRangeUpdates {
    public static void main(String[] args) throws IOException {
        // Test cases and input handling can be added here
    }
}

