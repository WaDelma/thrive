package thrive

import java.util.*

private const val BITS: Int = 5

class IntImplicitKeyHamtKotlin<T> : IntMap<T> {
    private val root: Node3<T>?

    constructor() {
        root = null
    }

    private constructor(node: Node3<T>) {
        this.root = node
    }

    override fun insert(key: Int, value: T): IntImplicitKeyHamtKotlin<T> {
        return when (root) {
            null -> IntImplicitKeyHamtKotlin(Trunk3<T>(0, arrayOf()).insert(key, value, 0))
            else -> IntImplicitKeyHamtKotlin(root.insert(key, value, 0))
        }
    }

    override fun get(key: Int): T? = root?.get(key, 0)

    override fun debug() {
        when (root)  {
            null -> println("null")
            else -> root.debug(0)
        }
    }

    internal data class StackElem<T>(val node: Node3<T>, val lowerbit: Int, val level: Int)

    override fun entries() = Trie3Iterator(ArrayList<StackElem<T>>().also { list -> root?.let { list.add(StackElem(it, 0, 0)) } }, 0)

    inner class Trie3Iterator internal constructor (private val stack: ArrayList<StackElem<T>>, private var index: Int): Iterator<Pair<Int, T>> {
        override fun next(): Pair<Int, T> {
            while (true) {
                val (node, i, level) = stack.pop()
                when (node) {
                    is Trunk3 -> stack.addAll(
                        (0 until 32)
                            .filter { (node.map ushr it) and 1 == 1 }
                            .mapIndexed { index, j -> StackElem(
                                node.children[index],
                                (j shl (level * BITS)) or i,
                                level + 1
                            ) }
                    )
                    is Leaf3 -> {
                        if (index < node.values.size) {
                            return ((index shl (level * BITS)) or i) to (node.values[index] as T).also {
                                stack.add(StackElem(node, i, level))
                                index += 1
                            }
                        } else {
                            index = 0
                        }
                    }
                }
            }
        }

        override fun hasNext(): Boolean = when (stack.size) {
            0 -> false
            1 -> when (val node = stack.peek().node) {
                is Leaf3 -> index < node.values.size
                else -> true
            }
            else -> true
        }
    }
}

internal sealed class Node3<T> {
    abstract fun debug(level: Int)
    abstract fun insert(key: Int, value: T, level: Int): Node3<T>
    abstract fun get(key: Int, level: Int): T?
}

private class Trunk3<T>(val map: Int, val children: Array<Node3<T>>) : Node3<T>() {
    override fun debug(level: Int) {
        val pad = generateSequence { " " }.take(level).joinToString("")
        println("${pad}Trunk(map=${map.toString(2)}")
        children.forEach {
            it.debug(level + 1)
        }
        println("$pad)")
    }
    override fun insert(key: Int, value: T, level: Int): Node3<T> {
        if (level > 32 / BITS) {
            return Leaf3<T>(0, arrayOf()).insert(key, value, level)
        }
        val bit = mask(key, BITS * level, BITS)
        val pos = 1 shl bit
        val index = index(this.map, pos)
        if ((map ushr bit) and 1 == 1) {
            val childs = children.copyOf()
            childs[index] = childs[index].insert(key, value, level + 1)
            return Trunk3(map, childs)
        }
        val childs = arrayOfNulls<Node3<T>>(children.size + 1)
        children.copyInto(childs, 0, 0, index)
        children.copyInto(childs, index + 1, index, children.size)
        childs[index] = Trunk3<T>(0, arrayOf()).insert(key, value, level + 1)
        return Trunk3(map or pos, childs as Array<Node3<T>>)
    }
    override fun get(key: Int, level: Int): T? {
        val bit = mask(key, BITS * level, BITS)
        val pos = 1 shl bit
        if ((map shr bit) and 1 == 1) {
            val index = index(this.map, pos)
            return children[index].get(key, level + 1)
        }
        return null
    }
}

private class Leaf3<T>(val map: Int, val values: Array<Any>) : Node3<T>() {
    override fun debug(level: Int) {
        val pad = generateSequence { " " }.take(level).joinToString("")
        println("${pad}Leaf(map=${map.toString(2)}")
        println("$pad ${values.contentToString()}")
        println("$pad)")
    }
    override fun insert(key: Int, value: T, level: Int): Node3<T> {
        val bit = mask(key, BITS * level, BITS)
        val pos = 1 shl bit
        val index = index(this.map, pos)
        if ((map ushr bit) and 1 == 1) {
            val vals = values.copyOf()
            vals[index] = value as Any
            return Leaf3(map, vals)
        }
        val vals = arrayOfNulls<Any>(values.size + 1)
        values.copyInto(vals, 0, 0, index)
        values.copyInto(vals, index + 1, index, values.size)
        vals[index] = value
        return Leaf3(map or pos, vals as Array<Any>)
    }
    override fun get(key: Int, level: Int): T? {
        val bit = mask(key, BITS * level, BITS)
        val pos = 1 shl bit
        if ((map shr bit) and 1 == 1) {
            val index = index(this.map, pos)
            return values[index] as T
        }
        return null
    }
}