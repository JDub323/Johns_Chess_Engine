package engine;

import chessUtilities.PrintColor;
import move.Move;

import static engine.Evaluator.TYPE_1;

public class TranspositionTable {

    private static final boolean PRINT_TT_ENTRIES = false;

    private static final int TT_SIZE = 1<<27;
    private static final int TT_HASH_MASK = TT_SIZE-1;
    private static final long LEFT_PARTIAL_HASH_MASK= 0xFFFFFFFF00000000L;

    public record TableEntry(int leftPartialHash, int eval, int bestMove, byte depth, byte nodeType) {
        //left hash exists to make sure there isn't a collision where there shouldn't be, though not perfect, makes speedy
    }

    public static TableEntry[] table = new TableEntry[TT_SIZE];

    public static void tryAddingEntry(long hash, int eval, int bestMove, byte depth, byte nodeType) {
        //uses a type 1 first, depth breaks tie replacement strategy
        int shortHash = (int) (hash & TT_HASH_MASK);
        TableEntry temp = table[shortHash];

        if (temp == null) {
            table[shortHash] = new TableEntry(getLeftHash(hash), eval, bestMove, depth, nodeType);

            if (PRINT_TT_ENTRIES && table[shortHash].nodeType() == TYPE_1){
                System.out.println(PrintColor.GREEN+"Entry Added: "+transpositionTableEntryString(hash)+PrintColor.RESET);
            }
            return;
        }

        if ((depth > temp.depth()) || (nodeType == TYPE_1 && temp.nodeType() != TYPE_1)) {//TODO: refactor this
            boolean printEntry = PRINT_TT_ENTRIES && temp.nodeType() == TYPE_1;

            if (printEntry)System.out.println(PrintColor.YELLOW+"Entry Num: "+transpositionTableEntryString(hash));

            table[shortHash] = new TableEntry(getLeftHash(hash), eval, bestMove, depth, nodeType);

            if (printEntry) System.out.println("Replaced By: "+transpositionTableEntryString(hash)+"\n"+PrintColor.RESET);
        }
    }

    public static boolean positionIsInTable(long hash) {
        TableEntry possibleEntry = table[(int) (hash & TT_HASH_MASK)];
        if (possibleEntry!=null) {
            return possibleEntry.leftPartialHash == getLeftHash(hash);
        }
        return false;
    }

    public static TableEntry getTableEntry(long hash) {
        return table[(int) (hash & TT_HASH_MASK)];
    }

    public static int getLeftHash(long l) {
        return (int) (l*LEFT_PARTIAL_HASH_MASK>>>32);
    }

    public static void printNumEntries() {
        double numEntries = 0;
        for (int i=0;i<TT_SIZE;i++) {
            if (table[i] != null) {
                numEntries++;
            }
        }
        System.out.println("Percent of Transposition Table that is full: "+numEntries/(double)TT_SIZE*100+"%");
    }

    public static String transpositionTableEntryString(long hash) {
        TableEntry entry = getTableEntry(hash);

        String NodeString = switch (entry.nodeType()){
            case 1-> "TYPE_1";
            case 2-> "TYPE_2";
            case 3-> "TYPE_3";
            default -> "error";
        };
        String hashString = Integer.toHexString(entry.leftPartialHash());
        String moveString = "- Best Move: "+Move.getStandardStringFromMove(entry.bestMove());

        return hashString + moveString+" Eval: "+entry.eval()+" Depth: "+entry.depth()+" Node Type: "+NodeString;
    }
}
