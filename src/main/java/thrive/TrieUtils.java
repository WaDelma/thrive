package thrive;

class TrieUtils {
    static int mask(int index, int shift, int bits) {
        return (index >>> shift) & ((1 << bits) - 1);
    }

    static int index(int bitmap, int pos) {
        return Integer.bitCount(bitmap & (pos - 1));
    }

    static void copyInto(Object src, Object dest, int destinationOffset, int startIndex, int endIndex) {
        System.arraycopy(src, startIndex, dest, destinationOffset, endIndex - startIndex);
    }
}
