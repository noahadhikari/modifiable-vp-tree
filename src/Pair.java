import java.util.Map.Entry;

public class Pair<K, V> implements Entry<K, V> {
    public K first;
    public V last;

    public Pair(K first, V last) {
        this.first = first;
        this.last = last;
    }

    @Override
    public String toString() {
        return first + "=" + last;
    }


    @Override
    public K getKey() {
        return first;
    }

    @Override
    public V getValue() {
        return last;
    }

    @Override
    public V setValue(V value) {
        V temp = this.last;
        this.last = value;
        return temp;
    }
}
