import java.util.function.Function;

public class Main {
    public static void main(String[] args) {
        Function<Integer, Integer> fact = Trampoline.Y(self ->
                (n, cont) -> {
                    if (n == 0) {
                        return cont.apply(1);
                    } else {
                        return self.call(n - 1, val1 -> cont.apply(n * val1));
                    }
                });
        System.out.println(fact.apply(5));

        Function<Integer, Integer> fibonacci = Trampoline.Y(self ->
                (n, cont) -> {
                    if (n < 2) {
                        return cont.apply(1);
                    } else {
                        return self.call(n - 1, val1 -> self.call(n - 2, val2 -> cont.apply(val1 + val2)));
                    }
                });
        System.out.println(fibonacci.apply(5));
    }
}