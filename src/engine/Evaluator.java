package engine;

import eval.StaticEval;
import position.CurrentPosition;
import position.Position;
import move.Move;

public class Evaluator implements Runnable{

    public int bestMove;
    @Override
    public void run() {
        long[] tempPA = CurrentPosition.position.PieceArray;
        byte[] tempSCP = CurrentPosition.position.squareCentricPos;
        boolean tempWTM = CurrentPosition.position.whiteToMove;
        Position pos= new Position(tempPA,tempSCP,tempWTM);
        pos.calculatePreCalculatedData();

        for (int i=1;i<100;i++) {
            findBestMove(pos,i);
        }
    }


    private int evaluatePosition(Position pos, int depth, int alpha, int beta) {
        if (depth==0) {
            return StaticEval.evaluationOf(pos);
        }

        if (pos.indexOfFirstEmptyMove==0) {
            if (pos.whiteToMove && pos.inCheck)return Integer.MIN_VALUE;
            if (pos.inCheck)return Integer.MAX_VALUE;
            return 0;
        }

        for (int i=0;i< pos.indexOfFirstEmptyMove;i++) {
            int moveToMake = pos.legalMoves[i];
            pos.makeMove(moveToMake);
            int eval = -evaluatePosition(pos,depth-1,-alpha,-beta);
            pos.unmakeMove(moveToMake);
            if (eval>=beta) {
                return beta;
            }
            alpha = Math.max(alpha,eval);
        }
        return alpha;
    }

    private void findBestMove(Position pos, int depth) {
        int tempMove = pos.legalMoves[0];
        pos.makeMove(tempMove);
        int bestEval = evaluatePosition(pos,1,Integer.MIN_VALUE,Integer.MAX_VALUE);
        pos.unmakeMove(tempMove);
        int tempBestMove = pos.legalMoves[0];

        for (int i=1;i<pos.indexOfFirstEmptyMove;i++) {
            int moveToMake = pos.legalMoves[i];
            pos.makeMove(moveToMake);
            int eval = evaluatePosition(pos,1,0,0);
            pos.unmakeMove(moveToMake);
            if (eval>bestEval)tempBestMove = pos.legalMoves[i];
        }
        bestMove=tempBestMove;
    }
}
