<@pp.dropOutputFile />

<#list STRUCTURES as structure>
<@pp.nestOutputFile name = "BenchmarkInsertLinear${structure.name}.java">

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
public class BenchmarkInsertLinear${structure.name} {
    @Param({"1", "10", "100", "1000", "10000", "100000", "1000000"})
    int size;

    ${structure.type} map;

    @Setup
    public void setup() {
        map = ${structure.creator};
    }

    @Benchmark
    public ${structure.type} insertLinear${structure.name}() {
        for (int i = 0; i < size; i++) {
            map = map.${structure.insert}(i, i);
        }
        return map;
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
            .include(BenchmarkInsertLinear${structure.name}.class.getSimpleName())
            .forks(1)
            .build();
        new Runner(opt).run();
    }
}
</@pp.nestOutputFile>
</#list>