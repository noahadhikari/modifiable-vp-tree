public class Pair<A, B> {
    public A first;
    public B last;

    public Pair(A first, B last) {
        this.first = first;
        this.last = last;
    }

    @Override
    public String toString() {
        return "(" + first + ", " + last + ")";
    }


}
