import java.util.function.*;


class Operator {

    public static LongBinaryOperator binaryOperator = (x, y) -> {
        long result = x;
        while (y > x) {
            result *= ++x;
        }
        return result;
    };
}