package thrive;

import kotlin.Pair;
import org.jetbrains.annotations.NotNull;
import org.organicdesign.fp.collections.RrbTree;

import java.util.Iterator;

public class RrbMap<T> implements IntMap<T> {
    private RrbTree.ImRrbt<T> tree;
    private int size = 0;

    public RrbMap() { tree = RrbTree.empty(); }

    private RrbMap(RrbTree.ImRrbt<T> tree, int size) { this.tree = tree; this.size = size; }

    @Override
    @NotNull
    public RrbMap<T> insert(int key, T value) {
        if (key == tree.size()) {
            return new RrbMap<>(tree.append(value), size + 1);
        }
        if (key < tree.size()) {
            return new RrbMap<>(tree.replace(key, value), tree.get(key) == null ? size + 1: size);
        }
        var tree2 = tree.mutable()
                .concat(() -> new Nulls<>(key - tree.size()))
                .append(value)
                .immutable();
        return new RrbMap<>(tree2, size + 1);
    }

    @Override
    public T get(int key) { return tree.get(key, null); }

    @Override
    @NotNull
    public Iterator<Pair<Integer, T>> entries() { return new Iter(tree.iterator(), size); }

    @Override
    public void debug() {

    }

    private class Iter implements Iterator<Pair<Integer, T>> {
        private int index = 0;
        private int found = 0;
        private int length;
        private Iterator<T> iter;

        private Iter(Iterator<T> iter, int length) {
            this.iter = iter;
            this.length = length;
        }

        @Override
        public boolean hasNext() {
            return found < length;
        }

        @Override
        public Pair<Integer, T> next() {
            T result;
            int i;
            do {
                result = iter.next();
                i = index;
                index += 1;
            } while(result == null);
            found += 1;
            return new Pair<>(i, result);
        }
    }

}
