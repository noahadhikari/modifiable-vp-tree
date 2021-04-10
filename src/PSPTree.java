import metrics.DistanceMetric;
import metrics.EuclideanMetric;

import java.util.*;

/**
 * Space-partitioning variant of a red-black tree as described in
 * https://www.cs.princeton.edu/~rs/talks/LLRB/Java/RedBlackBST.java
 */
public class PSPTree<T> {

    private int size;
    private PSPNode sentinel; // node of radius 0 centered at a random position
                              // in a cube of side length 2 centered at the origin
    private final int dimension;
    private final DistanceMetric distanceMetric;
    private PSPNodeComparator nodeComparator;

    /** Primary helper node building block of the PSPTree. */
    private class PSPNode {

        double[] position;
        double radius;
        PSPNode inner;
        PSPNode outer;
        T value;

        /**
         * @param position double array of the position of the node
         * @param radius radius of the node's "region"
         * @param inner inner child of the node
         * @param outer outer child of the node
         * @param value value stored within this node
         */
        PSPNode(double[] position, double radius, PSPNode inner, PSPNode outer, T value) {
            this.position = position;
            this.radius = radius;
            this.inner = inner;
            this.outer = outer;
            this.value = value;

        }

        /**
         * Returns the distance from THIS to OTHER.
         * @param other node to find distance to
         * @return double distance
         */
        public double distTo(PSPNode other) {
            return distanceMetric.distBetween(this.position, other.position);
        }

        /** Returns whether THIS is located at POS. */
        public boolean isAt(double[] pos) {
            return Arrays.equals(position, pos);
        }

        /** Returns a PDistComparator from this node P. */
        public Comparator<PSPNode> distComparator() {
            return new PDistComparator(this);
        }

        @Override
        public String toString() {
            String s = "(" + value + ", " + Arrays.toString(position) + ", " +
                             String.format("%.1f", radius) + ")";
            if (this.isLeaf()) {
                return s;
            } else if (inner == null) {
                return s + "\n" + outer.toString();
            } else if (outer == null) {
                return s + "\n" + inner.toString();
            } else {
                return s +
                    "\n" +
                    inner.toString() +
                    "\n" +
                    outer.toString();
            }
        }

        public boolean isLeaf() {
            return (outer == null && inner == null);
        }
    }

    /** Used for creating dummy nodes with certain radii for range searches near POS. */
    private PSPNode dummyNode(double[] pos, double r) {
        PSPNode n = new PSPNode(pos, r, null, null, null);
        n.position = pos;
        n.radius = r;
        return n;
    }
    /** Used for creating dummy nodes for neighbor searches near POS. */
    private PSPNode dummyNode(double[] pos) {
        return dummyNode(pos, 0);
    }

    /** Compares how far nodes are to a node P specified in the constructor. */
    private class PDistComparator implements Comparator<PSPNode> {
        PSPNode p;
        public PDistComparator(PSPNode p) {
            this.p = p;
        }

        /**
         * Compares whichever of O1 or O2 is closer to P.
         * Returns a positive value if O1 is farther than O2,
         * a negative value if O1 is closer than O2,
         * and zero if O1 is the same distance from P as O2.
         * @param o1 First node to consider
         * @param o2 Second node to consider
         * @return positive/negative/zero int
         */
        @Override
        public int compare(PSPNode o1, PSPNode o2) {
            return Double.compare(p.distTo(o1), p.distTo(o2));
        }


    }

    /** Comparator for testing if nodes are located within one another's radius or not. */
    private class PSPNodeComparator implements Comparator<PSPNode> {
        PSPNodeComparator() {
        }

        /**
         * Returns a positive value if O2 is outside O1, and a
         * negative value if O2 inside O1. Returns 0 if O1 on the
         * boundary of O2.
         *
         * @param o1 PSPNode to check the boundary of
         * @param o2 PSPNode to check if contained within the boundary of O1
         * @return positive/negative/zero integer
         */
        @Override
        public int compare(PSPNode o1, PSPNode o2) {
            double distance = distanceMetric.distBetween(o1.position, o2.position);
            return Double.compare(distance, o1.radius);
        }
    }

    /**
     * Returns a Pair containing the nearest node to P and the last node visited.
     * @param p Point to find nearest node to in the tree.
     * @return Pair(nearestNode, mostRecentNodeVisited)
     */
    private Pair<PSPNode, PSPNode> nearest(PSPNode p) {
        double bestDistance = Double.MAX_VALUE;
        PSPNode bestNode = null;
        PSPNode lastVisited = sentinel; // node to update to compare the point P with
        while (lastVisited != null) {
            double d = lastVisited.distTo(p);
            if (d < bestDistance) {
                bestNode = lastVisited;
                bestDistance = d;
            }
            int cmp = nodeComparator.compare(lastVisited, p);
            if (cmp > 0) { // outer excludes boundary
                if (lastVisited.outer == null) {
                    break;
                }
                lastVisited = lastVisited.outer;
            } else { // inner includes boundary
                if (lastVisited.inner == null) {
                    break;
                }
                lastVisited = lastVisited.inner;
            }
        }
        return new Pair<>(bestNode, lastVisited);
    }

    /**
     * Returns the node located at POS, if it exists.
     * Otherwise returns null.
     * @param pos Point to find in the tree
     * @return The node at POS, or null if no node exists at POS
     */
    private PSPNode getNode(double[] pos) {
        PSPNode dummy = dummyNode(pos);
        PSPNode n = nearest(dummy).first;
        if (n != null && n.isAt(pos)) {
            return n;
        }
        return null;
    }

    /** Inserts the given node CHILD into this tree. */
    private void insert(PSPNode child) {
        PSPNode parent = nearest(child).last;
        child.radius = parent.distTo(child);
        int cmp = nodeComparator.compare(parent, child);
        cmpHelper(cmp, parent, child);
    }

    /**
     *  Inserts the given VALUE at position POS, if POS doesn't already exist.
     *  If POS already exists in this tree, updates the value at POS to be VALUE.
     *  @param pos double[] representing position
     *  @param value value to insert of type T
     *  */
    public void insert(double[] pos, T value) {
        if (this.contains(pos)) {
            getNode(pos).value = value;
            return;
        }
        PSPNode n = new PSPNode(pos, 0, null, null, value);
        insert(n);
        size += 1;
    }

    /**
     * Removes and returns the value at position POS. Returns null if POS
     * not in the tree.
     * @param pos Position to delete
     * @return
     */
    public T delete(double[] pos) {
        PSPNode dummy = dummyNode(pos);
        PSPNode p = sentinel;
        PSPNode n = sentinel.outer;
        while (n != null && !n.isAt(pos)) {
            p = n;
            int cmp = nodeComparator.compare(n, dummy);
            if (cmp > 0) { // pos is outside n, n = p.outer
                n = n.outer;
            } else {
                n = n.inner;
            }
        }

        if (n == null) {
            return null;
        }

        int cmp = nodeComparator.compare(p, n);
        // >0 n = p.outer
        // <=0 n = p.inner

        if (n.isLeaf()) { // n has no children
            cmpHelper(cmp, p, null);
        } else if (n.inner == null || n.outer == null) { // n has one child
            PSPNode child;
            if (n.inner == null) {
                child = n.outer;
            } else {
                child = n.inner;
            }
            cmpHelper(cmp, p, child);
        } else { // n has two children
            cmpHelper(cmp, p, null);

            n.inner.radius = p.distTo(n.inner);
            insert(n.inner);
            n.outer.radius = p.distTo(n.outer);
            insert(n.outer);
        }


        size -= 1;
        return n.value;
    }

    private void cmpHelper(int cmp, PSPNode n, PSPNode finalNodeVal) {
        if (cmp > 0) {
            n.outer = finalNodeVal;
        } else {
            n.inner = finalNodeVal;
        }
    }

    /**
     * Returns whether this tree contains the position POS.
     * @param pos double[] representing position
     * @return boolean
     */
    public boolean contains(double[] pos) {
        return getNode(pos) != null;
    }

    /** Returns the size of this tree. */
    public int size() {
        return size;
    }

    /**
     * Returns the nodes within a hypersphere of radius r centered at POS.
     * @param pos Position where hypersphere is centered
     * @param r Radius of hypersphere
     * @return Array of nodes
     */
    public PSPNode[] rangeSearch(double[] pos, double r) {
        PSPNode p = dummyNode(pos, r);
        return null;
    }


    public Pair<double[], T>[] kNearestNeighbor(double[] pos, int k) {
        // Assumes k <= size.

        PSPNode p = dummyNode(pos);
        Comparator<PSPNode> pComparator = p.distComparator();
        PriorityQueue<PSPNode> nearestNodes = new PriorityQueue<>(pComparator);

        PSPNode m = sentinel.outer;
        int cmp = nodeComparator.compare(m, p);
        if (cmp > 0) {

        }

        Pair<double[], T>[] arr = (Pair<double[], T>[]) new Object[k];
        for (int i = 0; i < k; i++) {
            PSPNode q = nearestNodes.poll();
            Pair<double[], T> pair = new Pair(q.position, q.value);
            arr[i] = pair;
        }

        return arr;
    }

    @Override
    public String toString() {
        return "{" + sentinel.outer.toString() + "}";
    }

    private PSPNode createSentinel() {
        double[] startPoint = new double[dimension];
        Random r = new Random();
        Iterator<Double> doubleIterator = r.doubles().iterator();
        for (int i = 0; i < dimension; i++) {
            startPoint[i] = doubleIterator.next();
        }
        return new PSPNode(startPoint,0, null, null, null);
    }

    public PSPTree(DistanceMetric d, int dimension) {
        this.distanceMetric = d;
        this.dimension = dimension;
        this.nodeComparator = new PSPNodeComparator();
        this.sentinel = createSentinel();
    }

}
