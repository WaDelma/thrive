<@pp.dropOutputFile />

<#list STRUCTURES as structure>
<@pp.nestOutputFile name = "BenchmarkGet${structure.name}.java">

package thrive;

import ${structure.path};
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.HashSet;
import java.util.Random;

@State(Scope.Thread)
public class BenchmarkGet${structure.name} {
    @Param({"1", "2", "4", "8", "16", "32", "64", "128", "256", "512", "1024", "2048", "4096", "8192", "16384", "32768",
    "65536", "131072", "262144", "524288", "1048576", "2097152", "4194304", "8388608", "16777316"})
    static int size = 0;

    static int[] xs;

    ${structure.type} map;

    static HashSet< Integer> set;

    @Setup
    public void setup() {
        xs = new int[size];
        map = ${structure.creator};
        Random rand = new Random(42);
        set = new HashSet<>(size);
        for (int c = 0; c < size; c++) {
            while (true) {
                xs[c] = rand.nextInt();
                if (set.contains(xs[c])) {
                    continue;
                }
                set.add(xs[c]);
                map = map.${structure.insert}(xs[c], xs[c]);
                break;
            }
        }
    }


    @State(Scope.Thread)
    public static class GetState {
        public int[] is = new int[100];
        public int[] nis = new int[100];

        @Setup
        public void setup() {
            Random rand = new Random(Thread.currentThread().getId());
            rand.nextInt();
            rand.nextInt();
            for (int n = 0; n < is.length; n++) {
                is[n] = xs[rand.nextInt(size)];
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
    public void hittingGet${structure.name}(GetState state, Blackhole bh) {
        for (int i: state.is) {
            bh.consume(map.get(i));
        }
    }

    @Benchmark
    @Fork(3)
    public void missingGet${structure.name}(GetState state, Blackhole bh) {
        for (int i: state.nis) {
            bh.consume(map.get(i));
        }
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(BenchmarkGet${structure.name}.class.getSimpleName())
                .forks(1)
                .build();
        new Runner(opt).run();
    }
}
</@pp.nestOutputFile>
</#list>