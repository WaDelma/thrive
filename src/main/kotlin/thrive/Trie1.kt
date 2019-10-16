package thrive

import java.util.ArrayList

// TODO: Benchmark the effects of changing this
private const val BITS: Int = 5

class Trie1<T> : IntMap<T> {
    private val root: Node<T>?

    constructor() {
        root = null
    }

    private constructor(node: Node<T>) {
        this.root = node
    }

    override fun insert(key: Int, value: T): Trie1<T> {
        val pos = 1 shl mask(key, 0, BITS)
        return when (root) {
            null -> Trie1(Node(0, pos, arrayOf(key, value as Any)))
            else -> Trie1(root.insert(key, value, 0))
        }
    }

    override fun get(key: Int): T? = root?.get(key, 0)

    override fun debug() {
        when (root)  {
            null -> println("null")
            else -> root.debug(0)
        }
    }

    override fun entries() = Trie1Iterator(ArrayList<Node<T>>().also { root?.let(it::add) })

    inner class Trie1Iterator internal constructor (private val stack: ArrayList<Node<T>>, private var index: Int = 0, private var first: Boolean = true): Iterator<Pair<Int, T>> {
        override fun next(): Pair<Int, T> {
            first = false
            while (true) {
                val node = stack.pop()
                val datum = Integer.bitCount(node.dataMap)
                if (index == 0) {
                    val nodes = Integer.bitCount(node.nodeMap)
                    (0 until nodes).forEach {
                        stack.add(node.values[node.values.lastIndex - it] as Node<T>)
                    }
                }
                if (index < datum) {
                    stack.add(node)
                    return ((node.values[2 * index] as Int) to node.values[2 * index + 1] as T).also {
                        index += 1
                    }
                } else {
                    index = 0
                }
            }
        }

        override fun hasNext(): Boolean = when (stack.size) {
            0 -> false
            1 -> first || index <  Integer.bitCount(stack.peek().dataMap)
            else -> true
        }
    }
}

internal class Node<T>(val nodeMap: Int, val dataMap: Int, val values: Array<Any>) {
    fun debug(level: Int) {
        val pad = generateSequence { " " }.take(level).joinToString("")
        println("${pad}Node(nodeMap=${nodeMap.toString(2)}, dataMap=${dataMap.toString(2)}")
        values.asSequence().chunked(2)
            .forEachIndexed { i, vs ->
                val fst = vs[0]
                if (fst is Node<*>) {
                    fst.debug(level + 1)
                    if (vs.size >= 2) {
                        val snd = vs[1]
                        if (snd is Node<*>) {
                            snd.debug(level + 1)
                        } else {
                            println("$pad wtf $snd")
                        }
                    }
                } else if (vs.size >= 2) {
                    println("$pad ${2 * i}. ${vs[0]}: ${vs[1]}")
                } else  {
                    println("$pad wtf2 ${vs[0]}")
                }
            }
        println("$pad)")
    }

    fun insert(key: Int, value: T, level: Int): Node<T> {
        val bit = mask(key, BITS * level, BITS)
        val pos = 1 shl bit
        if ((dataMap shr bit) and 1 == 1) {
            // There exists key-value pair in the place we would go in the internal storage
            val index = (2 * index(this.dataMap, pos))
            if (key == values[index]) {
                // They had the same key, so we are going to replace the value
                val vals = values.copyOf()
                vals[index + 1] = value as Any;
                return Node(nodeMap, dataMap, vals)
            }
            // They had different key so we have to be in a children node instead
            // The key-value pair already in the array takes twice the space so the new array can be one smaller
            val vals = arrayOfNulls<Any>(values.size - 1)
            // Copy datum without key-value pair
            values.copyInto(vals, 0, 0, index)
            val datum = Integer.bitCount(dataMap)
            values.copyInto(vals, index, index + 2, datum * 2)
            // Copy children nodes
            val nodeIndex = this.values.size - 1 - index(this.nodeMap or pos, pos)
            values.copyInto(vals, nodeIndex, nodeIndex + 1, values.size)
            val nodes = Integer.bitCount(nodeMap)
            values.copyInto(vals, vals.size - nodes - 1, values.size - nodes, nodeIndex + 1)
            // Add node that contains both key-value pairs
            vals[nodeIndex - 1] = Node<T>(
                0,
                1 shl mask(values[index] as Int, BITS * (level + 1), BITS),
                arrayOf(values[index], values[index + 1])
            ).insert(key, value, level + 1) as Any
            return Node(nodeMap or pos, dataMap and pos.inv(), vals as Array<Any>)
        } else if ((nodeMap shr bit) and 1 == 1) {
            // There exists child node that we have to insert into
            val index = this.values.lastIndex - index(this.nodeMap, pos)
            val vals = values.copyOf()
            vals[index] = (values[index] as Node<T>).insert(key, value, level + 1);
            return Node(nodeMap, dataMap, vals)
        }
        // Now we can just save us into the internal storage
        val index = 2 * index(this.dataMap, pos)
        // Our key-value will take two more slots
        val vals = arrayOfNulls<Any>(values.size + 2)
        // Copy slots before us
        values.copyInto(vals, 0, 0, index)
        // Copy slots after us
        values.copyInto(vals, index + 2, index, values.size)
        // Insert our key-value pair
        vals[index] = key
        vals[index + 1] = value as Any
        return Node(nodeMap, dataMap or pos, vals as Array<Any>)
    }

    fun get(key: Int, level: Int): T? {
        val bit = mask(key, BITS * level, BITS)
        val pos = 1 shl bit
        if ((dataMap shr bit) and 1 == 1) {
            val index = 2 * index(this.dataMap, pos)
            if (key == values[index]) {
                return values[index + 1] as T
            }
            return null
        } else if ((nodeMap shr bit) and 1 == 1) {
            val index = this.values.size - 1 - index(this.nodeMap, pos)
            return (values[index] as Node<T>).get(key, level + 1)
        }
        return null
    }
}