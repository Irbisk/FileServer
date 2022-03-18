class Predicate {
    public static final TernaryIntPredicate ALL_DIFFERENT = (a, b, c) -> a != b && b != c && a != c;

    @FunctionalInterface
    public interface TernaryIntPredicate {
        boolean test(int a, int b, int c);
    }
}