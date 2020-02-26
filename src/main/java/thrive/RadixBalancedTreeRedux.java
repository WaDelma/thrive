package thrive;

import kotlin.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RadixBalancedTreeRedux<T> implements IntMap<T> {
    private Node root;
    private int height;
    private int capacity;
    private int size;

    private RadixBalancedTreeRedux(RadixBalancedTreeRedux<T> tree){
        root = tree.root;
        height = tree.height;
        size = tree.size;
        capacity = tree.capacity;
    }
    public RadixBalancedTreeRedux(){
        root = new RadixNode(new Object[1]);
    }

    @NotNull
    @Override
    public RadixBalancedTreeRedux<T> insert(int key, T value) {
        var cur = new RadixBalancedTreeRedux<>(this);
        for (int i = capacity; i <= key; i++) {
            cur = cur.append(null);
        }
        var res = cur.root.updated(key, value, cur.height);
        if (res.component2()) {
            cur.size += 1;
        }
        cur.root = res.component1();
        return cur;
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public T get(int key) {
        return (T) root.get(key, height);
    }

    private RadixBalancedTreeRedux<T> append(Object elem) {
        var tree = new RadixBalancedTreeRedux<>(this);
        if (needNewRoot()) {
            var ch = new Object[2];
            ch[0] = root;
            ch[1] = newBranch(elem, height);
            tree.height += 1;
            tree.capacity += 1;
            tree.root = new RadixNode(ch);
            return tree;
        }
        tree.root = root.appended(elem, height, capacity);
        tree.capacity += 1;
        return tree;
    }

    private boolean needNewRoot() {
        return capacity == 1 << (BITS * (height + 1) + 1);
    }

    private Node newBranch(Object elem, int level) {
        var newNode = new RadixNode(new Object[1]);
        if (level == 0) {
            newNode.children[0] = elem;
        } else {
            newNode.children[0] = newBranch(elem, level - 1);
        }
        return newNode;
    }

    @Override
    public void debug() {
        if (root != null) {
            root.debug(0);
        } else {
            System.out.println("null");
        }
    }

    @NotNull
    @Override
    public Iterator<Pair<Integer, T>> entries() {
        var stack = new ArrayList<>();
        stack.add(root);
        return new RrbIterator(stack);
    }

    class RrbIterator implements Iterator<Pair<Integer, T>> {
        private int returned = 0;
        private int index = 0;
        private ArrayList<Object> stack;

        private RrbIterator(ArrayList<Object> stack) {
            this.stack = stack;
        }

        @Override
        public boolean hasNext() {
            return returned < size;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Pair<Integer, T> next() {
            while (true) {
                var node = stack.remove(stack.size() - 1);
                if (node instanceof RadixNode) {
                    Object[] children = ((RadixNode) node).children;
                    for (int i = children.length - 1; i >= 0; i--) {
                        stack.add(children[i]);
                    }
                } else if (node != null) {
                    returned += 1;
                    var idx = index;
                    index += 1;
                    return new Pair<>(idx, (T) node);
                } else {
                    index += 1;
                }
            }
        }
    }

    private static int FACTOR = 32;
    private static int BITS = (int)(Math.log(FACTOR) / Math.log(2));
    private static int MASK = (1 << BITS) - 1;
    private static int BOTTOM_MASK = (1 << (BITS + 1)) - 1;

    interface Node {
        Object get(int key, int level);
        Pair<Node, Boolean> updated(int key, Object value, int level);
        Node appended(Object elem, int level, int end);
        boolean isEmpty();
        void debug(int depth);
    }
    static class RadixNode implements Node {
        Object[] children;

        RadixNode(Object[] children) {
            this.children = children;
        }

        @Override
        public Object get(int key, int level) {
            if (level == 0) {
                var idx = key & BOTTOM_MASK;
                if (idx >= children.length) {
                    return null;
                }
                return children[idx];
            }
            var idx = (key >> (level * BITS + 1)) & MASK;
            if (idx >= children.length || children[idx] == null) {
                return null;
            }
            return ((Node) children[idx]).get(key, level - 1);
        }

        @Override
        public Pair<Node, Boolean> updated(int key, Object value, int level) {
            var arr = Arrays.copyOf(children, children.length);
            boolean added;
            if (level == 0) {
                var idx = key & BOTTOM_MASK;
                added = arr[idx] == null;
                arr[idx] = value;
            } else {
                var idx = (key >> (level * BITS + 1)) & MASK;
                if (children[idx] == null) {
                    added = true;
                    var newNode = new RadixNode(new Object[FACTOR]);
                    arr[idx] = newNode.updated(key, value, level - 1).component1();
                } else {
                    var res = ((Node) children[idx]).updated(key, value, level - 1);
                    added = res.component2();
                    arr[idx] = res.component1();
                }
            }
            return new Pair<>(
                new RadixNode(arr),
                added
            );
        }

        public Node appended(Object elem, int level, int end) {
            if (level == 0) {
                return copyAndUpdate(end & BOTTOM_MASK, elem);
            } else {
                var indexInNode = (end >> (level * BITS + 1)) & MASK;
                if (indexInNode >= children.length || children[indexInNode] == null) {
                    var newNode = new RadixNode(new Object[1]);
                    newNode.children[0] = elem;
                    return copyAndUpdate(
                        indexInNode,
                        newNode
                    );
                } else {
                    return copyAndUpdate(
                        indexInNode,
                        ((Node) children[indexInNode]).appended(elem, level - 1, end)
                    );
                }
            }
        }

        private Node copyAndUpdate(int indexInNode, Object elem) {
            var arr = Arrays.copyOf(children, indexInNode + 1);
            arr[indexInNode] = elem;
            return new RadixNode(arr);
        }

        @Override
        public boolean isEmpty() {
            return Arrays.stream(children).allMatch(o -> {
                if (o instanceof Node) {
                    return ((Node)o).isEmpty();
                } else {
                    return o == null;
                }
            });
        }

        @Override
        public void debug(int level) {
            var pad = Stream.generate(() -> "  ").limit(level).collect(Collectors.joining());
            if (children[0] instanceof Node) {
                if (Arrays.stream(children).filter(Objects::nonNull).allMatch(o -> ((Node)o).isEmpty())) {
                    System.out.println(pad + "Rnull(" + (FACTOR - level) + ")");
                } else {
                    System.out.println(pad + "R(");
                    var empty = 0;
                    for (Object child : children) {
                        if (child instanceof Node) {
                            if (((Node) child).isEmpty()) {
                                empty += 1;
                            } else {
                                if (empty > 0) {
                                    System.out.println(pad + "  Rnull(" + empty + "x" + (FACTOR - level) + ")");
                                }
                                empty = 0;
                                ((Node) child).debug(level + 1);
                            }
                        } else {
                            System.out.println(pad + "  null");
                        }
                    }
                    if (empty > 0) {
                        System.out.println(pad + "  Rnull(" + empty + "x" + (FACTOR - level) + ")");
                    }
                    System.out.println(pad + ")");
                }
            } else if (Arrays.stream(children).anyMatch(Objects::nonNull)) {
                System.out.print(pad + "R[");
                var empty = 0;
                var fst = true;
                for(var child: children) {
                    if (child == null) {
                        empty += 1;
                    } else {
                        if (empty > 0) {
                            System.out.print((fst ? "" : ", ") + "nullx" + empty);
                            fst = false;
                        }
                        empty = 0;
                        System.out.print((fst ? "" : ", ") + child);
                        fst = false;
                    }
                }
                if (empty > 0) {
                    System.out.print((fst ? "" : ", ") + "nullx" + empty);
                }
                System.out.println("]");
            } else {
                System.out.println(pad + "Rnull(" + (FACTOR - level) + ")");
            }
        }

    }
}
