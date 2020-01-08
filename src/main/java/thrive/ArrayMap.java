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

    private ArrayMap(Object[] array, int size) {
        this.array = array;
        this.size = size;
    }

    @Override
    @NotNull
    public ArrayMap<T> insert(int key, T value) {
        Object[] newArray;
        if (key >= array.length) {
            var tmp = new Object[Math.max(array.length == 0 ? 64 : 2 * array.length, key + 1)];
            System.arraycopy(array, 0, tmp, 0, array.length);
            newArray = tmp;
        } else {
            newArray = array.clone();
        }
        var newSize = size;
        if (newArray[key] == null) {
            newSize += 1;
        }
        newArray[key] = value;
        return new ArrayMap<>(newArray, newSize);
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
