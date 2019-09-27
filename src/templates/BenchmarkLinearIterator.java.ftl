<@pp.dropOutputFile />

<#list STRUCTURES as structure>
<@pp.nestOutputFile name = "BenchmarkLinearIterator${structure.name}.java">

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
public class BenchmarkLinearIterator${structure.name} {
    @Param({"1", "2", "4", "8", "16", "32", "64", "128", "256", "512", "1024", "2048", "4096", "8192", "16384", "32768",
    "65536", "131072", "262144", "524288", "1048576", "2097152", "4194304", "8388608", "16777316"})
    static int size = 0;

    ${structure.type} map;

    @Setup
    public void setup() {
        map = ${structure.creator};
        for (int c = 0; c < size; c++) {
            map = map.${structure.insert}(c, (Integer)c);
        }
    }

    @Benchmark
    public void iterateLinear${structure.name}(Blackhole bh) {
        var iter = map.${structure.iterator}();
        while (iter.hasNext()) {
            bh.consume(iter.next());
        }
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(BenchmarkLinearIterator${structure.name}.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}
</@pp.nestOutputFile>
</#list>