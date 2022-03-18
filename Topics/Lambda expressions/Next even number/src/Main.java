import java.util.function.LongUnaryOperator;

class Operator {

    public static LongUnaryOperator unaryOperator = x -> x % 2 == 0 ? x + 2 : ++x;
}