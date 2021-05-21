package metrics;

public class ManhattanMetric implements DistanceMetric {
    @Override
    public double distBetween(double[] pos1, double[] pos2) {
        assert pos1.length == pos2.length : "points must be of equal dimension";
        double dist = 0;
        for (int i = 0; i < pos1.length; i++) {
            dist += Math.abs(pos1[i] - pos2[i]);
        }
        return dist;
    }
}
