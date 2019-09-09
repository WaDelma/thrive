<@pp.dropOutputFile />

<#list STRUCTURES as structure>
<@pp.nestOutputFile name = "BenchmarkGetLinear${structure.name}.java">

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
public class BenchmarkGetLinear${structure.name} {
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

    @State(Scope.Thread)
    public static class GetState {
        public int[] is = new int[100];

        @Setup
        public void setup() {
            Random rand = new Random(Thread.currentThread().getId());
            rand.nextInt();
            rand.nextInt();
            for (int n = 0; n < is.length; n++) {
                is[n] = rand.nextInt(size);
            }
        }
    }

    @Benchmark
    @Fork(3)
    public void hittingGetLinear${structure.name}(GetState state, Blackhole bh) {
        for (int i: state.is) {
            bh.consume(map.get(i));
        }
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(BenchmarkGetLinear${structure.name}.class.getSimpleName())
                .forks(1)
                .build();
        new Runner(opt).run();
    }
}
</@pp.nestOutputFile>
</#list>