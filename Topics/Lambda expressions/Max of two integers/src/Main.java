import java.util.function.IntBinaryOperator;

class Operator {

    public static IntBinaryOperator binaryOperator = (x, y) -> x > y ? x : y;
}