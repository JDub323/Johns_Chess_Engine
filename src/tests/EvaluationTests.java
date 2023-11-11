package tests;

import engine.Evaluator;
import move.Move;
import position.Position;

import java.lang.reflect.Array;

public class EvaluationTests {//TODO: add more evaluation tests to quickly make sure I didn't break my code by adding something

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


    private static Evaluator evaluator;

    public static void testAllTestPositions(int depth) {
        testMidGameTestPositions(depth);
        testEndGameTestPositions(depth);
    }

    public static void testMidGameTestPositions(int depth) {
        for (int i = 0; i< Array.getLength(MGTestPositions); i++) {
            testTestPosition(MGTestPositions[i], depth, i+1);
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
        evaluator = new Evaluator(depth);

        evaluator.findBestMove(pos,depth);

        int bestMove = evaluator.bestMove;
        int eval = evaluator.bestEval;

        System.out.println("Position "+positionNumber+" scores: "+eval+" after move: "+ Move.getStringFromMove(bestMove) + " with alpha beta");
    }

    public static void testEvalOfMove(String positionFen, String move, int depth) {
        Position pos = new Position(positionFen);
        evaluator = new Evaluator(depth);

        int moveToMake = Move.makeMoveFromString(move, (byte) 0, (byte) 0);
        pos.makeMove(moveToMake);

        int eval = -evaluator.evaluatePosition(pos,depth,Integer.MIN_VALUE+1,Integer.MAX_VALUE-1);
        System.out.print("Eval with pruning of "+move+" is: "+eval);
    }
}
