package thrive

class Trie<T> {
    private val root: Node<T>?

    constructor() {
        root = null
    }

    private constructor(node: Node<T>) {
        this.root = node
    }

    fun insert(key: Int, value: T): Trie<T> {
        val pos = 1u shl mask(key.toUInt(), 0).toInt()
        return when (root) {
            null -> Trie(Node(0u, pos, arrayOf(key.toUInt(), value as Any)))
            else -> Trie(root.insert(key.toUInt(), value, 0))
        }
    }

    fun get(key: Int): T? = root?.get(key.toUInt(), 0)

    fun debug() {
        when (root)  {
            null -> println("null")
            else -> root.debug(0)
        }
    }
}

private class Node<T>(val nodeMap: UInt, val dataMap: UInt, val values: Array<Any>) {
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

    fun insert(key: UInt, value: T, level: Int): Node<T> {
        val bit = mask(key, 5 * level).toInt()
        val pos = 1u shl bit
        if ((dataMap shr bit) and 1u == 1u) {
            // There exists key-value pair in the place we would go in the internal storage
            val index = (2u * index(this.dataMap, pos)).toInt()
            if (key == values[index] as UInt) {
                // They had the same key, so we are going to replace the value
                // TODO: Would it be better to not copy the old value? (arrayOfNulls)
                val vals = values.copyOf()
                vals[index + 1] = value as Any;
                return Node(nodeMap, dataMap, vals)
            }
            // They had different key so we have to be in a children node instead
            // The key-value pair already in the array takes twice the space so the new array can be one smaller
            val vals = arrayOfNulls<Any>(values.size - 1)
            // Copy datum without key-value pair
            values.copyInto(vals, 0, 0, index)
            val datum = Integer.bitCount(dataMap.toInt())
            values.copyInto(vals, index, index + 2, datum * 2)
            // Copy children nodes
            val nodeIndex = this.values.size.toUInt() - 1u - index(this.nodeMap or pos, pos)
            values.copyInto(vals, nodeIndex.toInt(), nodeIndex.toInt() + 1, values.size)
            val nodes = Integer.bitCount(nodeMap.toInt())
            values.copyInto(vals, vals.size - nodes - 1, values.size - nodes, nodeIndex.toInt() + 1)
            // Add node that contains both key-value pairs
            vals[nodeIndex.toInt() - 1] = Node<T>(
                0u,
                1u shl mask(values[index] as UInt, 5 * (level + 1)).toInt(),
                arrayOf(values[index], values[index + 1])
            ).insert(key, value, level + 1) as Any
            return Node(nodeMap or pos, dataMap and pos.inv(), vals as Array<Any>)
        } else if ((nodeMap shr bit) and 1u == 1u) {
            // There exists child node that we have to insert into
            val index = this.values.size.toUInt() - 1u - index(this.nodeMap, pos)
            val vals = values.copyOf()
            vals[index.toInt()] = (values[index.toInt()] as Node<T>).insert(key, value, level + 1);
            return Node(nodeMap, dataMap, vals)
        }
        // Now we can just save us into the internal storage
        val index = (2u * index(this.dataMap, pos)).toInt()
        // Our key-value will take two more slots
        val vals = arrayOfNulls<Any>(values.size + 2)
        // Copy slots before us
        values.copyInto(vals, 0, 0, index)
        val datum = Integer.bitCount(dataMap.toInt())
        // Copy slots after us
        values.copyInto(vals, index + 2, index, values.size)
        // Insert our key-value pair
        vals[index] = key
        vals[index + 1] = value as Any
        return Node(nodeMap, dataMap or pos, vals as Array<Any>)
    }

    fun get(key: UInt, level: Int): T? {
        val bit = mask(key, 5 * level).toInt()
        val pos = 1u shl bit
        if ((dataMap shr bit) and 1u == 1u) {
            val index = (2u * index(this.dataMap, pos)).toInt()
            if (key == values[index] as UInt) {
                return values[index + 1] as T
            }
            // TODO: Repo and nikula txt to overleaf
            return null
        } else if ((nodeMap shr bit) and 1u == 1u) {
            val index = this.values.size.toUInt() - 1u - index(this.nodeMap, pos)
            return (values[index.toInt()] as Node<T>).get(key, level + 1)
        }
        return null
    }
}

private fun mask(index: UInt, shift: Int): UInt = (index shr shift) and 0b11111u
private fun index(bitmap: UInt, pos: UInt): UInt = Integer.bitCount((bitmap and (pos - 1u)).toInt()).toUInt()