import metrics.EuclideanMetric;
import metrics.SquaredEuclideanMetric;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FunctionalityTest {

    @Test
    void basicInsertTest() {
        PSPTree<String> p = new PSPTree<>(new SquaredEuclideanMetric(), 2);
        Position a = new Position(0., 0.);
        Position b = new Position(4., 2.);
        Position c = new Position(5., 1.);
        Position d = new Position(-4., -6.);
        Position e = new Position(0., -3.);
        p.insert(a, "A");
        p.insert(b, "B");
        p.insert(c, "C");
        p.insert(d, "D");
        p.insert(e, "E");
        p.insert(e, "F");
        System.out.println(p);
        assertEquals(5, p.size());
    }

    @Test
    void basicDeleteTest() {
        PSPTree<String> p = new PSPTree<>(new SquaredEuclideanMetric(), 2);
        Position a = new Position(0., 0.);
        Position b = new Position(4., 2.);
        Position c = new Position(5., 1.);
        Position d = new Position(-4., -6.);
        Position e = new Position(0., -3.);
        Position f = new Position(0., -345.);
        p.insert(a, "A");
        p.insert(b, "B");
        p.insert(c, "C");
        p.insert(d, "D");
        p.insert(e, "E");
        p.delete(b);
        p.delete(f);
        System.out.println(p);
        assertEquals(4, p.size());
    }

    @Test
    void basicContainsTest() {
        PSPTree<String> p = new PSPTree<>(new SquaredEuclideanMetric(), 2);
        Position a = new Position(0., 0.);
        Position b = new Position(4., 2.);
        Position c = new Position(5., 1.);
        Position d = new Position(-4., -6.);
        Position e = new Position(0., -3.);
        Position f = new Position(0., -345.);
        p.insert(a, "A");
        p.insert(b, "B");
        p.insert(c, "C");
        p.insert(d, "D");
        p.insert(e, "E");
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
        System.out.println(p);
    }


    @Test
    void rangeSearch() {
    }

    @Test
    void kNearestNeighbor() {
    }
}