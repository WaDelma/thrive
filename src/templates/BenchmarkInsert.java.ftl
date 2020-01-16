<@pp.dropOutputFile />

<#list STRUCTURES as structure>
<@pp.nestOutputFile name = "BenchmarkInsert_${structure.name}.java">

package thrive;

import ${structure.path};
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.RunnerException;

import java.util.HashSet;
import java.util.Random;

@State(Scope.Thread)
public class BenchmarkInsert_${structure.name} {
    @Param({"1", "2", "4", "8", "16", "32", "64", "128", "256", "512", "1024", "2048", "4096", "8192", "16384", "32768",
    "65536", "131072", "262144", "524288", "1048576", "2097152", "4194304", "8388608", "16777316"})
    int size = 0;
    @Param({"0.25", "0.5", "0.75"})
    double density = 0.;

    ${structure.type} map;

    int[] xs;

    @Setup
    public void setup() {
        xs = new int[size];
        map = ${structure.creator};
        var rand = new Random(42);
        var set = new HashSet< Integer>(size);
        for (var c = 0; c < size; c++) {
            while (true) {
                xs[c] = rand.nextInt((int) Math.ceil(size / density));
                if (set.contains(xs[c])) {
                    continue;
                }
                set.add(xs[c]);
                break;
            }
        }
    }

    @Benchmark
    public ${structure.type} insert${structure.name}() {
        var m = map;
        for (var i: xs) {
            m = m.${structure.insert}(i, (Integer) i);
        }
        return m;
    }

    public static void main(String[] args) throws RunnerException {
        var opt = new OptionsBuilder()
            .include(BenchmarkInsert_${structure.name}.class.getSimpleName())
            .build();
        new Runner(opt).run();
    }
}
</@pp.nestOutputFile>
</#list>