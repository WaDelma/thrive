package thrive

interface Trie<T> {
    fun insert(key: Int, value: T): Trie<T>
    fun get(key: Int): T?
    fun debug()
    fun entries() : Iterator<Pair<Int, T>> {
        TODO()
    }
}