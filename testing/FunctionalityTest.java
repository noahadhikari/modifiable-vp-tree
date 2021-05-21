import metrics.EuclideanMetric;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FunctionalityTest {

    Position a = new Position(0, 0);
    Position b = new Position(4, 2);
    Position c = new Position(5, 1);
    Position d = new Position(-4, -6);
    Position e = new Position(0, -3);
    Position f = new Position(7, 10);

    public Map<Position, String> createBasicTreeMap() {
        PSPTreeMap<String> p = new PSPTreeMap<>(new EuclideanMetric(), 2);
        p.put(a, "A");
        p.put(b, "B");
        p.put(c, "C");
        p.put(d, "D");
        p.put(e, "E");
        p.put(f, "F");
        return p;
    }

    public Map<Position, String> createBasicHashMap() {
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
        Map<Position, String> p = createBasicTreeMap();
        Map<Position, String> expected = createBasicHashMap();
        assertEquals(6, p.size());
        assertEquals(expected.entrySet(), p.entrySet());
    }

    @Test
    void basicDeleteTest() {
        Map<Position, String> p = createBasicTreeMap();
        Map<Position, String> expected = createBasicHashMap();
        p.remove(b); expected.remove(b);
        p.remove(f); expected.remove(f);
        assertEquals(4, p.size());
        assertEquals(expected.entrySet(), p.entrySet());
    }

    @Test
    void basicContainsKeyTest() {
        Map<Position, String> p = createBasicTreeMap();
        p.remove(b);
        assertTrue(p.containsKey(a));
        assertFalse(p.containsKey(b));
    }


    @Test
    void basicMultiDimensionalTest() {
        Map<Position, String> p = new PSPTreeMap<>(new EuclideanMetric(), 4);
        Map<Position, String> expected = createBasicHashMap();

        Position a = new Position(0., 0., 5., 3.);
        Position b = new Position(4., 2., -8., 6.);
        Position c = new Position(5., 1., -4., 0.);
        Position d = new Position(-4., -6., 0., 0.);
        p.put(a, "A"); expected.put(a, "A");
        p.put(b, "B"); expected.put(b, "B");
        p.put(c, "C"); expected.put(c, "C");
        p.put(d, "D"); expected.put(d, "D");
        p.remove(c); expected.remove(c);
        assertEquals(expected.entrySet(), p.entrySet());
    }


    @Test
    void rangeSearch() {
    }

    @Test
    void basicKNearestNeighborTest() {
        PSPTreeMap<Integer> t = new PSPTreeMap<>(new EuclideanMetric(), 1);
        int m = 30;
        for (int i = 0; i <= m; i++) {
            t.put(new Position(i), i);
        }
        for (Pair<Position, Integer> p : t.kNearestNeighbor(new Position(10), 400)) {
            System.out.println(p);
        }

    }
}