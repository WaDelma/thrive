package thrive;

class TrieUtils {
    static int mask(int index, int shift, int bits) {
        return (index >>> shift) & ((1 << bits) - 1);
    }

    static int indexS(short bitmap, short pos) {
        return Integer.bitCount(Short.toUnsignedInt(bitmap) & (Short.toUnsignedInt(pos) - 1));
    }

    static int indexI(int bitmap, int pos) {
        return Integer.bitCount(bitmap & (pos - 1));
    }

    static int indexL(long bitmap, long pos) {
        return Long.bitCount(bitmap & (pos - 1));
    }

    static void copyInto(Object src, Object dest, int destinationOffset, int startIndex, int endIndex) {
        System.arraycopy(src, startIndex, dest, destinationOffset, endIndex - startIndex);
    }
}
