package engine;

import eval.StaticEval;
import move.Move;
import position.CurrentPosition;
import position.Position;
import position.Type;

public class Evaluator implements Runnable{//always analyzes the current position

    public int bestMove;

    @Override
    public void run() {
        //make a new position that is a copy of the old one
        Position pos= new Position(CurrentPosition.position.getFen());//probably too slow, def a way to make faster

        findBestMove(pos,4);
    }

/*  IN CASE I NEED TO INTERRUPT THE AI EARLY
 int i=1;
while (!exit) {
            findBestMove(pos,i);
            i++;
        }
 */

    private int evaluatePosition(Position pos, int depth, int alpha, int beta) {
        if (depth==0) {// should be quiescenceEvaluation(pos, -beta, -alpha); in the future or maybe just (pos, alpha, beta), unsure
            return StaticEval.evaluate(pos);
        }

        for (int i=0;i< pos.indexOfFirstEmptyMove;i++) {
            int moveToMake = pos.legalMoves[i];

            pos.makeMove(moveToMake);
            int eval = -evaluatePosition(pos,depth-1,-beta,-alpha);
            pos.unmakeMove(moveToMake);

            if (eval>=beta) {
                return beta;
            }
            alpha = Math.max(alpha,eval);
        }
        return alpha;
    }
    private int quiescenceEvaluation(Position pos, int alpha, int beta) {
        int standingPat = StaticEval.evaluate(pos);
        if (standingPat >= beta) return beta;
        alpha = Math.max(alpha, standingPat);

        for (int i=0;i<pos.indexOfFirstEmptyMove;i++) {
            int moveEvaluating = pos.legalMoves[i];

            if (Move.getCapturedPieceFromMove(moveEvaluating) != Type.Empty) {//only look deeper for captures
                pos.makeMove(moveEvaluating);
                int eval = -quiescenceEvaluation(pos, -beta, -alpha);
                pos.unmakeMove(moveEvaluating);

                if (eval >= beta)return beta;
                alpha = Math.max(alpha, eval);
            }
        }
        return alpha;
    }
    private void findBestMove(Position pos, int depth) {//TODO: add alpha beta pruning to this level
        int alpha = Integer.MIN_VALUE+1;
        int beta = Integer.MAX_VALUE-1;
        bestMove = pos.legalMoves[0];
        pos.makeMove(bestMove);
        int bestEval = -evaluatePosition(pos, depth, alpha, beta);
        pos.unmakeMove(bestMove);

        for (int i=1;i<pos.indexOfFirstEmptyMove;i++) {
            int moveToMake = pos.legalMoves[i];

            pos.makeMove(moveToMake);
            int eval = -evaluatePosition(pos, depth, alpha, beta);
            pos.unmakeMove(moveToMake);

            if (eval>bestEval){
                bestMove = pos.legalMoves[i];
                bestEval = eval;
            }

        }
    }
}
