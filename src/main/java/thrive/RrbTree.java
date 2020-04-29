package thrive;

import kotlin.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

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

    private RrbTree(Object[] root, int size, int levels) {
        this.root = root;
        this.size = size;
        this.levels = levels;
    }

    public static <K> RrbTree<K> fromIterable(Iterable<K> it) {
        var tree = new RrbTree<K>();
        for (var i: it) {
            tree = tree.append(i);
        }
        return tree;
    }

    public static <K> RrbTree<K> single(K value) {
        var tree = new RrbTree<K>();
        tree.root = new Object[] { value };
        tree.size = 1;
        return tree;
    }

    @Nullable
    @Override
    public T get(int key) {
        if (root == null || key >= size) {
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
                idx = idx - (bidx == 0 ? 0 : sizes[bidx - 1]);
            } else if (i < node.length) {
                node = cast(node[i]);
            } else {
                return null;
            }
        }
        var i = idx & MASK;
        if (i < node.length) {
            return cast(node[i]);
        }
        return null;
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
            return RrbTree.single(value);
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

    public RrbTree<T> concatenate(RrbTree<T> other) {
        if (root == null) {
            return other;
        }
        if (other.root == null) {
            return this;
        }
        var fst = root;
        for (int l = levels; l < other.levels; l++) {
            fst = new Object[] {fst};
        }
        var snd = other.root;
        for (int l = other.levels; l < levels; l++) {
            snd = new Object[] {snd};
        }
        var newLevels = Math.max(levels, other.levels);
        var newRoot = mergedTrees(fst, snd, newLevels);
        var newSize = size + other.size;
        // TODO: Do we need non-radix node check?
        if (newRoot.length == 1) { // || (newRoot.length == 2 && newRoot[1] instanceof int[])) {
            return new RrbTree<>((Object[]) newRoot[0], newSize, newLevels);
        } else {
            return new RrbTree<>(newRoot, newSize, newLevels + 1);
        }
    }

    private Object[] mergedTrees(Object[] left, Object[] right, int level) {
        if (level == 0) {
            return mergedLeaves(left, right);
        }
        Object[] merged;
        var lastLeft = (Object[]) left[left.length - 1];
        var firstRight = (Object[]) right[0];
        if (level == 1) {
            merged = mergedLeaves(lastLeft, firstRight);
        } else {
            merged = mergedTrees(lastLeft, firstRight,level - 1);
        }
        var leftInit = Arrays.copyOf(left, left.length - 1);
        var rightInit = Arrays.copyOfRange(right, 1, right.length);
        return mergeRebalance(leftInit, merged, rightInit, level);
    }

    private Object[] mergeRebalance(Object[] left, Object[] center, Object[] right, int level) {
        var merged = Arrays.copyOf(left, left.length + center.length + right.length);
        System.arraycopy(center, 0, merged, left.length, center.length);
        System.arraycopy(right, 0, merged, left.length + center.length, right.length);
        var newNode = new ArrayList<>(FACTOR);
        var newSubtree = new ArrayList<>(FACTOR);
        var newRoot = new ArrayList<>(FACTOR);
        for (var subtree: merged) {
            for (var node: (Object[]) subtree) {
                if (newNode.size() == FACTOR) {
                    if (newSubtree.size() == FACTOR) {
                        newRoot.add(computeSizes(newSubtree, level));
                        newSubtree.clear();
                    }
                    newSubtree.add(computeSizes(newNode, level));
                    newNode.clear();
                }
                newNode.add(node);
            }
        }
        if (newSubtree.size() == FACTOR) {
            newRoot.add(computeSizes(newSubtree, level));
            newSubtree.clear();
        }
        newSubtree.add(computeSizes(newNode, level));
        newRoot.add(computeSizes(newSubtree, level));
        return computeSizes(newRoot, level);
    }

    // So did non-radix node taint the path from it to root?
    private Object[] computeSizes(ArrayList<Object> newSubtree, int level) {
        var size = newSubtree.size();
        var res = Arrays.copyOf(newSubtree.toArray(), size + 1);
        var newSizes = new int[size];
        var sum = 0;
        for (int i = 0; i < size; i++) {
            var child = newSubtree.get(i);
            if (child instanceof Object[]) {
                var c = (Object[]) child;
                if (c[c.length - 1] instanceof int[]) {
                    var sizes = (int[]) c[c.length - 1];
                    sum += sizes[sizes.length - 1];
                } else {
                    // FACTOR ** level = (2 ** BITS) ** level = 2 ** (BITS * level)
                    sum += 1 << (BITS * level);
                }
            } else {
                // TODO: Do we need to take account leaf case? If so we can optimize it by taking length?
                sum += 1;
            }
            newSizes[i] = sum;
        }
        res[size] = newSizes;
        return res;
    }

    private Object[] mergedLeaves(Object[] left, Object[] right) {
        // TODO: Take account non-radix nodes?
        if (left.length + right.length <= FACTOR) {
            var newLeft = Arrays.copyOf(left, left.length + right.length);
            System.arraycopy(right, 0, newLeft, left.length, right.length);
            return new Object[] { newLeft };
        }
        var spaceLeft = FACTOR - left.length;
        var takeRight = Math.min(spaceLeft, right.length);
        var newLeft = Arrays.copyOf(left, FACTOR);
        System.arraycopy(right, 0, newLeft, left.length, takeRight);
        if (takeRight < spaceLeft) {
            return new Object[] { newLeft };
        }
        var newRight = Arrays.copyOfRange(right, takeRight, right.length);
        return new Object[] { newLeft, newRight };
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

    public int getSize() {
        return size;
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
