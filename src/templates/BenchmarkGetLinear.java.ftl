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
    @Param({"1", "2", "4", "8", "16", "32", "64", "128", "256", "512", "1024", "2048", "4096", "8192", "16384", "32768",
    "65536", "131072", "262144", "524288", "1048576", "2097152", "4194304", "8388608", "16777316"})
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