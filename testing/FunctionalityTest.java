import metrics.EuclideanMetric;
import metrics.SquaredEuclideanMetric;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FunctionalityTest {

    Position a = new Position(0., 0.);
    Position b = new Position(4., 2.);
    Position c = new Position(5., 1.);
    Position d = new Position(-4., -6.);
    Position e = new Position(0., -3.);
    Position f = new Position(7, 10);

    public PSPTreeMap<String> createBasicTree() {
        PSPTreeMap<String> p = new PSPTreeMap<>(new SquaredEuclideanMetric(), 2);
        p.insert(a, "A");
        p.insert(b, "B");
        p.insert(c, "C");
        p.insert(d, "D");
        p.insert(e, "E");
        p.insert(f, "F");
        return p;
    }

    public Map<Position, String> createBasicMap() {
        Map<Position, String> p = new HashMap<>();
        p.put(a, "A");
        p.put(b, "B");
        p.put(c, "C");
        p.put(d, "D");
        p.put(e, "E");
        p.put(f, "F");
        return p;
    }

    @Test
    void basicInsertTest() {
        PSPTreeMap<String> p = createBasicTree();
        Map<Position, String> q = createBasicMap();
        System.out.println("Insert:\n" + p);
        assertEquals(6, p.size());
    }

    @Test
    void basicDeleteTest() {
        PSPTreeMap<String> p = createBasicTree();
        p.delete(b);
        p.delete(f);
        System.out.println("Delete:\n" + p);
        assertEquals(4, p.size());
    }

    @Test
    void basicContainsTest() {
        PSPTreeMap<String> p = createBasicTree();
        p.delete(b);
        assertTrue(p.contains(a));
        assertFalse(p.contains(b));
    }


    @Test
    void basicMultiDimensionalTest() {
        PSPTreeMap<String> p = new PSPTreeMap<>(new SquaredEuclideanMetric(), 4);
        Position a = new Position(0., 0., 5., 3.);
        Position b = new Position(4., 2., -8., 6.);
        Position c = new Position(5., 1., -4., 0.);
        Position d = new Position(-4., -6., 0., 0.);
        p.insert(a, "A");
        p.insert(b, "B");
        p.insert(c, "C");
        p.insert(d, "D");
        p.delete(c);
        System.out.println("Multi:\n" + p);
    }


    @Test
    void rangeSearch() {
    }

    @Test
    void basicKNearestNeighborTest() {
        PSPTreeMap<Integer> t = new PSPTreeMap<>(new EuclideanMetric(), 1);
        int m = 30;
        for (int i = 0; i <= m; i++) {
            t.insert(new Position(i), i);
        }
        for (Pair<Position, Integer> p : t.kNearestNeighbor(new Position(10), 400)) {
            System.out.println(p);
        }

    }
}