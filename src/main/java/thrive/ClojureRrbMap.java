package thrive;

import clojure.lang.IPersistentVector;
import clojure.lang.ISeq;
import kotlin.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public class ClojureRrbMap<T> implements IntMap<T> {
    private IPersistentVector tree;
    private int size = 0;

    public ClojureRrbMap() { tree = (IPersistentVector) clojure.java.api.Clojure.var("clojure.core", "vector").invoke(); }

    private ClojureRrbMap(IPersistentVector tree, int size) { this.tree = tree; this.size = size; }

    @Override
    @NotNull
    public ClojureRrbMap<T> insert(int key, T value) {
        if (key == tree.length()) {
            return new ClojureRrbMap<>(tree.cons(value), size + 1);
        }
        if (key < tree.length()) {
            return new ClojureRrbMap<>(tree.assocN(key, value), tree.nth(key) == null ? size + 1: size);
        }
        var tree2 = tree;
        for (int i = 0; i < key - tree.length(); i++) {
            tree2 = tree2.cons(null);
        }
        return new ClojureRrbMap<>(tree2.cons(value), size + 1);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T get(int key) { return (T) tree.nth(key, null); }

    @Override
    @NotNull
    public Iterator<Pair<Integer, T>> entries() { return new Iter(tree.seq(), size); }

    @Override
    public void debug() {

    }

    private class Iter implements Iterator<Pair<Integer, T>> {
        private int index = 0;
        private int found = 0;
        private int length;
        private ISeq iter;

        private Iter(ISeq iter, int length) {
            this.iter = iter;
            this.length = length;
        }

        @Override
        public boolean hasNext() {
            return found < length;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Pair<Integer, T> next() {
            Object result;
            int i;
            do {
                result = iter.first();
                iter = iter.next();
                i = index;
                index += 1;
            } while(result == null);
            found += 1;
            return new Pair<>(i, (T) result);
        }
    }
}
