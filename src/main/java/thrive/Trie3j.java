package thrive;

import kotlin.NotImplementedError;
import kotlin.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.BaseStream;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static thrive.TrieUtils.*;

class Trie3j<T> implements Trie<T> {
    private static final int BITS = 5;

    private Node3j<T> root;

    public Trie3j() {
        root = null;
    }

    private Trie3j(Node3j<T> node) {
        this.root = node;
    }

    @NotNull
    @Override
    public Trie3j<T> insert(int key, T value) {
        if (root == null) {
            return new Trie3j<>(new Trunk3<T>(0, new Object[] {}).insert(key, value, 0));
        }
        return new Trie3j<>(root.insert(key, value, 0));
    }

    @Override
    public T get(int key) {
        if (root != null) {
            return root.get(key, 0);
        }
        return null;
    }

    @Override
    public void debug() {
        if (root == null) {
            System.out.println("null");
        } else {
            root.debug(0);
        }
    }

    class StackElem<T> {
        Node3j<T> node;
        int lowerbit;
        int level;
        StackElem(Node3j<T> node) {
            this.node = node;
            lowerbit = 0;
            level = 0;
        }
        StackElem(Node3j<T> node, int lowerbit, int level) {
            this.node = node;
            this.lowerbit = lowerbit;
            this.level = level;
        }
    }

    @NotNull
    @Override
    public Iterator<Pair<Integer, T>> entries() {
        var stack = new ArrayList<StackElem<T>>();
        if (root != null) {
            stack.add(new StackElem<>(root));
        }
        return new Trie3jIterator(stack);
    }

    private class Trie3jIterator implements Iterator<Pair<Integer, T>> {
        private ArrayList<StackElem<T>> stack;
        private int index;

        Trie3jIterator(ArrayList<StackElem<T>> stack) {
            this.stack = stack;
        }

        @Override
        public Pair<Integer, T> next() {
            while (true) {
                var s = stack.remove(stack.size() - 1);
                var node = s.node;
                var i = s.lowerbit;
                var level = s.level;
                if (node instanceof Trunk3) {
                    AtomicInteger ii = new AtomicInteger(0);
                    stack.addAll(
                        IntStream.range(0, 32)
                            .filter((it) -> ((((Trunk3<T>) node).map >>> it) & 1) == 1)
                            .boxed()
                            .map((j) -> {
                                var index = ii.getAndAdd(1);
                                return new StackElem<>(
                                    (Node3j<T>) ((Trunk3<T>) node).children[index],
                                    (j << (level * BITS)) | i,
                                    level + 1
                                );
                            })
                            .collect(Collectors.toList())
                    );
                } else if (node instanceof Leaf3) {
                    if (index < ((Leaf3<T>) node).values.length) {
                        var result = new Pair<>((index << (level * BITS)) | i, ((T) ((Leaf3<T>) node).values[index]));
                        stack.add(new StackElem<>(node, i, level));
                        index += 1;
                        return result;
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
                case 1: {
                    var node = stack.get(stack.size() - 1).node;
                    if (node instanceof Leaf3) {
                        return index < ((Leaf3<T>) node).values.length;
                    } else {
                        return true;
                    }
                }
                default: return true;
            }
        }
    }
    interface Node3j<T> {
        void debug(int level);
        Node3j<T> insert(int key, T value, int level);
        T get(int key, int level);
    }

    private static class Trunk3<T> implements Node3j<T> {
        private int map;
        private Object[] children;

        Trunk3() {
            children = new Object[] {};
        }
        Trunk3(int map, Object[] children) {
            this.map = map;
            this.children = children;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void debug(int level) {
            var pad = Stream.generate(() -> "").limit(level).collect(Collectors.joining());
            System.out.println(pad + "Trunk(map=" + Integer.toBinaryString(map));
            for (Object child : children) {
                ((Node3j<T>)child).debug(level + 1);
            }
            System.out.println(pad + ")");
        }
        @Override
        @SuppressWarnings("unchecked")
        public Node3j<T> insert(int key, T value, int level) {
            if (level > 32 / BITS) {
                return new Leaf3<T>().insert(key, value, level);
            }
            var bit = mask(key, BITS * level, BITS);
            var pos = 1 << bit;
            var index = index(this.map, pos);
            if (((map >>> bit) & 1) == 1) {
                var childs = children.clone();
                childs[index] = ((Node3j<T>)childs[index]).insert(key, value, level + 1);
                return new Trunk3<>(map, childs);
            }
            var childs = new Object[children.length + 1];
            copyInto(children, childs, 0, 0, index);
            copyInto(children, childs, index + 1, index, children.length);
            childs[index] = new Trunk3<T>().insert(key, value, level + 1);
            return new Trunk3<>(map | pos, childs);
        }
        @Override
        @SuppressWarnings("unchecked")
        public T get(int key, int level) {
            var bit = mask(key, BITS * level, BITS);
            var pos = 1 << bit;
            if (((map >>> bit) & 1) == 1) {
                var index = index(this.map, pos);
                return ((Node3j<T>)children[index]).get(key, level + 1);
            }
            return null;
        }
    }

    private static class Leaf3<T> implements Node3j<T> {
        private int map;
        private Object[] values;

        Leaf3() {
            values = new Object[] {};
        }

        Leaf3(int map, Object[] values) {
            this.map = map;
            this.values = values;
        }

        @Override
        public void debug(int level) {
            var pad = Stream.generate(() -> "").limit(level).collect(Collectors.joining());
            System.out.println(pad + "Leaf(map=" + Integer.toBinaryString(map));
            System.out.println(pad + " " + Arrays.toString(values));
            System.out.println(pad + ")");
        }
        @Override
        public Node3j<T> insert(int key, T value, int level) {
            var bit = mask(key, BITS * level, BITS);
            var pos = 1 << bit;
            var index = index(this.map, pos);
            if (((map >>> bit) & 1) == 1) {
                var vals = values.clone();
                vals[index] = value;
                return new Leaf3<>(map, vals);
            }
            var vals = new Object[values.length + 1];
            copyInto(values, vals, 0, 0, index);
            copyInto(values, vals, index + 1, index, values.length);
            vals[index] = value;
            return new Leaf3<>(map | pos, vals);
        }
        @Override
        @SuppressWarnings("unchecked")
        public T get(int key, int level) {
            var bit = mask(key, BITS * level, BITS);
            var pos = 1 << bit;
            if (((map >>> bit) & 1) == 1) {
                var index = index(this.map, pos);
                return (T) values[index];
            }
            return null;
        }
    }
}
