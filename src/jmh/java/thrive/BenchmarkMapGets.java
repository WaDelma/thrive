package thrive;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.HashSet;
import java.util.Random;

@State(Scope.Thread)
public class BenchmarkMapGets {
    @Param({"1", "10", "100", "1000", "10000", "100000", "1000000"})
    static int size = 0;

    int[] xs;

    Trie map;

    static HashSet<Integer> set;

    @Setup
    public void setup() {
        xs = new int[size];
        map = new Trie();
        var rand = new Random(42);
        set = new HashSet<>(size);
        for (int c = 0; c < size; c++) {
            while (true) {
                xs[c] = rand.nextInt();
                if (set.contains(xs[c])) {
                    continue;
                }
                set.add(xs[c]);
                map = map.insert(xs[c], xs[c]);
                break;
            }
        }
    }


    @State(Scope.Thread)
    public static class GetState {
        public int[] is = new int[10];
        public int[] nis = new int[10];

        @Setup
        public void setup() {
            Random rand = new Random(Thread.currentThread().getId());
            rand.nextInt();
            rand.nextInt();
            for (int n = 0; n < is.length; n++) {
                is[n] = rand.nextInt(size);
            }
            for (int n = 0; n < nis.length; n++) {
                while (true) {
                    nis[n] = rand.nextInt();
                    if (set.contains(nis[n])) {
                        continue;
                    }
                    break;
                }
            }
        }
    }

    @Benchmark
    @Fork(3)
    public Trie<Integer> hittingGet(GetState state, Blackhole bh) {
        for (var i: state.is) {
            bh.consume(map.get(i));
        }
        return map;
    }

    @Benchmark
    @Fork(3)
    public Trie<Integer> missingGet(GetState state, Blackhole bh) {
        for (var i: state.nis) {
            bh.consume(map.get(i));
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
