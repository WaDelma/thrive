package thrive;

import java.util.Iterator;

public class Nulls<T> implements Iterator<T> {
    private int i;
    Nulls(int i) {
        this.i = i;
    }

    @Override
    public boolean hasNext() {
        return i > 0;
    }

    @Override
    public T next() {
        i -= 1;
        return null;
    }
}
