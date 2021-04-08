import edu.princeton.cs.algs4.RedBlackBST;

public class RBPSPTree extends RedBlackBST {

    private class Node implements Comparable<Node> {
        Point position;
        double radius;
        Node inner;
        Node outer;

        Node() {
        }

        Node(Point position, double radius, Node inner, Node outer) {
            this.position = position;
            this.radius = radius;
            this.inner = inner;
            this.outer = outer;
        }


        @Override
        public int compareTo(Node o) {
            return 0;
        }
    }

}
