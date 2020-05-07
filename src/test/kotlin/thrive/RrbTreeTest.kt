package thrive

import org.junit.Test
import kotlin.random.Random
import kotlin.test.assertEquals

class RrbTreeTest {
//    @Test
//    fun `concatenate empty trees`() {
//        val a = RrbTree<String>()
//        val b = RrbTree<String>()
//        val c = a.concatenate(b)
//        assertEquals(0, c.size)
//        assertEquals(null, c[0])
//    }
//    @Test
//    fun `concatenate with empty tree`() {
//        val a = RrbTree.single("1")
//        val b = RrbTree<String>()
//        val c = a.concatenate(b)
//        assertEquals(1, c.size)
//        assertEquals("1", c[0])
//        assertEquals(null, c[1])
//    }
//    @Test
//    fun `concatenate to empty tree`() {
//        val a = RrbTree<String>()
//        val b = RrbTree.single("v1")
//        val c = a.concatenate(b)
//        assertEquals(1, c.size)
//        assertEquals("v1", c[0])
//        assertEquals(null, c[1])
//    }
//    @Test
//    fun `concatenate singleton trees`() {
//        val a = RrbTree.single("v1")
//        val b = RrbTree.single("v2")
//        val c = a.concatenate(b)
//        assertEquals(2, c.size)
//        assertEquals("v1", c[0])
//        assertEquals("v2", c[1])
//        assertEquals(null, c[2])
//    }
//
//    @Test
//    fun `concatenate 16 element trees`() {
//        val a = RrbTree.fromIterable((0..15).map { "v$it" })
//        val b = RrbTree.fromIterable((16..31).map { "v$it" })
//        val c = a.concatenate(b)
//        assertEquals(c.size, 32)
//        (0..31).forEach {
//            assertEquals("v$it", c[it])
//        }
//        assertEquals(null, c[32])
//    }
//
//    @Test
//    fun `concatenate 32 element trees`() {
//        val a = RrbTree.fromIterable((0..31).map { "v$it" })
//        val b = RrbTree.fromIterable((32..63).map { "v$it" })
//        val c = a.concatenate(b)
//        assertEquals(c.size, 64)
//        (0..63).forEach {
//            assertEquals("v$it", c[it])
//        }
//        assertEquals(null, c[64])
//    }
//
//    @Test
//    fun `concatenate 33 element tree with 32 element one`() {
//        val a = RrbTree.fromIterable((0..32).map { "v$it" })
//        val b = RrbTree.fromIterable((33..64).map { "v$it" })
//        val c = a.concatenate(b)
//        assertEquals(c.size, 65)
//        (0..64).forEach {
//            assertEquals("v$it", c[it])
//        }
//        assertEquals(null, c[65])
//    }
//
//    @Test
//    fun `concatenate 32 element tree with 33 element one`() {
//        val a = RrbTree.fromIterable((0..31).map { "v$it" })
//        val b = RrbTree.fromIterable((32..64).map { "v$it" })
//        val c = a.concatenate(b)
//        assertEquals(c.size, 65)
//        (0..64).forEach {
//            assertEquals("v$it", c[it] )
//        }
//        assertEquals(null, c[65])
//    }
//
//    @Test
//    fun `concatenate 33 element trees`() {
//        val a = RrbTree.fromIterable((0..32).map { "v$it" })
//        val b = RrbTree.fromIterable((33..65).map { "v$it" })
//        val c = a.concatenate(b)
//        assertEquals(c.size, 66)
//        (0..65).forEach {
//            assertEquals("v$it", c[it])
//        }
//        assertEquals(null, c[66])
//    }
//
//    @Test
//    fun `construct tree of size 33 with concatenating singletons`() {
//        var map = RrbTree<String>()
//        (0..32).forEach {
//            map = map.concatenate(RrbTree.single("v$it"))
//        }
//        (0..32).forEach {
//            assertEquals("v$it", map[it])
//        }
//    }
//
//
//    @Test
//    fun `getting values out of range for keys from 0 to 32`() {
//        var map = RrbTree<String>()
//        (0..32).forEach {
//            map = map.concatenate(RrbTree.single("v$it"))
//        }
//        (33..1000000).forEach {
//            assertEquals(null, map.get(it))
//        }
//    }
//
//    @Test
//    fun `concatenate three singletons left assoc`() {
//        val a = RrbTree.single("a")
//        val b = RrbTree.single("b")
//        val c = RrbTree.single("c")
//        val d = a.concatenate(b).concatenate(c)
//        assertEquals(d.size, 3)
//        assertEquals("a", d[0])
//        assertEquals("b", d[1])
//        assertEquals("c", d[2])
//        assertEquals(null, d[3])
//    }
//
//    @Test
//    fun `concatenate three singletons right assoc`() {
//        val a = RrbTree.single("a")
//        val b = RrbTree.single("b")
//        val c = RrbTree.single("c")
//        val d = a.concatenate(b.concatenate(c))
//        assertEquals(d.size, 3)
//        assertEquals("a", d[0])
//        assertEquals("b", d[1])
//        assertEquals("c", d[2])
//        assertEquals(null, d[3])
//    }
//
//    @Test
//    fun `concatenate three 32 trees left assoc`() {
//        val a = RrbTree.fromIterable((0..31).map { "v$it" })
//        val b = RrbTree.fromIterable((32..63).map { "v$it" })
//        val c = RrbTree.fromIterable((64..95).map { "v$it" })
//        val d = a.concatenate(b).concatenate(c)
//        assertEquals(d.size, 96)
//        (0..85).forEach{
//            assertEquals("v$it", d[it])
//        }
//        assertEquals(null, d[96])
//    }
//
//    @Test
//    fun `concatenate three 32 trees right assoc`() {
//        val a = RrbTree.fromIterable((0..31).map { "v$it" })
//        val b = RrbTree.fromIterable((32..63).map { "v$it" })
//        val c = RrbTree.fromIterable((64..95).map { "v$it" })
//        val d = a.concatenate(b.concatenate(c))
//        assertEquals(d.size, 96)
//        (0..85).forEach {
//            assertEquals("v$it", d[it])
//        }
//        assertEquals(null, d[96])
//    }

    @Test
    fun `concatenate x trees of size y for all x, y between 2 and 16`() {
//        (2..16).forEach { x ->
//            (2..16).forEach { y ->
//                println("($x, $y)")
        val x = 7
        val y = 7
                val ts = (0 until x).map { n ->
                    RrbTree.fromIterable((0 until y).map { "v${n * y + it}" })
                }
                val res = ts.fold(RrbTree<String>()) { acc, tree ->
                    print("acc: ")
                    acc.debug()
                    print("tree: ")
                    tree.debug()
                    acc.concatenate(tree)
                }
                (0 until (x * y)).forEach {
                    assertEquals("v$it", res[it])
                }
//            }
//        }
    }

//    sealed class Res {
//        data class Index(val i: Int) : Res()
//        data class Sum(val n: Int) : Res()
//    }
//
//    @Test
//    fun `concatenate random trees`() {
//        val rand = Random(42)//1000000
//        val sizes = generateSequence { rand.nextInt(10) }.distinct().take(5).toList()
//        val maps = generateSequence(0, Int::inc).groupingBy { n ->
//            sizes.foldIndexed(Res.Sum(0) as Res) { i, acc, c ->
//                when (acc) {
//                    is Res.Sum -> if (n < acc.n) {
//                        println(n)
//                        Res.Index(i)
//                    } else {
//                        Res.Sum(acc.n + c)
//                    }
//                    else -> acc
//                }
//            }
//        }
//            .fold(RrbTree<String>()) { acc, v -> acc.append("$v") }
//            .values
//        val res = maps.fold(RrbTree<String>()) { acc, tree -> acc.concatenate(tree) }
//
//        (0 until sizes.sum()).forEach {
//            assertEquals("v$it", res[it])
//        }
//    }
}