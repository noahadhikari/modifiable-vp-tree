import metrics.DistanceMetric;
import java.util.*;

/**
 * Space-partitioning tree that uses hyperspheres to separate space into
 * inner and outer regions.
 */
public class PSPTree<T> implements Iterable<Pair<Position, T>> {

    private int size;
    private final PSPNode sentinel; // node of radius 0 centered at a random position
                              // in a cube of side length 2 centered at the origin
    private final int dimension;
    private final DistanceMetric distanceMetric;
    private final PSPNodeComparator nodeComparator;

    private class PSPNode implements Iterable<PSPNode> {

        Position position;
        double radius;
        PSPNode inner;
        PSPNode outer;
        T value;

        /**
         * Primary helper node building block of the PSPTree.
         * @param position double array of the position of the node
         * @param radius radius of the node's "region"
         * @param inner inner child of the node
         * @param outer outer child of the node
         * @param value value stored within this node
         */
        PSPNode(Position position, double radius, PSPNode inner, PSPNode outer, T value) {
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
            return this.position.distTo(other.position, distanceMetric);
        }

        /** Returns whether THIS is located at POS. */
        public boolean isAt(Position pos) {
            return position.equals(pos);
        }

        /** Returns a PDistComparator from this node P. */
        public Comparator<PSPNode> distComparator() {
            return new PDistComparator(this);
        }


        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (PSPNode node : this) {
                sb.append(node.toPair());
                sb.append(" ;\n");
            }
            return sb.toString();
        }

        public boolean isLeaf() {
            return (outer == null && inner == null);
        }

        /**
         * Helper method for iterator, combining the inner and outer nodes of n.
         * @param l List to append to
         * @param n Node to consider
         * @return in-order iterator over all the nodes of n
         */
        private Iterator<PSPNode> iterator(List<PSPNode> l, PSPNode n) {
            if (n.inner != null) {
                for (PSPNode p : n.inner) {
                    l.add(p);
                }
            }

            l.add(this);

            if (n.outer != null) {
                for (PSPNode p : n.outer) {
                    l.add(p);
                }
            }
            return l.iterator();
        }

        /**
         * Returns an iterator representing "in-order" traversal through the nodes in the tree.
         * There is no natural order in a PSPTree.
         */
        @SuppressWarnings("NullableProblems")
        @Override
        public Iterator<PSPNode> iterator() {
            return iterator(new ArrayList<>(), this);
        }

        public Pair<Position, T> toPair() {
            return new Pair<>(position, value);
        }
    }

    /**Returns an iterator over all PSPNodes in this PSPTree. PSPTrees have
     * no natural order.
     * @return Iterator of Pair(position, value) over all nodes in the tree
     * */
    @Override
    public Iterator<Pair<Position, T>> iterator() {
        List<Pair<Position, T>> l = new ArrayList<>();
        for (PSPNode n : sentinel.outer) {
            l.add(n.toPair());
        }
        return l.iterator();
    }

    /** Used for creating dummy nodes with certain radii for range searches near POS. */
    private PSPNode dummyNode(Position pos, double r) {
        PSPNode n = new PSPNode(pos, r, null, null, null);
        n.position = pos;
        n.radius = r;
        return n;
    }
    /** Used for creating dummy nodes for neighbor searches near POS. */
    private PSPNode dummyNode(Position pos) {
        return dummyNode(pos, 0);
    }

    private class PDistComparator implements Comparator<PSPNode> {
        PSPNode p;
        /** Compares how far nodes are to a node P specified in the constructor. */
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


    private class PSPNodeComparator implements Comparator<PSPNode> {

        /** Comparator for testing if nodes are located within one another's radius or not. */
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
            double distance = o1.distTo(o2);
            return Double.compare(distance, o1.radius);
        }
    }

    /**
     * Returns a Pair containing the nearest node to P and the last node visited.
     * @param p Point to find nearest node to in the tree.
     * @return Pair(nearestNode, mostRecentNodeVisited)
     */
    private Pair<PSPNode, PSPNode> nearestAndLastVisited(PSPNode p) {
        double bestDistance = Double.POSITIVE_INFINITY;
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
    private PSPNode getNode(Position pos) {
        PSPNode dummy = dummyNode(pos);
        PSPNode n = nearestAndLastVisited(dummy).first;
        if (n != null && n.isAt(pos)) {
            return n;
        }
        return null;
    }

    /** Inserts the given node CHILD into this tree. */
    private void insert(PSPNode child) {
        PSPNode parent = nearestAndLastVisited(child).last;
        child.radius = parent.distTo(child);
        int cmp = nodeComparator.compare(parent, child);
        nodeSetWithCmp(cmp, parent, child);
    }

    /**
     *  Inserts the given VALUE at position POS, if POS doesn't already exist.
     *  If POS already exists in this tree, updates the value at POS to be VALUE.
     *  @param pos Position representing position
     *  @param value value to insert of type T
     *  */
    public void insert(Position pos, T value) {
        PSPNode n = getNode(pos);
        if (n != null) {
            n.value = value;
            return;
        }
        PSPNode newNode = new PSPNode(pos, 0, null, null, value);
        insert(newNode);
        size++;
    }

    /**
     * Removes and returns the value at position POS. Returns null if POS
     * not in the tree.
     * @param pos Position to delete
     * @return value of the deleted node
     */
    public T delete(Position pos) {
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
            nodeSetWithCmp(cmp, p, null);
        } else if (n.inner == null || n.outer == null) { // n has one child
            PSPNode child;
            if (n.inner == null) {
                child = n.outer;
            } else {
                child = n.inner;
            }
            nodeSetWithCmp(cmp, p, child);
        } else { // n has two children
            nodeSetWithCmp(cmp, p, null);

            n.inner.radius = p.distTo(n.inner);
            insert(n.inner);
            n.outer.radius = p.distTo(n.outer);
            insert(n.outer);
        }


        size--;
        return n.value;
    }

    /** Sets the child of N to be OTHERNODE depending on the appropriate value of CMP. */
    private void nodeSetWithCmp(int cmp, PSPNode n, PSPNode otherNode) {
        if (cmp > 0) {
            n.outer = otherNode;
        } else {
            n.inner = otherNode;
        }
    }

    /**
     * Returns whether this tree contains the position POS.
     * @param pos Position representing position
     * @return boolean
     */
    public boolean contains(Position pos) {
        return getNode(pos) != null;
    }

    /** Returns the size of this PSPTree. */
    public int size() {
        return size;
    }

    /**
     * Returns the nodes within a hypersphere of radius r centered at POS.
     * @param pos Position where hypersphere is centered
     * @param r Radius of hypersphere
     * @return Array of nodes
     */
    public PSPNode[] rangeSearch(Position pos, double r) {
        PSPNode p = dummyNode(pos, r);
        return null;
    }


    /**
     * Returns the K closest nodes to POS. If K > size, only returns size elements.
     * @param pos Position to search near
     * @param k Number of neighbors to find
     * @return List containing the K nearest neighbors to POS
     */
    public List<Pair<Position, T>> kNearestNeighbor(Position pos, int k) {
        if (k >= size()) {
            k = size();
        }
        PSPNode p = dummyNode(pos);
        Set<PSPNode> found = new HashSet<>();
        List<Pair<Position, T>> pairList = new ArrayList<>();

        return pairList;
    }

    @Override
    public String toString() {
        return "{\n" + sentinel.outer.toString() + "}";
    }

    /**
     * Creates and returns the sentinel node, a node with a radius of 0
     * created within a unit hypersphere of the origin (0, ..., 0).
     * The first real node in the tree is given by sentinel.outer.
     * @return the sentinel node
     */
    private PSPNode createSentinel() {
        Double[] startPoint = new Double[dimension];
        Random r = new Random();
        for (int i = 0; i < dimension; i++) {
            startPoint[i] = r.nextDouble();
        }
        return new PSPNode(new Position(startPoint), 0, null, null, null);
    }

    public PSPTree(DistanceMetric d, int dimension) {
        this.distanceMetric = d;
        this.dimension = dimension;
        this.nodeComparator = new PSPNodeComparator();
        this.sentinel = createSentinel();
    }

}
