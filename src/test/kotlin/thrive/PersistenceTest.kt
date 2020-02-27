package thrive

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.random.Random
import kotlin.test.assertEquals

@RunWith(Parameterized::class)
class PersistenceTest(val intMap: () -> IntMap<String>, val desc: String) {
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
            arrayOf<Any>({ RadixBalancedTree<String>() }, "RadixBalancedTree"),
            arrayOf<Any>({ RadixBalancedTreeRedux<String>() }, "RadixBalancedTreeRedux")
        )
    }

    @Test
    fun `adding value for key 0 works`() {
        val map = intMap()
        val map2 = map.insert(0, "Hello, World!")
        assertEquals(null, map.get(0))
        assertEquals("Hello, World!", map2.get(0))
        assertEquals(null, map2.get(1))
    }

    @Test
    fun `replacing value for key 1 works`() {
        val map = intMap()
        val map2 = map.insert(1, "Hello, World!")
        val map3= map.insert(1, "Get out")
        assertEquals("Hello, World!", map2.get(1))
        assertEquals("Get out", map3.get(1))
    }

    @Test
    fun `adding value for keys 0 and 1 works`() {
        val map = intMap()
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
    fun `adding values with random keys doesn't affect previous version`() {
        var map = intMap()
        Random(13).let { rand ->
            generateSequence { rand.nextInt(1000000) }
                .distinct()
                .take(1000)
                .forEachIndexed { n, key ->
                    val nextMap = map.insert(key, "v$key-$n")
                    assertEquals(null, map.get(key))
                    map = nextMap
                }
        }
    }
}