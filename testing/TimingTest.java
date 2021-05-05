import com.google.common.base.Stopwatch;
import metrics.SquaredEuclideanMetric;

import org.junit.jupiter.api.Test;
import org.knowm.xchart.QuickChart;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;


public class TimingTest {
    @Test
    void dimensionalityTest() throws InterruptedException {
        // theoretically avg O(f(d) log d), where distanceMetric runs in O(f(d)) time.
        // for dimension d
        int N = 1000; // number of nodes to insert into each dimension
        int K = 10000; // max dimension of nodes
        int c = (int) Math.log10(K);
        double[] dimensions = new double[c];
        double[] times = new double[c];
        Random rand = new Random();



        for (int k = 10; k <= K; k *= 10) { //loop through dimensions 1, 10, ... , K
            PSPTree<Integer> p = new PSPTree<>(new SquaredEuclideanMetric(), k);
                long time = 0;
                for (int i = 0; i < N; i++) { //for each n, add one node n times
                    Double[] pos = new Double[k];
                    for (int j = 0; j < k; j++) { // randomize the position array
                        pos[j] = rand.nextDouble();
                    }
                    Stopwatch timer = Stopwatch.createStarted();
                    p.insert(new Position(pos), i);
                    timer.stop();
                    time += timer.elapsed(TimeUnit.MILLISECONDS);
                }

                int c2 = (int) Math.log10(k) - 1;
                dimensions[c2] = k;
                times[c2] = time;
            }

        System.out.println(Arrays.toString(dimensions));
        System.out.println(Arrays.toString(times));
        XYChart chart = QuickChart.getChart("Times for N = " + N, "dimension",
                                          "time (ms)", "f",
                                                dimensions, times);

        new SwingWrapper<>(chart).displayChart();
        sleep(50000);


    }

    @Test
    void nodeTest() throws InterruptedException {
        int N = 1000000; // max number of nodes to insert into each dimension
        int step = N / 10;
        int K = 2; // dimension of nodes
        int c = N / step;
        double[] nodes = new double[c];
        double[] times = new double[c];
        Random rand = new Random();



        for (int n = 0; n < N; n += step) { //loop through nodes 10, ... , K
            PSPTree<Integer> p = new PSPTree<>(new SquaredEuclideanMetric(), K);
            long time = 0;
            for (int i = 0; i < N; i++) { //for each n, add one node n times
                Double[] pos = new Double[K];
                for (int j = 0; j < K; j++) { // randomize the position array
                    pos[j] = rand.nextDouble();
                }
                Stopwatch timer = Stopwatch.createStarted();
                p.insert(new Position(pos), i);
                timer.stop();
                time += timer.elapsed(TimeUnit.MILLISECONDS);
            }

            int c2 = n / step;
            nodes[c2] = n;
            times[c2] = time;
        }

        System.out.println(Arrays.toString(nodes));
        System.out.println(Arrays.toString(times));
        XYChart chart = QuickChart.getChart("Times for K = " + K, "number of nodes",
                "time (ms)", "f",
                nodes, times);

        new SwingWrapper<>(chart).displayChart();
        sleep(50000);
    }
}
