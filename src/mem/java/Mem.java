import clojure.lang.IPersistentMap;
import org.openjdk.jol.info.GraphLayout;
import org.organicdesign.fp.collections.PersistentHashMap;
import org.organicdesign.fp.collections.PersistentTreeMap;
import scala.math.Ordering$;
import thrive.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class Mem {
    interface Map {
        void insert(Integer key, Integer value);
        boolean contains(Integer key);
        Object finish();
    }

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
    private static <M> Map unify(
        M m,
        TriConsumer<Holder<M>, Integer, Integer> insert,
        Function<Holder<M>, M> finish,
        BiFunction<Holder<M>, Integer, Boolean> contains
    ) {
        var h = new Holder<>(m);
        return new Map() {
            @Override
            public void insert(Integer k, Integer v) {
                insert.consume(h, k, v);
            }

            @Override
            public boolean contains(Integer key) {
                return contains.apply(h, key);
            }

            @Override
            public Object finish() {
                return finish.apply(h);
            }
        };
    }

    private static Map fromPersistentIntMap(IntMap<Integer> intMap) {
        return unify(intMap, (m, k, v) -> m.val = m.val.insert(k, v), Holder::getVal, (m, k) -> m.val.get(k) != null);
    }

    private static final HashMap<String, Supplier<Map>> structures = new HashMap<>() {{
        this.put("Trie1", () -> fromPersistentIntMap(new Trie1<>()));
        this.put("Trie1j", () -> fromPersistentIntMap(new Trie1j<>()));
        this.put("Trie1j64", () -> fromPersistentIntMap(new Trie1j64<>()));
        this.put("Trie2", () -> fromPersistentIntMap(new Trie2<>()));
        this.put("Trie2j", () -> fromPersistentIntMap(new Trie2j<>()));
        this.put("Trie2j64", () -> fromPersistentIntMap(new Trie2j64<>()));
        this.put("Trie2j16", () -> fromPersistentIntMap(new Trie2j16<>()));
        this.put("Trie3", () -> fromPersistentIntMap(new Trie3<>()));
        this.put("PaguroRrbMap", () -> fromPersistentIntMap(new RrbMap<>()));
        this.put("PaguroHashMap", () -> unify(PersistentHashMap.<Integer, Integer>empty(), (m, k, v) -> m.val = m.val.assoc(k, v), Holder::getVal, (m, k) -> m.val.containsKey(k)));
        this.put("PaguroTreeMap", () -> unify(PersistentTreeMap.<Integer, Integer>empty(), (m, k, v) -> m.val = m.val.assoc(k, v), Holder::getVal, (m, k) -> m.val.containsKey(k)));
        this.put("ScalaRrbMap", () -> fromPersistentIntMap(new ScalaRrbMap<>()));
        this.put("ScalaHashMap", () -> unify(scala.collection.immutable.HashMap$.MODULE$.empty(), (m, k, v) -> m.val = m.val.updated(k, v), Holder::getVal, (m, k) -> m.val.contains(k)));
        this.put("ScalaTreeMap", () -> unify(scala.collection.immutable.TreeMap$.MODULE$.empty(Ordering$.MODULE$.comparatorToOrdering(java.util.Comparator.<Integer>naturalOrder())), (m, k, v) -> m.val = m.val.updated(k, v), Holder::getVal, (m, k) -> m.val.contains(k)));
        this.put("ScalaIntMap", () -> unify(scala.collection.immutable.IntMap$.MODULE$.empty(), (m, k, v) -> m.val = m.val.updated((int) k, v), Holder::getVal, (m, k) -> m.val.contains(k)));
        this.put("ClojureRrbMap", () -> fromPersistentIntMap(new ClojureRrbMap<>()));
        this.put("ClojureHashMap", () -> unify((IPersistentMap) clojure.java.api.Clojure.var("clojure.core", "hash-map").invoke(), (m, k, v) -> m.val = m.val.assoc(k, v), Holder::getVal, (m, k) -> m.val.containsKey(k)));
        this.put("ClojureTreeMap", () -> unify((IPersistentMap) clojure.java.api.Clojure.var("clojure.core", "sorted-map").invoke(), (m, k, v) -> m.val = m.val.assoc(k, v), Holder::getVal, (m, k) -> m.val.containsKey(k)));
        this.put("SdkMap", () -> fromPersistentIntMap(new SdkMap<>()));
        this.put("ArrayMap", () -> fromPersistentIntMap(new ArrayMap<>()));
    }};

    private static <T> void check(
            String name,
            int sizes,
            Function<Supplier<Map>, T> init,
            TriConsumer<T, Integer, Integer> add,
            Function<T, Object> finish
    ) {
        for (var m: structures.entrySet()) {
            try {
                for (int i = 0; i <= sizes; i++) {
                    var amount = 1 << i;
                    var map = init.apply(m.getValue());
                    for (int j = 0; j < amount; j++) {
                        add.consume(map, j, amount);
                    }
                    var layout = GraphLayout.parseInstance(finish.apply(map));
                    System.out.println(name + "," + m.getKey() + "," + amount + "," + layout.totalSize());
                }
            } catch (OutOfMemoryError e) {
                System.out.println("out of memory");
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("test,name,amount,size");
        check(
                "lin",
                23,
                Supplier::get,
                (p, i, a) -> p.insert(i, 0),
                Map::finish
        );
        check(
                "lincumu",
                23,
                (m) -> new Pair<>(m.get(), new ArrayList<>()),
                (p, i, a) -> {
                    p.fst.insert(i, 0);
                    p.snd.add(p.fst.finish());
                },
                (p) -> p.snd.toArray()
        );
        check(
                "rand",
                23,
                (m) -> new Pair<>(m.get(), new Random(42)),
                (p, i, a) -> {
                    int k;
                    do {
                        k = p.snd.nextInt((int) Math.ceil(a / 0.5));
                    } while (p.fst.contains(k));
                    p.fst.insert(k, 0);
                },
                (p) -> p.fst.finish()
        );
        check(
                "randcumu",
                23,
                (m) -> new Triple<>(m.get(), new Random(42), new ArrayList<>()),
                (p, i, a) -> {
                    int k;
                    do {
                        k = p.snd.nextInt((int) Math.ceil(a / 0.5));
                    } while (p.fst.contains(k));
                    p.fst.insert(k, 0);
                    p.thr.add(p.fst.finish());
                },
                (p) -> p.thr.toArray()
        );
    }
}
