<@pp.dropOutputFile />

<#list STRUCTURES as structure>
<@pp.nestOutputFile name = "BenchmarkIteratorLinear${structure.name}.java">

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
public class BenchmarkIteratorLinear${structure.name} {
    @Param({"1", "10", "100", "1000", "10000", "100000", "1000000"})
    static int size = 0;

    ${structure.type} map;

    @Setup
    public void setup() {
        map = ${structure.creator};
        for (int c = 0; c < size; c++) {
            map = map.${structure.insert}(c, c);
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
                .include(BenchmarkIteratorLinear${structure.name}.class.getSimpleName())
                .forks(1)
                .build();
        new Runner(opt).run();
    }
}
</@pp.nestOutputFile>
</#list>