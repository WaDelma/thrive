package thrive

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.random.Random
import kotlin.test.assertEquals

@RunWith(Parameterized::class)
class IntMapIterationTest(val intMap: () -> IntMap<String>, val desc: String) {
    companion object Params {
        @Parameterized.Parameters(name = " with {1}")
        @JvmStatic
        fun data() = arrayOf(
            arrayOf<Any>({ IntChamp32Kotlin<String>() }, "IntChamp32Kotlin"),
            arrayOf<Any>({ IntHamt32Kotlin<String>() }, "IntHamt32Kotlin"),
            arrayOf<Any>({ IntChamp32Java<String>() }, "IntChamp32Java"),
            arrayOf<Any>({ IntChamp64Java<String>() }, "IntChamp64Java"),
            arrayOf<Any>({ IntHamt32Java<String>() }, "IntHamt32Java"),
            arrayOf<Any>({ IntHamt64Java<String>() }, "IntHamt64Java"),
            arrayOf<Any>({ IntHamt16Java<String>() }, "IntHamt16Java"),
            arrayOf<Any>({ IntImplicitKeyHamtKotlin<String>() }, "IntImplicitKeyHamtKotlin"),
            arrayOf<Any>({ IntImplicitKeyHamtJava<String>() }, "IntImplicitKeyHamtJava"),
            arrayOf<Any>({ ArrayMap<String>() }, "ArrayMap"),
            arrayOf<Any>({ RrbMap<String>() }, "RrbMap"),
            arrayOf<Any>({ ClojureRrbMap<String>() }, "ClojureRrbMap"),
            arrayOf<Any>({ ScalaRrbMap<String>() }, "ScalaRrbMap"),
            arrayOf<Any>({ RadixBalancedTree<String>() }, "RadixBalancedTree"),
            arrayOf<Any>({ RadixBalancedTreeRedux<String>() }, "RadixBalancedTreeRedux")
        )
    }

    @Test
    fun `iterating empty map is empty`() {
        assert(!intMap().entries().hasNext())
    }

    @Test
    fun `iterating singleton map is singleton`() {
        val map = intMap().insert(1, "1")
        val iter = map.entries()
        assert(iter.hasNext())
        assertEquals(1 to "1", iter.next())
        assert(!iter.hasNext())
    }

    @Test
    fun `value can be taken from iterator without checking if it has one`() {
        val map = intMap().insert(1, "1")
        assertEquals(1 to "1", map.entries().next())
    }

    @Test
    fun `iterating 32 entries works`() {
        var map = intMap()
        (0..31).forEach {
            map = map.insert(it, "v$it")
        }
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
        var map = intMap()
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
        var map = intMap()
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
        var map = intMap()
        val rand = Random(42)
        val added = generateSequence { rand.nextInt(1000000) }.distinct().take(10000).toHashSet()
        added.forEachIndexed { i, key ->
            map = map.insert(key, "v$key $i")
        }
        map.entries().forEachRemaining { (k, v) ->
            assert(added.remove(k)) { v }
            assert("v$k " in v) { v }
        }
        assert(added.isEmpty()) { added }
    }
}