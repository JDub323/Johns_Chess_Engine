package tests;

import eval.StaticEval;
import position.Position;

import java.lang.reflect.Array;

public class StaticEvalTests {

    public static final String[] testPositions = {
            "r1bq1rk1/1pp1bppp/p1np1n2/4p3/2PP4/5NP1/PP2PPBP/RNBQ1RK1 w - - 0 5",
            "rnbqkb1r/1p2pppp/p2p1n2/8/3NP3/2N5/PPP2PPP/R1BQKB1R w KQkq - 0 5",
            "r1bqkb1r/ppp2ppp/2n2n2/3pp3/2P5/5N2/PP1PPP1P/RNBQKB1R w KQkq - 0 5",
            "r1bq1rk1/ppp1bppp/2np1n2/8/2PP4/2N2N2/PP2PPPP/R1BQKB1R w KQ - 0 5",
            "r1bq1rk1/ppp1bppp/2np1n2/8/2PPP3/2N5/PP3PPP/R1BQ1RK1 w - - 0 5",
            "r1bq1rk1/pppn1ppp/4pn2/3pP3/3P4/2N5/PPP2PPP/R1BQK2R b KQ - 0 5",
            "r1bqkb1r/ppp1pppp/2n2n2/3p4/3P4/2N2N2/PPP1PPPP/R1B1KB1R b KQkq - 0 5",
            "r1bqkb1r/pp3ppp/2p1pn2/3p4/3P4/2N2N2/PP2PPPP/R1BQKBR1 b Qkq - 0 5",
            "rnbqkb1r/ppp2ppp/4pn2/3N4/3P4/2N2N2/PPP1PPPP/R1BQKB1R b KQkq - 0 5",
            "r1bqkb1r/ppp1pppp/2n2n2/3p4/3P4/2N2N2/PPP1PPPP/R1BQKB1R b Kkq - 0 5"
    };

    public static void testAllTestPositions() {//sanity test to show eval is working
        for (int i=0; i<Array.getLength(testPositions); i++) {
            testTestPosition(testPositions[i], i+1);
        }
    }

    public static void testTestPosition(String fen, int positionNumber) {
        Position pos = new Position(fen);
        int eval = StaticEval.evaluate(pos);
        System.out.println("Position "+positionNumber+" scores: "+eval);
    }

}
