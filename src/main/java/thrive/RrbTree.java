package thrive;

import kotlin.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Iterator;

public class RrbTree<T> implements IntMap<T> {
    private static int FACTOR = 32;
    private static int BITS = (int)(Math.log(FACTOR) / Math.log(2));
    private static int MASK = (1 << BITS) - 1;
    private static int LINEAR_TRESSHOLD = 5;

    private Object[] root;
    private int levels;
    private int size;

    public RrbTree() {
        root = null;
    }

    public RrbTree(RrbTree tree) {
        this.root = tree.root;
        this.size = tree.size;
        this.levels = tree.levels;
    }

    public RrbTree(T value) {
        root = new Object[] { value };
        size = 1;
    }

    public RrbTree(Object[] root, int size, int levels) {
        this.root = root;
        this.size = size;
        this.levels = levels;
    }

    @Nullable
    @Override
    public T get(int key) {
        if (root == null) {
            return null;
        }
        var node = root;
        var idx = key;
        for (int l = levels; l > 0; l--) {
            var last = node.length - 1;
            var i = radixIndex(idx, l);
            if (node[last] instanceof int[]) {
                var sizes = (int[]) node[last];
                var bidx = getIndex(sizes, idx);
                node = cast(node[bidx]);
                idx =  i - sizes[bidx];
            } else if (i < node.length) {
                node = cast(node[i]);
            } else {
                return null;
            }
        }
        return cast(node[idx & MASK]);
    }

    private int radixIndex(int idx, int level) {
        return (idx >> (BITS * level)) & MASK;
    }

    private int getIndex(int[] sizes, int idx) {
        var low = 0;
        var high = sizes.length;
        while (LINEAR_TRESSHOLD < high - low) {
            var mid = (high - low) / 2;
            if (sizes[mid] <= idx) {
                low = mid;
            } else {
                high = mid;
            }
        }
        while (sizes[low] <= idx) {
            low += 1;
        }
        return low;
    }

    @NotNull
    @Override
    public RrbTree<T> insert(int key, T value) {
        if (key == size) {
            return append(value);
        }
        var newTree = new RrbTree<T>(this);
        if (key < size) {
            return newTree.updated(key, value);
        }
        for (int i = size; i < key; i++) {
            newTree = newTree.append(null);
        }
        return newTree.append(value);
    }

    private RrbTree<T> updated(int key, T value) {
        if (root == null) {
            return this;
        }
        var node = updateTree(root, key, value, levels);
        if (node == null) {
            return this;
        }
        return new RrbTree<T>(node, size, levels);
    }

    private Object[] updateTree(Object[] node, int key, T value, int level) {
        if (level == 0) {
            var newNode = Arrays.copyOf(node, node.length);
            newNode[key & MASK] = value;
            return newNode;
        } else {
            var last = node.length - 1;
            if (node[last] instanceof int[]) {
                var sizes = (int[]) node[last];
                var bidx = getIndex(sizes, key);
                return updateNode(node, key, value, level - 1, bidx);
            } else {
                var idx = radixIndex(key, level);
                return updateNode(node, key, value, level - 1, idx);
            }
        }
    }

    @Nullable
    private Object[] updateNode(Object[] node, int key, T value, int level, int idx) {
        var newNode = updateTree(cast(node[idx]), key, value, level);
        if (newNode == null) {
            return null;
        }
        var n = Arrays.copyOf(node, node.length);
        n[idx] = newNode;
        return n;
    }

    public RrbTree<T> append(T value) {
        if (root == null) {
            return new RrbTree<>(value);
        }
        var newNode = appendTree(root, value, levels);
        if (newNode != null) {
            return new RrbTree<>(newNode, size + 1, levels);
        }
        return new RrbTree<>(
                new Object[] {
                        root,
                        single(value, levels)
                },
                size + 1,
                levels + 1
        );
    }

    private Object[] appendTree(Object[] node, T value, int level) {
        if (level == 0) {
            if (node.length < FACTOR) {
                var newNode = Arrays.copyOf(node, node.length + 1);
                newNode[node.length] = value;
                return newNode;
            } else {
                return null;
            }
        } else {
            var last = node.length - 1;
            if (node[last] instanceof int[]) {
                var sizes = (int[]) node[last];
                var newNode = appendTree(cast(node[last - 1]), value, level - 1);
                if (newNode != null) {
                    var n = Arrays.copyOf(node, node.length);
                    n[last - 1] = newNode;
                    sizes[last] += 1;
                    return n;
                }
                if (node.length < FACTOR - 1) {
                    var n = Arrays.copyOf(node, node.length + 1);
                    var newSizes = Arrays.copyOf(sizes, node.length + 1);
                    var newLast = node.length;
                    newSizes[newLast] = 1;
                    n[newLast - 1] = single(value, level - 1);
                    n[newLast] = newSizes;
                    return n;
                } else {
                    var n = Arrays.copyOf(node, node.length);
                    n[last] = single(value, level - 1);
                    return n;
                }
            } else {
                var newNode = appendTree(cast(node[last]), value, level - 1);
                if (newNode != null) {
                    var n = Arrays.copyOf(node, node.length);
                    n[last] = newNode;
                    return n;
                }
                if (node.length < FACTOR) {
                    var newLast = node.length;
                    var n = Arrays.copyOf(node, node.length + 1);
                    n[newLast] = single(value, level - 1);
                    return n;
                } else {
                    return null;
                }
            }
        }
    }

    private Object[] single(T value, int level) {
        if (level == 0) {
            return new Object[] { value };
        }
        return new Object[] { single(value, level - 1) };
    }

    @NotNull
    @Override
    public Iterator<Pair<Integer, T>> entries() {
        return null;
    }

    @SuppressWarnings("unchecked")
    private <A> A cast(Object o) {
        return (A) o;
    }

    @Override
    public void debug() { }
}
