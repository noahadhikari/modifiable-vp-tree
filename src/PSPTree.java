
/**
 * Space-partitioning variant of a left-leaning red-black tree as described in
 * https://www.cs.princeton.edu/~rs/talks/LLRB/Java/RedBlackBST.java
 *
 *
 * @param <T> Generic value of type T
 */
public class PSPTree<T> {
    private static final boolean RED = true;
    private static final boolean BLACK = false;

    private class PSPNode {
        double[] position;
        T value;
        PSPNode inner;
        PSPNode outer;

        PSPNode() {
        }

        PSPNode(double[] position, T value, PSPNode inner, PSPNode outer) {
            this.position = position;
            this.value = value;
            this.inner = inner;
            this.outer = outer;
        }
    }

    public PSPTree() {

    }
}
