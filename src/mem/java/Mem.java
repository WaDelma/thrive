import org.openjdk.jol.info.GraphLayout;
import org.organicdesign.fp.collections.PersistentHashMap;
import org.organicdesign.fp.collections.PersistentTreeMap;
import thrive.Trie1;
import thrive.Trie1j;
import thrive.Trie2;
import thrive.Trie2j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.function.BiConsumer;
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
    private static <M> Pair<BiConsumer<Integer, Integer>, Supplier<Object>> unify(M m, TriConsumer<Holder<M>, Integer, Integer> t) {
        var h = new Holder<>(m);
        return new Pair<>((k, v) -> t.consume(h, k, v), h::getVal);
    }
    private static final ArrayList<Supplier<Pair<BiConsumer<Integer, Integer>, Supplier<Object>>>> structures = new ArrayList<>() {{
        this.add(() -> unify(new Trie1<>(), (m, k, v) -> m.val = m.val.insert(k, v)));
        this.add(() -> unify(new Trie1j<>(), (m, k, v) -> m.val = m.val.insert(k, v)));
        this.add(() -> unify(new Trie2<>(), (m, k, v) -> m.val = m.val.insert(k, v)));
        this.add(() -> unify(new Trie2j<>(), (m, k, v) -> m.val = m.val.insert(k, v)));
        this.add(() -> unify(PersistentHashMap.<Integer, Integer>empty(), (m, k, v) -> m.val = m.val.assoc(k, v)));
        this.add(() -> unify(PersistentTreeMap.<Integer, Integer>empty(), (m, k, v) -> m.val = m.val.assoc(k, v)));
    }};
    public static void main(String[] args) {
        for (var m: structures) {
            for (int i = 0; i < 23; i++) {
                var amount = 1 << i;
                var map = m.get();
                for (int j = 0; j < amount; j++) {
                    map.fst.accept(j, 0);
                }
                var layout = GraphLayout.parseInstance(map.snd.get());
                try {
                    layout.toImage(m.getClass().getSimpleName() + "_lin_" + i);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        Random rand = new Random(42);
        for (var m: structures) {
            for (int i = 0; i < 23; i++) {
                var amount = 1 << i;
                var map = m.get();
                for (int j = 0; j < amount; j++) {
                    map.fst.accept(rand.nextInt(), 0);
                }
                var layout = GraphLayout.parseInstance(map.snd.get());
                try {
                    layout.toImage(m.getClass().getSimpleName() + "_rand_" + i);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
