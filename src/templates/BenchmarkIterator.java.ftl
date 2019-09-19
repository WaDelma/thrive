<@pp.dropOutputFile />

<#list STRUCTURES as structure>
<@pp.nestOutputFile name = "BenchmarkIterator${structure.name}.java">

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
import java.util.Iterator;

@State(Scope.Thread)
public class BenchmarkIterator${structure.name} {
    @Param({"1", "2", "4", "8", "16", "32", "64", "128", "256", "512", "1024", "2048", "4096", "8192", "16384", "32768",
    "65536", "131072", "262144", "524288", "1048576", "2097152", "4194304", "8388608", "16777316"})
    static int size = 0;

    int[] xs;

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
                map = map.${structure.insert}(xs[c], xs[c]);
                break;
            }
        }
    }

    @Benchmark
    public void iterate${structure.name}(Blackhole bh) {
        Iterator iter = map.${structure.iterator}();
        while (iter.hasNext()) {
            bh.consume(iter.next());
        }
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(BenchmarkIterator${structure.name}.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}
</@pp.nestOutputFile>
</#list>