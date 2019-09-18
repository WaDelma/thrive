package thrive

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.random.Random
import kotlin.test.assertEquals

@RunWith(Parameterized::class)
class TrieIterationTest(val trie: () -> Trie<String>, val desc: String) {
    companion object Params {
        @Parameterized.Parameters(name = " with {1}")
        @JvmStatic
        fun data() = arrayOf(
            arrayOf<Any>({ Trie1<String>() }, "Trie1"),
            arrayOf<Any>({ Trie2<String>() }, "Trie2"),
            arrayOf<Any>({ Trie1j<String>() }, "Trie1j"),
            arrayOf<Any>({ Trie2j<String>() }, "Trie2j"),
            arrayOf<Any>({ Trie3<String>() }, "Trie3"),
            arrayOf<Any>({ Trie3j<String>() }, "Trie3j")
        )
    }

    @Test
    fun `iterating empty map is empty`() {
        assert(!trie().entries().hasNext())
    }

    @Test
    fun `iterating singleton map is singleton`() {
        val map = trie().insert(1, "1")
        val iter = map.entries()
        assert(iter.hasNext())
        assertEquals(1 to "1", iter.next())
        assert(!iter.hasNext())
    }

    @Test
    fun `value can be taken from iterator without checking if it has one`() {
        val map = trie().insert(1, "1")
        assertEquals(1 to "1", map.entries().next())
    }

    @Test
    fun `iterating 32 entries works`() {
        var map = trie()
        (0..31).forEach {
            map = map.insert(it, "v$it")
        }
        map.debug()
        val set = mutableSetOf<Int>()
        map.entries().forEachRemaining { (n, s) ->
            set.add(n)
            assertEquals("v$n", s)
        }
        assertEquals(32, set.size, set.toString())
        (0..31).forEach {
            assert(it in set)
        }
    }

    @Test
    fun `iterating 33 entries works`() {
        var map = trie()
        (0..32).forEach {
            map = map.insert(it, "v$it")
        }
        val set = mutableSetOf<Int>()
        map.entries().forEachRemaining { (n, s) ->
            set.add(n)
            assertEquals("v$n", s)
        }
        assertEquals(33, set.size, set.toString())
        (0..32).forEach {
            assert(it in set)
        }
    }

    @Test
    fun `iterating 65 entries works`() {
        var map = trie()
        (0..64).forEach {
            map = map.insert(it, "v$it")
        }
        val set = mutableSetOf<Int>()
        map.entries().forEachRemaining { (n, s) ->
            set.add(n)
            assertEquals("v$n", s)
        }
        assertEquals(65, set.size, set.toString())
        (0..64).forEach {
            assert(it in set)
        }
    }

    @Test
    fun `Iterating lots of values works`() {
        var map = trie()
        val rand = Random(42)
        val added = generateSequence(rand::nextInt).take(10000).toHashSet()
        val expect = hashSetOf<Int>()
        added.forEachIndexed { i, key ->
            expect.add(i)
            map = map.insert(key, "v$key $i")
        }
        map.entries().forEachRemaining { (k, v) ->
            assert(added.remove(k)) { v }
            assert("v$k " in v) { v }
        }
        assert(added.isEmpty()) { added }
    }
}