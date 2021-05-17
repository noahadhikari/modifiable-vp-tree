import metrics.DistanceMetric;
import java.util.*;
import java.util.stream.Collectors;

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

        /**Returns a PDistComparator from this node P.
         * PDistComparators compare how far nodes are from P.
         */
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
         * Returns an iterator representing "pre-order" traversal through the nodes in the tree.
         * There is no natural order in a PSPTree.
         */
        @SuppressWarnings("NullableProblems")
        @Override
        public Iterator<PSPNode> iterator() {
            List<PSPNode> l = new ArrayList<>();
            l.add(this);
            if (inner != null) {
                for (PSPNode p : inner) {
                    l.add(p);
                }
            }

            if (outer != null) {
                for (PSPNode q : outer) {
                    l.add(q);
                }
            }
            return l.iterator();
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
    /** Used for creating dummy nodes for neighbor searches near POS. Usually the radius
     * is representative of the best known distance to POS, and defaults to positive infinity. */
    private PSPNode dummyNode(Position pos) {
        return dummyNode(pos, Double.POSITIVE_INFINITY);
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
     * Returns the parent of any node inserted into the tree.
     * @param goal Point to find nearest node to in the tree.
     * @return parent node
     */
    private PSPNode findInsertionParent(PSPNode goal) {
        PSPNode p = sentinel; // node to update to compare the point P with
        while (p != null) {
            double d = p.distTo(goal);
            if (d < goal.radius) {
                goal.radius = d;
            }
            int cmp = nodeComparator.compare(p, goal);
            if (cmp > 0) { // outer excludes boundary
                if (p.outer == null) {
                    break;
                }
                p = p.outer;
            } else { // inner includes boundary
                if (p.inner == null) {
                    break;
                }
                p = p.inner;
            }
        }
        return p;
    }

    /**
     * Returns the node located at POS, if it exists.
     * Otherwise returns null.
     * @param pos Point to find in the tree
     * @return The node at POS, or null if no node exists at POS
     */
    private PSPNode getNode(Position pos) {
        if (size() == 0) {
            return null;
        }
        PSPNode n = oneNearestNeighbor(dummyNode(pos));
        if (n != null) {
            if (n.isAt(pos)) {
                return n;
            }
        }
        return null;
    }

    /**Inserts the given node CHILD into this tree.
     * Finds the appropriate parent automatically so specifying the
     * parent is not necessary. */
    private void insert(PSPNode child) {
        PSPNode parent = findInsertionParent(child);
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
        n.inner = null;
        n.outer = null;
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

    /**Returns the candidate-nearest neighbor (close but not necessarily the closest) to GOAL
     * starting from START.*/
    private PSPNode candidateNearestNeighbor(PSPNode start, PSPNode goal) {
        PSPNode candidate = start;
        //finds the candidate-nearest neighbor
        while (start != null) {
            double d = start.distTo(goal);
            if (d < goal.radius) {
                candidate = start;
                goal.radius = d;
            }
            int cmp = nodeComparator.compare(start, goal);
            if (cmp > 0) { // outer excludes boundary
                if (start.outer == null) {
                    break;
                }
                start = start.outer;
            } else { // inner includes boundary
                if (start.inner == null) {
                    break;
                }
                start = start.inner;
            }
        }
        return candidate;
    }

    private PSPNode oneNearestNeighbor(PSPNode start, PSPNode goal) {
        PSPNode candidate = candidateNearestNeighbor(start, goal);
        PSPNode otherStart;
        int cmp = nodeComparator.compare(candidate, goal);
        if (cmp > 0) {
            otherStart = candidate.inner;
        } else {
            otherStart = candidate.outer;
        }
        PSPNode best = candidate;
        if (otherStart != null) {
            PSPNode otherCandidate = oneNearestNeighbor(otherStart, goal);
            Comparator<PSPNode> cmptr = goal.distComparator();
            // < 0 if O1.distTo(P) < O2.distTo(P)
            int distCmp = cmptr.compare(otherCandidate, candidate);
            if (distCmp <= 0) {
                best = otherCandidate;
            }
        }

        return best;
    }
    /** Returns the direct nearest neighbor in the tree to P.*/
    private PSPNode oneNearestNeighbor(PSPNode p) {
        return oneNearestNeighbor(sentinel.outer, p);
    }

    /**
     * Returns the K closest nodes to POS in ascending order.
     * If K is greater than the size of this tree,
     * returns only size nodes.
     * @param p PSPNode to search near
     * @param k Number of neighbors to find
     * @return List containing the K nearest neighbors to POS
     */
    private List<PSPNode> kNearestNeighbor(PSPNode p, int k) {
        if (k > size) {
            k = size;
        }
        List<PSPNode> nodeList = new ArrayList<>(k);
        for (int i = 0; i < k; i++) {
            PSPNode best = oneNearestNeighbor(p);
            nodeList.add(best);
            delete(best.position);
            p.radius = Double.POSITIVE_INFINITY;
        }
        for (PSPNode deleted : nodeList) { //restores tree
            insert(deleted.position, deleted.value);
        }
        return nodeList;
    }

    /**
     * Returns the K closest nodes to POS in ascending order.
     * If K is greater than the size of this tree,
     * returns only size nodes.
     * @param pos Position to search near
     * @param k Number of neighbors to find
     * @return List containing the K nearest neighbors to POS
     */
    public List<Pair<Position, T>> kNearestNeighbor(Position pos, int k) {
        PSPNode dummy = dummyNode(pos);
        List<PSPNode> neighbors = kNearestNeighbor(dummy, k);
        return neighbors.stream().map(PSPNode::toPair).collect(Collectors.toList());
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
        double[] startPoint = new double[dimension];
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
