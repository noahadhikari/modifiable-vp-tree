import metrics.EuclideanMetric;
import metrics.SquaredEuclideanMetric;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FunctionalityTest {

    Position a = new Position(0., 0.);
    Position b = new Position(4., 2.);
    Position c = new Position(5., 1.);
    Position d = new Position(-4., -6.);
    Position e = new Position(0., -3.);
    Position f = new Position(7, 10);

    public PSPTree<String> createBasicTree() {
        PSPTree<String> p = new PSPTree<>(new SquaredEuclideanMetric(), 2);
        p.insert(a, "A");
        p.insert(b, "B");
        p.insert(c, "C");
        p.insert(d, "D");
        p.insert(e, "E");
        p.insert(f, "F");
        return p;
    }

    @Test
    void basicInsertTest() {
        PSPTree<String> p = createBasicTree();
        System.out.println("Insert:" + p);
        assertEquals(6, p.size());
    }

    @Test
    void basicDeleteTest() {
        PSPTree<String> p = createBasicTree();
        p.delete(b);
        p.delete(f);
        System.out.println("Delete:" + p);
        assertEquals(4, p.size());
    }

    @Test
    void basicContainsTest() {
        PSPTree<String> p = createBasicTree();
        p.delete(b);
        assertTrue(p.contains(a));
        assertFalse(p.contains(b));
    }


    @Test
    void basicMultiDimensionalTest() {
        PSPTree<String> p = new PSPTree<>(new SquaredEuclideanMetric(), 4);
        Position a = new Position(0., 0., 5., 3.);
        Position b = new Position(4., 2., -8., 6.);
        Position c = new Position(5., 1., -4., 0.);
        Position d = new Position(-4., -6., 0., 0.);
        p.insert(a, "A");
        p.insert(b, "B");
        p.insert(c, "C");
        p.insert(d, "D");
        p.delete(c);
        System.out.println("Multi:" + p);
    }


    @Test
    void rangeSearch() {
    }

    @Test
    void basicKNearestNeighborTest() {
        PSPTree<Integer> t = new PSPTree<>(new EuclideanMetric(), 1);
        int m = 30;
        for (int i = 0; i <= m; i++) {
            t.insert(new Position(i), i);
        }
        for (Pair<Position, Integer> p : t.kNearestNeighbor(new Position(10), 400)) {
            System.out.println(p);
        }

    }
}