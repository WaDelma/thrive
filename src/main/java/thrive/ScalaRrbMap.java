package thrive;

import kotlin.Pair;
import org.jetbrains.annotations.NotNull;
import scala.collection.immutable.Vector;
import scala.collection.immutable.Vector$;
import scala.collection.immutable.VectorIterator;

import java.util.Iterator;

public class ScalaRrbMap<T> implements IntMap<T> {
    private Vector<T> tree;
    private int size = 0;

    public ScalaRrbMap() { tree = Vector$.MODULE$.empty(); }

    private ScalaRrbMap(Vector<T> tree, int size) { this.tree = tree; this.size = size; }

    @Override
    @NotNull
    public ScalaRrbMap<T> insert(int key, T value) {
        if (key == tree.size()) {
            return new ScalaRrbMap<>(tree.appendBack(value), size + 1);
        }
        if (key < tree.size()) {
            return new ScalaRrbMap<>(tree.updateAt(key, value), tree.apply(key) == null ? size + 1: size);
        }
        var tree2 = tree;
        for (int i = 0; i < key - tree.size(); i++) {
            tree2 = tree2.appendBack(null);
        }
        return new ScalaRrbMap<>(tree2.appendBack(value), size + 1);
    }

    @Override
    public T get(int key) {
        if (key < tree.length()) {
            return tree.apply(key);
        } else {
            return null;
        }
    }

    @Override
    public void debug() {

    }

    @Override
    @NotNull
    public Iterator<Pair<Integer, T>> entries() { return new Iter(tree.iterator(), size); }

    private class Iter implements Iterator<Pair<Integer, T>> {
        private int index = 0;
        private int found = 0;
        private int length;
        private VectorIterator<T> iter;

        private Iter(VectorIterator<T> iter, int length) {
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
                index++;
            } while(result == null);
            found++;
            return new Pair<>(i, result);
        }
    }
}
