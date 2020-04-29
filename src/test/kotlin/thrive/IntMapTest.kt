package thrive

import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.random.Random
import kotlin.test.assertEquals

@RunWith(Parameterized::class)
class IntMapTest(val intMap: () -> IntMap<Any>, val desc: String) {
    companion object Params {
        @Parameterized.Parameters(name = " with {1}")
        @JvmStatic
        fun data() = arrayOf(
            arrayOf<Any>({ IntChamp32Kotlin<Any>() }, "IntChamp32Kotlin"),
            arrayOf<Any>({ IntHamt32Kotlin<Any>() }, "IntHamt32Kotlin"),
            arrayOf<Any>({ IntChamp32Java<Any>() }, "IntChamp32Java"),
            arrayOf<Any>({ IntChamp64Java<Any>() }, "IntChamp64Java"),
            arrayOf<Any>({ IntHamt16Java<Any>() }, "IntHamt16Java"),
            arrayOf<Any>({ IntHamt32Java<Any>() }, "IntHamt32Java"),
            arrayOf<Any>({ IntHamt64Java<Any>() }, "IntHamt64Java"),
            arrayOf<Any>({ IntImplicitKeyHamtKotlin<Any>() }, "IntImplicitKeyHamtKotlin"),
            arrayOf<Any>({ IntImplicitKeyHamtJava<Any>() }, "IntImplicitKeyHamtJava"),
            arrayOf<Any>({ ArrayMap<Any>() }, "ArrayMap"),
            arrayOf<Any>({ RrbMap<Any>() }, "RrbMap"),
            arrayOf<Any>({ ClojureVectorMap<Any>() }, "ClojureVectorMap"),
            arrayOf<Any>({ ScalaRrbMap<Any>() }, "ScalaRrbMap"),
            arrayOf<Any>({ RadixBalancedTree<Any>() }, "RadixBalancedTree"),
            arrayOf<Any>({ RadixBalancedTreeRedux<Any>() }, "RadixBalancedTreeRedux"),
            arrayOf<Any>({ RrbTree<Any>() }, "RrbTree")
        )
    }

    @Test
    fun `empty map doesn't contain value for key 0`() {
        val map = intMap()
        assertEquals(null, map[0])
    }

    @Test
    fun `adding two values with same remainder under modulus 32`() {
        var map = intMap()
        val x = 4;
        map = map.insert(x, "v$x")
        val y = 32 + x
        map = map.insert(y, "v$y")
        assertEquals("v$x", map[x])
        assertEquals("v$y", map[y])
    }

    @Test
    fun `adding value for keys from 0 to 32 works`() {
        var map = intMap()
        (0..32).forEach {
            map = map.insert(it, "v$it")
            assertEquals("v$it", map[it])
        }
        (0..32).forEach {
            assertEquals("v$it", map[it])
        }
    }

    @Test
    fun `getting values out of range for keys from 0 to 32`() {
        var map = intMap()
        (0..32).forEach {
            map = map.insert(it, "v$it")
        }
        (33..1000000).forEach {
            assertEquals(null, map[it])
        }
    }

    @Test
    fun `adding value for keys from 0 to 31 works`() {
        var map = intMap()
        (0..31).forEach {
            map = map.insert(it, "v$it")
            assertEquals("v$it", map[it])
        }
        (0..31).forEach {
            assertEquals("v$it", map[it])
        }
    }

    @Test
    fun `adding value for keys from 0 to 63 works`() {
        var map = intMap()
        (0..63).forEach {
            map = map.insert(it, "v$it")
            (0..it).reversed().forEach {
                assertEquals("v$it", map[it])
            }
        }
    }
    @Test
    fun `adding 64 random values`() {
        var map = intMap()
        val rand = Random(77)
        val vals = generateSequence { rand.nextInt(128) }.distinct().take(64).toSet()
        vals.forEach {
            map = map.insert(it, "v$it")
        }
        vals.forEach {
            assertEquals("v$it", map[it])
        }
    }

    @Test
    fun `adding value for keys from 63 to 0 works`() {
        var map = intMap()
        (0..63).reversed().forEach {
            map = map.insert(it, "v$it")
            (it..63).forEach {
                assertEquals("v$it", map[it])
            }
        }
    }

    @Test
    fun `adding value for keys from 0 to 127 works`() {
        var map = intMap()
        (0..127).forEach {
            map = map.insert(it, "v$it")
            (0..it).reversed().forEach {
                assertEquals("v$it", map[it])
            }
        }
    }

    @Test
    fun `adding value for keys from 127 to 0 works`() {
        var map = intMap()
        (0..127).reversed().forEach {
            map = map.insert(it, "v$it")
            (it..127).forEach {
                assertEquals("v$it", map[it])
            }
        }
    }

    @Test
    fun `adding value for keys from 0 to 255 in random permutation works`() {
        var map = intMap()
        val keys = (0..255).toList().shuffled(Random(42))
        (0..255).forEach {
            map = map.insert(keys[it], "v${keys[it]}_$it")
            (0..it).reversed().forEach {
                assertEquals("v${keys[it]}_$it", map[keys[it]])
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
                assertEquals("v$key $i", map[key])
            }
        }
    }

    @Test
    fun `getting non-existent keys work`() {
        var map = intMap()
        val rand = Random(77)
        val missing = generateSequence { rand.nextInt(1000000) }.distinct().take(1000).toSet()
        val added = generateSequence { rand.nextInt(1000000) }.distinct().filter { it !in missing }.take(200).toList()
        added.forEach {
            map = map.insert(it, "v$it")
        }
        missing.forEach {
            assertEquals(null, map[it])
        }
    }

    @Test
    @Ignore
    fun `adding huge amount of key value pairs works`() {
        var map = intMap()
        val amount = Int.MAX_VALUE / 128
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
            assertEquals(it, map[it])
        }
    }
}