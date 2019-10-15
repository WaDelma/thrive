package thrive;

import kotlin.Pair;
import scala.collection.immutable.Vector;
import scala.collection.immutable.VectorIterator;

import java.util.Iterator;

public class ScalaRrbMap<T> {
    private Vector<T> tree;

    public ScalaRrbMap() { tree = Vector.empty(); }

    private ScalaRrbMap(Vector<T> tree) { this.tree = tree; }

    public ScalaRrbMap<T> insert(int key, T value) {
        if (key == tree.size()) {
            return new ScalaRrbMap<>(tree.appendBack(value));
        }
        if (key < tree.size()) {
            return new ScalaRrbMap<>(tree.updateAt(key, value));
        }
        var tree2 = tree;
        for (int i = 0; i < key - tree.size(); i++) {
            tree2 = tree2.appendBack(null);
        }
        return new ScalaRrbMap<>(tree.appendBack(value));
    }

    public T get(int key) { return tree.apply(key); }

    public Iterator<Pair<Integer, T>> entries() { return new Iter(tree.iterator()); }

    private class Iter implements Iterator<Pair<Integer, T>> {
        private int index = 0;
        private VectorIterator<T> iter;

        private Iter(VectorIterator<T> iter) {
            this.iter = iter;
        }

        @Override
        public boolean hasNext() {
            return iter.hasNext();
        }

        @Override
        public Pair<Integer, T> next() {
            var i = index;
            index++;
            return new Pair<>(i, iter.next());
        }
    }
}
