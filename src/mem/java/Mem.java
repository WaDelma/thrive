import org.openjdk.jol.info.GraphLayout;
import org.organicdesign.fp.collections.PersistentHashMap;
import org.organicdesign.fp.collections.PersistentTreeMap;
import thrive.*;

import java.util.ArrayList;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Mem {
    private static class Pair<F, S> {
        F fst;
        S snd;
        Pair(F fst, S snd) {
            this.fst = fst;
            this.snd = snd;
        }
    }
    private static class Triple<F, S, T> {
        F fst;
        S snd;
        T thr;
        Triple(F fst, S snd, T thr) {
            this.fst = fst;
            this.snd = snd;
            this.thr = thr;
        }
    }
    private static class Holder<T> {
        T val;
        Holder(T val) {
            this.val = val;
        }
        T getVal() {
            return val;
        }
    }
    interface TriConsumer<T, S, V> {
        void consume(T t, S s, V v);
    }
    private static <M> Pair<BiConsumer<Integer, Integer>, Supplier<Object>> unify(
        M m,
        TriConsumer<Holder<M>, Integer, Integer> t,
        Function<Holder<M>, M> get
    ) {
        var h = new Holder<>(m);
        return new Pair<>((k, v) -> t.consume(h, k, v), () -> get.apply(h));
    }
    private static Pair<BiConsumer<Integer, Integer>, Supplier<Object>> fromPersistentIntMap(IntMap intMap) {
        return unify(intMap, (m, k, v) -> m.val = m.val.insert(k, v), Holder::getVal);
    }
    @SuppressWarnings("unchecked")
    private static final ArrayList<Supplier<Pair<BiConsumer<Integer, Integer>, Supplier<Object>>>> structures = new ArrayList<>() {{
        this.add(() -> fromPersistentIntMap(new Trie1<>()));
        this.add(() -> fromPersistentIntMap(new Trie1j<>()));
        this.add(() -> fromPersistentIntMap(new Trie2<>()));
        this.add(() -> fromPersistentIntMap(new Trie2j<>()));
        this.add(() -> fromPersistentIntMap(new Trie3<>()));
        this.add(() -> fromPersistentIntMap(new Trie3j<>()));
        this.add(() -> fromPersistentIntMap(new RrbMap<>()));
        this.add(() -> fromPersistentIntMap(new ScalaRrbMap<>()));
        this.add(() -> fromPersistentIntMap(new ClojureRrbMap<>()));
        this.add(() -> unify(PersistentHashMap.<Integer, Integer>empty(), (m, k, v) -> m.val = m.val.assoc(k, v), Holder::getVal));
        this.add(() -> unify(PersistentTreeMap.<Integer, Integer>empty(), (m, k, v) -> m.val = m.val.assoc(k, v), Holder::getVal));
        this.add(() -> unify(new ArrayMap<>(), (m, k, v) -> { m.val.insert(k, v); }, (h) -> {
            try {
                return (ArrayMap<Object>) h.getVal().clone();
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException();
            }
        }));
    }};

    static <T> void check(
            String name,
            int sizes,
            Function<Supplier<Pair<BiConsumer<Integer, Integer>, Supplier<Object>>>, T> init,
            BiConsumer<T, Integer> add,
            Function<T, Object> getMap,
            Function<T, Object> finish
    ) {
        for (var m: structures) {
            for (int i = 0; i <= sizes; i++) {
                var amount = 1 << i;
                var map = init.apply(m);
                for (int j = 0; j < amount; j++) {
                    add.accept(map, j);
                }
                var layout = GraphLayout.parseInstance(finish.apply(map));
                System.out.println(name + "," + getMap.apply(map).getClass().getSimpleName() + "," + amount +  "," + layout.totalSize());
            }
        }
    }
    public static void main(String[] args) {
        System.out.println("test,name,amount,size");
        check(
                "lin",
                23,
                Supplier::get,
                (p, i) -> p.fst.accept(i, 0),
                (p) -> p.snd.get(),
                (p) -> p.snd.get()
        );
        check(
                "lincumu",
                23,
                (m) -> new Pair<>(m.get(), new ArrayList<>()),
                (p, i) -> {
                    p.fst.fst.accept(i, 0);
                    p.snd.add(p.fst.snd.get());
                },
                (p) -> p.fst.snd.get(),
                (p) -> p.snd.toArray()
        );
        check(
                "rand",
                23,
                (m) -> new Pair<>(m.get(), new Random(42)),
                (p, i) -> p.fst.fst.accept(p.snd.nextInt(), 0),
                (p) -> p.fst.snd.get(),
                (p) -> p.fst.snd.get()
        );
        check(
                "randcumu",
                23,
                (m) -> new Triple<>(m.get(), new Random(42), new ArrayList<>()),
                (p, i) -> {
                    p.fst.fst.accept(p.snd.nextInt(), 0);
                    p.thr.add(p.fst.snd.get());
                },
                (p) -> p.fst.snd.get(),
                (p) -> p.thr.toArray()
        );
    }
}
