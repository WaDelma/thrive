<@pp.dropOutputFile />

<#list STRUCTURES as structure>
<@pp.nestOutputFile name = "BenchmarkLinearInsert_${structure.name}.java">

package thrive;

import ${structure.path};
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.RunnerException;

import java.util.HashSet;
import java.util.Random;

@State(Scope.Thread)
public class BenchmarkLinearInsert_${structure.name} {
    @Param({"1", "2", "4", "8", "16", "32", "64", "128", "256", "512", "1024", "2048", "4096", "8192", "16384", "32768",
    "65536", "131072", "262144", "524288", "1048576", "2097152", "4194304", "8388608", "16777316"})
    int size = 0;

    ${structure.type} map;

    @Setup
    public void setup() {
        map = ${structure.creator};
    }

    @Benchmark
    public ${structure.type} insertLinear${structure.name}() {
        var m = map;
        for (int i = 0; i < size; i++) {
            m = m.${structure.insert}(i, (Integer)i);
        }
        return m;
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
            .include(BenchmarkLinearInsert_${structure.name}.class.getSimpleName())
            .build();
        new Runner(opt).run();
    }
}
</@pp.nestOutputFile>
</#list>