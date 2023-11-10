package ChessUtilities;

public class PseudorandomNumberGenerator {




    long rotate(long input, int rotateBy) {
        return (input<<rotateBy) | (input>>>(64-rotateBy));
    }

    int hammingDistance(long a, long b) {
        return (Long.bitCount(a^b));
    }
}
