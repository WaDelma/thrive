package thrive;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.RunnerException;

import java.util.HashSet;
import java.util.Random;

@State(Scope.Thread)
public class BenchmarkMapInserts {
    @Param({"1", "10", "100", "1000", "10000", "100000", "1000000"})
    int size;

    @Param({"trie1", "trie2"})
    private static String structure;
    Trie trie() {
        if (structure.equals("trie1")) {
            return new Trie1();
        } else if (structure.equals("trie2")) {
            return new Trie2();
        } else {
            throw new UnsupportedOperationException();
        }
    }

    int[] xs;

    @Setup
    public void setup() {
        xs = new int[size];
        var rand = new Random(42);
        var set = new HashSet<Integer>(size);
        for (int c = 0; c < size; c++) {
            while (true) {
                xs[c] = rand.nextInt();
                if (set.contains(xs[c])) {
                    continue;
                }
                set.add(xs[c]);
                break;
            }
        }
    }

    @Benchmark
    public Trie<Integer> insert() {
        var map = trie();
        for (var i: xs) {
            map = map.insert(i, i);
        }
        return map;
    }

    public static void main(String[] args) throws RunnerException {
        var opt = new OptionsBuilder()
            .include(BenchmarkMapInserts.class.getSimpleName())
            .forks(1)
            .build();
        new Runner(opt).run();
    }
}