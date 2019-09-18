package thrive;

import kotlin.NotImplementedError;
import kotlin.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;
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
    public Trie<T> insert(int key, T value) {
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

    @NotNull
    @Override
    public Iterator<Pair<Integer, T>> entries() {
        throw new NotImplementedError();
    }


    //    internal data class StackElem<T>(var node: Node3j<T>, var lowerbit: Int, var level: Int)
//
//    override fun entries() = Trie3jIterator(ArrayList<StackElem<T>>().also { list -> root?.let { list.add(StackElem(it, 0, 0)) } }, 0)
//
//    inner class Trie3jIterator internal constructor (private var stack: ArrayList<StackElem<T>>, private var index: Int): Iterator<Pair<Int, T>> {
//        override fun next(): Pair<Int, T> {
//            while (true) {
//                var (node, i, level) = stack.pop()
//                when (node) {
//                    is Trunk3 -> stack.addAll(
//                        (0 until 32)
//                            .filter { (node.map ushr it) and 1 == 1 }
//                            .mapIndexed { index, j -> StackElem(
//                                node.children[index],
//                                (j shl (level * BITS)) or i,
//                                level + 1
//                            ) }
//                    )
//                    is Leaf3 -> {
//                        if (index < node.values.size) {
//                            return ((index shl (level * BITS)) or i) to (node.values[index] as T).also {
//                                stack.add(StackElem(node, i, level))
//                                index += 1
//                            }
//                        } else {
//                            index = 0
//                        }
//                    }
//                }
//            }
//        }
//
//        override fun hasNext(): Boolean = when (stack.size) {
//            0 -> false
//            1 -> when (var node = stack.peek().node) {
//                is Leaf3 -> index < node.values.size
//                else -> true
//            }
//            else -> true
//        }
//    }
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
        public void debug(int level) {
            var pad = Stream.generate(() -> "").limit(level).collect(Collectors.joining());
            System.out.println(pad + "Trunk(map=" + Integer.toBinaryString(map));
            for (Object child : children) {
                ((Node3j<T>)child).debug(level + 1);
            }
            System.out.println(pad + ")");
        }
        @Override
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
