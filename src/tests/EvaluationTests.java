package tests;

import engine.Evaluator;
import move.Move;
import position.Position;

import java.lang.reflect.Array;

public class EvaluationTests {

    private static final String[] MGTestPositions = {
            "r1bq1br1/ppppk1p1/5p2/4pQp1/3P4/4K3/PPP1PPPP/RN3B1R w - - 0 5"
    };
    private static final int[] MGStockfishEval = {
            -112
    };
    private static final String[] MGStockfishBestMove = {
            "d4d5"
    };

    private static final String[] EGTestPositions = {

    };
    private static final int[] EGStockfishEval = {
            1
    };
    private static final String[] EGStockfishBestMove = {
            "temp"
    };


    private static final Evaluator evaluator = new Evaluator();

    public static void testAllTestPositions(int depth) {
        testMidGameTestPositions(depth);
        testEndGameTestPositions(depth);
    }

    public static void testMidGameTestPositions(int depth) {
        for (int i = 0; i< Array.getLength(MGTestPositions); i++) {
            testTestPosition(MGTestPositions[i], depth, i+1);
            testTestPositionWithoutAlphaBeta(MGTestPositions[i], depth, i+1);
            System.out.println("Stockfish scores: "+ MGStockfishEval[i]+" after move: "+ MGStockfishBestMove[i]+"\n");
        }
    }

    public static void testEndGameTestPositions(int depth) {
        for (int i = 0; i< Array.getLength(EGTestPositions); i++) {
            testTestPosition(EGTestPositions[i], depth, i+1);
            System.out.println("Stockfish scores: "+ EGStockfishEval[i]+" after move: "+ EGStockfishBestMove[i]+"\n");
        }
    }

    public static void testTestPosition(String fen, int depth, int positionNumber) {
        Position pos = new Position(fen);

        evaluator.findBestMove(pos,depth);

        int bestMove = evaluator.bestMove;
        int eval = evaluator.bestEval;

        System.out.println("Position "+positionNumber+" scores: "+eval+" after move: "+ Move.getStringFromMove(bestMove) + " with alpha beta");
    }
    public static void testTestPositionWithoutAlphaBeta(String fen, int depth, int positionNumber) {
        Position pos = new Position(fen);

        evaluator.findBestMoveWithoutAlphaBeta(pos,depth);

        int bestMove = evaluator.bestMove;
        int eval = evaluator.bestEval;

        System.out.println("Position "+positionNumber+" scores: "+eval+" after move: "+ Move.getStringFromMove(bestMove)+" Without Alpha Beta, which shouldn't change the eval");
    }
}
