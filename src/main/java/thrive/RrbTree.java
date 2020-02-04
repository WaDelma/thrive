package thrive;

import kotlin.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Iterator;

public class RrbTree<T> implements IntMap<T> {
    Node root;
    int height;

    @NotNull
    @Override
    public IntMap<T> insert(int key, T value) {
        return null;
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public T get(int key) {
        return (T) root.get(key, height);
    }

    @Override
    public void debug() {}

    @NotNull
    @Override
    public Iterator<Pair<Integer, T>> entries() {
        return null;
    }

    private static int BITS = 5;
    private static int MASK = (1 << BITS) - 1;

    interface Node {
        Object get(int key, int level);
        Node updated(int key, Object value, int level);
    }
    static class RadixNode implements Node {
        Object[] children;

        RadixNode(Object[] children) {
            this.children = children;
        }

        @Override
        public Object get(int key, int level) {
            if (level == 0) {
                return children[key & level];
            }
            var idx = (key >> (level * BITS)) & MASK;
            return ((Node) children[idx]).get(key, level - 1);
        }

        @Override
        public Node updated(int key, Object value, int level) {
            var idx = (key >> (level * BITS)) & MASK;
            var arr = Arrays.copyOf(children, children.length);
            if (level == 0) {
                arr[idx] = value;
            } else {
                arr[idx] = ((Node) children[idx]).updated(key, value, level - 1);
            }
            return new RadixNode(arr);
        }
    }
    // TODO: transience for inserts
}
