package thrive

interface IntMap<T> {
    fun insert(key: Int, value: T): IntMap<T>
    fun get(key: Int): T?
    fun debug()
    fun entries() : Iterator<Pair<Int, T>>
}