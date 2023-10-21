import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

public class Trampoline {
    interface ProxyFn<T, R> {
        Bounce<R> call(T t, Function<R, Bounce<R>> cont);
    }

    // Bounce := ExpVal
    //         | () -> Bounce
    sealed interface Bounce<R> permits ExpVal, Thunk {
    }

    record ExpVal<R>(R result) implements Bounce<R> {
    }

    record Thunk<R>(Supplier<Bounce<R>> thunk) implements Bounce<R> {
    }

    // Landin's knot?
    public static <T, R> Function<T, R> Y(Function<ProxyFn<T, R>, ProxyFn<T, R>> f) {
        AtomicReference<ProxyFn<T, R>> self = new AtomicReference<>();
        self.set(
                // f.apply((t, cont) -> lazy.get().call(t, cont))
                // 每遇到函数调用, 都新建一层 thunk, cont 也可能含有函数调用
                (t, cont) -> new Thunk<>(
                        () -> f.apply(self.get()).call(t, r -> new Thunk<>(() -> cont.apply(r)))
                )
        );


        return t -> {
            Bounce<R> result = f.apply(self.get()).call(t, ExpVal::new);
            while (true) {
                switch (result) {
                    case ExpVal<R> expVal -> {
                        return expVal.result;
                    }
                    case Thunk<R> thunk -> {
                        // System.out.println(1111111111);
                        result = thunk.thunk.get();
                    }
                }
            }
        };
    }
}
