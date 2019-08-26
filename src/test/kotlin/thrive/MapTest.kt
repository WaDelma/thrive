package thrive

import org.junit.Test
import kotlin.random.Random
import kotlin.test.assertEquals

class MapTest {
    @Test
    fun `empty map doesn't contain value for key 0`() {
        val map = Map<String>()
        assertEquals(null, map.get(0));
    }

    @Test
    fun `adding value for key 0 works`() {
        val map = Map<String>()
        val map2 = map.insert(0, "Hello, World!")
        assertEquals(null, map.get(0));
        assertEquals("Hello, World!", map2.get(0));
        assertEquals(null, map2.get(1));
    }

    @Test
    fun `replacing value for key 1 works`() {
        val map = Map<String>()
        val map2 = map.insert(1, "Hello, World!")
        val map3= map.insert(1, "Get out")
        assertEquals("Hello, World!", map2.get(1));
        assertEquals("Get out", map3.get(1));
    }

    @Test
    fun `adding value for keys 0 and 1 works`() {
        val map = Map<String>()
        val map2 = map.insert(0, "Hello, World!")
        assertEquals(null, map.get(0));
        val map3 = map2.insert(1, "Heya")
        assertEquals(null, map.get(0));
        assertEquals(null, map2.get(1));
        assertEquals(null, map.get(1));
        assertEquals("Hello, World!", map3.get(0));
        assertEquals("Heya", map3.get(1));
    }

    @Test
    fun `adding value for keys from 0 to 31 works`() {
        var map = Map<String>()
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
        var map = Map<String>()
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
        var map = Map<String>()
        (0..63).forEach {
            map = map.insert(it, "v$it")
            (0..it).reversed().forEach {
                assertEquals("v$it", map.get(it))
            }
        }
    }

    @Test
    fun `adding value for keys from 63 to 0 works`() {
        var map = Map<String>()
        (0..63).reversed().forEach {
            map = map.insert(it, "v$it")
            (it..63).forEach {
                assertEquals("v$it", map.get(it))
            }
        }
    }

    @Test
    fun `adding value for keys from 0 to 127 in random permutation works`() {
        var map = Map<String>()
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
        var map = Map<String>()
        val rand = Random(42)
        val added = generateSequence(rand::nextInt).take(10000).toSet()
        added.forEachIndexed { i, key ->
            map = map.insert(key, "v$key $i")
            added.asSequence().take(i).forEachIndexed {i, key ->
                assertEquals("v$key $i", map.get(key))
            }
        }
    }

    @Test
    fun `getting non-existent keys work`() {
        var map = Map<String>()
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
        var map = Map<String>()
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
    fun `adding huge amount of key value pairs works`() {
        var map = Map<Int>()
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