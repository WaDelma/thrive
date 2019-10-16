package thrive

import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.random.Random
import kotlin.test.assertEquals

@RunWith(Parameterized::class)
class IntMapTest(val intMap: () -> IntMap<String>, val desc: String) {
    companion object Params {
        @Parameterized.Parameters(name = " with {1}")
        @JvmStatic
        fun data() = arrayOf(
            arrayOf<Any>({ Trie1<String>() }, "Trie1"),
            arrayOf<Any>({ Trie2<String>() }, "Trie2"),
            arrayOf<Any>({ Trie1j<String>() }, "Trie1j"),
            arrayOf<Any>({ Trie2j<String>() }, "Trie2j"),
            arrayOf<Any>({ Trie3<String>() }, "Trie3"),
            arrayOf<Any>({ Trie3j<String>() }, "Trie3j"),
            arrayOf<Any>({ ArrayMap<String>() }, "ArrayMap"),
            arrayOf<Any>({ RrbMap<String>() }, "RrbMap"),
            arrayOf<Any>({ ClojureRrbMap<String>() }, "ClojureRrbMap"),
            arrayOf<Any>({ ScalaRrbMap<String>() }, "ScalaRrbMap")
        )
    }

    @Test
    fun `empty map doesn't contain value for key 0`() {
        val map = intMap()
        assertEquals(null, map.get(0))
    }

    @Test
    fun `adding value for keys from 0 to 31 works`() {
        var map = intMap()
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
        var map = intMap()
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
        var map = intMap()
        (0..63).forEach {
            map = map.insert(it, "v$it")
            (0..it).reversed().forEach {
                assertEquals("v$it", map.get(it))
            }
        }
    }

    @Test
    fun `adding value for keys from 63 to 0 works`() {
        var map = intMap()
        (0..63).reversed().forEach {
            map = map.insert(it, "v$it")
            (it..63).forEach {
                assertEquals("v$it", map.get(it))
            }
        }
    }

    @Test
    fun `adding value for keys from 0 to 127 in random permutation works`() {
        var map = intMap()
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
        var map = intMap()
        val rand = Random(42)
        val added = generateSequence { rand.nextInt(1000000) }.distinct().take(5000).toSet()
        added.forEachIndexed { i, key ->
            map = map.insert(key, "v$key $i")
            added.asSequence().take(i).forEachIndexed {i, key ->
                assertEquals("v$key $i", map.get(key))
            }
        }
    }

    @Test
    fun `getting non-existent keys work`() {
        var map = intMap()
        val rand = Random(77)
        val missing = generateSequence { rand.nextInt(1000000) }.distinct().take(100).toSet()
        val added = generateSequence { rand.nextInt(1000000) }.distinct().filter { it !in missing }.take(200).toList()
        added.forEach {
            map = map.insert(it, "v$it")
        }
        missing.forEach {
            assertEquals(null, map.get(it))
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
}