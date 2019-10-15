package thrive

import org.junit.Test
import kotlin.test.assertEquals

class RrbMapTest {
    @Test
    fun `inserting to index 0 works`() {
        (0..1000).forEach {
            val map = RrbMap<Int>()
                .insert(it, it)
            assertEquals(it, map[it]);
        }
    }
}