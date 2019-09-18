package thrive;

import kotlin.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static thrive.TrieUtils.index;
import static thrive.TrieUtils.mask;

public final class Trie2j<T> implements Trie<T> {
    private static final int BITS = 5;

    private Node<T> root;

    public Trie2j() {
        root = null;
    }

    private Trie2j(Node<T> root) {
        this.root = root;
    }

    @NotNull
    @Override
    public Trie2j<T> insert(int key, T value) {
        var pos = 1 << mask(key, 0, BITS);
        if (root != null) {
            return new Trie2j<>(root.insert(key, value, 0));
        } else {
            return new Trie2j<>(new Leaf<>(pos, new int[]{key}, new Object[]{value}));
        }
    }

    @Override
    public T get(int key) {
        if (root != null) {
            return root.get(key, 0);
        } else {
            return null;
        }
    }

    @Override
    public void debug() {
        if (root != null) {
            root.debug(0);
        } else {
            System.out.println("null");
        }
    }

    @Override
    public Iterator<Pair<Integer, T>> entries() {
        var list = new ArrayList<>();
        if (root != null) {
            list.add(root);
        }
        return new Trie2jIterator(list);
    }

    class Trie2jIterator implements Iterator<Pair<Integer, T>> {
        private ArrayList<Object> stack;
        private int index;

        private Trie2jIterator(ArrayList<Object> stack) {
            this.stack = stack;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Pair<Integer, T> next() {
            while (true) {
                var node = stack.remove(stack.size() - 1);
                if (node instanceof Trunk) {
                    stack.addAll(Arrays.asList(((Trunk) node).children));
                } else if (node instanceof Leaf) {
                    var n = (Leaf) node;
                    if (index < n.values.length) {
                        var res = new Pair<>(n.keys[index], (T) n.values[index]);
                        stack.add(node);
                        index += 1;
                        return res;
                    } else {
                        index = 0;
                    }
                }
            }
        }

        @Override
        public boolean hasNext() {
            switch (stack.size()) {
                case 0: return false;
                case 1:
                    var node = stack.get(stack.size() - 1);
                    if (node instanceof Leaf) {
                        return index < ((Leaf) node).values.length;
                    }
                    return true;
                default: return true;
            }
        }
    }

    private static void copyInto(Object src, Object dest, int destinationOffset, int startIndex, int endIndex) {
        System.arraycopy(src, startIndex, dest, destinationOffset, endIndex - startIndex);
    }

    private interface Node<T> {
        void debug(int level);
        Node<T> insert(int key, T value, int level);
        T get(int key, int level);
    }

    private static final class Trunk<T> implements Node<T> {
        private int map;
        private Object[] children;

        private Trunk(int map, Object[] children) {
            this.map = map;
            this.children = children;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void debug(int level) {
            var pad = Stream.generate(() -> "").limit(level).collect(Collectors.joining());
            System.out.println(pad + "Trunk(map=" + Integer.toBinaryString(map));
            for (Object child : children) {
                ((Node<T>)child).debug(level + 1);
            }
            System.out.println(pad + ")");
        }

        @NotNull
        @Override
        @SuppressWarnings("unchecked")
        public Trunk<T> insert(int key, T value, int level) {
            var bit = mask(key, BITS * level, BITS);
            var pos = 1 << bit;
            var index = index(this.map, pos);
            if (((map >>> bit) & 1) == 1) {
                // TODO: Would it be better to not copy the old value? (arrayOfNulls)
                var childs = children.clone();
                childs[index] = ((Node<T>) childs[index]).insert(key, value, level + 1);
                return new Trunk<>(map, childs);
            }
            var childs = new Object[children.length + 1];
            copyInto(children, childs, 0, 0, index);
            copyInto(children, childs, index + 1, index, children.length);
            var bit2 = mask(key, BITS * (level + 1), BITS);
            childs[index] = new Leaf<>(1 << bit2, new int[]{key}, new Object[]{value});
            return new Trunk<>(map | pos, childs);
        }
        
        @Override
        @SuppressWarnings("unchecked")
        public T get(int key, int level) {
            var bit = mask(key, BITS * level, BITS);
            var pos = 1 << bit;
            if (((map >>> bit) & 1) == 1) {
                var index = index(this.map, pos);
                return ((Node<T>)children[index]).get(key, level + 1);
            }
            return null;
        }
    }

    private static final class Leaf<T> implements Node<T> {
        private int map;
        private int[] keys;
        Object[] values;

        private Leaf(int map, int[] keys, Object[] values) {
            this.map = map;
            this.keys = keys;
            this.values = values;
        }

        @Override
        public void debug(int level) {
            var pad = Stream.generate(() -> "").limit(level).collect(Collectors.joining());
            System.out.println(pad + "Leaf(map=" + Integer.toBinaryString(map));
            System.out.println(pad + " " + Arrays.toString(keys));
            System.out.println(pad + " " + Arrays.toString(values));
            System.out.println(pad + ")");
        }

        @Override
        @SuppressWarnings("unchecked")
        public Node<T> insert(int key, T value, int level) {
            var bit = mask(key, BITS * level, BITS);
            var pos = 1 << bit;
            var index = index(this.map, pos);
            if (((map >>> bit) & 1) == 1) {
                // There exists value the place we would go in the internal storage
                if (key == keys[index]) {
                    // They had the same key, so we are going to replace the value
                    var vals = values.clone();
                    vals[index] = value;
                    return new Leaf<>(map, keys, vals);
                }

                var children = new Object[keys.length];
                for (int i = 0; i < children.length; i++) {
                    var bit2 = mask(keys[i], BITS * (level + 1), BITS);
                    children[i] = new Leaf<>(1 << bit2, new int[]{keys[i]}, new Object[]{values[i]});
                }
                children[index] = ((Node<T>) children[index]).insert(key, value, level + 1);
                return new Trunk<>(map, children);
            }
            // Now we can just save us into the internal storage
            var kes = new int[keys.length + 1];
            var vals = new Object[values.length + 1];
            // Copy slots before us
            copyInto(keys, kes, 0, 0, index);
            copyInto(values, vals, 0, 0, index);
            // Copy slots after us
            copyInto(keys, kes, index + 1, index, keys.length);
            copyInto(values, vals, index + 1, index, values.length);
            // Insert our key-value pair
            kes[index] = key;
            vals[index] = value;
            return new Leaf<>(map | pos, kes, vals);
        }

        @Override
        @SuppressWarnings("unchecked")
        public T get(int key, int level) {
            var bit = mask(key, BITS * level, BITS);
            var pos = 1 << bit;
            if (((map >>> bit) & 1) == 1) {
                var index = index(this.map, pos);
                if (key == keys[index]) {
                    return (T) values[index];
                }
            }
            return null;
        }
    }
}