package thrive

import java.util.ArrayList

internal fun mask(index: Int, shift: Int, bits: Int): Int = (index ushr shift) and ((1 shl bits) - 1)
internal fun index(bitmap: Int, pos: Int): Int = Integer.bitCount(bitmap and (pos - 1))

fun <T> ArrayList<T>.pop(): T = this.removeAt(this.size - 1)
fun <T> ArrayList<T>.peek(): T = this[this.size - 1]