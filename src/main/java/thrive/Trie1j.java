package thrive;

import kotlin.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public final class Trie1j<T> implements Trie<T> {
    private static final int BITS = 5;

    private Node<T> root;

    public Trie1j() {
        this.root = null;
    }

    private Trie1j(Node<T> node) {
        this.root = node;
    }

    @NotNull
    @Override
    public Trie1j<T> insert(int key, T value) {
        var pos = 1 << mask(key, 0);
        if (root == null) {
            return new Trie1j<>(new Node<>(0, pos, new Object[]{key, value}));
        }
        return new Trie1j<>(root.insert(key, value, 0));
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

    public Iterator<Pair<Integer, T>> entries() {
        var list = new ArrayList<Node<T>>();
        if (root != null) {
            list.add(root);
        }
        return new Trie1jIterator(list);
    }

    private class Trie1jIterator implements Iterator<Pair<Integer, T>> {
        private ArrayList<Node<T>> stack;
        private int index;
        private boolean first = true;
        private Trie1jIterator(ArrayList<Node<T>> stack) {
            this.stack = stack;
        }

        @Override
        public Pair<Integer, T> next() {
            first = false;
            while (true) {
                var node = stack.remove(stack.size() - 1);
                var datum = Integer.bitCount(node.dataMap);
                if (index == 0) {
                    var nodes = Integer.bitCount(node.nodeMap);
                    for (var i = 0; i < nodes; i++) {
                        stack.add((Node<T>) node.values[node.values.length - 1 - i]);
                    }
                }
                if (index < datum) {
                    stack.add(node);
                    var res = new Pair<>((Integer) node.values[2 * index], (T) node.values[2 * index + 1]);
                    index += 1;
                    return res;
                } else {
                    index = 0;
                }
            }
        }

        @Override
        public boolean hasNext() {
            switch (stack.size()) {
                case 0: return false;
                case 1: return first || index < Integer.bitCount(stack.get(stack.size() - 1).dataMap);
                default: return true;
            }
        }
    }

    private static int mask(int index, int shift) {
        return (index >>> shift) & ((1 << BITS) - 1);
    }

    private static int index(int bitmap, int pos) {
        return Integer.bitCount(bitmap & (pos - 1));
    }

    private static void copyInto(Object[] array, Object[] destination, int destinationOffset, int startIndex, int endIndex) {
        System.arraycopy(array, startIndex, destination, destinationOffset, endIndex - startIndex);
    }

    private static class Node<T> {
        private int nodeMap;
        private int dataMap;
        private Object[] values;

        private Node(int nodeMap, int dataMap, Object[] values) {
            this.nodeMap = nodeMap;
            this.dataMap = dataMap;
            this.values = values;
        }

        private void debug(int level) {
            var pad = Stream.generate(() -> "").limit(level).collect(Collectors.joining());
            System.out.println(pad + "Node(nodeMap=" + Integer.toBinaryString(nodeMap) + ", dataMap=" + Integer.toBinaryString(dataMap));
            for (int i = 0; i < values.length; i += 2) {
                var fst = values[i];
                var flag = i + 1 < values.length;
                if (fst instanceof Node<?>) {
                    ((Node<?>) fst).debug(level + 1);
                    if (flag) {
                        var snd = values[i + 1];
                        if (snd instanceof Node<?>) {
                            ((Node<?>) snd).debug(level + 1);
                        } else {
                            System.out.println(pad + " wtf $snd");
                        }
                    }
                } else if (flag) {
                    System.out.println(pad + " " + i + ". " + fst + ": " + values[i + 1]);
                } else  {
                    System.out.println(pad + " wtf2 " + fst);
                }
            }
            System.out.println(pad + ")");
        }

        @SuppressWarnings("unchecked")
        private Node<T> insert(int key, T value, int level) {
            var bit = mask(key, BITS * level);
            var pos = 1 << bit;
            if (((dataMap >>> bit) & 1) == 1) {
                // There exists key-value pair in the place we would go in the internal storage
                var index = 2 * index(this.dataMap, pos);
                if (key == (int) values[index]) {
                    // They had the same key, so we are going to replace the value
                    // TODO: Would it be better to not copy the old value? (arrayOfNulls)
                    var vals = values.clone();
                    vals[index + 1] = value;
                    return new Node<>(nodeMap, dataMap, vals);
                }
                // They had different key so we have to be in a children node instead
                // The key-value pair already in the array takes twice the space so the new array can be one smaller
                var vals = new Object[values.length - 1];
                // Copy datum without key-value pair
                copyInto(values, vals, 0, 0, index);
                var datum = Integer.bitCount(dataMap);
                copyInto(values, vals, index, index + 2, datum * 2);
                // Copy children nodes
                var nodeIndex = this.values.length - 1 - index(this.nodeMap | pos, pos);
                copyInto(values, vals, nodeIndex, nodeIndex + 1, values.length);
                var nodes = Integer.bitCount(nodeMap);
                copyInto(values, vals, vals.length - nodes - 1, values.length - nodes, nodeIndex + 1);
                // Add node that contains both key-value pairs
                vals[nodeIndex - 1] = new Node<T>(
                        0,
                        1 << mask((int) values[index], BITS * (level + 1)),
                        new Object[] {values[index], values[index + 1]}
                ).insert(key, value, level + 1);
                return new Node<>(nodeMap | pos, dataMap & ~pos, vals);
            } else if (((nodeMap >>> bit) & 1) == 1) {
                // There exists child node that we have to insert into
                var index = this.values.length - 1 - index(this.nodeMap, pos);
                var vals = values.clone();
                vals[index] = ((Node<T>) values[index]).insert(key, value, level + 1);
                return new Node<>(nodeMap, dataMap, vals);
            }
            // Now we can just save us into the internal storage
            var index = 2 * index(this.dataMap, pos);
            // Our key-value will take two more slots
            var vals = new Object[values.length + 2];
            // Copy slots before us
            copyInto(values, vals, 0, 0, index);
            // Copy slots after us
            copyInto(values, vals, index + 2, index, values.length);
            // Insert our key-value pair
            vals[index] = key;
            vals[index + 1] = value;
            return new Node<>(nodeMap, dataMap | pos, vals);
        }

        @SuppressWarnings("unchecked")
        private T get(int key, int level) {
            var bit = mask(key, BITS * level);
            var pos = 1 << bit;
            if (((dataMap >>> bit) & 1) == 1) {
                var index = 2 * index(this.dataMap, pos);
                if (key == (int) values[index]) {
                    return (T) values[index + 1];
                }
                return null;
            } else if (((nodeMap >>> bit) & 1) == 1) {
                var index = this.values.length - 1 - index(this.nodeMap, pos);
                return ((Node<T>) values[index]).get(key, level + 1);
            }
            return null;
        }
    }
}