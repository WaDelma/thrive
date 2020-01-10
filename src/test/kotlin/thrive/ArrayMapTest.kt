package thrive

import org.junit.Test
import java.util.*
import kotlin.streams.asSequence
import kotlin.test.assertEquals

class ArrayMapTest {
    @Test
    fun `empty has size 0`() {
       assertEquals(0, ArrayMap<Int>().size())
    }
    @Test
    fun `map with one entry has size 1`() {
        val map= ArrayMap<Int>()
            .insert(0, 0)
        assertEquals(1, map.size())
    }
    @Test
    fun `map with two entry has size 2`() {
        val map= ArrayMap<Int>()
            .insert(0, 0)
            .insert(100, 100)
        assertEquals(2, map.size())
    }
    @Test
    fun `replacing entry doesn't change size`() {
        val map= ArrayMap<Int>()
            .insert(0, 0)
            .insert(0, 1)
        assertEquals(1, map.size())
    }
    @Test
    fun `size works for lots of entries`() {
        var map = ArrayMap<Int>()
        val amount = 100000
        (0 until amount).forEach {
            map = map.insert(it, it)
        }
        assertEquals(amount, map.size())
    }
    @Test
    fun `iterating empty map doesn't produce anything`() {
        assertEquals(0, ArrayMap<Int>().entries().asSequence().count())
    }
    @Test
    fun `iterating singleton map has the entry`() {
        val map= ArrayMap<Int>()
            .insert(0, 0)
        assertEquals(setOf(0 to 0), map.entries().asSequence().toSet())
    }
    @Test
    fun `iterating two entry map has both entries`() {
        val map= ArrayMap<Int>()
            .insert(0, 0)
            .insert(100, 100)
        assertEquals(setOf(0 to 0, 100 to 100), map.entries().asSequence().toSet())
    }
    @Test
    fun `iterating map with lots of entries works`() {
        var map = ArrayMap<Int>()
        val rng = Random(42)
        val amount = 10000
        val nums = rng
            .ints(0, amount * 10)
            .limit(amount.toLong())
            .asSequence()
            .toSet()
        nums.forEach { map = map.insert(it, it) }
        assertEquals(nums.map { it to it }.toSet(), map.entries().asSequence().toSet())
    }
}