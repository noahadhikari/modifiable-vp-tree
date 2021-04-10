import metrics.EuclideanMetric;
import metrics.SquaredEuclideanMetric;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PSPTreeFunctionalityTest {

    @Test
    void basicInsertTest() {
        PSPTree<String> p = new PSPTree<>(new SquaredEuclideanMetric(), 2);
        double[] a = new double[]{0, 0};
        double[] b = new double[]{4, 2};
        double[] c = new double[]{5, 1};
        double[] d = new double[]{-4, -6};
        double[] e = new double[]{0, -3};
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
        double[] a = new double[]{0, 0};
        double[] b = new double[]{4, 2};
        double[] c = new double[]{5, 1};
        double[] d = new double[]{-4, -6};
        double[] e = new double[]{0, -3};
        double[] f = new double[]{0, -345};
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
        double[] a = new double[]{0, 0};
        double[] b = new double[]{4, 2};
        double[] c = new double[]{5, 1};
        double[] d = new double[]{-4, -6};
        double[] e = new double[]{0, -3};
        double[] f = new double[]{0, -345};
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
        double[] a = new double[]{0, 0, 5, 3};
        double[] b = new double[]{4, 2, -8, 6};
        double[] c = new double[]{5, 1, -4, 0};
        double[] d = new double[]{-4, -6, 0, 0};
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