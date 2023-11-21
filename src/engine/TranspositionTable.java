package engine;

public class TranspositionTable {

    private static final int TT_SIZE = 1<<30;
    private static final int TT_HASH_MASK = TT_SIZE-1;
    private static final long LEFT_PARTIAL_HASH_MASK= 0xFFFFFFFF00000000L;

    public record TableEntry(int leftPartialHash, int eval, int bestMove, byte depth, byte nodeType) {
        //left hash exists to make sure there isn't a collision where there shouldn't be, though not perfect, makes speedy
    }

    public static TableEntry[] table = new TableEntry[TT_SIZE];

    public static void tryAddingEntry(long hash, int eval, int bestMove, byte depth, byte nodeType) {//always replace replacement strategy
        table[(int)hash] = new TableEntry(getLeftHash(hash), eval, bestMove, depth, nodeType);
    }

    public static boolean positionIsInTable(long hash) {
        TableEntry possibleEntry = table[(int)hash];
        if (possibleEntry!=null) {
            return possibleEntry.leftPartialHash == getLeftHash(hash);
        }
        return false;
    }

    public static int getLeftHash(long l) {
        return (int) (l*LEFT_PARTIAL_HASH_MASK>>>32);
    }
}
