<@pp.dropOutputFile />

<#list STRUCTURES as structure>
<@pp.nestOutputFile name = "BenchmarkLinearGet_${structure.name}.java">

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
import java.util.concurrent.atomic.AtomicInteger;

@State(Scope.Thread)
public class BenchmarkLinearGet_${structure.name} {
    @Param({"1", "2", "4", "8", "16", "32", "64", "128", "256", "512", "1024", "2048", "4096", "8192", "16384", "32768",
    "65536", "131072", "262144", "524288", "1048576", "2097152", "4194304", "8388608", "16777316"})
    int size = 0;

    ${structure.type} map;
    int[] is = new int[1000];

    @Setup
    public void setup() {
        map = ${structure.creator};
        for (var c = 0; c < size; c++) {
            map = map.${structure.insert}(c, (Integer)c);
        }
        var rand = new Random(42);
        for (var n = 0; n < is.length; n++) {
            is[n] = rand.nextInt(size);
        }
    }

    @Benchmark
    public void hittingGetLinear${structure.name}(Blackhole bh) {
        for (var i: is) {
            bh.consume(map.${structure.get}(i));
        }
    }

    public static void main(String[] args) throws RunnerException {
        var opt = new OptionsBuilder()
                .include(BenchmarkLinearGet_${structure.name}.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}
</@pp.nestOutputFile>
</#list>