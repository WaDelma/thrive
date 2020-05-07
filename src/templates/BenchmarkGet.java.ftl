<@pp.dropOutputFile />

<#list STRUCTURES as structure>
<@pp.nestOutputFile name = "BenchmarkGet_${structure.name}.java">

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
public class BenchmarkGet_${structure.name} {
    @Param({"1", "2", "4", "8", "16", "32", "64", "128", "256", "512", "1024", "2048", "4096", "8192", "16384", "32768",
    "65536", "131072", "262144", "524288", "1048576", "2097152", "4194304", "8388608", "16777316"})
    int size = 0;
    @Param({"0.5"})
    double density = 0.;

    int[] xs;

    ${structure.type} map;

    HashSet< Integer> set;

    int[] is = new int[1000];
    int[] nis = new int[1000];

    @Setup
    public void setup() {
        xs = new int[size];
        map = ${structure.creator};
        var rand = new Random(42);
        set = new HashSet<>(size);
        for (var c = 0; c < size; c++) {
            while (true) {
                xs[c] = rand.nextInt((int) Math.ceil(size / density));
                if (set.contains(xs[c])) {
                    continue;
                }
                set.add(xs[c]);
                map = map.${structure.insert}(xs[c], (Integer)xs[c]);
                break;
            }
        }
        for (var n = 0; n < is.length; n++) {
            is[n] = xs[rand.nextInt(size)];
        }
        for (var n = 0; n < nis.length; n++) {
            while (true) {
                nis[n] = rand.nextInt((int) Math.ceil(size / density));
                if (set.contains(nis[n])) {
                    continue;
                }
                break;
            }
        }
    }

    @Benchmark
    public void hittingGet${structure.name}(Blackhole bh) {
        for (var i: is) {
            bh.consume(map.${structure.get}(i));
        }
    }

    @Benchmark
    public void missingGet${structure.name}(Blackhole bh) {
        for (var i: nis) {
            bh.consume(map.${structure.get}(i));
        }
    }

    public static void main(String[] args) throws RunnerException {
        var opt = new OptionsBuilder()
                .include(BenchmarkGet_${structure.name}.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}
</@pp.nestOutputFile>
</#list>