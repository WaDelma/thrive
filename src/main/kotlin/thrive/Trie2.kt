package thrive

import java.util.*

class Trie2<T> : Trie<T> {
    private val root: Node2<T>?

    constructor() {
        root = null
    }

    private constructor(node: Node2<T>) {
        this.root = node
    }

    override fun insert(key: Int, value: T): Trie2<T> {
        val pos = 1 shl mask(key, 0)
        return when (root) {
            null -> Trie2(Leaf(pos, intArrayOf(key), arrayOf(value as Any)))
            else -> Trie2(root.insert(key, value, 0))
        }
    }

    override fun get(key: Int): T? = root?.get(key, 0)

    override fun debug() {
        when (root)  {
            null -> println("null")
            else -> root.debug(0)
        }
    }
}

private sealed class Node2<T> {
    abstract fun debug(level: Int)
    abstract fun insert(key: Int, value: T, level: Int): Node2<T>
    abstract fun get(key: Int, level: Int): T?
}

private class Trunk<T>(val map: Int, val children: Array<Node2<T>>): Node2<T>() {
    override fun debug(level: Int) {
        val pad = generateSequence { " " }.take(level).joinToString("")
        println("${pad}Trunk(map=${map.toString(2)}")
        children.forEach {
            it.debug(level + 1)
        }
        println("$pad)")
    }
    override fun insert(key: Int, value: T, level: Int): Node2<T> {
        val bit = mask(key, 5 * level)
        val pos = 1 shl bit
        val index = index(this.map, pos)
        if ((map ushr bit) and 1 == 1) {
            // TODO: Would it be better to not copy the old value? (arrayOfNulls)
            val childs = children.copyOf()
            childs[index] = childs[index].insert(key, value, level + 1)
            return Trunk(map, childs)
        }
        val childs = arrayOfNulls<Node2<T>>(children.size + 1)
        children.copyInto(childs, 0, 0, index)
        children.copyInto(childs, index + 1, index, children.size)
        val bit2 = mask(key, 5 * (level + 1))
        childs[index] = Leaf(1 shl bit2, intArrayOf(key), arrayOf(value as Any))
        return Trunk(map or pos, childs as Array<Node2<T>>)
    }
    override fun get(key: Int, level: Int): T? {
        val bit = mask(key, 5 * level)
        val pos = 1 shl bit
        if ((map shr bit) and 1 == 1) {
            val index = index(this.map, pos)
            return children[index].get(key, level + 1)
        }
        return null
    }
}

private class Leaf<T>(val map: Int, val keys: IntArray, val values: Array<Any>): Node2<T>() {
    override fun debug(level: Int) {
        val pad = generateSequence { " " }.take(level).joinToString("")
        println("${pad}Leaf(map=${map.toString(2)}")
        println("$pad ${Arrays.toString(keys)}")
        println("$pad ${Arrays.toString(values)}")
        println("$pad)")
    }
    override fun insert(key: Int, value: T, level: Int): Node2<T> {
        val bit = mask(key, 5 * level)
        val pos = 1 shl bit
        val index = index(this.map, pos)
        if ((map ushr bit) and 1 == 1) {
            // There exists value the place we would go in the internal storage
            if (key == keys[index]) {
                // They had the same key, so we are going to replace the value
                val vals = values.copyOf()
                vals[index] = value as Any;
                return Leaf(map, keys, vals)
            }

            val children = arrayOfNulls<Node2<T>>(keys.size)
            for (i in 0..children.size - 1) {
                val bit2 = mask(keys[i], 5 * (level + 1))
                children[i] = Leaf<T>(1 shl bit2, intArrayOf(keys[i]), arrayOf(values[i]))
            }
            children[index] = children[index]!!.insert(key, value, level + 1)
            return Trunk(map, children as Array<Node2<T>>)
        }
        // Now we can just save us into the internal storage
        val kes = IntArray(keys.size + 1)
        val vals = arrayOfNulls<Any>(values.size + 1)
        // Copy slots before us
        keys.copyInto(kes, 0, 0, index)
        values.copyInto(vals, 0, 0, index)
        // Copy slots after us
        keys.copyInto(kes, index + 1, index, keys.size)
        values.copyInto(vals, index + 1, index, values.size)
        // Insert our key-value pair
        kes[index] = key
        vals[index] = value as Any
        return Leaf(map or pos, kes, vals as Array<Any>)
    }
    override fun get(key: Int, level: Int): T? {
        val bit = mask(key, 5 * level)
        val pos = 1 shl bit
        if ((map shr bit) and 1 == 1) {
            val index = index(this.map, pos)
            if (key == keys[index]) {
                return values[index] as T
            }
        }
        return null
    }
}

private fun mask(index: Int, shift: Int): Int = (index ushr shift) and 0b11111
private fun index(bitmap: Int, pos: Int): Int = Integer.bitCount((bitmap and (pos - 1)))