package thrive

import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.random.Random
import kotlin.test.assertEquals


@RunWith(Parameterized::class)
class TrieTest(val trie: () -> Trie<String>, val desc: String) {
    companion object Params {
        @Parameterized.Parameters(name = " with {1}")
        @JvmStatic
        fun data() = arrayOf(
            arrayOf<Any>({ Trie1<String>() }, "Trie1"),
            arrayOf<Any>({ Trie2<String>() }, "Trie2"),
            arrayOf<Any>({ Trie1j<String>() }, "Trie1j"),
            arrayOf<Any>({ Trie2j<String>() }, "Trie2j")
        )
    }

    @Test
    fun `empty map doesn't contain value for key 0`() {
        val map = trie()
        assertEquals(null, map.get(0))
    }

    @Test
    fun `adding value for key 0 works`() {
        val map = trie()
        val map2 = map.insert(0, "Hello, World!")
        assertEquals(null, map.get(0))
        assertEquals("Hello, World!", map2.get(0))
        assertEquals(null, map2.get(1))
    }

    @Test
    fun `replacing value for key 1 works`() {
        val map = trie()
        val map2 = map.insert(1, "Hello, World!")
        val map3= map.insert(1, "Get out")
        assertEquals("Hello, World!", map2.get(1))
        assertEquals("Get out", map3.get(1))
    }

    @Test
    fun `adding value for keys 0 and 1 works`() {
        val map = trie()
        val map2 = map.insert(0, "Hello, World!")
        assertEquals(null, map.get(0))
        val map3 = map2.insert(1, "Heya")
        assertEquals(null, map.get(0))
        assertEquals(null, map2.get(1))
        assertEquals(null, map.get(1))
        assertEquals("Hello, World!", map3.get(0))
        assertEquals("Heya", map3.get(1))
    }

    @Test
    fun `adding value for keys from 0 to 31 works`() {
        var map = trie()
        (0..31).forEach {
            map = map.insert(it, "v$it")
            assertEquals("v$it", map.get(it))
        }
        (0..31).forEach {
            assertEquals("v$it", map.get(it))
        }
    }

    @Test
    fun `adding value for keys from 0 to 32 works`() {
        var map = trie()
        (0..32).forEach {
            map = map.insert(it, "v$it")
            assertEquals("v$it", map.get(it))
        }
        (0..32).forEach {
            assertEquals("v$it", map.get(it))
        }
    }

    @Test
    fun `adding value for keys from 0 to 63 works`() {
        var map = trie()
        (0..63).forEach {
            map = map.insert(it, "v$it")
            (0..it).reversed().forEach {
                assertEquals("v$it", map.get(it))
            }
        }
    }

    @Test
    fun `adding value for keys from 63 to 0 works`() {
        var map = trie()
        (0..63).reversed().forEach {
            map = map.insert(it, "v$it")
            (it..63).forEach {
                assertEquals("v$it", map.get(it))
            }
        }
    }

    @Test
    fun `adding value for keys from 0 to 127 in random permutation works`() {
        var map = trie()
        val keys = (0..127).toList().shuffled(Random(42))
        (0..127).forEach {
            map = map.insert(keys[it], "v${keys[it]}_$it")
            (0..it).reversed().forEach {
                assertEquals("v${keys[it]}_$it", map.get(keys[it]))
            }
        }
    }

    @Test
    fun `adding values with random keys work`() {
        var map = trie()
        val rand = Random(42)
        val added = generateSequence(rand::nextInt).take(5000).toSet()
        added.forEachIndexed { i, key ->
            map = map.insert(key, "v$key $i")
            added.asSequence().take(i).forEachIndexed {i, key ->
                assertEquals("v$key $i", map.get(key))
            }
        }
    }

    @Test
    fun `getting non-existent keys work`() {
        var map = trie()
        val rand = Random(77)
        val missing = generateSequence(rand::nextInt).take(100).toSet()
        val added = generateSequence(rand::nextInt).filter { it !in missing }.take(200).toList()
        added.forEach {
            map = map.insert(it, "v$it")
        }
        missing.forEach {
            assertEquals(null, map.get(it))
        }
    }

    @Test
    fun `adding values with random keys doesn't affect previous version`() {
        var map = trie()
        Random(13).let { rand ->
            generateSequence(rand::nextInt)
                .take(1000)
                .forEachIndexed { n, key ->
                    val nextMap = map.insert(key, "v$key-$n")
                    assertEquals(null, map.get(key))
                    map = nextMap
                }
        }
    }

    @Test
    @Ignore
    fun `adding huge amount of key value pairs works`() {
        var map = Trie1<Int>()
        val amount = Int.MAX_VALUE / 32
        println("$amount")
        println("INSERT")
        (0..amount).forEach {
            map = map.insert(it, it)
            if (it % 1000000 == 0) {
                println(it.toFloat() / amount.toFloat())
            }
        }
        println("GET")
        (0..amount).forEach {
            if (it % 1000000 == 0) {
                println(it.toFloat() / amount.toFloat())
            }
            assertEquals(it, map.get(it))
        }
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
    fun `iterating 31 entries works`() {
        var map = trie()
        (0..30).forEach {
            map = map.insert(it, "v$it")
        }
        val set = mutableSetOf<Int>()
        map.entries().forEachRemaining { (n, s) ->
            set.add(n)
            assertEquals("v$n", s)
        }
        assertEquals(31, set.size)
        (0..30).forEach {
            assert(it in set)
        }
    }

    @Test
    fun `iterating 32 entries works`() {
        var map = trie()
        (0..31).forEach {
            map = map.insert(it, "v$it")
        }
        val set = mutableSetOf<Int>()
        map.entries().forEachRemaining { (n, s) ->
            set.add(n)
            assertEquals("v$n", s)
        }
        assertEquals(32, set.size)
        (0..31).forEach {
            assert(it in set)
        }
    }

    @Test
    fun `Iterating lots of values works`() {
        var map = trie()
        val rand = Random(42)
        val added = generateSequence(rand::nextInt).take(10000).toSet()
        added.forEachIndexed { i, key ->
            map = map.insert(key, "v$key $i")
        }
        map.entries().forEachRemaining { (k, v) ->
            assert(k in added)  { v }
        }
    }
}