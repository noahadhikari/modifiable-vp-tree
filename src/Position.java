import metrics.DistanceMetric;

import java.util.Arrays;

public class Position {
    double[] pos;
    Position(double... p) {
        pos = p;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        @SuppressWarnings("unchecked")
        Position position = (Position) o;
        return Arrays.equals(pos, position.pos);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(pos);
    }

    public double distTo(Position other, DistanceMetric d) {
        return d.distBetween(this.pos, other.pos);
    }

    public String toString() {
        return Arrays.toString(pos);
    }
}
