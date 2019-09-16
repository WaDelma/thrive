<@pp.dropOutputFile />

<#list STRUCTURES as structure>
<@pp.nestOutputFile name = "BenchmarkInsert${structure.name}.java">

package thrive;

import ${structure.path};
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.RunnerException;

import java.util.HashSet;
import java.util.Random;

@State(Scope.Thread)
public class BenchmarkInsert${structure.name} {
    @Param({"1", "2", "4", "8", "16", "32", "64", "128", "256", "512", "1024", "2048", "4096", "8192", "16384", "32768",
    "65536", "131072", "262144", "524288", "1048576", "2097152", "4194304", "8388608", "16777316"})
    int size = 0;

    ${structure.type} map;

    int[] xs;

    @Setup
    public void setup() {
        xs = new int[size];
        map = ${structure.creator};
        Random rand = new Random(42);
        HashSet< Integer> set = new HashSet< Integer>(size);
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
    public ${structure.type} insert${structure.name}() {
        for (int i: xs) {
            map = map.${structure.insert}(i, i);
        }
        return map;
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
            .include(BenchmarkInsert${structure.name}.class.getSimpleName())
            .forks(1)
            .build();
        new Runner(opt).run();
    }
}
</@pp.nestOutputFile>
</#list>