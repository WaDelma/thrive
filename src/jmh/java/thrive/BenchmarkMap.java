package thrive;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

@State(Scope.Thread)
public class BenchmarkMap {
    @Param({"1", "10", "100", "1000", "10000", "100000", "1000000"})
    int size = 0;

    @Benchmark
    public Map<Integer> insert() {
        var map = new Map<Integer>();
        for (int i = 0; i < size; i++) {
            map = map.insert(i, i);
        }
        return map;
    }

    public static void main(String[] args) throws RunnerException {
        var opt = new OptionsBuilder()
            .include(BenchmarkMap.class.getSimpleName())
            .forks(1)
            .build();
        new Runner(opt).run();
    }
}