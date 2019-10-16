package thrive;

import kotlin.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Iterator;

public class ArrayMap<T> implements Cloneable, IntMap<T>  {
    private Object[] array;
    private int size;

    public ArrayMap() {
        array = new Object[0];
    }

    @Override
    @NotNull
    public ArrayMap<T> insert(int key, T value) {
        if (key >= array.length) {
            var tmp = new Object[Math.max(array.length == 0 ? 64 : 2 * array.length, key + 1)];
            System.arraycopy(array, 0, tmp, 0, array.length);
            array = tmp;
        }
        if (array[key] == null) {
            size += 1;
        }
        array[key] = value;
        return this;
    }

    public int size() {
        return size;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T get(int key) {
        if (key < array.length) {
            return (T) array[key];
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public Object clone() throws CloneNotSupportedException {
        ArrayMap<T> clone = (ArrayMap<T>) super.clone();
        clone.array = Arrays.copyOf(this.array, this.array.length);
        clone.size = this.size;
        return clone;
    }

    @Override
    @NotNull
    public Iterator<Pair<Integer, T>> entries() {
        return new Iter();
    }

    @Override
    public void debug() {

    }

    private class Iter implements Iterator<Pair<Integer, T>> {
        private int index = 0;
        private int produced = 0;

        @Override
        public boolean hasNext() {
            return produced < size;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Pair<Integer, T> next() {
            produced += 1;
            while (true) {
                var i = index;
                index += 1;
                if (array[i] != null) {
                    return new Pair<>(i, (T)array[i]);
                }
            }
        }
    }
}
