package thrive;

import kotlin.Pair;
import org.organicdesign.fp.collections.RrbTree;

import java.util.Iterator;

public class RrbMap<T> {
    private RrbTree<T> tree;

    public RrbMap() { tree = RrbTree.empty(); }

    private RrbMap(RrbTree<T> tree) { this.tree = tree; }

    public RrbMap<T> insert(int key, T value) {
        if (key == tree.size()) {
            return new RrbMap<>(tree.append(value));
        }
        if (key < tree.size()) {
            return new RrbMap<>(tree.replace(key, value));
        }
        var tree2 = tree;
        for (int i = 0; i < key - tree.size(); i++) {
            tree2 = tree2.append(null);
        }
        return new RrbMap<>(tree.append(value));
    }

    public T get(int key) { return tree.get(key, null); }

    public Iterator<Pair<Integer, T>> entries() { return new Iter(tree.iterator()); }

    private class Iter implements Iterator<Pair<Integer, T>> {
        private int index = 0;
        private Iterator<T> iter;

        private Iter(Iterator<T> iter) {
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
