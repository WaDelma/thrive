package thrive;

import kotlin.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SdkMap<T> implements IntMap<T> {
    private HashMap<Integer, T> map;

    public SdkMap() {
        map = new HashMap<>();
    }

    private SdkMap(HashMap<Integer, T> map) {
        this.map = map;
    }

    @NotNull
    @Override
    public SdkMap<T> insert(int key, T value) {
        this.map.put(key, value);
        return this;
    }

    @Nullable
    @Override
    public T get(int key) {
        return map.get(key);
    }

    @Override
    public void debug() {
    }

    @NotNull
    @Override
    public Iterator<Pair<Integer, T>> entries() {
        return new Iter(map.entrySet().iterator());
    }

    private class Iter implements Iterator<Pair<Integer, T>> {
        private Iterator<Map.Entry<Integer, T>> iter;

        private Iter(Iterator<Map.Entry<Integer, T>> iter) {
            this.iter = iter;
        }

        @Override
        public boolean hasNext() {
            return iter.hasNext();
        }

        @Override
        public Pair<Integer, T> next() {
            var entry = iter.next();
            return new Pair<>(entry.getKey(), entry.getValue());
        }
    }
}
