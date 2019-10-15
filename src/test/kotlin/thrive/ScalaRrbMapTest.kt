package thrive

import org.junit.Test
import kotlin.test.assertEquals

class ScalaRrbMapTest {
    @Test
    fun `inserting to index 0 works`() {
        (0..1000).forEach {
            val map= ScalaRrbMap<Int>()
                .insert(it, it)
            assertEquals(it, map[it]);
        }
    }
}